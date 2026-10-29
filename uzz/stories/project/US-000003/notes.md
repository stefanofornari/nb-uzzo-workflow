# project/US-000003 — Implementation Notes

Technical notes for [Refresh Knowledge Base Content from the Project View](US-000003.md).

1. `UzzoKnowledgeBaseNode.getActions` appends a `RefreshKnowledgeBaseAction` to the node's delegated actions, so the action is available from the Projects view context menu.
2. `RefreshKnowledgeBaseAction` extends `AbstractAction` and takes its localized name from `@Messages`.
3. `actionPerformed` posts `refreshRecursively(root)` to the default `RequestProcessor`, keeping folder refresh off the Event Dispatch Thread.
4. `refreshRecursively` refreshes a folder and then recurses into its subfolders, so deep and already-expanded folders are refreshed as well.
5. The action complements `RefreshingChildren`, which refreshes a folder automatically when the tree requests its children on expansion.

### Keywords
- implementation-notes
- refresh
- context-menu
- netbeans
