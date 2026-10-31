package ste.uzzo.nb;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Initializes the Uzzo knowledge base of a Maven project that has none. It is
 * offered on the project node, since the Uzzo Knowledge Base node only exists
 * once the {@code uzz} folder does.
 */
@ActionID(category = "Project", id = "ste.uzzo.nb.InitializeKnowledgeBaseAction")
@ActionRegistration(displayName = "#InitializeKnowledgeBaseAction.name", lazy = false)
@ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1500)
@Messages("InitializeKnowledgeBaseAction.name=Initialize Uzzo Knowledge Base")
public final class InitializeKnowledgeBaseAction extends AbstractAction implements ContextAwareAction {

    private final Project project;

    public InitializeKnowledgeBaseAction() {
        this(null);
    }

    private InitializeKnowledgeBaseAction(Project project) {
        super(Bundle.InitializeKnowledgeBaseAction_name());
        this.project = project;
        // an existing "uzz" file stays enabled so that the run reports the conflict
        setEnabled(project != null && !hasKnowledgeBase(project));
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new InitializeKnowledgeBaseAction(context.lookup(Project.class));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (project == null) {
            return;
        }
        RequestProcessor.getDefault().post(() -> {
            String error = initialize(project);
            if (error != null) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(error, NotifyDescriptor.ERROR_MESSAGE));
            }
        });
    }

    /** Initializes the knowledge base; returns an error message, or null on success. */
    String initialize(Project project) {
        try {
            KnowledgeBaseInitializer.initialize(project.getProjectDirectory());
            return null;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private static boolean hasKnowledgeBase(Project project) {
        FileObject uzz = project.getProjectDirectory().getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        return uzz != null && uzz.isFolder();
    }
}
