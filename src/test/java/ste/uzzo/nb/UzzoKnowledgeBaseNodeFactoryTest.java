package ste.uzzo.nb;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import static org.assertj.core.api.BDDAssertions.then;

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
    void no_keys_without_uzz_folder() {
        then(nodes.keys()).isEmpty();
    }

    @Test
    void one_key_when_uzz_folder_exists() throws IOException {
        projectDir.createFolder("uzz");

        then(nodes.keys()).containsExactly("uzz");
    }

    @Test
    void no_keys_when_uzz_is_a_file_not_a_folder() throws IOException {
        projectDir.createData("uzz");

        then(nodes.keys()).isEmpty();
    }

    @Test
    void node_is_the_knowledge_base_node() throws IOException {
        projectDir.createFolder("uzz");

        Node node = nodes.node("uzz");

        then(node).isInstanceOf(UzzoKnowledgeBaseNode.class);
        then(node.getDisplayName()).isEqualTo("Uzzo Knowledge Base");
    }

    @Test
    void no_node_for_a_missing_folder() {
        then(nodes.node("uzz")).isNull();
    }

    @Test
    void fires_change_when_uzz_folder_is_created_and_deleted() throws IOException {
        AtomicInteger changes = new AtomicInteger();
        nodes.addNotify();
        nodes.addChangeListener(e -> changes.incrementAndGet());

        FileObject uzz = projectDir.createFolder("uzz");
        then(waitFor(() -> changes.get() >= 1)).as("no change event on create").isTrue();

        int afterCreate = changes.get();
        uzz.delete();
        then(waitFor(() -> changes.get() > afterCreate)).as("no change event on delete").isTrue();
    }

    @Test
    void ignores_other_folders_appearing() throws IOException {
        AtomicInteger changes = new AtomicInteger();
        nodes.addNotify();
        nodes.addChangeListener(e -> changes.incrementAndGet());

        projectDir.createFolder("src");
        projectDir.createFolder("target");

        then(changes.get()).isZero();
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
