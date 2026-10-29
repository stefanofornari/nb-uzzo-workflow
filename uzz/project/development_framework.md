# Development Framework

## Purpose

How the `nb-uzzo-workflow` NetBeans module is built, tested, and installed.

## Technology Stack

- **Language / Runtime**: Java 17
- **Frameworks / Libraries**: NetBeans Platform APIs (`RELEASE280`), Ikonli Swing and Material Design 2 icon packs (`12.3.1`)
- **Build Tool**: Maven (`nbm-maven-plugin` 14.0, packaging `nbm`)
- **Test Tools**: JUnit 5 (`5.10.2`), AssertJ (`3.27.7`), `nb-xtest-double`, Maven Surefire 3.2.5

## Development Workflow

- Follow TDD where practical: write a failing test, make it pass, then refactor.
- Keep the module installable as a NetBeans plugin after every change.
- Verify changes with `mvn clean package` before handing them over.

## Build and Execution

- **Build**: `mvn clean package`
- **Test**: `mvn test`
- **Run**: install `target/nb-uzzo-workflow-*.nbm` through NetBeans *Tools → Plugins → Downloaded → Add Plugins…*

### Keywords
- development-framework
- toolchain
- build
- testing
- workflow
