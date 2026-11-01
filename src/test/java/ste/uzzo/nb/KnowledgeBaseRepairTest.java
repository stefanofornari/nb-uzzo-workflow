/*
 * Copyright 2026 the original author or authors from the Uzzo and Uzzo Workflow project
 * (https://stefanofornari.github.io/uzzo-workflow).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ste.uzzo.nb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import ste.uzzo.nb.KnowledgeBaseInitializer.Kind;
import ste.uzzo.nb.KnowledgeBaseInitializer.Result;

/** US-000008 - the initialization is additive and can complete a partial knowledge base. */
class KnowledgeBaseRepairTest {

    @TempDir
    Path tempDir;

    private FileObject projectDir() {
        return FileUtil.toFileObject(tempDir.toFile());
    }

    private static KnowledgeBaseContent realContent() {
        return ClasspathKnowledgeBaseContent.of(KnowledgeBaseRepairTest.class.getClassLoader());
    }

    private Result run() throws IOException {
        return KnowledgeBaseInitializer.initialize(projectDir(), realContent(), ProjectFacts.NONE);
    }

    @Test
    void a_new_knowledge_base_is_fresh_and_lists_what_was_created() throws IOException {
        Result result = run();

        then(result.kind()).isEqualTo(Kind.FRESH);
        then(result.created()).contains("skills/kb/SKILL.md", "project/overview.md", "library/.gitkeep");
        then(result.skipped()).isEmpty();
    }

    @Test
    void repairs_a_partial_knowledge_base_without_touching_existing_content() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/stories"));
        Path scope = Files.writeString(tempDir.resolve("uzz/stories/scope.md"), "custom");

        Result result = run();

        then(result.kind()).isEqualTo(Kind.REPAIR);
        then(tempDir.resolve("uzz/project/overview.md")).exists();
        then(tempDir.resolve("uzz/skills/kb/SKILL.md")).exists();
        then(scope).hasContent("custom");
        then(result.skipped()).containsExactly("stories/scope.md");
        then(result.created()).doesNotContain("stories/scope.md").contains("project/overview.md");
    }

    @Test
    void an_empty_file_is_populated_from_the_template() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/project"));
        Path file = Files.createFile(tempDir.resolve("uzz/project/coding_standard.md"));

        Result result = run();

        then(Files.readString(file)).contains("# Coding Standard");
        then(result.created()).contains("project/coding_standard.md");
        then(result.skipped()).doesNotContain("project/coding_standard.md");
    }

    @Test
    void a_non_empty_skill_is_never_overwritten() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/skills/kb"));
        Path skill = Files.writeString(tempDir.resolve("uzz/skills/kb/SKILL.md"), "mine");

        Result result = run();

        then(skill).hasContent("mine");
        then(result.skipped()).contains("skills/kb/SKILL.md");
    }

    @Test
    void a_complete_knowledge_base_is_left_alone() throws IOException {
        run();
        Path overview = tempDir.resolve("uzz/project/overview.md");
        Files.writeString(overview, "edited");

        Result result = run();

        then(result.kind()).isEqualTo(Kind.COMPLETE);
        then(result.created()).isEmpty();
        then(overview).hasContent("edited");
    }

    @Test
    void a_missing_gitkeep_is_restored_only_in_an_empty_folder() throws IOException {
        run();
        Files.delete(tempDir.resolve("uzz/library/.gitkeep"));

        Result result = run();

        then(result.kind()).isEqualTo(Kind.REPAIR);
        then(result.created()).containsExactly("library/.gitkeep");

        Files.writeString(tempDir.resolve("uzz/library/note.md"), "x");
        Files.delete(tempDir.resolve("uzz/library/.gitkeep"));
        then(run().kind()).isEqualTo(Kind.COMPLETE);
        then(tempDir.resolve("uzz/library/.gitkeep")).doesNotExist();
    }

    @Test
    void reports_the_placeholders_of_the_created_articles_with_file_and_field() throws IOException {
        Result result = run();

        then(result.placeholders()).contains("project/overview.md: [PROJECT NAME]", "stories/scope.md: [VERSION]");
        then(result.placeholders()).doesNotContain("skills/kb/SKILL.md: [PROJECT NAME]");
    }

    @Test
    void placeholders_with_a_known_fact_are_not_reported() throws IOException {
        Result result = KnowledgeBaseInitializer.initialize(
                projectDir(), realContent(), new ProjectFacts("App", "1.0", null, null, null));

        then(result.placeholders()).doesNotContain("project/overview.md: [PROJECT NAME]")
                .contains("project/overview.md: [NAME]");
    }

    @Test
    void placeholders_of_skipped_files_are_not_reported() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/stories"));
        Files.writeString(tempDir.resolve("uzz/stories/scope.md"), "# [PROJECT NAME]");

        Result result = run();

        then(result.placeholders()).noneMatch(p -> p.startsWith("stories/scope.md"));
    }

    @Test
    void checkboxes_and_filled_links_are_not_placeholders() throws IOException {
        KnowledgeBaseContent content = new KnowledgeBaseInitializerTest.StubContent(Map.of(
                "skills/uzzo-init/references/templates.md",
                "## 1. `project/overview.md`\n\n```markdown\n- [ ] todo\n- [x] done\n- [App](https://x/y)\n- [REPO]([URL])\n```\n"));

        Result result = KnowledgeBaseInitializer.initialize(projectDir(), content, ProjectFacts.NONE);

        then(result.placeholders()).containsExactly("project/overview.md: [REPO]", "project/overview.md: [URL]");
    }

    @Test
    void a_section_that_is_a_file_is_a_conflict_and_nothing_is_written() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz"));
        Path project = Files.writeString(tempDir.resolve("uzz/project"), "mine");

        assertThatThrownBy(this::run)
                .isInstanceOf(IOException.class)
                .hasMessageContaining(project.toString());
        then(project).hasContent("mine");
        then(tempDir.resolve("uzz/stories")).doesNotExist();
        then(tempDir.resolve("uzz/skills")).doesNotExist();
    }

    @Test
    void a_folder_where_a_file_is_expected_is_a_conflict_listing_every_path() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/skills/kb/SKILL.md"));
        Files.createDirectories(tempDir.resolve("uzz/project/overview.md"));

        assertThatThrownBy(this::run)
                .isInstanceOf(IOException.class)
                .hasMessageContaining(tempDir.resolve("uzz/skills/kb/SKILL.md").toString())
                .hasMessageContaining(tempDir.resolve("uzz/project/overview.md").toString());
        then(tempDir.resolve("uzz/stories")).doesNotExist();
    }

    @Test
    void a_failed_repair_keeps_the_existing_knowledge_base() throws IOException {
        Path library = Files.createDirectories(tempDir.resolve("uzz/library"));
        Files.writeString(library.resolve("note.md"), "mine");
        KnowledgeBaseContent content = new KnowledgeBaseInitializerTest.StubContent(Map.of("skills/a/../b.md", "x"));

        assertThatThrownBy(() -> KnowledgeBaseInitializer.initialize(projectDir(), content, ProjectFacts.NONE))
                .isInstanceOf(IOException.class);
        then(library.resolve("note.md")).hasContent("mine");
    }
}
