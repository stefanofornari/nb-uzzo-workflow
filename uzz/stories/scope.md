# Project Name: Uzzo Workflow for Netbeans
**Version:** 0.0.0-SNAPSHOT

## Overall Description

A NetBeans module that integrates the Uzzo knowledge base into the IDE for NetBeans projects. Whenever a project contains a top-level `uzz` directory, it surfaces that directory in the Projects tree, decorates the knowledge base sections, and redirects documents opened from the knowledge base into a dedicated editor deck.

Functional domains:
- **Knowledge Base Node**: surface the project's `uzz` folder in the NetBeans logical view and keep it in sync with the file system.
- **Section Presentation**: give the `library`, `project`, `skills`, and `stories` folders their dedicated icons while keeping normal folder behaviour.
- **Dedicated Editor Deck**: dock documents opened from inside `uzz` into a separate editor deck on the right of the IDE.

## In-Scope Capabilities

* **Knowledge Base Node**:
  - [ ] Show an **Uzzo Knowledge Base** node for NetBeans projects that contain a top-level `uzz` folder, for every project type whose logical view supports `NodeFactory` extensions.
  - [ ] Add, remove, and update the node when the `uzz` folder is created, deleted, or renamed.
  - [ ] Refresh the knowledge base from the Projects view so content created or deleted outside NetBeans becomes visible.
  - [ ] Hide meta, OS, editor, and temporary files from the knowledge base tree.

* **Section Presentation**:
  - [ ] Render `library`, `project`, `skills`, and `stories` with their Material Design 2 icons.
  - [ ] Keep unchanged folders displayed as plain folder nodes.

* **Dedicated Editor Deck**:
  - [ ] Dock editors for files under `uzz` into the `uzzoKnowledgeBase` deck.
  - [ ] Leave editors outside the knowledge base in the central editor deck.

## Out-of-Scope / Future Enhancements

* [Feature or behaviour explicitly excluded from the current cycle]
* [Deferred idea that would cause scope creep]

## Technical Context & Infrastructure

* **Development Toolchain**: Java 17 + Maven, NetBeans Platform `RELEASE280`, Ikonli 12.3.1, JUnit 5
* **Target Systems/Platforms**: NetBeans IDE (module packaged as `nbm`), NetBeans projects with an extensible logical view (Maven, Java SE, Java module, Ant freeform, Gradle, web, PHP, Java EE, Ant-based API support)
* **Project Reference Docs**:
  - Project Overview: `uzz/project/overview.md`
  - Coding Standard: `uzz/project/coding_standard.md`
  - Development Framework: `uzz/project/development_framework.md`

### Keywords
- scope
- requirements
- boundaries
- in-scope
- out-of-scope
