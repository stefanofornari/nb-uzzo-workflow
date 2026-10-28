package ste.uzzo.nb;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UzzoKnowledgeBaseNodeFactoryTest {

    private FileObject projectDir;
    private NodeList<String> nodes;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        projectDir = FileUtil.createMemoryFileSystem().getRoot();
        nodes = (NodeList<String>) new UzzoKnowledgeBaseNodeFactory().createNodes(new TestProject(projectDir));
    }

    @AfterEach
    void tearDown() {
        nodes.removeNotify();
    }

    @Test
    void noKeysWithoutUzzFolder() {
        assertEquals(List.of(), nodes.keys());
    }

    @Test
    void oneKeyWhenUzzFolderExists() throws IOException {
        projectDir.createFolder("uzz");

        assertEquals(List.of("uzz"), nodes.keys());
    }

    @Test
    void noKeysWhenUzzIsAFileNotAFolder() throws IOException {
        projectDir.createData("uzz");

        assertEquals(List.of(), nodes.keys());
    }

    @Test
    void nodeIsTheKnowledgeBaseNode() throws IOException {
        projectDir.createFolder("uzz");

        Node node = nodes.node("uzz");

        assertInstanceOf(UzzoKnowledgeBaseNode.class, node);
        assertEquals("Uzzo Knowledge Base", node.getDisplayName());
    }

    @Test
    void noNodeForAMissingFolder() {
        assertNull(nodes.node("uzz"));
    }

    @Test
    void firesChangeWhenUzzFolderIsCreatedAndDeleted() throws IOException {
        AtomicInteger changes = new AtomicInteger();
        nodes.addNotify();
        nodes.addChangeListener(e -> changes.incrementAndGet());

        FileObject uzz = projectDir.createFolder("uzz");
        assertTrue(waitFor(() -> changes.get() >= 1), "no change event on create");

        int afterCreate = changes.get();
        uzz.delete();
        assertTrue(waitFor(() -> changes.get() > afterCreate), "no change event on delete");
    }

    @Test
    void ignoresOtherFoldersAppearing() throws IOException {
        AtomicInteger changes = new AtomicInteger();
        nodes.addNotify();
        nodes.addChangeListener(e -> changes.incrementAndGet());

        projectDir.createFolder("src");
        projectDir.createFolder("target");

        assertEquals(0, changes.get());
    }

    private static boolean waitFor(BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return condition.getAsBoolean();
    }
}
