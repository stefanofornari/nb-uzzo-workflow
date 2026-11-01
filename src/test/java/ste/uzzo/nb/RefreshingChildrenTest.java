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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import static org.assertj.core.api.BDDAssertions.then;

class RefreshingChildrenTest {

    @TempDir
    Path tempDir;

    private Path project;
    private Node projectNode;

    @BeforeEach
    void setUp() throws IOException {
        project = Files.createDirectories(tempDir.resolve("uzz/stories/project"));
        Files.writeString(project.resolve("US-000001.md"), "one");
        UzzoKnowledgeBaseNode node = new UzzoKnowledgeBaseNode(
                FileUtil.toFileObject(tempDir.resolve("uzz").toFile()));
        projectNode = projectNode(node);
        projectNode.getChildren().getNodes(true);
    }

    @Test
    void refreshes_nested_folder_when_nodes_are_requested() throws IOException {
        createSecondStory();
        then(projectNode.getChildren().findChild("US-000002")).isNull();

        projectNode.getChildren().getNodes(true);

        then(projectNode.getChildren().findChild("US-000002")).isNotNull();
    }

    @Test
    void refreshes_nested_folder_when_the_node_count_is_requested() throws IOException {
        createSecondStory();
        then(projectNode.getChildren().findChild("US-000002")).isNull();

        projectNode.getChildren().getNodesCount(true);

        then(projectNode.getChildren().findChild("US-000002")).isNotNull();
    }

    private void createSecondStory() throws IOException {
        Files.createDirectories(project.resolve("US-000002"));
        Files.writeString(project.resolve("US-000002/US-000002.md"), "two");
    }

    private static Node projectNode(UzzoKnowledgeBaseNode node) {
        Node stories = node.getChildren().findChild("stories");
        then(stories).as("stories child missing").isNotNull();
        Node project = stories.getChildren().findChild("project");
        then(project).as("project child missing").isNotNull();
        return project;
    }
}
