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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import static org.assertj.core.api.BDDAssertions.then;

class RefreshKnowledgeBaseActionTest {

    @TempDir
    Path tempDir;

    @Test
    void refreshes_folders_nested_below_the_knowledge_base() throws IOException {
        Path project = Files.createDirectories(tempDir.resolve("uzz/stories/project"));
        Files.writeString(project.resolve("US-000001.md"), "one");
        FileObject uzzDir = FileUtil.toFileObject(tempDir.resolve("uzz").toFile());
        UzzoKnowledgeBaseNode node = new UzzoKnowledgeBaseNode(uzzDir);
        Node projectNode = projectNode(node);
        projectNode.getChildren().getNodes(true);

        Files.createDirectories(project.resolve("US-000002"));
        Files.writeString(project.resolve("US-000002/US-000002.md"), "two");
        then(projectNode.getChildren().findChild("US-000002")).isNull();

        RefreshKnowledgeBaseAction.refreshRecursively(uzzDir);

        then(projectNode.getChildren().findChild("US-000002")).isNotNull();
    }

    @Test
    void knowledge_base_node_offers_the_refresh_action() throws IOException {
        Path uzz = Files.createDirectories(tempDir.resolve("uzz"));
        UzzoKnowledgeBaseNode node = new UzzoKnowledgeBaseNode(
                FileUtil.toFileObject(uzz.toFile()));

        boolean found = Arrays.stream(node.getActions(false))
                .map(action -> action.getValue(Action.NAME))
                .anyMatch("Refresh Knowledge Base"::equals);

        then(found).as("Refresh Knowledge Base action missing").isTrue();
    }

    private static Node projectNode(UzzoKnowledgeBaseNode node) {
        Node stories = node.getChildren().findChild("stories");
        then(stories).as("stories child missing").isNotNull();
        Node project = stories.getChildren().findChild("project");
        then(project).as("project child missing").isNotNull();
        return project;
    }

    @Test
    void the_refresh_action_shows_the_lamp_icon_in_the_popup_menu() throws IOException {
        Path uzz = Files.createDirectories(tempDir.resolve("uzz"));
        RefreshKnowledgeBaseAction action = new RefreshKnowledgeBaseAction(FileUtil.toFileObject(uzz.toFile()));

        then(action.getPopupPresenter().getIcon()).isNotNull();
        then(action.getPopupPresenter().getText()).isEqualTo("Refresh Knowledge Base");
    }
}
