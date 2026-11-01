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

import java.awt.Image;
import java.util.Arrays;
import javax.swing.Action;
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

    private final Action refreshAction;

    @Messages("UzzoKnowledgeBaseNode.displayName=Uzzo Knowledge Base")
    UzzoKnowledgeBaseNode(FileObject uzzDir) {
        this(uzzDir, DataFolder.findFolder(uzzDir).getNodeDelegate());
    }

    private UzzoKnowledgeBaseNode(FileObject uzzDir, Node delegate) {
        super(delegate, new RefreshingChildren(delegate) {
            @Override
            protected Node copyNode(Node original) {
                if (original.getCookie(DataFolder.class) == null) {
                    return new FilterNode(original);
                }
                return SectionNode.wrap(original);
            }
        });
        this.refreshAction = new RefreshKnowledgeBaseAction(uzzDir);
        disableDelegation(DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_DISPLAY_NAME);
        super.setDisplayName(Bundle.UzzoKnowledgeBaseNode_displayName());
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] actions = super.getActions(context);
        Action[] result = Arrays.copyOf(actions, actions.length + 1);
        result[actions.length] = refreshAction;
        return result;
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
