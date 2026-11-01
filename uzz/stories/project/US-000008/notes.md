# project/US-000008 — Implementation Notes

Technical notes for [Repair an Incomplete Knowledge Base and Report the Outcome](US-000008.md).

1. `KnowledgeBaseInitializer.initialize` now serves both cases: `uzz` absent gives a `FRESH` run, `uzz` a folder gives `REPAIR` (something was written) or `COMPLETE` (nothing was). `uzz` as a file is rejected with its path.
2. Everything that should exist is computed first (skills, six articles). Existing paths are then checked and any wrong-typed one (a section or ancestor that is a file, an expected file that is a folder) aborts the run before anything is written, listing every conflicting path.
3. The existing knowledge base is refreshed first, since files may have changed outside the IDE. A file is written only when missing or empty (size 0); a file with content is skipped and reported. Extra user files are never touched or reported. `.gitkeep` is added last, only to a section folder that is empty.
4. On failure, a `uzz` created by the run is deleted; an existing `uzz` is left exactly as it is.
5. `Result` carries kind, created (folders end with `/`), skipped, placeholders and incomplete. Placeholders are scanned only in articles written by the run, as `file: [FIELD]`; checkboxes and filled links are not placeholders.
6. `OutcomeReport` turns a `Result` into the dialog: information, warning when content was incomplete, error for an `IOException`. Initialize now always shows a report (except on cancel).
7. `RepairKnowledgeBaseAction` is offered on the Uzzo Knowledge Base node (always, so a complete KB reports "already complete"). It uses the pom facts as they are, without the dialog, since it cannot know beforehand whether anything will be created.

8. On the project node there is a single entry: `InitializeKnowledgeBaseAction` is "Initialize Uzzo Knowledge Base" while there is no `uzz` folder and becomes "Repair Uzzo Knowledge Base" (delegating to `RepairKnowledgeBaseAction`) once there is one. Initialize, Repair and Refresh all show the lamp icon through a popup presenter.

### Keywords
- implementation-notes
- repair
- report
- additive
