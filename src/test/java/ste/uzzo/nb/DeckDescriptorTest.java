package ste.uzzo.nb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Guards the window-system descriptors, which fail silently at runtime: when the
 * mode file is rejected NetBeans only logs it and the deck simply never exists.
 */
class DeckDescriptorTest {

    private static final String BASE = "/ste/uzzo/nb/";

    private static Document parse(String resource) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // The DOCTYPEs point at netbeans.org: never fetch them in a unit test.
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (InputStream in = DeckDescriptorTest.class.getResourceAsStream(resource)) {
            then(in).as("missing resource %s", resource).isNotNull();
            return builder.parse(in);
        }
    }

    @Test
    void mode_name_matches_the_name_the_hook_looks_up() throws Exception {
        Element mode = parse(BASE + "uzzoKnowledgeBase.wsmode").getDocumentElement();

        then(mode.getTagName()).isEqualTo("mode");
        Element name = (Element) mode.getElementsByTagName("name").item(0);
        then(name.getAttribute("unique")).isEqualTo(UzzoDeckHook.MODE_NAME);
    }

    @Test
    void mode_is_an_editor_deck_that_stays_when_empty() throws Exception {
        Element mode = parse(BASE + "uzzoKnowledgeBase.wsmode").getDocumentElement();

        Element kind = (Element) mode.getElementsByTagName("kind").item(0);
        Element empty = (Element) mode.getElementsByTagName("empty-behavior").item(0);
        then(kind.getAttribute("type")).isEqualTo("editor");
        then(empty.getAttribute("permanent")).isEqualTo("true");
    }

    @Test
    void mode_uses_a_format_version_netbeans_understands() throws Exception {
        // 2.4 was rejected by NetBeans 31 ("Cannot parse configuration of mode"), 2.3 is accepted.
        Element mode = parse(BASE + "uzzoKnowledgeBase.wsmode").getDocumentElement();

        then(mode.getAttribute("version")).isEqualTo("2.3");
    }

    @Test
    void layer_registers_the_mode_file_and_it_exists() throws Exception {
        Document layer = parse(BASE + "layer.xml");

        NodeList files = layer.getElementsByTagName("file");
        String url = null;
        for (int i = 0; i < files.getLength(); i++) {
            Element file = (Element) files.item(i);
            if ("uzzoKnowledgeBase.wsmode".equals(file.getAttribute("name"))) {
                url = file.getAttribute("url");
            }
        }
        then(url).as("layer.xml does not register uzzoKnowledgeBase.wsmode").isNotNull();
        then(DeckDescriptorTest.class.getResource(BASE + url))
                .as("layer url does not resolve: %s", url)
                .isNotNull();
    }

    @Test
    void manifest_points_at_the_layer() throws IOException {
        String manifest = Files.readString(Path.of("src/main/nbm/manifest.mf"));

        then(manifest).contains("OpenIDE-Module-Layer: ste/uzzo/nb/layer.xml");
    }
}
