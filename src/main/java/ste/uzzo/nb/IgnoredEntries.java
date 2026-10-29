package ste.uzzo.nb;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Name rules for entries that are not shown in the knowledge base tree. The
 * rules apply to both files and folders and are matched against the complete
 * name, extension included.
 *
 * <p>To hide another kind of entry, add a prefix, exact name, or suffix to the
 * corresponding set, or add a new predicate to {@link #RULES}.</p>
 */
final class IgnoredEntries {

    /** OS metadata files that have no leading dot. */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "thumbs.db",
            "desktop.ini");

    /** Name prefixes that mark hidden entries or editor lock files. */
    private static final Set<String> IGNORED_PREFIXES = Set.of(".");

    /** Editor and temporary-file suffixes. */
    private static final Set<String> IGNORED_SUFFIXES = Set.of(
            "~",
            ".tmp",
            ".bak",
            ".swp",
            ".swo",
            ".swx");

    private static final List<Predicate<String>> RULES = List.of(
            name -> IGNORED_PREFIXES.stream().anyMatch(name::startsWith),
            IGNORED_NAMES::contains,
            name -> IGNORED_SUFFIXES.stream().anyMatch(name::endsWith),
            // Emacs autosave files such as "#notes.md#".
            name -> name.startsWith("#") && name.endsWith("#"));

    private IgnoredEntries() {
    }

    /**
     * Tells whether an entry with the given file name must be hidden. Matching
     * is case-insensitive.
     *
     * @param name file or folder name as returned by
     *             {@link org.openide.filesystems.FileObject#getNameExt()}
     * @return {@code true} if the entry must not be listed
     */
    static boolean isIgnored(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        return RULES.stream().anyMatch(rule -> rule.test(normalized));
    }
}
