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
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Knowledge base content provided by the {@code uzzo-workflow-kb} dependency
 * under the {@code uzz/} classpath prefix. A classpath folder inside a jar
 * cannot be listed, so the content is located through a known resource and
 * listed from the jar (or the folder) it resolves to.
 */
final class ClasspathKnowledgeBaseContent implements KnowledgeBaseContent {

    private static final String PREFIX = "uzz/";
    private static final String MARKER = PREFIX + "skills/kb/SKILL.md";

    private final ClassLoader loader;

    private ClasspathKnowledgeBaseContent(ClassLoader loader) {
        this.loader = loader;
    }

    static KnowledgeBaseContent of(ClassLoader loader) {
        return new ClasspathKnowledgeBaseContent(loader);
    }

    @Override
    public List<String> paths() throws IOException {
        URL marker = loader.getResource(MARKER);
        if (marker == null) {
            throw new IOException("Uzzo knowledge base content not found on the classpath (" + MARKER + ")");
        }
        switch (marker.getProtocol()) {
            case "jar":
                return jarPaths(marker);
            case "file":
                return folderPaths(marker);
            default:
                throw new IOException("Unsupported location of the Uzzo knowledge base content: " + marker);
        }
    }

    @Override
    public byte[] read(String path) throws IOException {
        try (InputStream in = loader.getResourceAsStream(PREFIX + path)) {
            if (in == null) {
                throw new IOException("Uzzo knowledge base file not found: " + path);
            }
            return in.readAllBytes();
        }
    }

    private static List<String> jarPaths(URL marker) throws IOException {
        URLConnection connection = marker.openConnection();
        if (!(connection instanceof JarURLConnection)) {
            throw new IOException("Cannot open the Uzzo knowledge base content: " + marker);
        }
        connection.setUseCaches(false);
        List<String> paths = new java.util.ArrayList<>();
        try (JarFile jar = ((JarURLConnection) connection).getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            for (JarEntry entry : Collections.list(entries)) {
                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(PREFIX)) {
                    paths.add(name.substring(PREFIX.length()));
                }
            }
        }
        Collections.sort(paths);
        return paths;
    }

    private static List<String> folderPaths(URL marker) throws IOException {
        try {
            // <root>/uzz/skills/kb/SKILL.md -> <root>/uzz
            Path root = Paths.get(marker.toURI()).getParent().getParent().getParent();
            try (Stream<Path> files = Files.walk(root)) {
                return files.filter(Files::isRegularFile)
                        .map(p -> root.relativize(p).toString().replace('\\', '/'))
                        .sorted()
                        .toList();
            }
        } catch (URISyntaxException e) {
            throw new IOException("Cannot open the Uzzo knowledge base content: " + marker, e);
        }
    }
}
