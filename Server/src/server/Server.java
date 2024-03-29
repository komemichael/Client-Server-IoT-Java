package server;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable{

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    command_handler.ClientMessageHandler myClientCommandHandler;
    interfaces.userinterface myUI;
    int portNumber = 5570; int backlog = 500;
    boolean doListen = false;
    clientconnection.ClientConnection serverCC;
    

    public Server(int portNumber, int backlog, interfaces.userinterface myUI) {
        this.portNumber = portNumber;
        this.backlog = backlog;
        this.myUI = myUI;
        this.myClientCommandHandler = new command_handler.ClientMessageHandler(this);
     }

    public synchronized void setDoListen(boolean doListen){
        this.doListen = doListen;
    }
    public void startServer() {
        if (serverSocket != null) {
            stopServer();
        } else {
            try {
                serverSocket = new ServerSocket(portNumber, backlog);
            } catch (IOException e) {
                sendMessageToUI("Cannot create ServerSocket, because " + e +". Exiting program.", "graphical_UI");
                System.exit(1);
            } finally {
            }
        }
    }

    public void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                sendMessageToUI("Cannot close ServerSocket, because " + e +". Exiting program.", "graphical_UI");
                System.exit(1);
            } finally {
            }

        }
    }

    public void listen() {
        try {
            setDoListen(true);
            serverSocket.setSoTimeout(500);
            Thread myListenerThread = new Thread(this);  // Calling run method for itself
            myListenerThread.start();                    // Start Server run method
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopListening() {
        setDoListen(false);
    }

    @Override
    public void run() {
        while (true) {
            if (doListen == true) {
                try {
                    clientSocket = serverSocket.accept();                 // Added this method for Proxy Client
                    clientconnection.ClientConnection myCC = new clientconnection.ClientConnection(clientSocket, myClientCommandHandler, this);
                    setCC(myCC);
                    Thread myCCthread = new Thread(myCC);      // Start server client Connection between Android and proxy
                    myCCthread.start();
                    sendMessageToUI("Client connected:\n\tRemote Socket Address = " + clientSocket.getRemoteSocketAddress() + "\n\tLocal Socket Address = " + clientSocket.getLocalSocketAddress(), "graphical_UI");
                } catch (IOException e) {
                    //check doListen.
                } finally {
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {}
            }
        }
    }
    
    
    public void setCC(clientconnection.ClientConnection cc)
    {
        this.serverCC = cc;
    }
    
    public clientconnection.ClientConnection getCC()
    {
        return this.serverCC;
    }

    public void setPort(int portNumber) {
        this.portNumber = portNumber;
    }

    public int getPort() {
        return this.portNumber;
    }
    
    public void sendMessageToUI(String theString, String type) {
        myUI.update(theString, type);
    }
    
    public void sendMessageToUI(String theString) {
        myUI.update(theString, "graphical_UI");
    }
}