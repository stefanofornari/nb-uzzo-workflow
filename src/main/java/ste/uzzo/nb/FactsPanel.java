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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;

/** Editable view of the {@link ProjectFacts} proposed from the pom. */
@Messages({
    "FactsPanel.title=Initialize Uzzo Knowledge Base",
    "FactsPanel.intro=Values read from pom.xml. Edit or clear them; empty values stay placeholders.",
    "FactsPanel.name=Name:",
    "FactsPanel.version=Version:",
    "FactsPanel.summary=Summary:",
    "FactsPanel.repository=Repository URL:",
    "FactsPanel.javaRelease=Java release:"
})
final class FactsPanel extends JPanel {

    private final JTextField name = new JTextField(30);
    private final JTextField version = new JTextField(30);
    private final JTextField summary = new JTextField(30);
    private final JTextField repository = new JTextField(30);
    private final JTextField javaRelease = new JTextField(30);

    FactsPanel(ProjectFacts proposed) {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        add(new JLabel(Bundle.FactsPanel_intro()), c);
        c.gridwidth = 1;
        row(c, 1, Bundle.FactsPanel_name(), name, proposed.name());
        row(c, 2, Bundle.FactsPanel_version(), version, proposed.version());
        row(c, 3, Bundle.FactsPanel_summary(), summary, proposed.summary());
        row(c, 4, Bundle.FactsPanel_repository(), repository, proposed.repository());
        row(c, 5, Bundle.FactsPanel_javaRelease(), javaRelease, proposed.javaRelease());
    }

    private void row(GridBagConstraints c, int y, String label, JTextField field, String value) {
        field.setText(value == null ? "" : value);
        c.gridy = y;
        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(field, c);
    }

    /** The facts as currently edited; empty fields are absent. */
    ProjectFacts facts() {
        return new ProjectFacts(name.getText(), version.getText(), summary.getText(),
                repository.getText(), javaRelease.getText());
    }

    /** Shows the panel in a modal dialog; returns the edited facts, or null when cancelled. */
    static ProjectFacts show(ProjectFacts proposed) {
        FactsPanel panel = new FactsPanel(proposed);
        DialogDescriptor descriptor = new DialogDescriptor(panel, Bundle.FactsPanel_title());
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        return result == NotifyDescriptor.OK_OPTION ? panel.facts() : null;
    }
}
