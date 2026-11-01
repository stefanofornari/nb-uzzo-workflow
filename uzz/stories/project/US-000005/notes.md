# project/US-000005 — Implementation Notes

Technical notes for [Initialize the Knowledge Base from the Project Context Menu](US-000005.md).

1. `InitializeKnowledgeBaseAction` is a public `ContextAwareAction` registered with `@ActionRegistration(lazy = false)` and `@ActionReference(path = "Projects/org-netbeans-modules-maven/Actions")`. Each popup creates a fresh instance from the project in the context lookup, so enablement is recomputed every time.
2. The action is enabled when the project has no `uzz` folder (an `uzz` regular file keeps it enabled so the conflict is reported); it is disabled when `uzz` is a folder or no project is in the context.
3. `actionPerformed` posts the work to the default `RequestProcessor`; `initialize(Project)` returns an error message or `null`, and errors are shown with `DialogDisplayer` as an error message.
4. `KnowledgeBaseInitializer.initialize(FileObject)` works through `FileObject`, so the existing `UzzoKnowledgeBaseNodeFactory` file listener shows the node as soon as `uzz` is created.
5. It refuses to run when `uzz` already exists (file or folder), creates the four sections, then adds an empty `.gitkeep` to every section still empty. The `.gitkeep` step comes last so US-000006 can seed content first.
6. On failure the `IOException` message names the path being created and the partially created `uzz` is deleted.
7. Build and test with JDK 21 (NetBeans annotation processing fails on newer JDKs).

### Keywords
- implementation-notes
- initialization
- context-menu
- netbeans
