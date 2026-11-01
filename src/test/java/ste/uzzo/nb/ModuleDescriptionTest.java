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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Guards the module metadata shown in the Plugins dialog and on the plugin
 * portal. The long description is HTML, while the short description must stay
 * plain text because it is used in the plugin list.
 */
class ModuleDescriptionTest {

    @Test
    void manifest_sets_a_plain_text_short_description() throws Exception {
        String manifest = Files.readString(Path.of("src/main/nbm/manifest.mf"));

        then(manifest).contains(
                "OpenIDE-Module-Short-Description: "
                + "Brings the Uzzo Workflow and Knowledge Base into NetBeans projects.");
    }

    @Test
    void pom_long_description_is_html() throws Exception {
        Document pom = parsePom();
        String description = pom.getElementsByTagName("description").item(0).getTextContent().trim();

        then(description).startsWith("<html>");
        then(description).contains("<ol>", "<li>", "</html>");
    }

    private static Document parsePom() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try (InputStream in = Files.newInputStream(Path.of("pom.xml"))) {
            return factory.newDocumentBuilder().parse(in);
        }
    }
}
