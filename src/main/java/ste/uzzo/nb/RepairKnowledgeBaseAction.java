package ste.uzzo.nb;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Completes an existing knowledge base, offered on the Uzzo Knowledge Base
 * node. Additive: existing content is never changed. The facts come from the
 * pom as they are, without asking.
 */
@Messages("RepairKnowledgeBaseAction.name=Repair Uzzo Knowledge Base")
final class RepairKnowledgeBaseAction extends AbstractAction implements Presenter.Popup {

    private final FileObject uzzDir;
    private final KnowledgeBaseContent content;

    RepairKnowledgeBaseAction(FileObject uzzDir) {
        this(uzzDir, ClasspathKnowledgeBaseContent.of(RepairKnowledgeBaseAction.class.getClassLoader()));
    }

    RepairKnowledgeBaseAction(FileObject uzzDir, KnowledgeBaseContent content) {
        super(Bundle.RepairKnowledgeBaseAction_name());
        putValue("iconBase", InitializeKnowledgeBaseAction.ICON);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(InitializeKnowledgeBaseAction.ICON, false));
        this.uzzDir = uzzDir;
        this.content = content;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        RequestProcessor.getDefault().post(() -> DialogDisplayer.getDefault().notify(repair()));
    }

    /** Repairs the knowledge base; returns the report to show. */
    NotifyDescriptor repair() {
        FileObject projectDir = uzzDir.getParent();
        try {
            return OutcomeReport.of(KnowledgeBaseInitializer.initialize(
                    projectDir, content, ProjectFacts.fromPom(projectDir)));
        } catch (IOException e) {
            return OutcomeReport.failure(e);
        }
    }
}
