# project/US-000002 — Implementation Notes

Technical notes for [Open Knowledge Base Documents in a Dedicated Editor Deck](US-000002.md).

1. The deck is a NetBeans editor mode declared in `uzzoKnowledgeBase.wsmode` and registered through `layer.xml` under `Windows2/Modes`. It is an `editor` kind, joined on the right with weight 0.3, and stays visible when empty.
2. `UzzoDeckHook` is an `@OnShowing` runnable: it registers a listener on the `TopComponent` registry when the IDE window system is shown.
3. On `PROP_TC_OPENED` the hook defers the redirect with `EventQueue.invokeLater` so the window system can finish opening the component, then checks again after 400 ms because some editors (for example Markdown viewers) re-dock themselves right after opening.
4. A component is redirected only when it is open and is an editor `TopComponent`. The hook resolves its file from the component lookup (`DataObject` or `FileObject`) or, as a fallback, from the activated nodes.
5. The file is redirected only when its owning project contains a top-level `uzz` folder that is an ancestor of the file. Files outside `uzz`, files in a lookalike folder outside the project root, and files with no project owner are left alone.
6. If the `uzzoKnowledgeBase` mode is missing or the component is already docked there, the hook does nothing; otherwise it docks the component into the mode, opens it, and requests activation.
7. `UzzoDeckHookTest` covers knowledge-base detection and file resolution; `DeckDescriptorTest` guards the mode and layer descriptors, which fail silently at runtime when malformed.

### Keywords
- implementation-notes
- dedicated-deck
- window-system
- netbeans
