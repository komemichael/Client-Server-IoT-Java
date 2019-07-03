/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package komeapp;


/**
 *
 * @author Kome
 */
public class KomeApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        UI.graphical_UI gui = new UI.graphical_UI();
        client.Client myClient = new client.Client(5570, gui);
        command_handler.mainCommand_handler cmd_handler = new command_handler.mainCommand_handler(gui, myClient);
        gui.setCommandHandler(cmd_handler);
        cmd_handler.init_Handler();
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.setVisible(true);
                if (gui.isChanged() == true)
                {
                    System.out.println(gui.isChanged());
                    gui.runChanged();
                }
            }
        });
    }
    
}
