/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servermessagehandler;

import java.io.File;


/**
 *
 * @author Kome Michael
 */
public class ServerMesasgeHandler implements Runnable{
    
    client.Client myClient;
    String the_Command = "";
    String filename = "";
    
    public ServerMesasgeHandler(client.Client myClient)
    {
        this.myClient = myClient;
    }
    
    
     public void handleServerMessage(client.Client myClient,String msg) {
        if (msg.charAt(0)!= 0xFFFD) { //0xFFFD = UTF-8 encoding of 0xFF
            the_Command += msg;
        } else {
            handleCompleteServerMessage(myClient, the_Command);
            the_Command = "";
        }
    }

     
    public void handleCompleteServerMessage(client.Client myClient, String the_Command)
    {
        Thread t = new Thread(this);
        t.start();
    }
    
    @Override
    public void run()
    {
        String command; 
        if ((the_Command.contains("<")) && (the_Command.contains(">")))
        {
            command = "xmlcommand";
        }
        else
        {
            command = the_Command;
        }
        
        switch (command)
        {
            case "":
                
                break;
            case "file_ack":
                File file = new File(filename);
                myClient.sendFileToServer(file);
                break;
            case "RFT":
                myClient.set_Filename(this.the_Command);
                myClient.set_toRun(1);
                myClient.sendStringMessageToServer("file_ack");
                break;
            case "xmlcommand":
                myClient.sendMessageToUI(the_Command);
                break;
        }
    }

    public void handleServerMessage(String message) {
        myClient.sendMessageToUI(message);
    }
}
