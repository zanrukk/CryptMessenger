import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SocketReader extends SwingWorker<Void, String> {

    private List<ActionListener> actionListeners;
    private Client client;
    public SocketReader(Client client) {
        actionListeners = new ArrayList<>(25);
        this.client = client;
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    @Override
    protected Void doInBackground(){
        System.out.println("Connected to Server!");


            System.out.println("Before setting text area");

            String serverInput = null;
            try{
                do {
                    System.out.println("Read " + serverInput);

                    // HANDLE INPUT PART HERE
                    serverInput = client.readInput();

                    if (serverInput != null) {
                        publish(serverInput);
                    }

                } while (!serverInput.equals("/close"));
            }
            catch(Exception e){
                System.out.println("Program closed");

            }


        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String text : chunks) {
            ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, text);
            for (ActionListener listener : actionListeners) {
                listener.actionPerformed(evt);
            }
        }
    }

}