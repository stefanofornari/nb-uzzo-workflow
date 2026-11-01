/*
 * Copyright 2026 the original author or authors from the Uzzo and Uzzo Workflow project
 * (https://stefanofornari.github.io/uzzo-workflow).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
