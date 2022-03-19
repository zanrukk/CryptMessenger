import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Base64;

public class CryptoClientMessengerForm extends JFrame {
    private JButton connectButton;
    private JButton disconnectButton;
    private JRadioButton AESRadioButton;
    private JRadioButton DESRadioButton;
    private JRadioButton CBCRadioButton;
    private JRadioButton OFBRadioButton;
    private JTextArea textTextArea;
    private JTextArea cryptedTextTextArea;
    private JButton encryptButton;
    private JButton sendButton;
    private JTextArea chatAreaTextArea;
    private JPanel cryptoPanel;
    private JLabel connectionField;
    private String username = "";
    private Client client = new Client();

    public byte[] parseByteArrayFromText(String text){
        text = text .replace("[", "").replace("]", "");
        String[] byteValues = text.substring(1, text.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];

        for (int i=0, len=bytes.length; i<len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }

        return bytes;
    }

    public void showUsernameUI(){
        while(username.equals("")){
            username = JOptionPane.showInputDialog(this,
                    "Enter user name: ", "");
            updateUsernameUI(username);
        }
    }

    public CryptoClientMessengerForm(){
        /*Init client*/

        add(cryptoPanel);
        setSize(600, 400);
        setTitle("Crypto Messenger");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                encryptButton.setEnabled(true);
                sendButton.setEnabled(true);
                connectionField.setText("Connected");
                showUsernameUI();
                client.connect();

                SocketReader socketReader = new SocketReader(client);
                socketReader.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text = e.getActionCommand();

                        if(text.startsWith("connect/key/AES/")){
                            text = text.replace("connect/key/AES/", "");

                            byte[] byteArr = Base64.getDecoder().decode(text);
                            client.setAesKey(byteArr);
                        }
                        else if(text.startsWith("connect/key/DES/")){
                            text = text.replace("connect/key/DES/", "");

                            byte[] byteArr = Base64.getDecoder().decode(text);


                            client.setDesKey(byteArr);
                        }
                        else if(text.startsWith("connect/IV/AES/")){
                            text = text.replace("connect/IV/AES/", "");

                            byte[] byteArr = Base64.getDecoder().decode(text);


                            client.setAesIV(byteArr);
                        }
                        else if(text.startsWith("connect/IV/DES/")){
                            text = text.replace("connect/IV/DES/", "");

                            byte[] byteArr = Base64.getDecoder().decode(text);

                            client.setDesIV(byteArr);
                        }
                        else if(text.startsWith("message/")){
                            text = text.replace("message/", "");
                            chatAreaTextArea.append(text);
                            chatAreaTextArea.append("\n");
                            chatAreaTextArea.setCaretPosition(chatAreaTextArea.getDocument().getLength());

                        }

                    }
                });
                socketReader.execute();
            }
        });


        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                encryptButton.setEnabled(false);
                sendButton.setEnabled(false);
                connectionField.setText("Not Connected");

                client.close();
                username = "";
                chatAreaTextArea.setText("");
                textTextArea.setText("Text");
                cryptedTextTextArea.setText("Crypted Text");


                //TODO: clean chat

            }
        });


        AESRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.changeEncryptionAlgorithm("AES");
                AESRadioButton.setSelected(true);
                DESRadioButton.setSelected(false);

            }
        });


        DESRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.changeEncryptionAlgorithm("DES");
                AESRadioButton.setSelected(false);
                DESRadioButton.setSelected(true);
            }
        });

        CBCRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.changeEncrpytionMode("CBC");
                CBCRadioButton.setSelected(true);
                OFBRadioButton.setSelected(false);
            }
        });


        OFBRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.changeEncrpytionMode("OFB");
                CBCRadioButton.setSelected(false);
                OFBRadioButton.setSelected(true);
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!cryptedTextTextArea.getText().equals("")){
                    client.sendMessage(cryptedTextTextArea.getText());
                    textTextArea.setText("");
                    cryptedTextTextArea.setText("");
                }


            }
        });
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!textTextArea.getText().equals("")) {
                    cryptedTextTextArea.setText(client.encryptMessage(textTextArea.getText()));
                }
            }
        });

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                e.getWindow().dispose();
                if(disconnectButton.isEnabled()){
                    client.close();
                }

            }
        });

    }

    public void updateUsernameUI(String username){
        setTitle(String.format("Crypto Messenger, %s!", username));
        client.setUsername(username);
    }


}
