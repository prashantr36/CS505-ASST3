package project3.pa2;

import java.net.InetAddress;

public class SearchResult {
  protected SearchResult(byte[] searchId, InetAddress address, int fileServerPort, String fileName,
          boolean firewalled) {
    this.address = address;
    this.searchId = searchId;
    this.fileServerPort = fileServerPort;
    this.fileName = fileName;
  }
  protected byte[] searchId;
  protected InetAddress address;
  public InetAddress getAddress() {
    return address;
  }
  public String getFileName() {
    return fileName;
  }
  public int getFileServerPort() {
    return fileServerPort;
  }
  protected int fileServerPort;
  protected String fileName;
}