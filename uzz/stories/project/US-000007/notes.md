# project/US-000007 — Implementation Notes

Technical notes for [Pre-fill Project Facts from the Maven Project](US-000007.md).

1. `ProjectFacts` (name, version, summary, repository, Java release) reads `pom.xml` with the JDK DOM parser (doctype disabled), so no Maven or NetBeans Maven module dependency is needed. A missing or unreadable pom gives `ProjectFacts.NONE`.
2. Only what the pom states is proposed: `name` (else `artifactId`), `version` (else the parent's), `description`, `scm/url`, and the Java release from `maven.compiler.release`/`source` or the `maven-compiler-plugin` configuration (`1.8` becomes `8`). Values still containing `${...}` are treated as absent; the effective model is not resolved.
3. `ProjectFacts.fill` replaces only the placeholders it has a value for, and also sets the Maven toolchain (`Maven`, `mvn clean install`, `mvn test`) since the action is offered on Maven projects. `[RUN COMMAND]` and every other placeholder stay bracketed.
4. Facts are applied to the six generated articles only; the skills copied from the dependency are never modified.
5. `FactsPanel` is the editable dialog; `FactsPrompt` is the seam that lets tests answer without a UI. Empty fields are absent facts.
6. The action checks that `uzz` is absent before asking anything, so a conflict is reported without showing the dialog. Cancelling returns without creating anything.

### Keywords
- implementation-notes
- pom
- placeholders
- dialog
