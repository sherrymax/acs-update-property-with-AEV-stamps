package org.alfresco.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;

public class QueryXMLAttributes {

    private static Log logger = LogFactory.getLog(QueryXMLAttributes.class);

    public ArrayList < String > getSubjectFromStamp(InputStream inputStream) {

        ArrayList < String > stampSubjectList = new ArrayList < >();

        try {
            //Creating a DocumentBuilder Object
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            //Parsing the XML Document
            Document doc = dBuilder.parse(inputStream);
            System.out.println(">>>>>> STAMP COUNT FROM GET() CALL <<<<<<<< " + doc.getElementsByTagName("stamp").getLength());

            //extracting stamp subjects
            int count = 0;
            NodeList nList = doc.getElementsByTagName("stamp");
            for (int i = 0; i < nList.getLength(); i++) {
                Element ele = (Element) nList.item(i);
                String stampSubject = ele.getAttribute("subject");
                System.out.println("STAMP Subject >>> " + stampSubject);
                stampSubjectList.add(stampSubject);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return stampSubjectList;
    }
}