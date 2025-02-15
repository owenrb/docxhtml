package io.owenrbee.docxhtml.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.owenrbee.docxhtml.model.MediaRef;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class RelationshipParser {

    @Value("${route.base64.folder}")
    private String mediaFolder;

    public Map<String, MediaRef> parseRelationships(String filePath)
            throws ParserConfigurationException, IOException, SAXException {

        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);

        return parseRelationships(inputStream);

    }

    public Map<String, MediaRef> parseRelationships(InputStream xmlStream)
            throws ParserConfigurationException, IOException, SAXException {

        // Create a namespace-aware document builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the XML input stream
        Document document = builder.parse(xmlStream);

        // Get all Relationship elements under the namespace
        String namespaceURI = "http://schemas.openxmlformats.org/package/2006/relationships";
        NodeList nodeList = document.getElementsByTagNameNS(namespaceURI, "Relationship");

        // Extract Id and Target attributes
        Map<String, MediaRef> map = new HashMap<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);

            String id = element.getAttribute("Id");
            String target = element.getAttribute("Target");

            MediaRef mediaRef = new MediaRef();
            mediaRef.setTarget(target);

            String base64path = mediaFolder + "/" + mediaRef.getFilenameNoExt() + ".txt";
            String mdpath = mediaFolder + "/" + mediaRef.getFilenameNoExt() + ".md";

            if (!new File(base64path).exists()) {
                continue;
            }

            String base64 = new String(Files.readAllBytes(Paths.get(base64path)));
            mediaRef.setBase64(base64);

            if (new File(mdpath).exists()) {
                String markdown = new String(Files.readAllBytes(Paths.get(mdpath)));
                mediaRef.setMarkdown(markdown);
            } else {
                mediaRef.setMarkdown("");
            }

            map.put(id, mediaRef);
        }

        return map;
    }
}