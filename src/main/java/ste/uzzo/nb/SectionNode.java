package ste.uzzo.nb;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignB;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;
import org.kordamp.ikonli.swing.FontIcon;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * A top-level folder of the knowledge base (library, project, skills,
 * stories) shown with a Material Design 2 icon. Behaves like a normal folder node.
 */
final class SectionNode extends FilterNode {

    private static final int SIZE = 16;
    private static final Color COLOR = new Color(0x5B7DB1);

    private static final Map<String, Ikon> ICONS = Map.of(
            "library", MaterialDesignB.BOOKSHELF,
            "project", MaterialDesignT.TOOLBOX_OUTLINE,
            "skills", MaterialDesignB.BRAIN,
            "stories", MaterialDesignB.BOOK_ACCOUNT_OUTLINE);

    private final Image icon;

    private SectionNode(Node original, Ikon ikon) {
        super(original);
        this.icon = render(ikon);
    }

    /** Returns a SectionNode for a known section folder, a plain copy otherwise. */
    static Node wrap(Node original) {
        Ikon ikon = ICONS.get(original.getName());
        return ikon == null ? new FilterNode(original) : new SectionNode(original, ikon);
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return icon;
    }

    private static Image render(Ikon ikon) {
        // Ikonli resolves its font handlers through ServiceLoader, which needs
        // the module classloader as context classloader inside NetBeans.
        Thread t = Thread.currentThread();
        ClassLoader previous = t.getContextClassLoader();
        t.setContextClassLoader(SectionNode.class.getClassLoader());
        try {
            FontIcon fi = FontIcon.of(ikon, SIZE, COLOR);
            BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();
            try {
                fi.paintIcon(null, g, 0, 0);
            } finally {
                g.dispose();
            }
            return img;
        } finally {
            t.setContextClassLoader(previous);
        }
    }
}
