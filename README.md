# Uzzo Workflow for Netbeans

A Netbeans plugin that brings AI-based [Uzzo Development Workflow](https://github.com/stefanofornari/uzzo-workflow)
into Netbeans.

## Features

- **Knowledge Base node** – the `uzz` folder of a project appears in the logical
  view, just above the source folders, with an Aladdin's lamp icon. It behaves
  like any other folder (open, create, rename, delete, drag and drop) and follows
  file-system changes. If `uzz` is created or removed, the node appears or
  disappears. The node is contributed to every NetBeans project type that exposes
  the `Projects/<type>/Nodes` extension point: Maven, Java SE, Java module, Ant
  freeform, Gradle, web, PHP, Java EE, and Ant-based API support. Project types
  with a closed logical view cannot be extended by a module.
- **Dedicated deck** – documents opened from inside `uzz` are docked into a
  separate editor deck on the right of the IDE instead of the central editor deck.
- **External changes** – the knowledge base is refreshed when a folder is
  expanded, and the **Refresh Knowledge Base** action refreshes the whole tree.
  This makes files created by external tools visible without restarting the IDE.
- **Clean tree** – hidden entries (names starting with `.`), OS metadata
  (`Thumbs.db`, `desktop.ini`), editor files (`*~`, `*.swp`, `*.swo`, `*.swx`,
  `.#*`, `#*#`) and temporary/backup files (`*.tmp`, `*.bak`) are omitted from
  the tree. They stay on disk and remain available to other tools.
- **Initialize Knowledge Base** – an "Initialize Uzzo Knowledge Base" action on a
  Maven project without an `uzz` folder scaffolds `uzz/{library,project,skills,stories}`,
  seeds the workflow skills and seed articles from the `uzzo-workflow-kb` dependency,
  and pre-fills project facts (name, version, summary, repository, Java release)
  from the `pom.xml` through an editable confirmation dialog. Runs off the EDT and
  is additive: never overwrites existing content.
- **Repair Knowledge Base** – an "Repair Uzzo Knowledge Base" action on an existing
  (possibly incomplete) Uzzo Knowledge Base node completes the missing structure,
  reports created, skipped, placeholder and incomplete items, and leaves existing
  content untouched. A complete knowledge base is reported as already complete.

## Requirements

- JDK 17+
- Maven 3.6+
- NetBeans matching the `netbeans.version` property in `pom.xml`

## Build and install

```
mvn clean package
```

Then in NetBeans: *Tools → Plugins → Downloaded → Add Plugins…* and select
`target/nb-uzzo-workflow-*.nbm`.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

