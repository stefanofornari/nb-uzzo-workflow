package ste.uzzo.nb;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Creates the {@code uzz} knowledge base skeleton in a project directory.
 * Additive: it never touches an existing {@code uzz} file or folder.
 */
final class KnowledgeBaseInitializer {

    static final List<String> SECTIONS = List.of("library", "project", "skills", "stories");

    private static final String GITKEEP = ".gitkeep";

    private KnowledgeBaseInitializer() {
    }

    /**
     * Creates {@code uzz} with its four sections below {@code projectDir}.
     *
     * @return the new {@code uzz} folder and what could not be seeded
     * @throws IOException with a message naming the offending path, when
     *         {@code uzz} already exists or something cannot be created; in
     *         the latter case the partially created {@code uzz} is removed
     */
    @Messages({
        "KnowledgeBaseInitializer.failed=Cannot create {0}: {1}"
    })
    static Result initialize(FileObject projectDir) throws IOException {
        return initialize(projectDir, ClasspathKnowledgeBaseContent.of(KnowledgeBaseInitializer.class.getClassLoader()));
    }

    /**
     * @param root the new {@code uzz} folder
     * @param incomplete what could not be seeded; empty when everything was
     */
    record Result(FileObject root, List<String> incomplete) {
    }

    static Result initialize(FileObject projectDir, KnowledgeBaseContent content) throws IOException {
        return initialize(projectDir, content, ProjectFacts.NONE);
    }

    /** As above, filling the placeholders of the generated articles with the given facts. */
    static Result initialize(FileObject projectDir, KnowledgeBaseContent content, ProjectFacts facts)
            throws IOException {
        checkAbsent(projectDir);

        String target = FileUtil.getFileDisplayName(projectDir) + "/" + UzzoKnowledgeBaseNodeFactory.FOLDER_NAME;
        FileObject uzz = null;
        try {
            uzz = projectDir.createFolder(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
            for (String section : SECTIONS) {
                target = FileUtil.getFileDisplayName(uzz) + "/" + section;
                uzz.createFolder(section);
            }
            List<String> incomplete = new ArrayList<>();
            List<String> paths = List.of();
            try {
                paths = content.paths();
            } catch (IOException e) {
                incomplete.add("skills: " + e.getMessage());
            }
            for (String path : paths) {
                if (path.startsWith("skills/")) {
                    target = FileUtil.getFileDisplayName(uzz) + "/" + path;
                    validate(path);
                    byte[] bytes;
                    try {
                        bytes = content.read(path);
                    } catch (IOException e) {
                        incomplete.add(path + ": " + e.getMessage());
                        continue;
                    }
                    write(uzz, path, bytes);
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
                        continue;
                    }
                    target = FileUtil.getFileDisplayName(uzz) + "/" + article;
                    write(uzz, article, facts.fill(body).getBytes(StandardCharsets.UTF_8));
                }
            }

            for (String section : SECTIONS) {
                FileObject folder = uzz.getFileObject(section);
                if (folder.getChildren().length == 0) {
                    target = FileUtil.getFileDisplayName(folder) + "/" + GITKEEP;
                    folder.createData(GITKEEP);
                }
            }
            return new Result(uzz, List.copyOf(incomplete));
        } catch (IOException e) {
            if (uzz != null) {
                try {
                    uzz.delete();
                } catch (IOException ignored) {
                    // best effort: the original failure is the one to report
                }
            }
            throw new IOException(Bundle.KnowledgeBaseInitializer_failed(target, e.getMessage()), e);
        }
    }

    /** Fails with a message naming the path when {@code uzz} already exists as a file or folder. */
    @Messages("KnowledgeBaseInitializer.exists=Cannot initialize the knowledge base: {0} already exists.")
    static void checkAbsent(FileObject projectDir) throws IOException {
        FileObject existing = projectDir.getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        if (existing != null) {
            throw new IOException(Bundle.KnowledgeBaseInitializer_exists(FileUtil.getFileDisplayName(existing)));
        }
    }

    private static void validate(String path) throws IOException {
        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IOException("invalid path " + path);
            }
        }
    }

    private static void write(FileObject uzz, String path, byte[] bytes) throws IOException {
        validate(path);
        int slash = path.lastIndexOf('/');
        FileObject folder = slash < 0 ? uzz : FileUtil.createFolder(uzz, path.substring(0, slash));
        FileObject file = folder.createData(path.substring(slash + 1));
        try (OutputStream out = file.getOutputStream()) {
            out.write(bytes);
        }
    }
}
