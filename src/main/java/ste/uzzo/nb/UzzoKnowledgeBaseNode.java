package ste.uzzo.nb;

import java.awt.Image;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 * Folder node for the project's top-level "uzz" directory. Wraps the standard
 * folder node, so children, actions and file-system updates behave exactly as
 * for any other folder; only label and icon are customized.
 */
final class UzzoKnowledgeBaseNode extends FilterNode {

    private static final String ICON = "ste/uzzo/nb/lamp.png";

    @Messages("UzzoKnowledgeBaseNode.displayName=Uzzo Knowledge Base")
    UzzoKnowledgeBaseNode(FileObject uzzDir) {
        this(DataFolder.findFolder(uzzDir).getNodeDelegate());
    }

    private UzzoKnowledgeBaseNode(Node delegate) {
        super(delegate, new FilterNode.Children(delegate) {
            @Override
            protected Node copyNode(Node original) {
                return SectionNode.wrap(original);
            }
        });
        disableDelegation(DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_DISPLAY_NAME);
        super.setDisplayName(Bundle.UzzoKnowledgeBaseNode_displayName());
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public String getHtmlDisplayName() {
        return null;
    }
}
