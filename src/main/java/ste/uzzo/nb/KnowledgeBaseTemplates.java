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
