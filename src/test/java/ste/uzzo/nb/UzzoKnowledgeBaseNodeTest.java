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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import static org.assertj.core.api.BDDAssertions.then;

class UzzoKnowledgeBaseNodeTest {

    private UzzoKnowledgeBaseNode node;
    private FileObject uzzDir;

    @BeforeEach
    void setUp() throws IOException {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject uzz = root.createFolder("uzz");
        uzzDir = uzz;
        for (String name : new String[]{"library", "project", "skills", "stories", "misc"}) {
            uzz.createFolder(name);
        }
        uzz.createData("readme.txt");
        node = new UzzoKnowledgeBaseNode(uzz);
    }

    @Test
    void display_name_is_the_knowledge_base_label() {
        then(node.getDisplayName()).isEqualTo("Uzzo Knowledge Base");
    }

    @Test
    void has_the_lamp_icon_for_both_states() {
        then(node.getIcon(0)).isNotNull();
        then(node.getOpenedIcon(0)).isNotNull();
    }

    @Test
    void cannot_be_renamed_and_has_no_html_name() {
        then(node.canRename()).isFalse();
        then(node.getHtmlDisplayName()).isNull();
    }

    @Test
    void children_are_the_content_of_the_folder() {
        Map<String, Node> children = children();

        // 5 sub-folders + readme.txt
        then(node.getChildren().getNodesCount(true)).isEqualTo(6);
        then(children.get("misc")).isNotNull();
    }

    @Test
    void section_folders_get_section_nodes() {
        Map<String, Node> children = children();

        for (String section : new String[]{"library", "project", "skills", "stories"}) {
            then(children.get(section)).as(section).isInstanceOf(SectionNode.class);
        }
    }

    @Test
    void other_children_are_not_section_nodes() {
        Map<String, Node> children = children();

        then(children.get("misc")).isNotInstanceOf(SectionNode.class);
    }

    @Test
    void nested_folders_with_section_names_are_not_decorated() throws IOException {
        uzzDir.getFileObject("stories").createFolder("project");

        Node nested = children().get("stories").getChildren().findChild("project");

        then(nested).isNotNull();
        then(nested).isNotInstanceOf(SectionNode.class);
    }

    @Test
    void files_remain_leaf_nodes() {
        Node readme = children().get("readme.txt");

        then(readme).isNotNull();
        then(readme.getChildren()).isSameAs(Children.LEAF);
    }

    @Test
    void ignored_entries_are_hidden_at_the_root() throws IOException {
        uzzDir.createData(".gitkeep");
        uzzDir.createData("Thumbs.db");
        uzzDir.createData("draft.md.bak");
        uzzDir.createFolder(".git");

        then(node.getChildren().getNodesCount(true)).isEqualTo(6);

        uzzDir.createData("visible.notes.md");

        then(node.getChildren().getNodesCount(true)).isEqualTo(7);
    }

    @Test
    void ignored_entries_are_hidden_in_nested_folders() throws IOException {
        FileObject stories = uzzDir.getFileObject("stories");
        stories.createData(".gitkeep");
        stories.createData("desktop.ini");
        stories.createData("US-000001.md");
        stories.createFolder("cache.tmp");

        Node storiesNode = children().get("stories");

        then(storiesNode.getChildren().getNodesCount(true)).isEqualTo(1);
        then(storiesNode.getChildren().getNodes(true)[0].getName()).isEqualTo("US-000001.md");
    }

    private Map<String, Node> children() {
        Map<String, Node> byName = new HashMap<>();
        for (Node child : node.getChildren().getNodes(true)) {
            byName.put(child.getName(), child);
        }
        return byName;
    }
}
