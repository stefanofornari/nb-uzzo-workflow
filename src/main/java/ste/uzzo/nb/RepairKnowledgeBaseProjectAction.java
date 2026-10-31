package ste.uzzo.nb;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 * Offers the repair on the project node as well, for projects that already
 * have a {@code uzz} folder; the work is done by {@link RepairKnowledgeBaseAction}.
 */
@ActionID(category = "Project", id = "ste.uzzo.nb.RepairKnowledgeBaseProjectAction")
@ActionRegistration(displayName = "#RepairKnowledgeBaseProjectAction.name", lazy = false)
@ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1510)
@Messages("RepairKnowledgeBaseProjectAction.name=Repair Uzzo Knowledge Base")
public final class RepairKnowledgeBaseProjectAction extends AbstractAction
        implements ContextAwareAction, Presenter.Popup {

    private final FileObject uzzDir;

    public RepairKnowledgeBaseProjectAction() {
        this(null);
    }

    private RepairKnowledgeBaseProjectAction(Project project) {
        super(Bundle.RepairKnowledgeBaseProjectAction_name());
        putValue("iconBase", InitializeKnowledgeBaseAction.ICON);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(InitializeKnowledgeBaseAction.ICON, false));
        FileObject uzz = project == null ? null
                : project.getProjectDirectory().getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        uzzDir = uzz != null && uzz.isFolder() ? uzz : null;
        setEnabled(uzzDir != null);
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new RepairKnowledgeBaseProjectAction(context.lookup(Project.class));
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (uzzDir != null) {
            new RepairKnowledgeBaseAction(uzzDir).actionPerformed(event);
        }
    }
}
