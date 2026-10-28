package ste.uzzo.nb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void refreshesNestedFolderWhenNodesAreRequested() throws IOException {
        createSecondStory();
        assertNull(projectNode.getChildren().findChild("US-000002"));

        projectNode.getChildren().getNodes(true);

        assertNotNull(projectNode.getChildren().findChild("US-000002"));
    }

    @Test
    void refreshesNestedFolderWhenTheNodeCountIsRequested() throws IOException {
        createSecondStory();
        assertNull(projectNode.getChildren().findChild("US-000002"));

        projectNode.getChildren().getNodesCount(true);

        assertNotNull(projectNode.getChildren().findChild("US-000002"));
    }

    private void createSecondStory() throws IOException {
        Files.createDirectories(project.resolve("US-000002"));
        Files.writeString(project.resolve("US-000002/US-000002.md"), "two");
    }

    private static Node projectNode(UzzoKnowledgeBaseNode node) {
        Node stories = node.getChildren().findChild("stories");
        assertNotNull(stories, "stories child missing");
        Node project = stories.getChildren().findChild("project");
        assertNotNull(project, "project child missing");
        return project;
    }
}
