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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

class KnowledgeBaseTemplatesTest {

    private static final String SAMPLE = String.join("\n",
            "# Templates",
            "",
            "## 1. `project/overview.md`",
            "",
            "```markdown",
            "# Project Overview",
            "",
            "### Keywords",
            "- project",
            "```",
            "",
            "---",
            "",
            "## 2. `stories/index.md`",
            "",
            "```markdown",
            "# Stories Index",
            "```",
            "");

    @Test
    void extracts_the_fenced_block_of_a_template() {
        then(KnowledgeBaseTemplates.extract(SAMPLE, "project/overview.md"))
                .isEqualTo("# Project Overview\n\n### Keywords\n- project\n");
        then(KnowledgeBaseTemplates.extract(SAMPLE, "stories/index.md"))
                .isEqualTo("# Stories Index\n");
    }

    @Test
    void returns_null_for_an_unknown_template() {
        then(KnowledgeBaseTemplates.extract(SAMPLE, "project/nope.md")).isNull();
    }

    @Test
    void extracts_every_article_from_the_real_templates() throws IOException {
        String templates = new String(
                ClasspathKnowledgeBaseContent.of(getClass().getClassLoader())
                        .read("skills/uzzo-init/references/templates.md"),
                java.nio.charset.StandardCharsets.UTF_8);

        for (String path : KnowledgeBaseTemplates.ARTICLES) {
            then(KnowledgeBaseTemplates.extract(templates, path)).as(path)
                    .isNotNull().contains("### Keywords").doesNotContain("```");
        }
    }
}
