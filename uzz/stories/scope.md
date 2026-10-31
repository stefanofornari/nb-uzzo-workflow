# Project Name: Uzzo Workflow for Netbeans
**Version:** 1.0-SNAPSHOT

## Overall Description

A NetBeans module that integrates the Uzzo knowledge base into the IDE for Maven projects. It surfaces the project's top-level `uzz` directory in the Projects tree, decorates the knowledge base sections, and redirects documents opened from the knowledge base into a dedicated editor deck.

Functional domains:
- **Knowledge Base Node**: surface the project's `uzz` folder in the NetBeans logical view and keep it in sync with the file system.
- **Section Presentation**: give the `library`, `project`, `skills`, and `stories` folders their dedicated icons while keeping normal folder behaviour.
- **Dedicated Editor Deck**: dock documents opened from inside `uzz` into a separate editor deck on the right of the IDE.
- **Knowledge Base Initialization**: create or complete the `uzz` knowledge base of an existing Maven project from the IDE, programmatically (no LLM), equivalent to the `uzzo-init` skill and using the content shipped by the `uzzo-workflow` dependency.

## In-Scope Capabilities

* **Knowledge Base Node**:
  - [ ] Show an **Uzzo Knowledge Base** node for Maven projects that contain a top-level `uzz` folder.
  - [ ] Add, remove, and update the node when the `uzz` folder is created, deleted, or renamed.
  - [ ] Refresh the knowledge base from the Projects view so content created or deleted outside NetBeans becomes visible.
  - [ ] Hide meta, OS, editor, and temporary files from the knowledge base tree.

* **Section Presentation**:
  - [ ] Render `library`, `project`, `skills`, and `stories` with their Material Design 2 icons.
  - [ ] Keep unchanged folders displayed as plain folder nodes.

* **Dedicated Editor Deck**:
  - [ ] Dock editors for files under `uzz` into the `uzzoKnowledgeBase` deck.
  - [ ] Leave editors outside the knowledge base in the central editor deck.

* **Knowledge Base Initialization**:
  - [ ] Offer an **Initialize Uzzo Knowledge Base** action on Maven projects without a top-level `uzz` folder.
  - [ ] Create the `library`, `project`, `skills`, and `stories` skeleton and seed articles, additively and idempotently (never overwrite or delete existing content).
  - [ ] Take articles and workflow skills from the `uzzo-workflow` dependency, copied verbatim.
  - [ ] Pre-fill project name, version, summary, repository, and toolchain from the Maven `pom.xml`, with user confirmation; leave unknown values as placeholders.
  - [ ] Offer **Repair Uzzo Knowledge Base** on incomplete knowledge bases and report created, skipped, placeholder, and incomplete items.

## Out-of-Scope / Future Enhancements

* Initializing non-Maven projects.
* LLM-assisted scoping or story generation from the IDE (handled by the `scope` and `story` skills).
* Updating or upgrading already installed skills to a newer `uzzo-workflow` version.
* Editing the content of existing articles (initialization never modifies them).

## Technical Context & Infrastructure

* **Development Toolchain**: Java 17 + Maven, NetBeans Platform `RELEASE280`, Ikonli 12.3.1, JUnit 5
* **Target Systems/Platforms**: NetBeans IDE (module packaged as `nbm`), Maven projects
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
