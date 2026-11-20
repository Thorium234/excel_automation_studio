package com.excelstudio.ui;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Checks the contract between each FXML file and its controller without starting
 * the JavaFX toolkit: every {@code fx:id} must have a matching field, and every
 * {@code onAction} must have a matching method. This catches the most common
 * FXML regressions (renamed handlers and missing injections).
 */
class FxmlContractTest {

    @Test
    void everyViewHasValidFxml() throws Exception {
        for (View view : View.values()) {
            assertValid(view.fxmlPath());
        }
        assertValid("/fxml/dashboard.fxml");
    }

    private void assertValid(String path) throws Exception {
        URL resource = getClass().getResource(path);
        assertNotNull(resource, "Missing FXML resource: " + path);

        Document document;
        try (InputStream in = resource.openStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            document = factory.newDocumentBuilder().parse(in);
        }

        Element root = document.getDocumentElement();
        String controllerName = attribute(root, "controller");
        assertNotNull(controllerName, "FXML has no fx:controller: " + path);

        Class<?> controller = Class.forName(controllerName);
        List<String> problems = new ArrayList<>();

        NodeList elements = document.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);

            String id = attribute(element, "id");
            if (id != null && findField(controller, id) == null) {
                problems.add("fx:id=\"" + id + "\" has no field in " + controller.getSimpleName());
            }

            String action = attribute(element, "onAction");
            if (action != null) {
                String methodName = action.startsWith("#") ? action.substring(1) : action;
                if (findActionMethod(controller, methodName) == null) {
                    problems.add("onAction=\"" + action + "\" has no handler in " + controller.getSimpleName());
                }
            }
        }

        if (!problems.isEmpty()) {
            fail(path + " is inconsistent with " + controllerName + ": " + String.join("; ", problems));
        }
        assertTrue(true);
    }

    private static String attribute(Element element, String localName) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String namespace = attribute.getNamespaceURI();
            if (namespace != null
                    && namespace.startsWith("http://javafx.com/fxml")
                    && localName.equals(attribute.getLocalName())) {
                return attribute.getNodeValue();
            }
        }
        return null;
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // continue with the superclass
            }
        }
        return null;
    }

    private static Method findActionMethod(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() <= 1) {
                    return method;
                }
            }
        }
        return null;
    }
}
