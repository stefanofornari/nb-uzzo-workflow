package ste.uzzo.nb;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

import static org.assertj.core.api.BDDAssertions.then;

class UzzoDeckHookTest {

    private FileObject root;
    private FileObject uzzDoc;
    private FileObject sourceFile;

    @BeforeEach
    void setUp() throws IOException {
        root = FileUtil.createMemoryFileSystem().getRoot();
        uzzDoc = root.createFolder("uzz").createFolder("library").createData("guide.md");
        sourceFile = root.createFolder("src").createData("Main.java");
        TestOwnerQuery.project = new TestProject(root);
    }

    @AfterEach
    void tearDown() {
        TestOwnerQuery.project = null;
    }

    // --- isInKnowledgeBase -------------------------------------------------

    @Test
    void file_inside_uzz_belongs_to_the_knowledge_base() {
        then(UzzoDeckHook.isInKnowledgeBase(uzzDoc)).isTrue();
    }

    @Test
    void file_outside_uzz_does_not() {
        then(UzzoDeckHook.isInKnowledgeBase(sourceFile)).isFalse();
    }

    @Test
    void file_without_owner_does_not() {
        TestOwnerQuery.project = null;

        then(UzzoDeckHook.isInKnowledgeBase(uzzDoc)).isFalse();
    }

    @Test
    void project_without_uzz_folder_has_no_knowledge_base() throws IOException {
        FileObject other = FileUtil.createMemoryFileSystem().getRoot();
        FileObject file = other.createFolder("docs").createData("a.md");
        TestOwnerQuery.project = new TestProject(other);

        then(UzzoDeckHook.isInKnowledgeBase(file)).isFalse();
    }

    @Test
    void a_file_named_like_uzz_but_outside_the_folder_does_not() throws IOException {
        FileObject lookalike = root.createFolder("src2").createFolder("uzz").createData("x.md");

        then(UzzoDeckHook.isInKnowledgeBase(lookalike)).isFalse();
    }

    // --- fileOf ------------------------------------------------------------

    @Test
    void file_comes_from_the_data_object_in_the_lookup() throws IOException {
        TopComponent tc = new TopComponent(Lookups.fixed(DataObject.find(uzzDoc)));

        then(UzzoDeckHook.fileOf(tc)).isEqualTo(uzzDoc);
    }

    @Test
    void file_comes_from_a_file_object_in_the_lookup() {
        TopComponent tc = new TopComponent(Lookups.fixed(uzzDoc));

        then(UzzoDeckHook.fileOf(tc)).isEqualTo(uzzDoc);
    }

    @Test
    void file_falls_back_to_the_activated_nodes() {
        TopComponent tc = new TopComponent();
        Node node = new AbstractNode(Children.LEAF, Lookups.fixed(uzzDoc));
        tc.setActivatedNodes(new Node[]{node});

        then(UzzoDeckHook.fileOf(tc)).isEqualTo(uzzDoc);
    }

    @Test
    void file_is_null_when_nothing_exposes_it() {
        then(UzzoDeckHook.fileOf(new TopComponent())).isNull();
    }
}
