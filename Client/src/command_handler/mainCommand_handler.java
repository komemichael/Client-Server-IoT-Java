/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package command_handler;

import UI.graphical_UI_login;
import java.io.File;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kome
 */
public class mainCommand_handler implements Runnable{
    
    // Variables for the GUI in general
    UI.graphical_UI gui;
    client.Client myClient;
    private int toRun = 0;
    String type = "graphical_UI";
    
    // Variables for the commenting feature of the GUI
    String comment_person;
    boolean comment_isPublic;
    String comment_comment;
    
    //Constructor 
    public mainCommand_handler(UI.graphical_UI gui, client.Client myClient)
    {
        this.gui = gui;
        this.myClient = myClient;
    }
    
    public void init_Handler()
    {
        Thread t = new Thread(this);
        toRun = 1;
        t.start();
    }
    
    public void init()
    {
        try {
            //initialize Connection
            myClient.connectToServer();
            if (myClient.isConnected())
            {
                gui.update("Connected to Server", type);
                //gui.setColor("connect_btn", "green");
                //gui.setColor("disconnect_btn", "red");
                myClient.sendStringMessageToServer("client_init");
            }
            else
            {
                gui.update("Not connected to Server...No server online!", type);
                //gui.setColor("connect_btn", "red");
                //gui.setColor("disconnect_btn", "green");
            }
            
            //initialize FIle
            {
                File xmlfile = new File("files" + File.separator + "init_main_gui.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlfile);
                doc.getDocumentElement().normalize();

                //List of what to updates in the xml file
                String[] loop = {"ivcfevents", "ivcfprayer", "houseinfo"};
                String ivcfevents = "", ivcfprayer = "", houseinfo = "";
                
                for (String loop1 : loop) {
                    NodeList ivcfevent = doc.getElementsByTagName(loop1);
                     for (int j = 0; j < ivcfevent.getLength(); j++) {
                        Node node = ivcfevent.item(j);
                        if (node.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element eElement = (Element) node;
                            if (loop1.equals("ivcfevents"))
                            {
                                ivcfevents += eElement.getTextContent() + "\n";
                            }
                            if (loop1.equals("ivcfprayer"))
                            {
                                ivcfprayer += eElement.getTextContent() + "\n";
                            }
                            if (loop1.equals("houseinfo"))
                            {
                                houseinfo += eElement.getTextContent() + "\n";
                            }
                        }
                    }
                }
                
                gui.setInit(ivcfevents, ivcfprayer, houseinfo);
            }
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            System.out.println(ex + "Continue debugging everything good...");
        }
    }
    
    @Override
    public void run()
    {
 //       try {
            switch (toRun)
            {
                case 1: //inititalization
                    init();
                    break;
                case 2: //Disconnect from Server
                    myClient.stopThread();
                    gui.update("Disconnected from Server", type);
                case 3: //Send Xml to server
                    
//                    final String xmlStr = "";
//                    Document doc = convertStringToDocument(xmlStr);
//                    
//                    // TO_DO: Send this to the server which processes the message
//                    String string = convertDocumentToString(doc);
//                    System.out.println(string);
//                    gui.update(string, "graphical_UI");
//                    myClient.sendStringMessageToServer(string);
                    break;
                case 4: //Update client files
                    myClient.sendStringMessageToServer("update");
                    myClient.sendMessageToUI("Updating Files...");
                    //Write code for if file is updated, then refresh automatically
                    break;
            }
            toRun = 0;
//        } catch (IOException ex) {
//            Logger.getLogger(mainCommand_handler.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public void handleLogInbtn()
    {
       UI.graphical_UI_login login_UI = new graphical_UI_login(gui, false, myClient);
       login_UI.setVisible(true);
    }
    
    public void update_files()
    {
        toRun = 4;
        Thread t = new Thread(this);
        t.start();
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
    
    public void handleCommand(String command)
    {
        Thread t = new Thread (this);
        switch (command)
        {
            case "connect":
                toRun = 1;
                t.start();
                break;
            case "disconnect":
                toRun = 2;
                t.start();
                break;
        }
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
