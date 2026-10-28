package ste.uzzo.nb;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Refreshes the whole knowledge base tree, including folders nested below the
 * project's {@code uzz} directory. Needed because NetBeans does not notice
 * files created by external processes until the containing folder is
 * refreshed.
 */
@Messages("RefreshKnowledgeBaseAction.name=Refresh Knowledge Base")
final class RefreshKnowledgeBaseAction extends AbstractAction {

    private final FileObject root;

    RefreshKnowledgeBaseAction(FileObject root) {
        super(Bundle.RefreshKnowledgeBaseAction_name());
        this.root = root;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        RequestProcessor.getDefault().post(() -> refreshRecursively(root));
    }

    /** Refreshes the folder and every folder nested below it. */
    static void refreshRecursively(FileObject folder) {
        folder.refresh();
        for (FileObject child : folder.getChildren()) {
            if (child.isFolder()) {
                refreshRecursively(child);
            }
        }
    }
}
