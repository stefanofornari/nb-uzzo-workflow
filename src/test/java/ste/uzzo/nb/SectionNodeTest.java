package ste.uzzo.nb;

import java.awt.Image;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionNodeTest {

    private static Node nodeNamed(String name) {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setName(name);
        return node;
    }

    private static boolean hasVisiblePixels(Image image) {
        BufferedImage img = assertInstanceOf(BufferedImage.class, image);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if ((img.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @ParameterizedTest
    @ValueSource(strings = {"library", "project", "skills", "stories"})
    void knownSectionsGetAnIconNode(String name) {
        Node wrapped = SectionNode.wrap(nodeNamed(name));

        assertInstanceOf(SectionNode.class, wrapped);
        assertEquals(name, wrapped.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"library", "project", "skills", "stories"})
    void sectionIconIsRenderedAt16px(String name) {
        Image icon = SectionNode.wrap(nodeNamed(name)).getIcon(0);

        assertEquals(16, icon.getWidth(null));
        assertEquals(16, icon.getHeight(null));
        assertTrue(hasVisiblePixels(icon), "icon for '" + name + "' is blank");
    }

    @Test
    void openedIconIsTheSameAsClosedIcon() {
        Node node = SectionNode.wrap(nodeNamed("library"));

        assertEquals(node.getIcon(0), node.getOpenedIcon(0));
    }

    @Test
    void sectionsHaveDistinctIcons() {
        BufferedImage library = (BufferedImage) SectionNode.wrap(nodeNamed("library")).getIcon(0);
        BufferedImage skills = (BufferedImage) SectionNode.wrap(nodeNamed("skills")).getIcon(0);

        boolean differ = false;
        for (int y = 0; y < 16 && !differ; y++) {
            for (int x = 0; x < 16 && !differ; x++) {
                differ = library.getRGB(x, y) != skills.getRGB(x, y);
            }
        }
        assertTrue(differ, "library and skills render the same icon");
    }

    @Test
    void otherFoldersAreWrappedButKeepTheirOwnIcon() {
        Node original = nodeNamed("misc");

        Node wrapped = SectionNode.wrap(original);

        assertFalse(wrapped instanceof SectionNode);
        assertInstanceOf(FilterNode.class, wrapped);
        assertNotSame(original, wrapped);
        assertEquals("misc", wrapped.getName());
    }
}
