# project/US-000004 — Implementation Notes

Technical notes for [Hide Meta and Temporary Files from the Knowledge Base Tree](US-000004.md).

1. Filtering lives in `RefreshingChildren`, the children wrapper used by the knowledge base node and by every folder nested below it. Ignored entries are therefore hidden at every depth without touching the files on disk.
2. `IgnoredEntries` holds the rules behind a single `isIgnored(String name)` predicate. The name is lower-cased with `Locale.ROOT`, so matching is case-insensitive.
3. The rules are:
   - names starting with `.` (also covers `.#` editor lock files);
   - names equal to `Thumbs.db` or `desktop.ini`;
   - names ending with `~`, `.tmp`, `.bak`, `.swp`, `.swo`, or `.swx`;
   - names starting and ending with `#`.
4. To add an exclusion later:
   - exact name → add it to `IgnoredEntries.IGNORED_NAMES`;
   - prefix → add it to `IgnoredEntries.IGNORED_PREFIXES`;
   - suffix → add it to `IgnoredEntries.IGNORED_SUFFIXES`;
   - any other pattern → add a predicate to `IgnoredEntries.RULES`.
5. `RefreshingChildren.createNodes(Node)` returns an empty array for an ignored entry, which is the documented way of omitting an original child from a `FilterNode.Children`.
6. The name under test comes from `DataObject.getPrimaryFile().getNameExt()`, not `Node.getName()`, because `Node.getName()` drops the extension and would miss rules such as `Thumbs.db` or `*.bak`.
7. `RefreshingChildren.getNodesCount(boolean)` is derived from the filtered nodes, so callers that ask for a count see the same entries the tree lists.
8. The `uzz` sections are unaffected: `library`, `project`, `skills`, and `stories` do not match any rule, and an empty section stays visible even when its `.gitkeep` is hidden.

### Tests

- `IgnoredEntriesTest` covers every rule, case-insensitivity, and names that must stay visible.
- `UzzoKnowledgeBaseNodeTest` verifies filtering at the root and inside a nested folder.

### Keywords
- implementation-notes
- filtering
- meta-files
- temporary-files
