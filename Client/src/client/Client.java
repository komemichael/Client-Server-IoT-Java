/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.*;
import java.net.*;

public class Client implements Runnable{

    String type = "graphical_UI";
    String server_Message = "";
    InputStream input;
    OutputStream output;
    
    FileOutputStream file_output_stream;
    FileInputStream file_input_stream;
    String filename;
    
    byte msg = ' ';
    int toRun = 0;
    servermessagehandler.ServerMesasgeHandler myServerCommandHandler;
    ServerSocket serverSocket = null;
    Socket MyClient = null;
    int portNumber;
    boolean stopThisThread = true;
    interfaces.userinterface myUI;
    private StringBuffer sb = new StringBuffer();
    
    public Client(int portNumber, interfaces.userinterface myUI)
    {
        this.portNumber = portNumber;
        this.myUI = myUI;
        this.myServerCommandHandler = new servermessagehandler.ServerMesasgeHandler(this);
    }
    
    public void connectToServer()
    {
        try
        {
                MyClient = new Socket("localhost", 5570);
                //MyClient = new Socket("192.168.1.17", 7777);
                input = MyClient.getInputStream();
                output = MyClient.getOutputStream();
                Thread thisClientThread = new Thread(this);
                thisClientThread.start();
                stopThisThread = false;
        }
        catch (IOException e)
        {
            this.sendMessageToUI("client.Client.connectToServer \n"
                    + "Not Connected to Server...No server online");
        }
        finally{}
    }
    
    public void sendFileToServer(File file)
    {
        try {
            byte[] bytes = new byte[(int) file.length()];
            file_input_stream = new FileInputStream(file);
            BufferedInputStream buff_input_stream;
            buff_input_stream = new BufferedInputStream(file_input_stream);
            buff_input_stream.read(bytes,0,bytes.length);
            output.write(bytes, 0, bytes.length);
            output.flush();
            buff_input_stream.close();
        } catch (IOException e) {
            this.sendMessageToUI("client.Client.sendFileToServer \n"
                    + "cannot send File to socket; exiting program.");
            System.exit(1);
        } finally {
        }
    }
    
    public void sendStringMessageToServer(String theMessage) {
        for (int i = 0; i < theMessage.length(); i++) {
            byte msg = (byte) theMessage.charAt(i);           
            sendMessageToServer(msg);
        }
        sendMessageToServer((byte) 255);
    }
    
    
    public void sendMessageToServer(byte msg)
    {
        try
        {
            output.write(msg);
            output.flush();
        }
        catch(IOException e)
        {
            System.err.println("Cleint.client.sendMessageToServer"
                    + "Cannot send to Socket, Exiting!!");
            System.exit(1);
        }
        finally
        {}
    }
    
    public boolean isConnected()
    {   
        boolean isConnected = true;
        if(stopThisThread == true)
        {
            return false;
        }
        return isConnected;
        
    }
    
    public void stopThread()
    {
        stopThisThread = true;
        try {
            MyClient.close();
            MyClient = null;
            input = null;
            output = null;
        } catch (IOException ex) {
            System.out.println("client.Client.stopThread : \n" + ex);
        }
        
    }
    
    public void set_Filename(String filename)
    {
        this.filename = filename;
    }
    
    public void setPort(int portNumber) {
        this.portNumber = portNumber;
    }

    public int getPort() {
        return this.portNumber;
    }
    
    public void sendMessageToUI(String theString) {
        myUI.update(theString, type);
    }
    
    private String byteToString(byte theByte) {
        byte[] theByteArray = new byte[1];
        String theString = null;
        theByteArray[0] = theByte;
        try {
            theString = new String(theByteArray, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            this.sendMessageToUI("client.Client.byteToString \n"
                    + "Cannot convert from UTF-8 to String; exiting program.");
            System.exit(1);
        } finally {
            return theString;
        }
    }
    
    public void set_toRun(int run)
    {
        this.toRun = run;
    }    
     
    @Override
    public void run()
    {
        while (stopThisThread == false) {
            switch (toRun)
            {
                case 0: 
                    try {
                        msg = (byte) input.read();
                        server_Message = byteToString(msg);
                        myServerCommandHandler.handleServerMessage(this, server_Message); 
                    } catch (IOException e) {
                        myServerCommandHandler.handleServerMessage("IOException: " +e.toString() +
                            ". Stopping thread and disconnectings client: "
                                    + "client.Client.run");
                        stopThisThread = true;
                    }
                    break;
                case 1:
                    try {
                        byte[] bytes = new byte[6022386];
                        file_output_stream = new FileOutputStream(filename);
                        BufferedOutputStream buff_out_stream = new BufferedOutputStream(file_output_stream);   
                        int bytesRead = input.read(bytes,0,bytes.length); 
                        byte[] newbytes = new byte[bytesRead + 1];
                        newbytes[0] = 60;
                        System.arraycopy(bytes, 0, newbytes, 1, bytesRead);
                        buff_out_stream.write(newbytes, 0 , bytesRead);
                        buff_out_stream.flush();
                    } catch (IOException ex) {
                        System.out.println("cant send \n");
                    }
                    set_toRun(0);
                    break;  
            }
        }
    }
}

