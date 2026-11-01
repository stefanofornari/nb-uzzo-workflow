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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Redirects editors opened for files under a project's "uzz" folder into the
 * dedicated "uzzoKnowledgeBase" deck (mode) on the right of the IDE, instead of
 * the central editor deck. The mode itself is declared in layer.xml.
 */
@OnShowing
public final class UzzoDeckHook implements Runnable, PropertyChangeListener {

    static final String MODE_NAME = "uzzoKnowledgeBase";
    private static final Logger LOG = Logger.getLogger(UzzoDeckHook.class.getName());

    /** Some editors (e.g. Markdown plugins) re-dock themselves after opening: check again later. */
    private static final int RECHECK_DELAY_MS = 400;

    @Override
    public void run() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
        LOG.log(Level.INFO, "Uzzo deck hook registered; deck mode found: {0}",
                WindowManager.getDefault().findMode(MODE_NAME) != null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())
                && evt.getNewValue() instanceof TopComponent tc) {
            // Let the window system finish opening before moving the component.
            EventQueue.invokeLater(() -> redirect(tc));
            Timer recheck = new Timer(RECHECK_DELAY_MS, e -> redirect(tc));
            recheck.setRepeats(false);
            recheck.start();
        }
    }

    private static void redirect(TopComponent tc) {
        WindowManager wm = WindowManager.getDefault();
        if (!tc.isOpened() || !wm.isEditorTopComponent(tc)) {
            return;
        }
        Mode deck = wm.findMode(MODE_NAME);
        if (deck == null || wm.findMode(tc) == deck) {
            return;
        }
        FileObject fo = fileOf(tc);
        if (fo == null) {
            LOG.log(Level.INFO, "Cannot resolve the file of editor {0} ({1})",
                    new Object[]{tc.getClass().getName(), tc.getDisplayName()});
            return;
        }
        if (isInKnowledgeBase(fo)) {
            LOG.log(Level.FINE, "Docking {0} into the Uzzo deck", fo);
            deck.dockInto(tc);
            tc.open();
            tc.requestActive();
        }
    }

    static FileObject fileOf(TopComponent tc) {
        FileObject fo = fileOf(tc.getLookup().lookup(DataObject.class));
        if (fo == null) {
            fo = tc.getLookup().lookup(FileObject.class);
        }
        if (fo == null) {
            Node[] nodes = tc.getActivatedNodes();
            if (nodes != null) {
                for (Node n : nodes) {
                    fo = fileOf(n.getLookup().lookup(DataObject.class));
                    if (fo == null) {
                        fo = n.getLookup().lookup(FileObject.class);
                    }
                    if (fo != null) {
                        break;
                    }
                }
            }
        }
        return fo;
    }

    private static FileObject fileOf(DataObject dobj) {
        return dobj != null ? dobj.getPrimaryFile() : null;
    }

    static boolean isInKnowledgeBase(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            return false;
        }
        FileObject uzz = p.getProjectDirectory().getFileObject(UzzoKnowledgeBaseNodeFactory.FOLDER_NAME);
        return uzz != null && FileUtil.isParentOf(uzz, fo);
    }
}
