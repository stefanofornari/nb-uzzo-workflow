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
import java.util.List;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;

/** Describes the outcome of an initialization or repair for the user. */
final class OutcomeReport {

    private OutcomeReport() {
    }

    @Messages({
        "OutcomeReport.fresh=The knowledge base was created.",
        "OutcomeReport.repair=The knowledge base was repaired.",
        "OutcomeReport.complete=The knowledge base is already complete; nothing was written.",
        "OutcomeReport.incomplete=The knowledge base is incomplete: some content could not be seeded.",
        "OutcomeReport.created=Created:",
        "OutcomeReport.skipped=Skipped (already present):",
        "OutcomeReport.placeholders=Placeholders to fill:",
        "OutcomeReport.problems=Incomplete:",
        "OutcomeReport.next=Next steps: fill the placeholders, then run the scope, story and code skills."
    })
    static NotifyDescriptor of(KnowledgeBaseInitializer.Result result) {
        StringBuilder text = new StringBuilder();
        boolean incomplete = !result.incomplete().isEmpty();
        text.append(incomplete ? Bundle.OutcomeReport_incomplete() : switch (result.kind()) {
            case FRESH -> Bundle.OutcomeReport_fresh();
            case REPAIR -> Bundle.OutcomeReport_repair();
            case COMPLETE -> Bundle.OutcomeReport_complete();
        });
        section(text, Bundle.OutcomeReport_created(), result.created());
        section(text, Bundle.OutcomeReport_skipped(), result.skipped());
        section(text, Bundle.OutcomeReport_placeholders(), result.placeholders());
        section(text, Bundle.OutcomeReport_problems(), result.incomplete());
        if (result.kind() != KnowledgeBaseInitializer.Kind.COMPLETE) {
            text.append("\n\n").append(Bundle.OutcomeReport_next());
        }
        return new NotifyDescriptor.Message(text.toString(),
                incomplete ? NotifyDescriptor.WARNING_MESSAGE : NotifyDescriptor.INFORMATION_MESSAGE);
    }

    static NotifyDescriptor failure(IOException e) {
        return new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
    }

    private static void section(StringBuilder text, String title, List<String> items) {
        if (!items.isEmpty()) {
            text.append("\n\n").append(title);
            items.forEach(item -> text.append("\n  ").append(item));
        }
    }
}
