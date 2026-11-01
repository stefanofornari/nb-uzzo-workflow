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

import java.io.IOException;
import java.util.List;

/**
 * Source of the canonical Uzzo knowledge base content. Paths are relative to
 * the knowledge base root, use {@code /} and never start with {@code uzz/}.
 */
interface KnowledgeBaseContent {

    /** Lists every file the content provides, sorted. */
    List<String> paths() throws IOException;

    /** Reads one of the listed files. */
    byte[] read(String path) throws IOException;
}
