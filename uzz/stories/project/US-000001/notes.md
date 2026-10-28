# project/US-000001 — Implementation Notes

Technical notes for [Show the Uzzo Knowledge Base in the Projects View](US-000001.md).

1. The node is contributed by `UzzoKnowledgeBaseNodeFactory`, a `NodeFactory` registered for the `org-netbeans-modules-maven` project type at position 50.
2. Only the project's top-level `uzz` directory is surfaced. The node is present exactly when that folder exists and is a folder.
3. The factory listens to the project directory through a weak `FileChangeListener` and refreshes when the `uzz` entry is created, deleted, or renamed, so the node appears and disappears without reloading the project.
4. The node wraps the folder's original node, so children and standard folder actions behave as for any folder. Its display name is fixed to "Uzzo Knowledge Base" and node renaming is disabled; renaming the folder itself from the IDE or file system is still reflected by the factory.
5. Presence/absence and the create/delete refresh are covered by `UzzoKnowledgeBaseNodeFactoryTest`; label, icon, children, and section wrapping by `UzzoKnowledgeBaseNodeTest`.
6. Folder children are wrapped in `RefreshingChildren`, which refreshes the underlying folder before the tree lists it, so files created by external tools appear when the folder is expanded. The node also offers a **Refresh Knowledge Base** action that recursively refreshes the whole tree, including folders that are already expanded.

### Keywords
- implementation-notes
- knowledge-base
- node-factory
- netbeans
