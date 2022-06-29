package parozzz.github.com.simpleplcpanel.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class XmlTools
{
    public static String svgScrap(URL url)
    {
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try
        {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(url.toExternalForm());

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            var documentElement = doc.getDocumentElement(); //This should be the SVG node

            var pathNode = XmlTools.findFirstChild(documentElement, "path");
            if(pathNode == null)
            {
                return null;
            }

            var dAttribute = pathNode.getAttributes().getNamedItem("d");
            if(dAttribute == null)
            {
                return null;
            }

            return dAttribute.getTextContent();
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            MainLogger.getInstance().error("Error during svg scrap", e, url);
        }

        return null;
    }

    public static Node findFirstChild(Node masterNode, String childName)
    {
        var children = findAllChildren(masterNode, childName);
        return children.size() != 0 ? children.get(0) : null;
    }

    public static List<Node> findAllChildren(Node masterNode, String childName)
    {
        var masterChildren = masterNode.getChildNodes();

        var nodeList = new ArrayList<Node>();
        for(int x = 0; x < masterChildren.getLength(); x++)
        {
            var child = masterChildren.item(x);
            if(child.getNodeName().equals(childName))
            {
                nodeList.add(child);
            }
        }
        return nodeList;
    }


    private XmlTools() {}
}
