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
import java.util.Arrays;
import javax.swing.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.assertj.core.api.BDDAssertions.then;

class RepairKnowledgeBaseActionTest {

    @TempDir
    Path tempDir;

    private FileObject uzz() throws IOException {
        return FileUtil.toFileObject(Files.createDirectories(tempDir.resolve("uzz")).toFile());
    }

    private RepairKnowledgeBaseAction action() throws IOException {
        return new RepairKnowledgeBaseAction(uzz(),
                ClasspathKnowledgeBaseContent.of(getClass().getClassLoader()));
    }

    @Test
    void has_the_repair_name_and_the_lamp_icon() throws IOException {
        RepairKnowledgeBaseAction action = action();

        then(action.getValue(Action.NAME)).isEqualTo("Repair Uzzo Knowledge Base");
        then(action.getValue(Action.SMALL_ICON)).isNotNull();
        then(action.getPopupPresenter().getIcon()).isNotNull();
    }

    @Test
    void the_knowledge_base_node_offers_it() throws IOException {
        UzzoKnowledgeBaseNode node = new UzzoKnowledgeBaseNode(uzz());

        then(Arrays.stream(node.getActions(false)).map(a -> a == null ? null : a.getValue(Action.NAME)))
                .contains("Repair Uzzo Knowledge Base");
    }

    @Test
    void completes_a_partial_knowledge_base_and_reports_the_outcome() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz/stories"));
        Path scope = Files.writeString(tempDir.resolve("uzz/stories/scope.md"), "custom");

        NotifyDescriptor outcome = action().repair();

        then(tempDir.resolve("uzz/project/overview.md")).exists();
        then(scope).hasContent("custom");
        then(outcome.getMessage().toString()).contains("repaired").contains("stories/scope.md");
    }

    @Test
    void uses_the_facts_of_the_pom_without_asking() throws IOException {
        Files.writeString(tempDir.resolve("pom.xml"), "<project><name>Pom App</name><version>2.0</version></project>");

        action().repair();

        then(Files.readString(tempDir.resolve("uzz/project/overview.md"))).contains("**Name**: Pom App");
    }

    @Test
    void a_complete_knowledge_base_is_reported_as_such() throws IOException {
        action().repair();

        NotifyDescriptor outcome = action().repair();

        then(outcome.getMessage().toString()).contains("already complete");
    }

    @Test
    void a_conflict_is_reported_as_an_error() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz"));
        Files.writeString(tempDir.resolve("uzz/project"), "mine");

        NotifyDescriptor outcome = action().repair();

        then(outcome.getMessageType()).isEqualTo(NotifyDescriptor.ERROR_MESSAGE);
        then(outcome.getMessage().toString()).contains(tempDir.resolve("uzz/project").toString());
    }
}
