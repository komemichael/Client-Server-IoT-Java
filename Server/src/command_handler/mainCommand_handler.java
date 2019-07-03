/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package command_handler;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Kome
 */
public final class mainCommand_handler implements Runnable{
    
    // Variables for the GUI in general
    UI.graphical_UI gui;
    server.Server myServer;
    private int toRun = 0;
    
    // Variables for the commenting feature of the GUI
    String comment_person;
    boolean comment_isPublic;
    String comment_comment;
    
    //Constructor 
    public mainCommand_handler(UI.graphical_UI gui, server.Server myServer)
    {
        this.gui = gui;
        this.myServer = myServer;
    }
    
    public void initHandler()
    {
        Thread t  = new Thread(this);
        this.toRun = 1;
        t.start();
    }
    
    @Override
    public void run()
    {
        try {
            switch (toRun)
            {
                case 1:
                    this.myServer.startServer();
                    this.myServer.listen();
                    myServer.sendMessageToUI("Server Started and now listening");
                    break;
                case 2:
                    System.out.println();
                    final String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+
                            "<Comment id=\"1\">"
                            + "<person>"+ this.comment_person + "</person>"
                            + "<isPublic>"+ this.comment_isPublic + "</isPublic>\n"
                            + "<comment>"+ this.comment_comment + "</comment>"
                            + "</Comment>";
                    Document doc = convertStringToDocument(xmlStr);
                    
                    // TO_DO: Send this to the server which processes the message
                    String string = convertDocumentToString(doc);
                    System.out.println(string);
                    gui.update(string, "graphical_UI");
                    
                    break;
            }
            toRun = 0;
        } catch (IOException ex) {
            Logger.getLogger(mainCommand_handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void handleCommentCommand(String person, boolean isPublic, String comment)
    {
        this.comment_person = person;
        this.comment_isPublic = isPublic;
        this.comment_comment = comment;
        toRun = 2;
        Thread t = new Thread(this);
        t.start();
    }
    
    
    private static Document convertStringToDocument(String xmlStr) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try  
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse(new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (ParserConfigurationException e) { 
            System.out.println(e);
        } catch (SAXException ex) { 
            Logger.getLogger(mainCommand_handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static String convertDocumentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource((Node) doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
            System.out.println(e);
        }
        
        return null;
    }
}
