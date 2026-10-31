package ste.uzzo.nb;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;

class ClasspathKnowledgeBaseContentTest {

    @TempDir
    Path tempDir;

    @Test
    void lists_the_content_of_the_dependency_jar() throws IOException {
        KnowledgeBaseContent content = ClasspathKnowledgeBaseContent.of(getClass().getClassLoader());

        then(content.paths()).contains(
                "skills/kb/SKILL.md",
                "skills/kb/references/user_story.md",
                "skills/scope/SKILL.md",
                "skills/story/SKILL.md",
                "skills/code/SKILL.md",
                "skills/uzzo-init/SKILL.md",
                "skills/uzzo-init/references/templates.md"
        ).allMatch(p -> !p.startsWith("uzz/") && !p.startsWith("/"));
    }

    @Test
    void reads_the_bytes_of_a_file_in_the_jar() throws IOException {
        KnowledgeBaseContent content = ClasspathKnowledgeBaseContent.of(getClass().getClassLoader());

        byte[] expected;
        try (var in = getClass().getResourceAsStream("/uzz/skills/kb/SKILL.md")) {
            expected = in.readAllBytes();
        }

        then(content.read("skills/kb/SKILL.md")).isEqualTo(expected);
    }

    @Test
    void lists_the_content_when_it_is_a_folder() throws Exception {
        Files.createDirectories(tempDir.resolve("uzz/skills/kb"));
        Files.createDirectories(tempDir.resolve("uzz/skills/scope"));
        Files.writeString(tempDir.resolve("uzz/skills/kb/SKILL.md"), "kb");
        Files.writeString(tempDir.resolve("uzz/skills/scope/SKILL.md"), "scope");
        try (URLClassLoader loader = new URLClassLoader(new URL[] {tempDir.toUri().toURL()}, null)) {
            KnowledgeBaseContent content = ClasspathKnowledgeBaseContent.of(loader);

            then(content.paths()).containsExactly("skills/kb/SKILL.md", "skills/scope/SKILL.md");
            then(new String(content.read("skills/scope/SKILL.md"), StandardCharsets.UTF_8)).isEqualTo("scope");
        }
    }

    @Test
    void fails_when_the_content_is_not_on_the_classpath() throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {tempDir.toUri().toURL()}, null)) {
            KnowledgeBaseContent content = ClasspathKnowledgeBaseContent.of(loader);

            assertThatThrownBy(content::paths).isInstanceOf(IOException.class);
        }
    }

    @Test
    void fails_reading_a_file_that_does_not_exist() {
        KnowledgeBaseContent content = ClasspathKnowledgeBaseContent.of(getClass().getClassLoader());

        assertThatThrownBy(() -> content.read("skills/nope/SKILL.md"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("skills/nope/SKILL.md");
    }
}
