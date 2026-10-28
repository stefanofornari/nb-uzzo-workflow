package ste.uzzo.nb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class UzzoKnowledgeBaseNodeTest {

    private UzzoKnowledgeBaseNode node;
    private FileObject uzzDir;

    @BeforeEach
    void setUp() throws IOException {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject uzz = root.createFolder("uzz");
        uzzDir = uzz;
        for (String name : new String[]{"library", "project", "skills", "stories", "misc"}) {
            uzz.createFolder(name);
        }
        uzz.createData("readme.txt");
        node = new UzzoKnowledgeBaseNode(uzz);
    }

    @Test
    void displayNameIsTheKnowledgeBaseLabel() {
        assertEquals("Uzzo Knowledge Base", node.getDisplayName());
    }

    @Test
    void hasTheLampIconForBothStates() {
        assertNotNull(node.getIcon(0));
        assertNotNull(node.getOpenedIcon(0));
    }

    @Test
    void cannotBeRenamedAndHasNoHtmlName() {
        assertFalse(node.canRename());
        assertNull(node.getHtmlDisplayName());
    }

    @Test
    void childrenAreTheContentOfTheFolder() {
        Map<String, Node> children = children();

        // 5 sub-folders + readme.txt
        assertEquals(6, node.getChildren().getNodesCount(true));
        assertNotNull(children.get("misc"));
    }

    @Test
    void sectionFoldersGetSectionNodes() {
        Map<String, Node> children = children();

        for (String section : new String[]{"library", "project", "skills", "stories"}) {
            assertInstanceOf(SectionNode.class, children.get(section), section);
        }
    }

    @Test
    void otherChildrenAreNotSectionNodes() {
        Map<String, Node> children = children();

        assertFalse(children.get("misc") instanceof SectionNode);
    }

    @Test
    void nestedFoldersWithSectionNamesAreNotDecorated() throws IOException {
        uzzDir.getFileObject("stories").createFolder("project");

        Node nested = children().get("stories").getChildren().findChild("project");

        assertNotNull(nested);
        assertFalse(nested instanceof SectionNode);
    }

    @Test
    void filesRemainLeafNodes() {
        Node readme = children().get("readme.txt");

        assertNotNull(readme);
        assertSame(Children.LEAF, readme.getChildren());
    }

    private Map<String, Node> children() {
        Map<String, Node> byName = new HashMap<>();
        for (Node child : node.getChildren().getNodes(true)) {
            byName.put(child.getName(), child);
        }
        return byName;
    }
}
