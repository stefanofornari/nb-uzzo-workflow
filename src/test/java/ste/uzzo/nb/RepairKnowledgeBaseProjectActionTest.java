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
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

import static org.assertj.core.api.BDDAssertions.then;

class RepairKnowledgeBaseProjectActionTest {

    @TempDir
    Path tempDir;

    private Action actionFor(Lookup context) {
        return ((ContextAwareAction) new RepairKnowledgeBaseProjectAction()).createContextAwareInstance(context);
    }

    private Action actionForProject() {
        return actionForProject(tempDir);
    }

    private Action actionForProject(Path dir) {
        Project project = new TestProject(FileUtil.toFileObject(dir.toFile()));
        return actionFor(Lookups.singleton(project));
    }

    @Test
    void has_the_repair_name_and_the_lamp_icon() {
        Action action = actionForProject();

        then(action.getValue(Action.NAME)).isEqualTo("Repair Uzzo Knowledge Base");
        then(((Presenter.Popup) action).getPopupPresenter().getIcon()).isNotNull();
    }

    @Test
    void enabled_only_with_a_knowledge_base_folder() throws IOException {
        Path none = Files.createDirectories(tempDir.resolve("none"));
        Path file = Files.createDirectories(tempDir.resolve("file"));
        Files.writeString(file.resolve("uzz"), "x");
        Path folder = Files.createDirectories(tempDir.resolve("folder"));
        Files.createDirectories(folder.resolve("uzz"));

        then(actionForProject(none).isEnabled()).isFalse();
        then(actionForProject(file).isEnabled()).isFalse();
        then(actionForProject(folder).isEnabled()).isTrue();
    }

    @Test
    void disabled_without_a_project_in_context() {
        then(actionFor(Lookup.EMPTY).isEnabled()).isFalse();
    }
}
