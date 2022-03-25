package project3.pa1;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import project3.pa3.FileRepository;

public class Downloader {

    private static final int KEY_SIZE = 120;
    
    private String host;
    private int port;
    private byte[] key;
    private String fileName;
    private String out_fileName;
    private int size = 1094 *50;

    public Downloader(String host, int port, String filename) {
        this.host = host;
        this.port = port;
        this.fileName = filename;
        this.out_fileName = "downloads/" + this.fileName;
        this.size = 1094 *50;
        this.key = new byte[KEY_SIZE];
        System.arraycopy(this.fileName.getBytes(), 0, this.key, 0, Math.min(KEY_SIZE, this.fileName.getBytes().length));
    }

    public boolean start() throws Exception {
    	
        // connect to server
        byte[] buffer = new byte[size];
        Socket socket;
        try {
        	socket = new Socket(host, port);
        } catch (Exception e) {
            throw e;
        }
        // transfer command and key
        OutputStream out = socket.getOutputStream();
        out.write(key);
        
        FileOutputStream fout = new FileOutputStream(this.out_fileName);
        // download file
        InputStream in = socket.getInputStream();
        System.out.println("ready to download " + this.out_fileName);
        try {
            int len = in.read(buffer);
            if (len < 0) {
                System.out.println("connection closed without matched uploader");
            } else {
                while (len > 0) {
                    fout.write(buffer, 0, len);
                    len = in.read(buffer);
                }
            }
            fout.close();
            socket.close();
            return true;
        } catch (Exception e) {
            System.out.println("downloader socket exception");
            throw e;
        } finally {
        	fout.close();
        }
    }
}