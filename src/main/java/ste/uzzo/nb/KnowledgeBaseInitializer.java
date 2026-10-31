package ste.uzzo.nb;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Creates or completes the {@code uzz} knowledge base of a project directory.
 * Additive: an existing file or folder is never deleted, renamed, truncated or
 * overwritten; a file is written only when missing or empty.
 */
final class KnowledgeBaseInitializer {

    static final List<String> SECTIONS = List.of("library", "project", "skills", "stories");

    private static final String GITKEEP = ".gitkeep";

    /** A bracketed placeholder, not a checkbox and not a link that has been filled in. */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\[([^\\]\\[\\n]+)\\](?!\\([^)\\[\\n]*\\))");

    /** What the run found: nothing yet, something missing, or nothing missing. */
    enum Kind { FRESH, REPAIR, COMPLETE }

    /**
     * @param kind what kind of run it was
     * @param root the {@code uzz} folder
     * @param created paths (relative to {@code uzz}) written by the run; folders end with {@code /}
     * @param skipped expected files that already had content and were left alone
     * @param placeholders {@code file: [FIELD]} still to fill in the created articles
     * @param incomplete what could not be seeded; empty when everything was
     */
    record Result(Kind kind, FileObject root, List<String> created, List<String> skipped,
            List<String> placeholders, List<String> incomplete) {
    }

    private KnowledgeBaseInitializer() {
    }

    static Result initialize(FileObject projectDir) throws IOException {
        return initialize(projectDir, ClasspathKnowledgeBaseContent.of(KnowledgeBaseInitializer.class.getClassLoader()));
    }

    static Result initialize(FileObject projectDir, KnowledgeBaseContent content) throws IOException {
        return initialize(projectDir, content, ProjectFacts.NONE);
    }

