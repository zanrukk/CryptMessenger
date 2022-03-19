import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;

public class ClientHandler extends Thread
{
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    final SecretKey aesKey;
    final SecretKey desKey;
    private Cipher cipher;

    final IvParameterSpec aesIV;
    final IvParameterSpec desIV;
    final Server server;
    private String username;
    private FileIO fileIO;


    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, SecretKey aesKey, SecretKey desKey, IvParameterSpec aesIV, IvParameterSpec desIV, Server server, FileIO fileIO) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.aesKey = aesKey;
        this.desKey = desKey;
        this.aesIV = aesIV;
        this.desIV = desIV;
        this.server = server;
        this.fileIO = fileIO;
    }

    /* Encrypt == 1, Decrypt == 2*/
    public Cipher setCipher(String algName, String modeName, Integer encryptMode)  {


        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(String.format("%s/%s/PKCS5Padding", algName, modeName));
            if(algName.equals("AES")){
                cipher.init(encryptMode, aesKey, aesIV);
            }else{
                cipher.init(encryptMode, desKey, desIV);
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        return cipher;
    }


    @Override
    public void run()
    {
        String received;
        String algName = "AES";
        String modeName = "CBC";

        while (true)
        {
            try {

                received = dis.readUTF();
                if(received.equals("Exit"))
                {
//                    System.out.println("Client " + this.s + " sends exit...");
//                    System.out.println("Closing this connection.");
                    this.s.close();
//                    System.out.println("Connection closed");
                    break;
                }
                if(received.startsWith("connect/")){
                    received = received.replace("connect/", "");
                    this.username = received;
                    server.addUserName(username);

                    dos.writeUTF(String.format("connect/key/AES/%s", Base64.getEncoder().encodeToString(this.aesKey.getEncoded())));
                    dos.writeUTF(String.format("connect/key/DES/%s", Base64.getEncoder().encodeToString(this.desKey.getEncoded())));
                    dos.writeUTF(String.format("connect/IV/AES/%s", Base64.getEncoder().encodeToString(this.aesIV.getIV())));
                    dos.writeUTF(String.format("connect/IV/DES/%s", Base64.getEncoder().encodeToString(this.desIV.getIV())));
                }
                if(received.startsWith("alg/AES")){
                    algName = "AES";
                }
                else if(received.startsWith("alg/DES")){
                    algName = "DES";
                }
                else if(received.startsWith("mode/CBC")){
                    modeName = "CBC";
                }
                else if(received.startsWith("mode/OFB")){
                    modeName = "OFB";
                }
                else if(received.startsWith("send/")){
                    received = received.replace("send/", "");
                    server.broadcast(String.format("encrypt/%s".format(received)));
                    fileIO.write(String.format("MESSAGE SENT by %s: %s\n", username,received));
                    System.out.println(String.format("MESSAGE SENT by %s: %s\n", username,received));
                    cipher = setCipher(algName, modeName, Cipher.DECRYPT_MODE);


                    String decryptedMessage = new String(cipher.doFinal(Base64.getDecoder().decode(received)));
                    String message = String.format("%s> %s", username, decryptedMessage);

                    server.broadcast(message);
                }
//                else if(received.startsWith("encrypt/")){
//                    received = received.replace("encrypt/", "");
//
//                    cipher = setCipher(algName, modeName, Cipher.ENCRYPT_MODE);
//                    String encryptedMessage = Base64.getEncoder().encodeToString(cipher.doFinal(received.getBytes(StandardCharsets.UTF_8)));
//
//
//                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }

        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();
            server.removeUser(username, this);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    void sendMessage(String message) throws IOException {
        this.dos.writeUTF(String.format("message/%s", message));
    }
}