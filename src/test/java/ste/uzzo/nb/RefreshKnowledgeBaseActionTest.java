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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshKnowledgeBaseActionTest {

    @TempDir
    Path tempDir;

    @Test
    void refreshesFoldersNestedBelowTheKnowledgeBase() throws IOException {
        Path project = Files.createDirectories(tempDir.resolve("uzz/stories/project"));
        Files.writeString(project.resolve("US-000001.md"), "one");
        FileObject uzzDir = FileUtil.toFileObject(tempDir.resolve("uzz").toFile());
        UzzoKnowledgeBaseNode node = new UzzoKnowledgeBaseNode(uzzDir);
        Node projectNode = projectNode(node);
        projectNode.getChildren().getNodes(true);

        Files.createDirectories(project.resolve("US-000002"));
        Files.writeString(project.resolve("US-000002/US-000002.md"), "two");
        assertNull(projectNode.getChildren().findChild("US-000002"));

        RefreshKnowledgeBaseAction.refreshRecursively(uzzDir);

        assertNotNull(projectNode.getChildren().findChild("US-000002"));
    }

    @Test
    void knowledgeBaseNodeOffersTheRefreshAction() throws IOException {
        Path uzz = Files.createDirectories(tempDir.resolve("uzz"));
        UzzoKnowledgeBaseNode node = new UzzoKnowledgeBaseNode(
                FileUtil.toFileObject(uzz.toFile()));

        boolean found = Arrays.stream(node.getActions(false))
                .map(action -> action.getValue(Action.NAME))
                .anyMatch("Refresh Knowledge Base"::equals);

        assertTrue(found, "Refresh Knowledge Base action missing");
    }

    private static Node projectNode(UzzoKnowledgeBaseNode node) {
        Node stories = node.getChildren().findChild("stories");
        assertNotNull(stories, "stories child missing");
        Node project = stories.getChildren().findChild("project");
        assertNotNull(project, "project child missing");
        return project;
    }
}
