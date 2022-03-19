import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Client
{


    private String username;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket s;
    private InetAddress ip;
    private SecretKey aesKey;
    private SecretKey desKey;
    private IvParameterSpec aesIV;
    private IvParameterSpec desIV;
    private Cipher cipher;
    private String algName = "AES";
    private String modeName = "CBC";
//    public static void main(String[] args) throws IOException
//    {
//        Client client = new Client();
//        client.execute();
//    }

    /* Encrypt == 1, Decrypt == 2*/
    public Cipher setCipher(String algName, String modeName, Integer encryptMode)  {


        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(String.format("%s/%s/PKCS5Padding", algName, modeName));
            System.out.println(algName);
            System.out.println(modeName);

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

    public void connect(){
        try
        {
            // getting localhost ip
            ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            s = new Socket(ip, 5056);

            // obtaining input and out streams
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            dos.writeUTF(String.format("connect/%s", username));

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void close()  {
        try {
            dos.writeUTF("Exit");
            dis.close();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message){
        try {
            dos.writeUTF(String.format("send/%s", message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeEncryptionAlgorithm(String algorithmName){
        try {
            if(algorithmName.equals("AES")){
                this.algName = "AES";
                dos.writeUTF("alg/AES");
            }
            else{
                this.algName = "DES";
                dos.writeUTF("alg/DES");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeEncrpytionMode(String modeName){
        try {
            if(modeName.equals("CBC")){
                this.modeName = "CBC";
                dos.writeUTF("mode/CBC");
            }
            else{
                this.modeName = "OFB";
                dos.writeUTF("mode/OFB");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String encryptMessage(String message){

        cipher = setCipher(this.algName, this.modeName, Cipher.ENCRYPT_MODE);

        String encryptedMessage = "";
        try {
            encryptedMessage = Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }


        return encryptedMessage;
    }

    public String readInput() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            ;
        }
        return null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public DataInputStream getDis() {
        return dis;
    }


    public void setAesKey(byte[] aesKey) {
        System.out.println(aesKey.length);
        this.aesKey = new SecretKeySpec(aesKey, 0, aesKey.length, "AES");
    }

    public void setDesKey(byte[] desKey) {
        System.out.println(desKey.length);
        this.desKey = new SecretKeySpec(desKey, 0, desKey.length, "DES");;
    }

    public void setAesIV(byte[] aesIV) {
        this.aesIV = new IvParameterSpec(aesIV);
    }

    public void setDesIV(byte[] desIV) {
        this.desIV = new IvParameterSpec(desIV);
    }
}