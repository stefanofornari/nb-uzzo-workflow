# Project Overview

## Identity

- **Name**: Uzzo Workflow for Netbeans (`nb-uzzo-workflow`)
- **Version**: 0.0.0-SNAPSHOT
- **License**: [Apache License, Version 2.0](../../LICENSE)
- **Summary**: A NetBeans module that brings the Uzzo Knowledge Base into NetBeans projects. Whenever a project contains a top-level `uzz` directory, the module surfaces it in the Projects tree as **Uzzo Knowledge Base**, decorates the `library`, `project`, `skills`, and `stories` sections with dedicated Material Design 2 icons, hides meta, OS, editor, and temporary files, keeps the tree in sync with external changes, and docks knowledge base documents into a dedicated editor deck on the right of the IDE.

## Team & Roles

| Person | Role | Responsibilities |
|--------|------|------------------|
| Stefano Fornari | [ROLE] | [RESPONSIBILITIES] |

## Timeline

- **Start**: [YYYY-MM-DD]
- **Milestones**:
  - [MILESTONE] — [YYYY-MM-DD]

## Repositories

- [nb-uzzo-workflow](https://github.com/stefanofornari/nb-uzzo-workflow) — NetBeans module implementing the Uzzo Knowledge Base integration.

## Architecture

The project is a single NetBeans module packaged as `nbm` and built with Maven. It integrates with the NetBeans Platform through the following components:

- `UzzoKnowledgeBaseNodeFactory` — a `NodeFactory` registered at `position = 50` for the project types shipped with NetBeans that expose the `Projects/<type>/Nodes` extension point (Maven, Java SE, Java module, Ant freeform, Gradle, web, PHP, Java EE, and Ant-based API support). `NodeFactory.Registration` has no wildcard, so a project type whose logical view is not extensible cannot be covered; adding a type means adding its id to the annotation. The factory contributes the project's top-level `uzz` folder to the logical view and watches the project directory so the node appears, disappears, or updates when the folder is created, deleted, or renamed.
- `UzzoKnowledgeBaseNode` and `SectionNode` — node implementations. `SectionNode` wraps the four known section folders (`library`, `project`, `skills`, `stories`) and renders their Material Design 2 icons through Ikonli; unknown folders fall back to a plain folder node.
- `UzzoDeckHook` — an `@OnShowing` hook that listens for opened editor `TopComponent` instances and docks those whose files live under the project's `uzz` folder into the dedicated `uzzoKnowledgeBase` mode declared in `src/main/resources/ste/uzzo/nb/layer.xml`.

The module depends on NetBeans Platform APIs (project UI, filesystems, nodes, windows), Ikonli Swing and Material Design 2 icon packs. Tests are written with JUnit 5 and the `nb-xtest-double` support library.

### Keywords
- project
- overview
- team
- timeline
- repositories
- architecture
