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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void fileInsideUzzBelongsToTheKnowledgeBase() {
        assertTrue(UzzoDeckHook.isInKnowledgeBase(uzzDoc));
    }

    @Test
    void fileOutsideUzzDoesNot() {
        assertFalse(UzzoDeckHook.isInKnowledgeBase(sourceFile));
    }

    @Test
    void fileWithoutOwnerDoesNot() {
        TestOwnerQuery.project = null;

        assertFalse(UzzoDeckHook.isInKnowledgeBase(uzzDoc));
    }

    @Test
    void projectWithoutUzzFolderHasNoKnowledgeBase() throws IOException {
        FileObject other = FileUtil.createMemoryFileSystem().getRoot();
        FileObject file = other.createFolder("docs").createData("a.md");
        TestOwnerQuery.project = new TestProject(other);

        assertFalse(UzzoDeckHook.isInKnowledgeBase(file));
    }

    @Test
    void aFileNamedLikeUzzButOutsideTheFolderDoesNot() throws IOException {
        FileObject lookalike = root.createFolder("src2").createFolder("uzz").createData("x.md");

        assertFalse(UzzoDeckHook.isInKnowledgeBase(lookalike));
    }

    // --- fileOf ------------------------------------------------------------

    @Test
    void fileComesFromTheDataObjectInTheLookup() throws IOException {
        TopComponent tc = new TopComponent(Lookups.fixed(DataObject.find(uzzDoc)));

        assertEquals(uzzDoc, UzzoDeckHook.fileOf(tc));
    }

    @Test
    void fileComesFromAFileObjectInTheLookup() {
        TopComponent tc = new TopComponent(Lookups.fixed(uzzDoc));

        assertEquals(uzzDoc, UzzoDeckHook.fileOf(tc));
    }

    @Test
    void fileFallsBackToTheActivatedNodes() {
        TopComponent tc = new TopComponent();
        Node node = new AbstractNode(Children.LEAF, Lookups.fixed(uzzDoc));
        tc.setActivatedNodes(new Node[]{node});

        assertEquals(uzzDoc, UzzoDeckHook.fileOf(tc));
    }

    @Test
    void fileIsNullWhenNothingExposesIt() {
        assertNull(UzzoDeckHook.fileOf(new TopComponent()));
    }
}
