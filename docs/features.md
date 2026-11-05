---
title: Features
layout: default
nav_order: 2
---

# Features

## Knowledge Base node

The `uzz` folder of a project appears in the logical view, just above the source
folders, with an Aladdin's lamp icon. It behaves like any other folder (open,
create, rename, delete, drag and drop) and follows file-system changes. If
`uzz` is created or removed, the node appears or disappears.

The node is contributed to every NetBeans project type that exposes the
`Projects/<type>/Nodes` extension point: Maven, Java SE, Java module, Ant
freeform, Gradle, web, PHP, Java EE, and Ant-based API support. Project types
with a closed logical view cannot be extended by a module.

## Dedicated deck

Documents opened from inside `uzz` are docked into a separate editor deck on the
right of the IDE, instead of the central editor deck. Your knowledge base and
your code stay side by side.

## Initialize Knowledge Base

On a Maven project without an `uzz` folder, the **Initialize Uzzo Knowledge
Base** action:

- scaffolds `uzz/{library,project,skills,stories}`
- seeds the workflow skills and seed articles from the `uzzo-workflow-kb`
  dependency
- pre-fills project facts (name, version, summary, repository, Java release)
  from your `pom.xml`, in a confirmation dialog you can edit

It runs in the background, and it is additive: it never overwrites existing
content.

## Repair Knowledge Base

On an existing, possibly incomplete knowledge base, the **Repair Uzzo Knowledge
Base** action completes the missing structure. It reports what it created,
skipped, left as placeholders or found incomplete, and leaves existing content
untouched. A complete knowledge base is reported as already complete.

## External changes

The knowledge base is refreshed when a folder is expanded, and the **Refresh
Knowledge Base** action refreshes the whole tree. Files created by external
tools, such as an AI agent working in a terminal, become visible without
restarting the IDE.

## Clean tree

Hidden entries (names starting with `.`), OS metadata (`Thumbs.db`,
`desktop.ini`), editor files (`*~`, `*.swp`, `*.swo`, `*.swx`, `.#*`, `#*#`) and
temporary or backup files (`*.tmp`, `*.bak`) are omitted from the tree. They stay
on disk and remain available to other tools.
