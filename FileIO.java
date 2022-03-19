import java.io.FileWriter;
import java.io.IOException;

public class FileIO {


    public void write(String content){
        try {
            FileWriter myWriter = new FileWriter("log.txt", true);
            myWriter.append(content);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("IO error");
        }
    }



}
