package project3.pa1;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

import project3.pa3.FileRepository;
import project3.pa3.FileRepository.FileUnFoundException;

public class ServerThread implements Runnable {

    private static final int KEY_SIZE = 120;
    private static final int BUFFER_SIZE = 1024;

    private Socket socket;
    private Map<String, Socket> map;
    private byte[] commandKey;
    private String key;
    private InputStream in;
    private String filename;
    private RMIServerInterfaceImpl rmi;

    public ServerThread(Socket socket, RMIServerInterfaceImpl rmi) {
        this.socket = socket;
        commandKey = new byte[KEY_SIZE];
        this.rmi = rmi;
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
        try {
        	if(this.rmi.frep.get(key).isMasterClient()) {
				this.filename = "files/" + this.filename;
			} else {
				this.filename = "downloads/" + this.filename;
			}
			System.out.println(" This filename " + this.filename);
			forwardData();
		} catch (FileUnFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
        
        try {
            int len = fin.read(buffer);
            while(len > 0) {
                out.write(buffer, 0, len);
                len = fin.read(buffer);
            }
            fin.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("uploader socket exception");
            fin.close();
        }
    }

}