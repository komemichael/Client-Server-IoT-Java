/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package command_handler;

import clientconnection.ClientConnection;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Kome
 */
public class ClientMessageHandler{

    server.Server myServer;
    String theCommand = "";
    boolean isMessage = false;
    String person;
    String filename = "";

    public ClientMessageHandler(server.Server myServer) {
        this.myServer = myServer;
    }
    

    public void handleClientMessage(clientconnection.ClientConnection myClientConnection, String msg) {
         if (msg.charAt(0)!= 0xFFFD) { //0xFFFD = UTF-8 encoding of 0xFF
            theCommand += msg;
        } else {
            handleCompleteClientMessage(myClientConnection, theCommand);
            theCommand = "";
        }
    }


    public void handleClientMessage(String theExceptionalEvent) {
        myServer.sendMessageToUI(theExceptionalEvent);
    }
    

    public void handleCompleteClientMessage(clientconnection.ClientConnection myClientConnection, String theCommand) {
        String command = ""; 
        
        
        if ((theCommand.contains("<")) && (theCommand.contains(">")))
        {
            command = "xmlCommandNewEntry";
        }
        else if (theCommand.contains("..."))
        {
            System.out.println("...");
        }
        else
        {
            command = theCommand;
        }
        
        System.out.println("Command = "+command);
        
        
        switch (command) {
            case "update":
                myServer.sendMessageToUI("Files Updating...", "graphical_UI");
                this.filename = "files" + File.separator + "init_main_gui.xml";
                myClientConnection.sendStringMessageToClient(filename); 
                break;
            case "person":
                break;
            case "client_init":
                this.filename = "files" + File.separator + "init_main_gui.xml";
                myClientConnection.sendStringMessageToClient(filename);  
                break;
            case "file_ack":
                File file = new File(filename);
                myClientConnection.sendFileToClient(file);
                break;
            case "RFT":
                myClientConnection.set_toRun(1);
                myClientConnection.sendStringMessageToClient("file_ack");
                break;
            case "xmlCommandNewEntry":
                try {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse("files"+ File.separator + "loggedin.xml");
                    Node utiltype;
                    
                    if (theCommand.contains("Utility Bill"))
                    {
                        System.out.println("Utility processing..."); 
                        utiltype = (Node) doc.getElementsByTagName("nonutil").item(0);
                        
                    }
                    else
                    {
                        System.out.println("Utility processing..."); 
                        utiltype = (Node) doc.getElementsByTagName("util").item(0);
                    }
                    
                    System.out.println(theCommand);
                    
                    // fix here
                    //append the xml tag and them get node and append node to this node
                    Element newentry = doc.createElement("entry");
                    Document Command = convertStringToDocument(theCommand);
                    newentry.appendChild(Command);

                    Attr attr = doc.createAttribute("id");
                    int length = utiltype.getChildNodes().getLength();
                    attr.setValue("" + (length + 1));
                    newentry.setAttributeNode(attr);

                    utiltype.appendChild(newentry);

                    // write the content into xml file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File("files"+ File.separator + "loggedin.xml"));
                    transformer.transform(source, result);  
                    
                    StreamResult consoleResult = new StreamResult(System.out);
                    transformer.transform(source, consoleResult);
                } catch (ParserConfigurationException | SAXException |IOException | TransformerException ex) {
                    Logger.getLogger(ClientMessageHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;    
        }
    }
    
    private String byteToString(byte theByte) {
        byte[] theByteArray = new byte[1];
        String theString = null;
        theByteArray[0] = theByte;
        try {
            theString = new String(theByteArray, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            myServer.sendMessageToUI("Cannot convert from UTF-8 to String; exiting program.");
            System.exit(1);
        } finally {
            return theString;
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
}
