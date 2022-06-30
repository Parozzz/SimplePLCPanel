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
    private XmlTools() {}

    public static SVGScrapData svgScrap(URL url)
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

            var svgNode = doc.getDocumentElement(); //This should be the SVG node

            var viewportNode = svgNode.getAttributeNode("viewBox");
            if(viewportNode == null)
            {
                return null;
            }

            var viewportArray = viewportNode.getTextContent().split(" ");
            if(viewportArray.length != 4)
            {
                return null;
            }

            int startX = Util.parseInt(viewportArray[0], Integer.MIN_VALUE);
            int startY = Util.parseInt(viewportArray[1], Integer.MIN_VALUE);
            int endX = Util.parseInt(viewportArray[2], Integer.MIN_VALUE);
            int endY = Util.parseInt(viewportArray[3], Integer.MIN_VALUE);
            if(startX == Integer.MIN_VALUE || startY == Integer.MIN_VALUE || endX == Integer.MIN_VALUE || endY == Integer.MIN_VALUE)
            {
                return null;
            }


            var pathNode = XmlTools.findFirstChild(svgNode, "path");
            if(pathNode == null)
            {
                return null;
            }

            var dAttribute = pathNode.getAttributes().getNamedItem("d");
            if(dAttribute == null)
            {
                return null;
            }

            return new SVGScrapData(dAttribute.getTextContent(), endX - startX, endY - startY);
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

    public static class SVGScrapData
    {
        private final String path;
        private final int width;
        private final int height;
        private SVGScrapData(String path, int width, int height)
        {
            this.path = path;
            this.width = width;
            this.height = height;
        }

        public String getPath()
        {
            return path;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }
    }

}
