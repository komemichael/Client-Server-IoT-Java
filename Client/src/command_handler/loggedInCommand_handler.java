/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package command_handler;

import UI.Logged_In;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    //***************Variables**********************************************************//
    //Program running varables                                                          //
    UI.Logged_In gui;                                                                   //
    int toRun = 0;                                                                      //
    String type = "Logged_In";                                                          //
    client.Client myClient;                                                             //
    String payment_person_to_be_paid;                                                   //
    String payment_person_paying;                                                       //
    String payment_amount;                                                              //
    String payment_date;                                                                //
    String xmlStr = "";                                                                 //
    //***************Variables**********************************************************//
    
    //Constructor
    //***************Constructor*********************************************************//
    public loggedInCommand_handler(UI.Logged_In gui, client.Client myClient)             //
    {                                                                                    //
        this.gui = gui;                                                                  //
        this.myClient = myClient;                                                        //
    }                                                                                    //
    //***************Constructor*********************************************************//
    
    //***************Run method**********************************************************//
    @Override
    public void run()
    {
        switch (toRun)
        {
            case 1: //Payment Command                
                myClient.sendStringMessageToServer(xmlStr);
                break;
            case 2: //Enter bill command   
                myClient.sendStringMessageToServer(xmlStr);
                break;
        }
    }
    //***************Constructor*********************************************************//
    
    public void setPayment_person_to_be_paid(String person)
    {
        this.payment_person_to_be_paid = person;
    }
    
    
    public void hanfleFile(String filename)
    {    
        System.out.println("Remove this method in commandhandler.handleLoginCOmmand.handleFile");
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
    
    //Handle payments 
    //to Edit
    public void handlePaymentCommand(String person_paying, String amount, String date, String person_paid)
    {
        this.payment_amount = amount;
        this.payment_date = date;
        this.payment_person_paying = person_paying;
        this.payment_person_to_be_paid = person_paid;
        
        xmlStr = "...";//create string that does this 
        
        toRun = 1;
        Thread t = new Thread(this);
        t.start();
    }
    
    //Handle Enter bill
    public void handleEnterBillCommand(String billtype, String amount,String date,String item)
    {
        
        //check if client is connected forst
        this.payment_amount = amount;
        this.payment_date = date;
        
        System.out.println(billtype + " " + amount + " " + date + " " + item);
        xmlStr = "<billtype>"+ billtype + "</billtype>\n"+
                "<entry>\n"+
                    "<date>"+ this.payment_date + "</date>\n"+
                    "<buyer>"+ this.payment_person_to_be_paid + "</buyer>\n"+
                    "<amount>$"+ this.payment_amount + "</amount>\n"+
                    "<item>"+ item + "</item>\n"+
                    "<payers> </payers>\n"+
                "</entry>";
        
        
        
        toRun = 2;
        Thread t = new Thread(this);
        t.start();
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
