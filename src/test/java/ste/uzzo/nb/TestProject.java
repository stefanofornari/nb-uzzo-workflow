package ste.uzzo.nb;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** Minimal project: just a directory. */
final class TestProject implements Project {

    private final FileObject dir;

    TestProject(FileObject dir) {
        this.dir = dir;
    }

    @Override
    public FileObject getProjectDirectory() {
        return dir;
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
}
