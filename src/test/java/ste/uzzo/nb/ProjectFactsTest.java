package ste.uzzo.nb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.filesystems.FileUtil;

import static org.assertj.core.api.BDDAssertions.then;

class ProjectFactsTest {

    @TempDir
    Path tempDir;

    private ProjectFacts read(String pom) throws IOException {
        Files.writeString(tempDir.resolve("pom.xml"), pom);
        return ProjectFacts.fromPom(FileUtil.toFileObject(tempDir.toFile()));
    }

    @Test
    void reads_name_version_summary_repository_and_release() throws IOException {
        ProjectFacts facts = read("""
                <project>
                  <groupId>g</groupId><artifactId>a</artifactId>
                  <name>My App</name><version>1.2.3</version>
                  <description>  Does
                     things.  </description>
                  <scm><url>https://github.com/me/app</url></scm>
                  <properties><maven.compiler.release>21</maven.compiler.release></properties>
                </project>""");

        then(facts.name()).isEqualTo("My App");
        then(facts.version()).isEqualTo("1.2.3");
        then(facts.summary()).isEqualTo("Does things.");
        then(facts.repository()).isEqualTo("https://github.com/me/app");
        then(facts.javaRelease()).isEqualTo("21");
    }

    @Test
    void name_falls_back_to_the_artifact_id_and_version_to_the_parent() throws IOException {
        ProjectFacts facts = read("""
                <project>
                  <parent><groupId>g</groupId><artifactId>p</artifactId><version>9</version></parent>
                  <artifactId>a</artifactId>
                </project>""");

        then(facts.name()).isEqualTo("a");
        then(facts.version()).isEqualTo("9");
    }

    @Test
    void missing_facts_are_absent() throws IOException {
        ProjectFacts facts = read("<project><modelVersion>4.0.0</modelVersion></project>");

        then(facts).isEqualTo(ProjectFacts.NONE);
    }

    @Test
    void unresolved_property_references_are_absent() throws IOException {
        ProjectFacts facts = read("""
                <project><artifactId>a</artifactId><version>${rev}</version>
                  <description>${desc}</description></project>""");

        then(facts.version()).isNull();
        then(facts.summary()).isNull();
    }

    @Test
    void release_comes_from_source_or_the_compiler_plugin() throws IOException {
        then(read("<project><properties><maven.compiler.source>1.8</maven.compiler.source></properties></project>")
                .javaRelease()).isEqualTo("8");
        then(read("""
                <project><build><plugins><plugin>
                  <artifactId>maven-compiler-plugin</artifactId>
                  <configuration><release>17</release></configuration>
                </plugin></plugins></build></project>""").javaRelease()).isEqualTo("17");
    }

    @Test
    void no_pom_or_a_broken_pom_gives_no_facts() throws IOException {
        then(ProjectFacts.fromPom(FileUtil.toFileObject(tempDir.toFile()))).isEqualTo(ProjectFacts.NONE);
        then(read("<project><unclosed>")).isEqualTo(ProjectFacts.NONE);
    }

    @Test
    void blank_values_are_absent() {
        then(new ProjectFacts("  ", "", null, " ", "\t")).isEqualTo(ProjectFacts.NONE);
    }

    @Test
    void fills_the_known_placeholders_only() {
        ProjectFacts facts = new ProjectFacts("My App", "1.0", null, "https://x/y", "21");

        String text = facts.fill("""
                # [PROJECT NAME] [VERSION]
                - **Summary**: [One paragraph describing what the project is, who it serves, and the problem it solves.]
                - [REPOSITORY NAME]([URL]) — [PURPOSE]
                - **Language / Runtime**: [LANGUAGE AND VERSION]
                - **Build Tool**: [BUILD TOOL]
                - **Build**: `[BUILD COMMAND]`
                - **Test**: `[TEST COMMAND]`
                - **Run**: `[RUN COMMAND]`
                """);

        then(text).contains("# My App 1.0")
                .contains("[One paragraph describing")
                .contains("- [My App](https://x/y) — [PURPOSE]")
                .contains("**Language / Runtime**: Java 21")
                .contains("**Build Tool**: Maven")
                .contains("`mvn clean install`")
                .contains("`mvn test`")
                .contains("`[RUN COMMAND]`");
    }

    @Test
    void without_facts_only_the_maven_toolchain_is_filled() {
        String text = ProjectFacts.NONE.fill("# [PROJECT NAME] [VERSION]\n"
                + "- **Language / Runtime**: [LANGUAGE AND VERSION]\n- **Build Tool**: [BUILD TOOL]\n"
                + "`[BUILD COMMAND]` `[TEST COMMAND]`\n");

        then(text).isEqualTo("# [PROJECT NAME] [VERSION]\n"
                + "- **Language / Runtime**: [LANGUAGE AND VERSION]\n- **Build Tool**: Maven\n"
                + "`mvn clean install` `mvn test`\n");
    }

    @Test
    void the_summary_is_filled_when_known() {
        String text = new ProjectFacts(null, null, "A thing.", null, null)
                .fill("**Summary**: [One paragraph describing what the project is, who it serves, and the problem it solves.]");

        then(text).isEqualTo("**Summary**: A thing.");
    }
}
