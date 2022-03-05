package project3.pa2;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public abstract class Message implements Serializable {
  private static Random random = new Random();
  public static final byte DEFAULT_TTL = 7;
  private static final int ID_LENGTH = 16;
  protected byte[] messageId;
  protected byte messageType;
  private byte ttl;
  protected Message(byte messageType) {
    messageId = new byte[ID_LENGTH];
    random.nextBytes(messageId);
    try {
      messageId = MessageDigest.getInstance("SHA-256").digest(messageId);
    } catch (NoSuchAlgorithmException e) {
    }
    this.messageType = messageType;
    this.ttl = DEFAULT_TTL;
  }
  protected Message(byte[] messageId, byte messageType) {
    this(messageType);
    this.messageId = messageId;
  }
  protected void prepareForward() throws DeadMessageException {
    if (ttl-- <= 0) {
      throw new DeadMessageException();
    }
  }
  @Override
  public int hashCode() {
    int ret = 0;
    for (int i = 0; i < messageId.length; i++) {
      ret += messageId[i];
    }
    return ret;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Message)) {
      return false;
    }
    return ((Message) obj).hashCode() == hashCode();
  }
}