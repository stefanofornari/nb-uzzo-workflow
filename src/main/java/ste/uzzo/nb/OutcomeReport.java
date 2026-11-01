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
