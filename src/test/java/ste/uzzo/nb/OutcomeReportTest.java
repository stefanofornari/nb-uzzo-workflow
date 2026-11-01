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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openide.NotifyDescriptor;
import ste.uzzo.nb.KnowledgeBaseInitializer.Kind;
import ste.uzzo.nb.KnowledgeBaseInitializer.Result;

import static org.assertj.core.api.BDDAssertions.then;

class OutcomeReportTest {

    private static Result result(Kind kind, List<String> created, List<String> skipped,
            List<String> placeholders, List<String> incomplete) {
        return new Result(kind, null, created, skipped, placeholders, incomplete);
    }

    @Test
    void lists_created_skipped_placeholders_and_next_steps() {
        NotifyDescriptor report = OutcomeReport.of(result(Kind.REPAIR, List.of("project/overview.md"),
                List.of("stories/scope.md"), List.of("project/overview.md: [NAME]"), List.of()));
        String text = report.getMessage().toString();

        then(report.getMessageType()).isEqualTo(NotifyDescriptor.INFORMATION_MESSAGE);
        then(text).contains("repaired").contains("Created").contains("project/overview.md")
                .contains("Skipped").contains("stories/scope.md")
                .contains("Placeholders").contains("[NAME]")
                .contains("Next steps").contains("scope").contains("story").contains("code");
    }

    @Test
    void empty_sections_are_omitted() {
        String text = OutcomeReport.of(result(Kind.FRESH, List.of("a.md"), List.of(), List.of(), List.of()))
                .getMessage().toString();

        then(text).contains("created").doesNotContain("Skipped").doesNotContain("Placeholders")
                .doesNotContain("Incomplete");
    }

    @Test
    void a_complete_knowledge_base_says_so() {
        String text = OutcomeReport.of(result(Kind.COMPLETE, List.of(), List.of("a.md"), List.of(), List.of()))
                .getMessage().toString();

        then(text).contains("already complete").contains("nothing was written");
    }

    @Test
    void incomplete_content_is_a_warning() {
        NotifyDescriptor report = OutcomeReport.of(result(Kind.FRESH, List.of("a.md"), List.of(), List.of(),
                List.of("skills: no content")));

        then(report.getMessageType()).isEqualTo(NotifyDescriptor.WARNING_MESSAGE);
        then(report.getMessage().toString()).contains("incomplete").contains("skills: no content");
    }

    @Test
    void a_failure_is_an_error_with_the_message() {
        NotifyDescriptor report = OutcomeReport.failure(new java.io.IOException("boom /x"));

        then(report.getMessageType()).isEqualTo(NotifyDescriptor.ERROR_MESSAGE);
        then(report.getMessage().toString()).isEqualTo("boom /x");
    }
}
