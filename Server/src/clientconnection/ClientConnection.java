/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientconnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kome
 */
public class ClientConnection implements Runnable {

    InputStream input;
    OutputStream output;
    String filename = "";
            
    FileOutputStream file_output_stream;
    FileInputStream file_input_stream;
    
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    command_handler.ClientMessageHandler myClientCommandHandler;
    command_handler.loggedInCommand_handler loggedIn_command_handler;
    server.Server myServer;
    boolean stopThisThread = false;
    String theString = "";
    int toRun = 0;

    public ClientConnection(Socket clientSocket, command_handler.ClientMessageHandler myClientCommandHandler, server.Server myServer) {
        this.clientSocket = clientSocket;
        this.myClientCommandHandler = myClientCommandHandler;
        this.myServer = myServer;
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();
            
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            myServer.sendMessageToUI("Cannot create IO streams; exiting program.");
            System.exit(1);
        }
    }
    
    public void set_toRun(int run)
    {
        this.toRun = run;
    }

    @Override
    public void run() {
        byte msg;
        
        String theClientMessage;
        while (stopThisThread == false) { 
            switch (toRun)
            {
                case 0:
                    try {
                        msg = (byte) input.read();
                        theClientMessage = byteToString(msg);
                        myClientCommandHandler.handleClientMessage(this, theClientMessage); 
                    } catch (IOException e) {
                        myClientCommandHandler.handleClientMessage("IOException: "
                        + e.toString()
                        + ". Stopping thread and disconnecting client: "
                        + clientSocket.getRemoteSocketAddress());
                        disconnectClient();
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
    
    public void setFileName(String filename)
    {
        this.filename = filename;
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

    public void sendMessageToClient(byte msg) {
        try {
            output.write(msg);
            output.flush();
        } catch (IOException e) {
            myServer.sendMessageToUI("cannot send to socket; exiting program.");
            System.exit(1);
        } finally {
        }
    }
    
    public void sendFileToClient(File file) { 
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
            myServer.sendMessageToUI("cannot send to socket; exiting program.");
            System.exit(1);
        } finally { 
        }
    }


    public void sendStringMessageToClient(String theMessage) {
        for (int i = 0; i < theMessage.length(); i++) {
            byte msg = (byte) theMessage.charAt(i);
            sendMessageToClient(msg);
        }
        sendMessageToClient((byte) 255);
    }

    public void clientQuit() {
        disconnectClient();
    }

    public void clientDisconnect() {
        disconnectClient();
    }

    public void disconnectClient() {
        try {
            stopThisThread = true;
            clientSocket.close();
            clientSocket = null;
            input = null;
            output = null;
            
        } catch (IOException e) {
            myServer.sendMessageToUI("cannot close client socket; exiting program.");
            System.exit(1);
        } finally {
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
