package project3.pa2;


import java.net.InetAddress;

/**
 * Il messaggio di risposta ad un messaggio di ricerca.
 * @author Michele Comignano
 */
public class QueryHitMessage extends Message {
  /**
   * Crea un nuovo messaggio con i risultati di una ricerca nel caso il servent che lo
   * emette abbia un file server pubblicamente accessibile.
   * @param messageId
   * @param matches
   * @param fileServerPort
   */
  protected QueryHitMessage(byte[] messageId, String[] matches, int fileServerPort) {
    super(TYPE_ID);
    this.fileServerPort = fileServerPort;
    this.firewalled = false;
    this.matches = matches;
    this.address = null;
    this.messageId = messageId;
  }
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;
  protected InetAddress address;// TODO rimettere protetti
  protected int fileServerPort;
  protected String[] matches;
  protected int[] sizes;
  protected boolean firewalled;
  /**
   * L'identificatore del messaggio di pong.
   */
  public static final byte TYPE_ID = (byte) 0x81;
}