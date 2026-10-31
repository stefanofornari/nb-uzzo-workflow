package ste.uzzo.nb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.api.project.Project;
import org.openide.NotifyDescriptor;
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
        NotifyDescriptor outcome = new InitializeKnowledgeBaseAction().initialize(project());

        then(outcome).isNull();
        then(tempDir.resolve("uzz/stories")).isDirectory();
        then(tempDir.resolve("uzz/skills/kb/SKILL.md")).exists();
    }

    @Test
    void failure_is_reported_as_an_error_naming_the_path() throws IOException {
        Path file = Files.writeString(tempDir.resolve("uzz"), "x");

        NotifyDescriptor outcome = new InitializeKnowledgeBaseAction().initialize(project());

        then(outcome.getMessageType()).isEqualTo(NotifyDescriptor.ERROR_MESSAGE);
        then(outcome.getMessage().toString()).contains(file.toString());
    }

    @Test
    void unreadable_content_is_reported_as_a_warning_about_the_incomplete_knowledge_base() {
        KnowledgeBaseContent unreadable = new KnowledgeBaseContent() {
            @Override
            public java.util.List<String> paths() throws IOException {
                throw new IOException("no content");
            }

            @Override
            public byte[] read(String path) throws IOException {
                throw new IOException("no content");
            }
        };

        NotifyDescriptor outcome = new InitializeKnowledgeBaseAction(unreadable).initialize(project());

        then(outcome.getMessageType()).isEqualTo(NotifyDescriptor.WARNING_MESSAGE);
        then(outcome.getMessage().toString()).contains("incomplete");
        then(tempDir.resolve("uzz/stories")).isDirectory();
    }
}
