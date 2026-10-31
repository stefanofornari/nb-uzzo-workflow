package ste.uzzo.nb;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Initializes the Uzzo knowledge base of a Maven project that has none. It is
 * offered on the project node, since the Uzzo Knowledge Base node only exists
 * once the {@code uzz} folder does.
 */
@ActionID(category = "Project", id = "ste.uzzo.nb.InitializeKnowledgeBaseAction")
@ActionRegistration(displayName = "#InitializeKnowledgeBaseAction.name", lazy = false)
@ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1500)
@Messages("InitializeKnowledgeBaseAction.name=Initialize Uzzo Knowledge Base")
public final class InitializeKnowledgeBaseAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    static final String ICON = "ste/uzzo/nb/lamp.png";

    private final Project project;
    private final KnowledgeBaseContent content;
    private final FactsPrompt prompt;

    public InitializeKnowledgeBaseAction() {
        this(ClasspathKnowledgeBaseContent.of(InitializeKnowledgeBaseAction.class.getClassLoader()), FactsPanel::show);
    }

    InitializeKnowledgeBaseAction(KnowledgeBaseContent content, FactsPrompt prompt) {
        this(null, content, prompt);
    }

    private InitializeKnowledgeBaseAction(Project project, KnowledgeBaseContent content, FactsPrompt prompt) {
        super(Bundle.InitializeKnowledgeBaseAction_name());
        putValue("iconBase", ICON);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON, false));
        this.project = project;
        this.content = content;
        this.prompt = prompt;
        // an existing "uzz" file stays enabled so that the run reports the conflict
        setEnabled(project != null && !hasKnowledgeBase(project));
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new InitializeKnowledgeBaseAction(context.lookup(Project.class), content, prompt);
    }

    /** Needed for the icon to show next to the name in the popup menu. */
    @Override
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (project == null) {
            return;
        }
        RequestProcessor.getDefault().post(() -> {
            NotifyDescriptor outcome = initialize(project);
            if (outcome != null) {
                DialogDisplayer.getDefault().notify(outcome);
            }
        });
    }

    /** Initializes the knowledge base; returns a message to show, or null when everything went fine or the user cancelled. */
    @Messages("InitializeKnowledgeBaseAction.incomplete=The knowledge base was created but is incomplete:\n{0}")
    NotifyDescriptor initialize(Project project) {
        try {
            KnowledgeBaseInitializer.checkAbsent(project.getProjectDirectory());
            ProjectFacts facts = prompt.ask(ProjectFacts.fromPom(project.getProjectDirectory()));
            if (facts == null) {
                return null;
            }
            KnowledgeBaseInitializer.Result result =
                    KnowledgeBaseInitializer.initialize(project.getProjectDirectory(), content, facts);
            if (result.incomplete().isEmpty()) {
                return null;
            }
            return new NotifyDescriptor.Message(
                    Bundle.InitializeKnowledgeBaseAction_incomplete(String.join("\n", result.incomplete())),
                    NotifyDescriptor.WARNING_MESSAGE);
        } catch (IOException e) {
            return new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        }
    }

    private static boolean hasKnowledgeBase(Project project) {
        FileObject uzz = project.getProjectDirectory().getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        return uzz != null && uzz.isFolder();
    }
}
