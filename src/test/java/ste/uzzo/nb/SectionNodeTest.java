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

import static org.assertj.core.api.BDDAssertions.then;

class SectionNodeTest {

    private static Node nodeNamed(String name) {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setName(name);
        return node;
    }

    private static boolean hasVisiblePixels(Image image) {
        then(image).isInstanceOf(BufferedImage.class);
        BufferedImage img = (BufferedImage) image;
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
    void known_sections_get_an_icon_node(String name) {
        Node wrapped = SectionNode.wrap(nodeNamed(name));

        then(wrapped).isInstanceOf(SectionNode.class);
        then(wrapped.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"library", "project", "skills", "stories"})
    void section_icon_is_rendered_at_16px(String name) {
        Image icon = SectionNode.wrap(nodeNamed(name)).getIcon(0);

        then(icon.getWidth(null)).isEqualTo(16);
        then(icon.getHeight(null)).isEqualTo(16);
        then(hasVisiblePixels(icon)).as("icon for '%s' is blank", name).isTrue();
    }

    @Test
    void opened_icon_is_the_same_as_closed_icon() {
        Node node = SectionNode.wrap(nodeNamed("library"));

        then(node.getOpenedIcon(0)).isSameAs(node.getIcon(0));
    }

    @Test
    void sections_have_distinct_icons() {
        BufferedImage library = (BufferedImage) SectionNode.wrap(nodeNamed("library")).getIcon(0);
        BufferedImage skills = (BufferedImage) SectionNode.wrap(nodeNamed("skills")).getIcon(0);

        boolean differ = false;
        for (int y = 0; y < 16 && !differ; y++) {
            for (int x = 0; x < 16 && !differ; x++) {
                differ = library.getRGB(x, y) != skills.getRGB(x, y);
            }
        }
        then(differ).as("library and skills render the same icon").isTrue();
    }

    @Test
    void other_folders_are_wrapped_but_keep_their_own_icon() {
        Node original = nodeNamed("misc");

        Node wrapped = SectionNode.wrap(original);

        then(wrapped).isNotInstanceOf(SectionNode.class);
        then(wrapped).isInstanceOf(FilterNode.class);
        then(wrapped).isNotSameAs(original);
        then(wrapped.getName()).isEqualTo("misc");
    }
}
