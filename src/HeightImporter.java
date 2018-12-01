import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class HeightImporter {
    static void loadheight() {
        try {
            File inputFile = new File("map.osm");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());


            //Add way tag to list
            NodeList nList = doc.getElementsByTagName("way");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());//it says way

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    NodeList bList = eElement.getElementsByTagName("nd");

                    for (int count = 0; count < bList.getLength(); count++) {
                        Node node1 = bList.item(count);
                        if (node1.getNodeType() == node1.ELEMENT_NODE) {
                            Element buildingNode = (Element) node1;
                            System.out.println("Node Used : " + buildingNode.getAttribute("ref"));
                        }
                    }
                    NodeList bnList = eElement.getElementsByTagName("tag");
                    for (int i = 0; i < bnList.getLength(); i++) {
                        Node node2 = bnList.item(i);

                        if (node2.getNodeType() == node2.ELEMENT_NODE) {

                            Element buildingNode = (Element) node2;
                            Element Height = doc.createElement("height");

                            System.out.println("Attribute: " + buildingNode.getAttribute("k") + ":" + buildingNode.getAttribute("v"));

                            if ("building".equals(buildingNode.getAttribute("k"))) {
                                Element tag = doc.createElement("tag");
                                Attr height = doc.createAttribute("k");
                                height.setValue("height");
                                Attr heightvalue = doc.createAttribute("v");
                                heightvalue.setValue("30");
                                tag.setAttributeNode(height);
                                tag.setAttributeNode(heightvalue);
                                eElement.appendChild(tag);
                            }
                        }
                    }
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("E:\\map.xml"));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadnode() {
        //The XML parser
        try {
            File inputFile = new File("map.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            //add node tag to list
            NodeList nList = doc.getElementsByTagName("node");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    System.out.println("Node ID : " + eElement.getAttribute("id"));
                    System.out.println("Latitude : " + eElement.getAttribute("lat"));
                    System.out.println("Longitude : " + eElement.getAttribute("lon"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

