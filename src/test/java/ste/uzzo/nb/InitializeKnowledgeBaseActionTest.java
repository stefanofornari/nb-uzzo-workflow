package ste.uzzo.nb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import static org.assertj.core.api.BDDAssertions.then;

class InitializeKnowledgeBaseActionTest {

    @TempDir
    Path tempDir;

    private Action actionFor(Project project) {
        return ((ContextAwareAction) new InitializeKnowledgeBaseAction())
                .createContextAwareInstance(Lookups.singleton(project));
    }

    private Project project() {
        return new TestProject(FileUtil.toFileObject(tempDir.toFile()));
    }

    @Test
    void has_the_initialize_name() {
        then(actionFor(project()).getValue(Action.NAME)).isEqualTo("Initialize Uzzo Knowledge Base");
    }

    @Test
    void enabled_without_a_knowledge_base() {
        then(actionFor(project()).isEnabled()).isTrue();
    }

    @Test
    void disabled_with_a_knowledge_base() throws IOException {
        Files.createDirectories(tempDir.resolve("uzz"));

        then(actionFor(project()).isEnabled()).isFalse();
    }

    @Test
    void enabled_when_uzz_is_a_file() throws IOException {
        Files.writeString(tempDir.resolve("uzz"), "x");

        then(actionFor(project()).isEnabled()).isTrue();
    }

    @Test
    void disabled_without_a_project_in_context() {
        Action action = ((ContextAwareAction) new InitializeKnowledgeBaseAction())
                .createContextAwareInstance(Lookup.EMPTY);

        then(action.isEnabled()).isFalse();
    }

    @Test
    void invoking_creates_the_knowledge_base() throws Exception {
        InitializeKnowledgeBaseAction action = new InitializeKnowledgeBaseAction();
        Project project = project();

        String error = action.initialize(project);

        then(error).isNull();
        then(tempDir.resolve("uzz/stories")).isDirectory();
    }

    @Test
    void failure_is_returned_as_a_message_naming_the_path() throws IOException {
        Path file = Files.writeString(tempDir.resolve("uzz"), "x");

        String error = new InitializeKnowledgeBaseAction().initialize(project());

        then(error).contains(file.toString());
    }
}
