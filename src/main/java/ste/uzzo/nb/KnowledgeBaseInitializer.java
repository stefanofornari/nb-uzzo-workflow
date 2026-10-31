package ste.uzzo.nb;

import java.io.IOException;
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
     * @return the new {@code uzz} folder
     * @throws IOException with a message naming the offending path, when
     *         {@code uzz} already exists or something cannot be created; in
     *         the latter case the partially created {@code uzz} is removed
     */
    @Messages({
        "KnowledgeBaseInitializer.exists=Cannot initialize the knowledge base: {0} already exists.",
        "KnowledgeBaseInitializer.failed=Cannot create {0}: {1}"
    })
    static FileObject initialize(FileObject projectDir) throws IOException {
        FileObject existing = projectDir.getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        if (existing != null) {
            throw new IOException(Bundle.KnowledgeBaseInitializer_exists(FileUtil.getFileDisplayName(existing)));
        }

        String target = FileUtil.getFileDisplayName(projectDir) + "/" + UzzoKnowledgeBaseNodeFactory.FOLDER_NAME;
        FileObject uzz = null;
        try {
            uzz = projectDir.createFolder(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
            for (String section : SECTIONS) {
                target = FileUtil.getFileDisplayName(uzz) + "/" + section;
                uzz.createFolder(section);
            }
            for (String section : SECTIONS) {
                FileObject folder = uzz.getFileObject(section);
                if (folder.getChildren().length == 0) {
                    target = FileUtil.getFileDisplayName(folder) + "/" + GITKEEP;
                    folder.createData(GITKEEP);
                }
            }
            return uzz;
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
}
