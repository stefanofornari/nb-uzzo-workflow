package ste.uzzo.nb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;

class KnowledgeBaseInitializerTest {

    @TempDir
    Path tempDir;

    private FileObject projectDir() {
        return FileUtil.toFileObject(tempDir.toFile());
    }

    @Test
    void creates_the_four_sections_with_gitkeep_when_empty() throws IOException {
        FileObject uzz = KnowledgeBaseInitializer.initialize(projectDir());

        then(uzz.getNameExt()).isEqualTo("uzz");
        then(names(uzz)).containsExactlyInAnyOrder("library", "project", "skills", "stories");
        for (String section : List.of("library", "project", "skills", "stories")) {
            FileObject folder = uzz.getFileObject(section);
            then(folder.isFolder()).as(section).isTrue();
            then(names(folder)).as(section).containsExactly(".gitkeep");
            then(folder.getFileObject(".gitkeep").getSize()).as(section).isZero();
        }
    }

    @Test
    void creates_no_files_directly_under_the_root() throws IOException {
        FileObject uzz = KnowledgeBaseInitializer.initialize(projectDir());

        then(Arrays.stream(uzz.getChildren()).filter(FileObject::isData)).isEmpty();
    }

    @Test
    void creates_no_story_folders() throws IOException {
        FileObject uzz = KnowledgeBaseInitializer.initialize(projectDir());

        then(uzz.getFileObject("stories").getChildren()).extracting(FileObject::getNameExt)
                .containsExactly(".gitkeep");
    }

    @Test
    void reports_a_conflict_when_uzz_is_a_file_and_leaves_it_untouched() throws IOException {
        Path file = Files.writeString(tempDir.resolve("uzz"), "mine");

        assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir()))
                .isInstanceOf(IOException.class)
                .hasMessageContaining(file.toString());
        then(Files.readString(file)).isEqualTo("mine");
    }

    @Test
    void refuses_to_run_when_the_knowledge_base_already_exists() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/library"));

        assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir()))
                .isInstanceOf(IOException.class)
                .hasMessageContaining(tempDir.resolve("uzz").toString());
        then(tempDir.resolve("uzz/library")).isDirectory();
        then(tempDir.resolve("uzz/stories")).doesNotExist();
    }

    @Test
    void failure_names_the_path_and_leaves_no_empty_uzz_behind() {
        File dir = tempDir.toFile();
        dir.setWritable(false);
        try {
            assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir()))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining(tempDir.toString());
            then(tempDir.resolve("uzz")).doesNotExist();
        } finally {
            dir.setWritable(true);
        }
    }

    private static List<String> names(FileObject folder) {
        return Arrays.stream(folder.getChildren()).map(FileObject::getNameExt).toList();
    }
}
