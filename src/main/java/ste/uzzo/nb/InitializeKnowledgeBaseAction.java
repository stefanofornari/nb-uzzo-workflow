/*
 * Copyright 2026 the original author or authors from the Uzzo and Uzzo Workflow project
 * (https://stefanofornari.github.io/uzzo-workflow).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Project node entry for the Uzzo knowledge base: "Initialize" while the
 * project has no {@code uzz} folder, "Repair" (see
 * {@link RepairKnowledgeBaseAction}) once it has.
 */
@ActionID(category = "Project", id = "ste.uzzo.nb.InitializeKnowledgeBaseAction")
@ActionRegistration(displayName = "#InitializeKnowledgeBaseAction.name", lazy = false)
@ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1500)
@Messages("InitializeKnowledgeBaseAction.name=Initialize Uzzo Knowledge Base")
public final class InitializeKnowledgeBaseAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    static final String ICON = "ste/uzzo/nb/lamp.png";

    private final Project project;
    private final FileObject uzzDir;
    private final KnowledgeBaseContent content;
    private final FactsPrompt prompt;

    public InitializeKnowledgeBaseAction() {
        this(ClasspathKnowledgeBaseContent.of(InitializeKnowledgeBaseAction.class.getClassLoader()), FactsPanel::show);
    }

    InitializeKnowledgeBaseAction(KnowledgeBaseContent content, FactsPrompt prompt) {
        this(null, content, prompt);
    }

    private InitializeKnowledgeBaseAction(Project project, KnowledgeBaseContent content, FactsPrompt prompt) {
        super(knowledgeBase(project) != null
                ? Bundle.RepairKnowledgeBaseAction_name() : Bundle.InitializeKnowledgeBaseAction_name());
        putValue("iconBase", ICON);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON, false));
        this.project = project;
        this.uzzDir = knowledgeBase(project);
        this.content = content;
        this.prompt = prompt;
        // an existing "uzz" file stays enabled so that the run reports the conflict
        setEnabled(project != null);
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
        if (uzzDir != null) {
            new RepairKnowledgeBaseAction(uzzDir, content).actionPerformed(event);
            return;
        }
        RequestProcessor.getDefault().post(() -> {
            NotifyDescriptor outcome = initialize(project);
            if (outcome != null) {
                DialogDisplayer.getDefault().notify(outcome);
            }
        });
    }

    /** Initializes the knowledge base; returns the report to show, or null when the user cancelled. */
    NotifyDescriptor initialize(Project project) {
        try {
            KnowledgeBaseInitializer.checkNotFile(project.getProjectDirectory());
            ProjectFacts facts = prompt.ask(ProjectFacts.fromPom(project.getProjectDirectory()));
            if (facts == null) {
                return null;
            }
            return OutcomeReport.of(KnowledgeBaseInitializer.initialize(project.getProjectDirectory(), content, facts));
        } catch (IOException e) {
            return OutcomeReport.failure(e);
        }
    }

    /** The {@code uzz} folder of the project, or null when it has none. */
    private static FileObject knowledgeBase(Project project) {
        if (project == null) {
            return null;
        }
        FileObject uzz = project.getProjectDirectory().getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        return uzz != null && uzz.isFolder() ? uzz : null;
    }
}
