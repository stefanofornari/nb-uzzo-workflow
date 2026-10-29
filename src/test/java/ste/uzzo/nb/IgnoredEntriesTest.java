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
