/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package command_handler;

import UI.Logged_In;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Owner
 */
public class loggedInCommand_handler implements Runnable {

    private String command = " ";
    UI.Logged_In gui;
    int toRun = 0;
    String type = "Logged_In";
    clientconnection.ClientConnection myClient_connection;
    
    //Payment command handling variables
    String payment_person;
    String payment_amount;
    String payment_date;
    
    public loggedInCommand_handler(UI.Logged_In gui)
    {
        this.gui = gui;
    }
    
    public void set_Client_Connection(clientconnection.ClientConnection myClient_connection)
    {
        this.myClient_connection = myClient_connection;
    }
    
    
    @Override
    public void run()
    {
        switch (toRun)
        {
            case 1:
                final String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+
                            "<Comment id=\"1\">"
                            + "<person>"+ this.payment_person + "</person>"
                            + "<amount>"+ this.payment_amount + "</amount>\n"
                            + "<date>"+ this.payment_date + "</date>"
                            + "</Comment>";
                
                    try {
                        
                        Document doc = convertStringToDocument(xmlStr);
                        String string = convertDocumentToString(doc);
                        System.out.println(string);
                        gui.update(string, "Logged_In");
                    } catch (IOException ex) {
                        Logger.getLogger(loggedInCommand_handler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                break;
            case 2:
                
                break;
        }
    }
    
    
    public void hanfleFile(String filename)
    {    
        try {
            File f = new File("messages.txt");
            System.out.println(f);
            //if (f.exists())
            //{
                String file_string = "";
                String line;
                FileReader filereader = new FileReader(f);
                BufferedReader bufferedreader = new BufferedReader(filereader);

                while ((line = bufferedreader.readLine()) != null)
                {
                    file_string = file_string + line + "\n";
                }
                gui.setMessage(file_string);
 //           }
//            else
//            {
//                String[] item = filename.split("\\.");
//                gui.setMessage("No " + item[0]);
//            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Logged_In.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Logged_In.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void handlePaymentCommand(String person, String amount, String date)
    {
        this.payment_amount = amount;
        this.payment_date = date;
        this.payment_person = person;
        toRun = 1;
        Thread t = new Thread(this);
        t.start();
    }
    
    public void handle_file_edit(String filename, String messages, String action)
    {
        FileWriter file_writer = null;
        try {
            File file = new File(filename);
            file_writer = new FileWriter(file);
            BufferedWriter buffered_writer = new BufferedWriter(file_writer);
            
            if (action.equals("Write"))
            {
                buffered_writer.write(messages);
            }
            else
                if (action.equals("Append"))
                {
                    buffered_writer.append(messages);
                }
        } catch (IOException ex) {
            Logger.getLogger(loggedInCommand_handler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                file_writer.close();
            } catch (IOException ex) {
                Logger.getLogger(loggedInCommand_handler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void sqlthing()
    {
        try 
            (
                 // Step 1: Allocate a database 'Connection' object
                 Connection conn = DriverManager.getConnection(
                       "jdbc:mysql://localhost:3306/ebookshop?useSSL=false", "myuser", "xxxx");
                       // MySQL: "jdbc:mysql://hostname:port/databaseName", "username", "password"

                 // Step 2: Allocate a 'Statement' object in the Connection
                 Statement stmt = conn.createStatement();
            )
        {
            String query = "select * from database";
            ResultSet rs = stmt.executeQuery(query);
            
        } catch (SQLException e) {
            
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
