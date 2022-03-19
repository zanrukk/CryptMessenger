import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

// Server class
public class Server
{

    private Set<ClientHandler> clientThreads = new HashSet<ClientHandler>();
    private Set<String> usernames = new HashSet<>();

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Server server = new Server();

        server.execute();
    }

    public void execute() throws IOException, NoSuchAlgorithmException {
        // server is listening on port 5056
        ServerSocket ss = new ServerSocket(5056);

        SecureRandom srandom = new SecureRandom();

        KeyGenerator keyGeneratorAES = KeyGenerator.getInstance("AES");
        KeyGenerator keyGeneratorDES = KeyGenerator.getInstance("DES");
        SecretKey keyAES = keyGeneratorAES.generateKey();
        SecretKey keyDES = keyGeneratorDES.generateKey();

        byte[] initIV = new byte[16];
        srandom.nextBytes(initIV);
        IvParameterSpec aesSpec = new IvParameterSpec(initIV);
        byte[] initIV2 = new byte[8];
        srandom.nextBytes(initIV2);
        IvParameterSpec desSpec = new IvParameterSpec(initIV2);

        FileIO fileIO = new FileIO();
        fileIO.write(String.format("AES KEY: %s\n", Base64.getEncoder().encodeToString(keyAES.getEncoded())));
        fileIO.write(String.format("DES KEY: %s\n", Base64.getEncoder().encodeToString(keyDES.getEncoded())));
        fileIO.write(String.format("AES IV: %s\n", Base64.getEncoder().encodeToString(initIV)));
        fileIO.write(String.format("DES IV: %s\n", Base64.getEncoder().encodeToString(initIV2)));
        System.out.println(String.format("AES KEY: %s\n", Base64.getEncoder().encodeToString(keyAES.getEncoded())));
        System.out.println(String.format("DES KEY: %s\n", Base64.getEncoder().encodeToString(keyDES.getEncoded())));
        System.out.println(String.format("AES IV: %s\n", Base64.getEncoder().encodeToString(initIV)));
        System.out.println(String.format("DES IV: %s\n", Base64.getEncoder().encodeToString(initIV2)));

        // running infinite loop for getting
        // client request
        while (true)
        {
            Socket s = null;

            try
            {
                // socket object to receive incoming client requests
                s = ss.accept();

//                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());

                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

//                System.out.println("Assigning new thread for this client");

                // create a new thread object
                ClientHandler t = new ClientHandler(s, dis, dos, keyAES, keyDES, aesSpec, desSpec, this, fileIO);
                clientThreads.add(t);
                // Invoking the start() method
                t.setDaemon(true);
                t.start();

            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }

    void broadcast(String message) throws IOException {
//        System.out.println(clientThreads);
        for (ClientHandler aUser : clientThreads) {
            aUser.sendMessage(message);

        }
    }

    void addUserName(String userName) {
        usernames.add(userName);
    }

    /**
     * When a client is disconneted, removes the associated username and UserThread
     */
    void removeUser(String userName, ClientHandler aUser) {

        boolean removed = usernames.remove(userName);
        if (removed) {
            clientThreads.remove(aUser);
//            System.out.println("The user " + userName + " quitted");
        }
    }

}