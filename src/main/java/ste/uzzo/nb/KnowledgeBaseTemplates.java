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

import java.util.ArrayList;
import java.util.List;

/** Extracts the seed article templates of the {@code uzzo-init} skill. */
final class KnowledgeBaseTemplates {

    /** Where the templates live inside the knowledge base content. */
    static final String PATH = "skills/uzzo-init/references/templates.md";

    /** Articles seeded from the templates. */
    static final List<String> ARTICLES = List.of(
            "project/overview.md",
            "project/coding_standard.md",
            "project/development_framework.md",
            "stories/scope.md",
            "stories/users.md",
            "stories/index.md");

    private KnowledgeBaseTemplates() {
    }

    /**
     * Returns the body of the fenced block that follows the heading of
     * {@code path} (for example {@code ## 1. `project/overview.md`}), or null
     * when the template does not exist.
     */
    static String extract(String templates, String path) {
        String heading = "`" + path + "`";
        List<String> body = new ArrayList<>();
        boolean inHeading = false;
        boolean inBlock = false;
        for (String line : templates.split("\\R", -1)) {
            if (inBlock) {
                if (line.equals("```")) {
                    return String.join("\n", body) + "\n";
                }
                body.add(line);
            } else if (inHeading) {
                if (line.startsWith("```")) {
                    inBlock = true;
                } else if (line.startsWith("## ")) {
                    inHeading = false;
                }
            }
            if (!inBlock && line.startsWith("## ") && line.endsWith(heading)) {
                inHeading = true;
            }
        }
        return null;
    }
}
