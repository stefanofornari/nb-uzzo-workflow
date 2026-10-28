package ste.uzzo.nb;

import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Children that refresh the underlying folder before listing it. NetBeans does
 * not notice files created by external processes until the folder is
 * refreshed, so refreshing here makes those files appear when the folder is
 * expanded.
 */
class RefreshingChildren extends FilterNode.Children {

    private boolean refreshing;

    RefreshingChildren(Node original) {
        super(original);
    }

    @Override
    public Node[] getNodes(boolean optimalResult) {
        refreshFolder();
        return super.getNodes(optimalResult);
    }

    @Override
    public int getNodesCount(boolean optimalResult) {
        refreshFolder();
        return super.getNodesCount(optimalResult);
    }

    @Override
    protected Node copyNode(Node original) {
        if (original.getCookie(DataFolder.class) == null) {
            return new FilterNode(original);
        }
        return new FilterNode(original, new RefreshingChildren(original));
    }

    private void refreshFolder() {
        if (refreshing) {
            return;
        }
        DataFolder folder = original.getCookie(DataFolder.class);
        if (folder == null) {
            return;
        }
        refreshing = true;
        try {
            folder.getPrimaryFile().refresh();
        } finally {
            refreshing = false;
        }
    }
}
