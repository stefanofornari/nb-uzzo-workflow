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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertNotNull(in, "missing resource " + resource);
            return builder.parse(in);
        }
    }

    @Test
    void modeNameMatchesTheNameTheHookLooksUp() throws Exception {
        Element mode = parse(BASE + "uzzoKnowledgeBase.wsmode").getDocumentElement();

        assertEquals("mode", mode.getTagName());
        Element name = (Element) mode.getElementsByTagName("name").item(0);
        assertEquals(UzzoDeckHook.MODE_NAME, name.getAttribute("unique"));
    }

    @Test
    void modeIsAnEditorDeckThatStaysWhenEmpty() throws Exception {
        Element mode = parse(BASE + "uzzoKnowledgeBase.wsmode").getDocumentElement();

        Element kind = (Element) mode.getElementsByTagName("kind").item(0);
        Element empty = (Element) mode.getElementsByTagName("empty-behavior").item(0);
        assertEquals("editor", kind.getAttribute("type"));
        assertEquals("true", empty.getAttribute("permanent"));
    }

    @Test
    void modeUsesAFormatVersionNetBeansUnderstands() throws Exception {
        // 2.4 was rejected by NetBeans 31 ("Cannot parse configuration of mode"), 2.3 is accepted.
        Element mode = parse(BASE + "uzzoKnowledgeBase.wsmode").getDocumentElement();

        assertEquals("2.3", mode.getAttribute("version"));
    }

    @Test
    void layerRegistersTheModeFileAndItExists() throws Exception {
        Document layer = parse(BASE + "layer.xml");

        NodeList files = layer.getElementsByTagName("file");
        String url = null;
        for (int i = 0; i < files.getLength(); i++) {
            Element file = (Element) files.item(i);
            if ("uzzoKnowledgeBase.wsmode".equals(file.getAttribute("name"))) {
                url = file.getAttribute("url");
            }
        }
        assertNotNull(url, "layer.xml does not register uzzoKnowledgeBase.wsmode");
        assertNotNull(DeckDescriptorTest.class.getResource(BASE + url), "layer url does not resolve: " + url);
    }

    @Test
    void manifestPointsAtTheLayer() throws IOException {
        String manifest = Files.readString(Path.of("src/main/nbm/manifest.mf"));

        assertTrue(manifest.contains("OpenIDE-Module-Layer: ste/uzzo/nb/layer.xml"), manifest);
    }
}
