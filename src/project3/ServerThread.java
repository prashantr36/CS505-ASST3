package project3;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

public class ServerThread implements Runnable {

    private static final int KEY_SIZE = 120;
    private static final int BUFFER_SIZE = 1024;

    private Socket socket;
    private Map<String, Socket> map;
    private byte[] commandKey;
    private String key;
    private InputStream in;
    private String filename;

    public ServerThread(Socket socket) {
        this.socket = socket;
        commandKey = new byte[KEY_SIZE];
    }

    @Override
    public void run() {
    	
    	// read command key
        try {
            in = socket.getInputStream();
            in.read(commandKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        key = new String(Arrays.copyOfRange(commandKey, 0, commandKey.length));
        key = key.trim();
        this.filename = key;
        System.out.println(" This filename " + this.filename);
        try {
			forwardData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void forwardData() throws IOException {
    	// connect to server
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream fin = new FileInputStream(filename);

        // transfer command and key
        OutputStream out = socket.getOutputStream();
        
        // upload file
        System.out.println("ready to upload " + filename);
        try {
            int len = fin.read(buffer);
            while(len > 0) {
                out.write(buffer, 0, len);
                len = fin.read(buffer);
            }
            fin.close();
            socket.close();
            System.out.println("finished uploading " + filename);
        } catch (Exception e) {
            System.out.println("uploader socket exception");
            fin.close();
        }
    }

}