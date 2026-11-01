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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.BDDAssertions.then;

class IgnoredEntriesTest {

    @ParameterizedTest
    @ValueSource(strings = {".gitkeep", ".gitignore", ".git", ".DS_Store", ".idea", ".vscode"})
    void hidden_entries_are_ignored(String name) {
        then(IgnoredEntries.isIgnored(name)).as(name).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Thumbs.db", "desktop.ini", "THUMBS.DB", "Desktop.INI"})
    void os_metadata_is_ignored(String name) {
        then(IgnoredEntries.isIgnored(name)).as(name).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"notes.md~", "notes.md.swp", "notes.md.swo", "notes.md.swx", ".#notes.md", "#notes.md#"})
    void editor_junk_is_ignored(String name) {
        then(IgnoredEntries.isIgnored(name)).as(name).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"draft.md.tmp", "draft.md.bak", "DRAFT.MD.TMP", "DRAFT.MD.BAK"})
    void temporary_and_backup_files_are_ignored(String name) {
        then(IgnoredEntries.isIgnored(name)).as(name).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"README.md", "notes.md", "v1.2-notes.md", "my.notes.md",
        "desktop.ini.txt", "Thumbs.db.txt", "cache.tmp.md", "#notes.md", "notes.md#"})
    void regular_names_are_visible(String name) {
        then(IgnoredEntries.isIgnored(name)).as(name).isFalse();
    }

    @Test
    void missing_names_are_visible() {
        then(IgnoredEntries.isIgnored(null)).isFalse();
        then(IgnoredEntries.isIgnored("")).isFalse();
    }
}
