package nl.xs4all.pvbemmel.letour;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.output.*;
import org.xml.sax.*;

public class DocumentStuff {

  // http://stackoverflow.com/questions/2325388/java-shortest-way-to-pretty-\
  // print-to-stdout-a-org-w3c-dom-document
  public static void printDocument(org.w3c.dom.Document doc, OutputStream out)
      throws IOException, TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    // transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
        "4");

    // transformer.transform(new DOMSource(doc),
    // new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    transformer.transform(new DOMSource(doc), new StreamResult(
        new OutputStreamWriter(out, "iso-8859-1")));
  }
  public static void printDocument(Document doc, OutputStream out)
      throws JDOMException, IOException, TransformerException {
    org.w3c.dom.Document doc3c = new DOMOutputter().output(doc);
    printDocument(doc3c, out);
  }
  public static Document readDocument(InputStream is)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory domfactory = DocumentBuilderFactory.newInstance();
    domfactory.setNamespaceAware(true);
    DocumentBuilder dombuilder = domfactory.newDocumentBuilder();
    org.w3c.dom.Document doc3c = dombuilder.parse(is);
    Document doc = new DOMBuilder().build(doc3c);
    return doc;
  }
}