    /**
     * Creates {@code uzz} below {@code projectDir}, or adds what an existing one
     * lacks, filling the placeholders of the generated articles with the facts.
     *
     * @throws IOException with a message naming the offending path(s), when
     *         {@code uzz} is a file, an expected path has the wrong type, or
     *         something cannot be written; a {@code uzz} created by this run is
     *         then removed, an existing one is left as it is
     */
    @Messages({
        "KnowledgeBaseInitializer.failed=Cannot create {0}: {1}",
        "KnowledgeBaseInitializer.conflicts=Cannot initialize the knowledge base, these paths clash with it: {0}"
    })
    static Result initialize(FileObject projectDir, KnowledgeBaseContent content, ProjectFacts facts)
            throws IOException {
        projectDir.refresh();
        checkNotFile(projectDir);
        FileObject existing = projectDir.getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        boolean fresh = existing == null;
        String projectName = FileUtil.getFileDisplayName(projectDir);

        List<String> incomplete = new ArrayList<>();
        Map<String, byte[]> wanted = collect(content, facts, incomplete, projectName);
        if (!fresh) {
            // files may have been changed outside the IDE since it last looked
            RefreshKnowledgeBaseAction.refreshRecursively(existing);
            checkConflicts(existing, wanted);
        }

        List<String> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        String target = projectName + "/" + UzzoKnowledgeBaseNodeFactory.FOLDER_NAME;
        FileObject uzz = existing;
        try {
            if (fresh) {
                uzz = projectDir.createFolder(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
            }
            for (String section : SECTIONS) {
                if (uzz.getFileObject(section) == null) {
                    target = FileUtil.getFileDisplayName(uzz) + "/" + section;
                    uzz.createFolder(section);
                    created.add(section + "/");
                }
            }
            for (Map.Entry<String, byte[]> entry : wanted.entrySet()) {
                String path = entry.getKey();
                target = FileUtil.getFileDisplayName(uzz) + "/" + path;
                FileObject file = uzz.getFileObject(path);
                if (file != null && file.getSize() > 0) {
                    skipped.add(path);
                    continue;
                }
                write(uzz, path, file, entry.getValue());
                created.add(path);
            }
            for (String article : KnowledgeBaseTemplates.ARTICLES) {
                if (created.contains(article)) {
                    placeholders.addAll(placeholdersOf(article, wanted.get(article)));
                }
            }
            for (String section : SECTIONS) {
                FileObject folder = uzz.getFileObject(section);
                if (folder.getChildren().length == 0) {
                    target = FileUtil.getFileDisplayName(folder) + "/" + GITKEEP;
                    folder.createData(GITKEEP);
                    created.add(section + "/" + GITKEEP);
                }
            }
        } catch (IOException e) {
            if (fresh && uzz != null) {
                try {
                    uzz.delete();
                } catch (IOException ignored) {
                    // best effort: the original failure is the one to report
                }
            }
            throw new IOException(Bundle.KnowledgeBaseInitializer_failed(target, e.getMessage()), e);
        }

        Kind kind = fresh ? Kind.FRESH : created.isEmpty() ? Kind.COMPLETE : Kind.REPAIR;
        return new Result(kind, uzz, List.copyOf(created), List.copyOf(skipped),
                List.copyOf(placeholders), List.copyOf(incomplete));
    }

    /** Fails with a message naming the path when {@code uzz} exists as a regular file. */
    @Messages("KnowledgeBaseInitializer.exists=Cannot initialize the knowledge base: {0} already exists.")
    static void checkNotFile(FileObject projectDir) throws IOException {
        FileObject existing = projectDir.getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        if (existing != null && !existing.isFolder()) {
            throw new IOException(Bundle.KnowledgeBaseInitializer_exists(FileUtil.getFileDisplayName(existing)));
        }
    }

    /** Everything the knowledge base should contain, as path to bytes; content problems go to {@code incomplete}. */
    private static Map<String, byte[]> collect(KnowledgeBaseContent content, ProjectFacts facts,
            List<String> incomplete, String projectName) throws IOException {
        Map<String, byte[]> wanted = new LinkedHashMap<>();
        List<String> paths = List.of();
        try {
            paths = content.paths();
        } catch (IOException e) {
            incomplete.add("skills: " + e.getMessage());
        }
        for (String path : paths) {
            if (path.startsWith("skills/")) {
                validate(path, projectName);
                try {
                    wanted.put(path, content.read(path));
                } catch (IOException e) {
                    incomplete.add(path + ": " + e.getMessage());
                }
            }
        }

        String templates = null;
        try {
            templates = new String(content.read(KnowledgeBaseTemplates.PATH), StandardCharsets.UTF_8);
        } catch (IOException e) {
            incomplete.add(KnowledgeBaseTemplates.PATH + ": " + e.getMessage());
        }
        if (templates != null) {
            for (String article : KnowledgeBaseTemplates.ARTICLES) {
                String body = KnowledgeBaseTemplates.extract(templates, article);
                if (body == null) {
                    incomplete.add(article + ": template not found");
                } else {
                    wanted.put(article, facts.fill(body).getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        return wanted;
    }

    private static void checkConflicts(FileObject uzz, Map<String, byte[]> wanted) throws IOException {
        List<String> conflicts = new ArrayList<>();
        for (String section : SECTIONS) {
            FileObject child = uzz.getFileObject(section);
            if (child != null && !child.isFolder()) {
                conflicts.add(FileUtil.getFileDisplayName(child));
            }
        }
        for (String path : wanted.keySet()) {
            FileObject current = uzz;
            String[] segments = path.split("/");
            for (int i = 0; i < segments.length && current != null; i++) {
                FileObject child = current.getFileObject(segments[i]);
                boolean last = i == segments.length - 1;
                if (child != null && (last ? child.isFolder() : !child.isFolder())) {
                    String name = FileUtil.getFileDisplayName(child);
                    if (!conflicts.contains(name)) {
                        conflicts.add(name);
                    }
                    break;
                }
                current = child;
            }
        }
        if (!conflicts.isEmpty()) {
            throw new IOException(Bundle.KnowledgeBaseInitializer_conflicts(String.join(", ", conflicts)));
        }
    }

    private static List<String> placeholdersOf(String article, byte[] bytes) {
        List<String> found = new ArrayList<>();
        Matcher matcher = PLACEHOLDER.matcher(new String(bytes, StandardCharsets.UTF_8));
        while (matcher.find()) {
            String field = matcher.group(1).trim();
            String entry = article + ": [" + field + "]";
            if (!field.isEmpty() && !field.equalsIgnoreCase("x") && !found.contains(entry)) {
                found.add(entry);
            }
        }
        return found;
    }

    private static void validate(String path, String projectName) throws IOException {
        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IOException(Bundle.KnowledgeBaseInitializer_failed(
                        projectName + "/" + UzzoKnowledgeBaseNodeFactory.FOLDER_NAME + "/" + path, "invalid path"));
            }
        }
    }

    private static void write(FileObject uzz, String path, FileObject file, byte[] bytes) throws IOException {
        FileObject target = file;
        if (target == null) {
            int slash = path.lastIndexOf('/');
            FileObject folder = slash < 0 ? uzz : FileUtil.createFolder(uzz, path.substring(0, slash));
            target = folder.createData(path.substring(slash + 1));
        }
        try (OutputStream out = target.getOutputStream()) {
            out.write(bytes);
        }
    }
}
