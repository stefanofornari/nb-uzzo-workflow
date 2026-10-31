package ste.uzzo.nb;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    /** Content source with nothing to seed. */
    private static KnowledgeBaseContent noContent() {
        return new StubContent(Map.of());
    }

    private static KnowledgeBaseContent realContent() {
        return ClasspathKnowledgeBaseContent.of(KnowledgeBaseInitializerTest.class.getClassLoader());
    }

    private static final class StubContent implements KnowledgeBaseContent {
        private final Map<String, String> files;

        StubContent(Map<String, String> files) {
            this.files = new TreeMap<>(files);
        }

        @Override
        public List<String> paths() {
            return List.copyOf(files.keySet());
        }

        @Override
        public byte[] read(String path) throws IOException {
            String text = files.get(path);
            if (text == null) {
                throw new IOException("missing " + path);
            }
            return text.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Test
    void creates_the_four_sections_with_gitkeep_when_empty() throws IOException {
        FileObject uzz = KnowledgeBaseInitializer.initialize(projectDir(), noContent()).root();

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
        FileObject uzz = KnowledgeBaseInitializer.initialize(projectDir(), realContent()).root();

        then(Arrays.stream(uzz.getChildren()).filter(FileObject::isData)).isEmpty();
    }

    @Test
    void creates_no_story_folders() throws IOException {
        FileObject uzz = KnowledgeBaseInitializer.initialize(projectDir(), realContent()).root();

        then(names(uzz.getFileObject("stories"))).containsExactlyInAnyOrder("scope.md", "users.md", "index.md");
    }

    @Test
    void reports_a_conflict_when_uzz_is_a_file_and_leaves_it_untouched() throws IOException {
        Path file = Files.writeString(tempDir.resolve("uzz"), "mine");

        assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir(), noContent()))
                .isInstanceOf(IOException.class)
                .hasMessageContaining(file.toString());
        then(Files.readString(file)).isEqualTo("mine");
    }

    @Test
    void refuses_to_run_when_the_knowledge_base_already_exists() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/library"));

        assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir(), noContent()))
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
            assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir(), noContent()))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining(tempDir.toString());
            then(tempDir.resolve("uzz")).doesNotExist();
        } finally {
            dir.setWritable(true);
        }
    }

    @Test
    void copies_the_skills_verbatim() throws IOException {
        KnowledgeBaseContent content = realContent();

        FileObject uzz = KnowledgeBaseInitializer.initialize(projectDir(), content).root();

        for (String skill : List.of("kb", "scope", "story", "code", "uzzo-init")) {
            then(uzz.getFileObject("skills/" + skill + "/SKILL.md")).as(skill).isNotNull();
        }
        then(uzz.getFileObject("skills/kb/references/user_story.md")).isNotNull();
        for (String path : content.paths()) {
            if (path.startsWith("skills/")) {
                then(Files.readAllBytes(tempDir.resolve("uzz/" + path))).as(path).isEqualTo(content.read(path));
            }
        }
    }

    @Test
    void does_not_copy_the_project_and_stories_files_of_the_distribution() throws IOException {
        KnowledgeBaseContent content = new StubContent(Map.of(
                "project/coding_standard.md", "other project",
                "stories/users.md", "other actors",
                "skills/kb/SKILL.md", "kb"));

        KnowledgeBaseInitializer.initialize(projectDir(), content);

        then(tempDir.resolve("uzz/project/coding_standard.md")).doesNotExist();
        then(tempDir.resolve("uzz/stories/users.md")).doesNotExist();
        then(tempDir.resolve("uzz/skills/kb/SKILL.md")).hasContent("kb");
    }

    @Test
    void generates_the_six_articles_from_the_templates() throws IOException {
        KnowledgeBaseInitializer.Result result = KnowledgeBaseInitializer.initialize(projectDir(), realContent());

        for (String article : KnowledgeBaseTemplates.ARTICLES) {
            String text = Files.readString(tempDir.resolve("uzz/" + article));
            then(text).as(article).contains("### Keywords");
            then(text.substring(text.lastIndexOf("### Keywords")).lines().skip(1))
                    .as(article).allMatch(line -> line.startsWith("- ") || line.isBlank());
        }
        then(Files.readString(tempDir.resolve("uzz/project/overview.md"))).contains("[PROJECT NAME]");
        then(Files.readString(tempDir.resolve("uzz/stories/index.md"))).doesNotContain("| id |");
        then(result.incomplete()).isEmpty();
    }

    @Test
    void keeps_gitkeep_only_in_folders_left_empty() throws IOException {
        KnowledgeBaseInitializer.initialize(projectDir(), realContent());

        then(tempDir.resolve("uzz/library/.gitkeep")).exists().isEmptyFile();
        then(tempDir.resolve("uzz/skills/.gitkeep")).doesNotExist();
        then(tempDir.resolve("uzz/project/.gitkeep")).doesNotExist();
        then(tempDir.resolve("uzz/stories/.gitkeep")).doesNotExist();
    }

    @Test
    void creates_the_skeleton_and_reports_incomplete_when_the_content_cannot_be_read() throws IOException {
        KnowledgeBaseContent unreadable = new KnowledgeBaseContent() {
            @Override
            public List<String> paths() throws IOException {
                throw new IOException("no content");
            }

            @Override
            public byte[] read(String path) throws IOException {
                throw new IOException("no content");
            }
        };

        KnowledgeBaseInitializer.Result result = KnowledgeBaseInitializer.initialize(projectDir(), unreadable);

        then(result.incomplete()).isNotEmpty();
        then(tempDir.resolve("uzz/stories/.gitkeep")).exists();
        then(tempDir.resolve("uzz/project/.gitkeep")).exists();
        then(Files.walk(tempDir.resolve("uzz")).filter(Files::isRegularFile).map(p -> p.getFileName().toString()))
                .containsOnly(".gitkeep");
    }

    @Test
    void ignores_content_paths_escaping_the_knowledge_base() {
        KnowledgeBaseContent content = new StubContent(Map.of("skills/../../evil.md", "x"));

        assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir(), content))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("skills/../../evil.md");
        then(tempDir.resolve("evil.md")).doesNotExist();
        then(tempDir.resolve("uzz")).doesNotExist();
    }

    private static List<String> names(FileObject folder) {
        return Arrays.stream(folder.getChildren()).map(FileObject::getNameExt).toList();
    }
}
