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
