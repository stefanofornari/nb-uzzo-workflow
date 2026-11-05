---
title: Instructions
layout: default
nav_order: 3
---

# Instructions: from download to your first knowledge base

This walkthrough takes you from downloading the plugin to a dummy project that
has an Uzzo Knowledge Base (KB) in it. It takes about ten minutes.

1. TOC
{:toc}

## Prerequisites

- Apache NetBeans 28, the version the plugin is built against
- JDK 21 or later
- Maven (the Maven bundled with NetBeans is fine)

> Confirm the minimum JDK. The plugin is compiled for Java 21, while the README
> says JDK 17+.
{: .todo }

## 1. Download the plugin

1. Open the [releases page](https://github.com/stefanofornari/nb-uzzo-workflow/releases).
2. Under **Assets** of the latest release, download the `.nbm` file
   (`nb-uzzo-workflow-<version>.nbm`).

> No release has been published yet. Until then, build the plugin yourself:
> clone the repository and run `mvn clean package`. The `.nbm` is created in
> `target/`. Replace this note once the first release is out.
{: .todo }

> Screenshot: the releases page with the `.nbm` asset highlighted.
>
> `![Releases page](assets/images/instructions/01-download.png)`
{: .screenshot }

## 2. Install the plugin in NetBeans

1. In NetBeans, open **Tools → Plugins**.
2. Select the **Downloaded** tab and click **Add Plugins...**.
3. Pick the `.nbm` file, then click **Install**.
4. Follow the installer: accept the license and, if prompted, trust the unsigned
   plugin.
5. Restart NetBeans when asked.

> Screenshot: the **Downloaded** tab with the plugin added.
>
> `![Plugins dialog](assets/images/instructions/02-plugins-dialog.png)`
{: .screenshot }

> Screenshot: the installer's final page, before the restart.
>
> `![Install complete](assets/images/instructions/03-install-complete.png)`
{: .screenshot }

## 3. Create a dummy project

You only need a project to hold the knowledge base. The initialization action is
available on Maven projects, so create a small one.

1. Choose **File → New Project...**.
2. Select **Java with Maven → Java Application** and click **Next**.
3. Name it `uzzo-demo`, pick a location, and keep the default Group Id and
   Version. Click **Finish**.
4. The project appears in the **Projects** window.

> Screenshot: the **New Project** wizard with *Java Application* selected.
>
> `![New project wizard](assets/images/instructions/04-new-project.png)`
{: .screenshot }

> Screenshot: the new `uzzo-demo` project in the **Projects** window.
>
> `![Projects window](assets/images/instructions/05-project-created.png)`
{: .screenshot }

## 4. Initialize the knowledge base

1. Right-click the `uzzo-demo` project and choose **Initialize Uzzo Knowledge
   Base**.
2. A dialog proposes the project facts it read from your `pom.xml`: **Name**,
   **Version**, **Summary**, **Repository URL** and **Java release**. Edit them,
   or clear any you don't know yet: empty values stay as placeholders.
3. Click **OK**. The knowledge base is created in the background, and a report
   lists what was created, skipped, left as placeholders or found incomplete.

Initialization is additive: it never overwrites or deletes existing files, so it
is safe to run on a project that already has some content.

> Screenshot: the project's right-click menu with **Initialize Uzzo Knowledge
> Base**.
>
> `![Initialize action](assets/images/instructions/06-init-action.png)`
{: .screenshot }

> Screenshot: the confirmation dialog with the project facts.
>
> `![Facts dialog](assets/images/instructions/07-facts-dialog.png)`
{: .screenshot }

> Screenshot: the report shown when initialization completes.
>
> `![Outcome report](assets/images/instructions/08-outcome-report.png)`
{: .screenshot }

## 5. Explore the knowledge base

The project now has a top-level `uzz/` folder with four directories:

```
uzz/
├── library/
├── project/
├── skills/
└── stories/
```

In the **Projects** window, the folder shows as the **Uzzo Knowledge Base** node,
with an Aladdin's lamp icon, just above the source folders. Expand it to browse
the seeded workflow skills and articles.

> Screenshot: the **Uzzo Knowledge Base** node expanded in the **Projects**
> window.
>
> `![Uzzo Knowledge Base node](assets/images/instructions/09-kb-node.png)`
{: .screenshot }

Open a document, for example one under `project/`. It opens in a separate editor
deck on the right of the IDE, so the knowledge base stays next to your code.
Values the initializer could not know are bracketed placeholders such as
`[PROJECT NAME]`: replace them as you go.

> Screenshot: a knowledge base document open in the right-hand deck.
>
> `![Right-hand deck](assets/images/instructions/10-right-deck.png)`
{: .screenshot }

## Keeping it in sync

- If files change outside the IDE, for example when an AI agent writes to the
  knowledge base, expand a folder or run **Refresh Knowledge Base** to see them.
- If the structure is incomplete, right-click the project and choose **Repair
  Uzzo Knowledge Base**. Once a knowledge base exists, this replaces the
  *Initialize* entry. Existing content is left untouched.

## What's next

Read how the workflow works on the
[Uzzo Workflow site](https://stefanofornari.github.io/uzzo-workflow/): the
[scoping phase](https://stefanofornari.github.io/uzzo-workflow/workflow/scoping)
and the [knowledge base](https://stefanofornari.github.io/uzzo-workflow/knowledge-base)
conventions.
