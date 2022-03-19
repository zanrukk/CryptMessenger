import javax.swing.*;

public class CryptMessenger {
    CryptoClientMessengerForm cryptoClientMessengerForm;
    String username = "";

    public static void main(String[] args){
        CryptMessenger messenger = new  CryptMessenger();
        messenger.runUI();
    }
    public void runUI(){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {


                    cryptoClientMessengerForm = new CryptoClientMessengerForm();
                    cryptoClientMessengerForm.setVisible(true);


                }
            });

    }

    public CryptoClientMessengerForm getCryptoClientMessengerForm() {
        return cryptoClientMessengerForm;
    }

    public void setCryptoClientMessengerForm(CryptoClientMessengerForm cryptoClientMessengerForm) {
        this.cryptoClientMessengerForm = cryptoClientMessengerForm;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
