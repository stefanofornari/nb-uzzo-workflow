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
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Facts about a project that can be read from its {@code pom.xml}. Absent
 * (null) when the pom does not state them; nothing is guessed.
 */
record ProjectFacts(String name, String version, String summary, String repository, String javaRelease) {

    static final ProjectFacts NONE = new ProjectFacts(null, null, null, null, null);

    private static final String SUMMARY_PLACEHOLDER =
            "[One paragraph describing what the project is, who it serves, and the problem it solves.]";

    ProjectFacts {
        name = clean(name);
        version = clean(version);
        summary = clean(summary);
        repository = clean(repository);
        javaRelease = clean(javaRelease);
    }

    /** Reads the facts of the pom in {@code projectDir}; no pom or an unreadable one gives {@link #NONE}. */
    static ProjectFacts fromPom(FileObject projectDir) {
        FileObject pom = projectDir.getFileObject("pom.xml");
        if (pom == null || !pom.isData()) {
            return NONE;
        }
        try (InputStream in = pom.getInputStream()) {
            return parse(in);
        } catch (Exception e) {
            return NONE;
        }
    }

    /** Replaces the placeholders this project has a fact for; the others are left as they are. */
    String fill(String text) {
        String result = text;
        result = replace(result, "[PROJECT NAME]", name);
        result = replace(result, "[VERSION]", version);
        result = replace(result, SUMMARY_PLACEHOLDER, summary);
        if (repository != null && name != null) {
            result = result.replace("[REPOSITORY NAME]([URL])", "[" + name + "](" + repository + ")");
        }
        result = replace(result, "[LANGUAGE AND VERSION]", javaRelease == null ? null : "Java " + javaRelease);
        result = replace(result, "[BUILD TOOL]", "Maven");
        result = replace(result, "[BUILD COMMAND]", "mvn clean install");
        result = replace(result, "[TEST COMMAND]", "mvn test");
        return result;
    }

    private static String replace(String text, String placeholder, String value) {
        return value == null ? text : text.replace(placeholder, value);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static ProjectFacts parse(InputStream in) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document doc = factory.newDocumentBuilder().parse(in);
        Element project = doc.getDocumentElement();

        String name = resolved(text(project, "name"));
        if (name == null) {
            name = resolved(text(project, "artifactId"));
        }
        String version = resolved(text(project, "version"));
        if (version == null) {
            version = resolved(text(child(project, "parent"), "version"));
        }
        return new ProjectFacts(
                name,
                version,
                resolved(text(project, "description")),
                resolved(text(child(project, "scm"), "url")),
                release(project));
    }

    private static String release(Element project) {
        Element properties = child(project, "properties");
        for (String key : List.of("maven.compiler.release", "maven.compiler.source")) {
            String value = resolved(text(properties, key));
            if (value != null) {
                return normalizeRelease(value);
            }
        }
        for (Element plugin : plugins(project)) {
            if ("maven-compiler-plugin".equals(text(plugin, "artifactId"))) {
                Element configuration = child(plugin, "configuration");
                for (String key : List.of("release", "source")) {
                    String value = resolved(text(configuration, key));
                    if (value != null) {
                        return normalizeRelease(value);
                    }
                }
            }
        }
        return null;
    }

    private static List<Element> plugins(Element project) {
        List<Element> plugins = new ArrayList<>();
        Element plugin = child(child(child(project, "build"), "plugins"), "plugin");
        for (Node n = plugin; n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && e.getTagName().equals("plugin")) {
                plugins.add(e);
            }
        }
        return plugins;
    }

    private static String normalizeRelease(String value) {
        return value.startsWith("1.") ? value.substring(2) : value;
    }

    private static String resolved(String value) {
        return value == null || value.contains("${") ? null : value;
    }

    private static Element child(Element parent, String tag) {
        if (parent == null) {
            return null;
        }
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && e.getTagName().equals(tag)) {
                return e;
            }
        }
        return null;
    }

    private static String text(Element parent, String tag) {
        Element e = child(parent, tag);
        return e == null ? null : e.getTextContent();
    }
}
