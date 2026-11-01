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

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 * Adds the "Uzzo Knowledge Base" node (the project's top-level "uzz" folder)
 * to the logical view of NetBeans projects.
 *
 * <p>{@link NodeFactory.Registration} has no wildcard: a node factory is only
 * consulted by the project types whose logical view is composed from a
 * {@code Projects/&lt;type&gt;/Nodes} folder. The list below covers the project
 * types shipped with NetBeans that expose that extension point. A project type
 * missing from the list has a closed logical view and cannot be extended this
 * way; add its id here if it starts supporting {@code NodeFactory}s.</p>
 */
@NodeFactory.Registration(
        projectType = {
            "org-netbeans-modules-maven",
            "org-netbeans-modules-java-j2seproject",
            "org-netbeans-modules-java-j2semodule",
            "org-netbeans-modules-ant-freeform",
            "org-netbeans-modules-gradle",
            "org-netbeans-modules-web-project",
            "org-netbeans-modules-web-clientproject",
            "org-netbeans-modules-php-project",
            "org-netbeans-modules-j2ee-clientproject",
            "org-netbeans-modules-j2ee-ejbjarproject",
            "org-netbeans-modules-j2ee-earproject",
            "org-netbeans-modules-apisupport-project",
            "org-netbeans-modules-apisupport-project-suite"
        },
        position = 50)
public final class UzzoKnowledgeBaseNodeFactory implements NodeFactory {

    static final String FOLDER_NAME = "uzz";

    @Override
    public NodeList<?> createNodes(Project project) {
        return new UzzoNodeList(project.getProjectDirectory());
    }

    private static final class UzzoNodeList implements NodeList<String>, FileChangeListener {

        private final FileObject projectDir;
        private final ChangeSupport changes = new ChangeSupport(this);
        private final FileChangeListener weakListener;

        UzzoNodeList(FileObject projectDir) {
            this.projectDir = projectDir;
            this.weakListener = FileUtil.weakFileChangeListener(this, projectDir);
        }

        @Override
        public List<String> keys() {
            FileObject dir = projectDir.getFileObject(FOLDER_NAME);
            return dir != null && dir.isFolder() ? Collections.singletonList(FOLDER_NAME) : Collections.emptyList();
        }

        @Override
        public Node node(String key) {
            FileObject dir = projectDir.getFileObject(key);
            return dir != null && dir.isFolder() ? new UzzoKnowledgeBaseNode(dir) : null;
        }

        @Override
        public void addNotify() {
            projectDir.addFileChangeListener(weakListener);
        }

        @Override
        public void removeNotify() {
            projectDir.removeFileChangeListener(weakListener);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changes.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changes.removeChangeListener(l);
        }

        // The "uzz" folder appearing/disappearing/being renamed in the project dir
        @Override
        public void fileFolderCreated(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            check(fe.getName() + (fe.getExt().isEmpty() ? "" : "." + fe.getExt()));
            check(fe.getFile().getNameExt());
        }

        @Override
        public void fileDataCreated(FileEvent fe) { }

        @Override
        public void fileChanged(FileEvent fe) { }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) { }

        private void check(String name) {
            if (FOLDER_NAME.equals(name)) {
                changes.fireChange();
            }
        }
    }
}
