package ste.uzzo.nb;

import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test-only owner query: files under {@link #project}'s directory belong to it.
 * Registered through the default lookup so {@code FileOwnerQuery.getOwner} finds it.
 */
@ServiceProvider(service = FileOwnerQueryImplementation.class)
public final class TestOwnerQuery implements FileOwnerQueryImplementation {

    static volatile Project project;

    @Override
    public Project getOwner(URI file) {
        return null;
    }

    @Override
    public Project getOwner(FileObject file) {
        Project p = project;
        return p != null && FileUtil.isParentOf(p.getProjectDirectory(), file) ? p : null;
    }
}
