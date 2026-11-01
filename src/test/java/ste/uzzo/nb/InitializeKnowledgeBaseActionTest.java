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
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.api.project.Project;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

import static org.assertj.core.api.BDDAssertions.then;

class InitializeKnowledgeBaseActionTest {

    @TempDir
    Path tempDir;

    private Action actionFor(Project project) {
        return ((ContextAwareAction) new InitializeKnowledgeBaseAction())
                .createContextAwareInstance(Lookups.singleton(project));
    }

    private static final FactsPrompt PROMPT_AS_IS = proposed -> proposed;

    private static InitializeKnowledgeBaseAction action(FactsPrompt prompt) {
        return new InitializeKnowledgeBaseAction(
                ClasspathKnowledgeBaseContent.of(InitializeKnowledgeBaseActionTest.class.getClassLoader()), prompt);
    }

    private Project project() {
        return new TestProject(FileUtil.toFileObject(tempDir.toFile()));
    }

    @Test
    void has_the_initialize_name() {
        then(actionFor(project()).getValue(Action.NAME)).isEqualTo("Initialize Uzzo Knowledge Base");
    }

    @Test
    void has_the_lamp_icon() {
        Action action = actionFor(project());

        then(action.getValue("iconBase")).isEqualTo("ste/uzzo/nb/lamp.png");
        then(action.getValue(Action.SMALL_ICON)).isNotNull();
    }

    @Test
    void the_popup_presenter_shows_the_icon_and_the_name() {
        JMenuItem item = ((Presenter.Popup) actionFor(project())).getPopupPresenter();

        then(item.getIcon()).isNotNull();
        then(item.getText()).isEqualTo("Initialize Uzzo Knowledge Base");
    }

    @Test
    void enabled_without_a_knowledge_base() {
        then(actionFor(project()).isEnabled()).isTrue();
    }

    @Test
    void becomes_the_repair_action_with_a_knowledge_base() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz"));
        Action action = actionFor(project());

        then(action.getValue(Action.NAME)).isEqualTo("Repair Uzzo Knowledge Base");
        then(action.isEnabled()).isTrue();
        then(((Presenter.Popup) action).getPopupPresenter().getText()).isEqualTo("Repair Uzzo Knowledge Base");
        then(((Presenter.Popup) action).getPopupPresenter().getIcon()).isNotNull();
    }

    @Test
    void enabled_when_uzz_is_a_file() throws IOException {
        Files.writeString(tempDir.resolve("uzz"), "x");

        then(actionFor(project()).isEnabled()).isTrue();
    }

    @Test
    void disabled_without_a_project_in_context() {
        Action action = ((ContextAwareAction) new InitializeKnowledgeBaseAction())
                .createContextAwareInstance(Lookup.EMPTY);

        then(action.isEnabled()).isFalse();
    }

    @Test
    void invoking_creates_the_knowledge_base() throws Exception {
        NotifyDescriptor outcome = action(PROMPT_AS_IS).initialize(project());

        then(outcome.getMessageType()).isEqualTo(NotifyDescriptor.INFORMATION_MESSAGE);
        then(outcome.getMessage().toString()).contains("created").contains("project/overview.md");
        then(tempDir.resolve("uzz/stories")).isDirectory();
        then(tempDir.resolve("uzz/skills/kb/SKILL.md")).exists();
    }

    @Test
    void failure_is_reported_as_an_error_naming_the_path() throws IOException {
        Path file = Files.writeString(tempDir.resolve("uzz"), "x");

        NotifyDescriptor outcome = action(PROMPT_AS_IS).initialize(project());

        then(outcome.getMessageType()).isEqualTo(NotifyDescriptor.ERROR_MESSAGE);
        then(outcome.getMessage().toString()).contains(file.toString());
    }

    @Test
    void unreadable_content_is_reported_as_a_warning_about_the_incomplete_knowledge_base() {
        KnowledgeBaseContent unreadable = new KnowledgeBaseContent() {
            @Override
            public java.util.List<String> paths() throws IOException {
                throw new IOException("no content");
            }

            @Override
            public byte[] read(String path) throws IOException {
                throw new IOException("no content");
            }
        };

        NotifyDescriptor outcome = new InitializeKnowledgeBaseAction(unreadable, PROMPT_AS_IS).initialize(project());

        then(outcome.getMessageType()).isEqualTo(NotifyDescriptor.WARNING_MESSAGE);
        then(outcome.getMessage().toString()).contains("incomplete");
        then(tempDir.resolve("uzz/stories")).isDirectory();
    }

    private static final String POM = """
            <project><artifactId>a</artifactId><name>Pom Name</name><version>3.1</version>
              <description>From the pom.</description></project>""";

    @Test
    void the_dialog_is_proposed_the_facts_of_the_pom() throws IOException {
        Files.writeString(tempDir.resolve("pom.xml"), POM);
        ProjectFacts[] proposed = new ProjectFacts[1];

        action(p -> {
            proposed[0] = p;
            return p;
        }).initialize(project());

        then(proposed[0]).isEqualTo(new ProjectFacts("Pom Name", "3.1", "From the pom.", null, null));
    }

    @Test
    void edited_values_are_written_to_the_articles() throws IOException {
        Files.writeString(tempDir.resolve("pom.xml"), POM);

        action(p -> new ProjectFacts("Edited", p.version(), null, null, null)).initialize(project());

        then(Files.readString(tempDir.resolve("uzz/project/overview.md")))
                .contains("**Name**: Edited").contains("**Version**: 3.1").contains("[One paragraph describing");
        then(Files.readString(tempDir.resolve("uzz/stories/scope.md"))).contains("# Project Name: Edited");
    }

    @Test
    void cancelling_the_dialog_creates_nothing() throws IOException {
        NotifyDescriptor outcome = action(p -> null).initialize(project());

        then(outcome).isNull();
        then(tempDir.resolve("uzz")).doesNotExist();
    }

    @Test
    void an_existing_uzz_is_reported_without_asking_anything() throws IOException {
        Files.writeString(tempDir.resolve("uzz"), "x");
        boolean[] asked = new boolean[1];

        NotifyDescriptor outcome = action(p -> {
            asked[0] = true;
            return p;
        }).initialize(project());

        then(asked[0]).isFalse();
        then(outcome.getMessageType()).isEqualTo(NotifyDescriptor.ERROR_MESSAGE);
    }
}
