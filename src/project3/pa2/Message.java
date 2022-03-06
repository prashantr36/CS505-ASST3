package project3.pa2;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public abstract class Message implements Serializable {
  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  private static Random random = new Random();
  public static final byte DEFAULT_TTL = 7;
  private static final int ID_LENGTH = 16;
  protected byte[] messageId;
  protected byte messageType;
  public byte ttl;
  protected Message(byte messageType) {
    try {
      this.messageId = new byte[ID_LENGTH]; 
      random = new Random();
      random.nextBytes(messageId);
      this.messageId = MessageDigest.getInstance("SHA-256").digest(messageId);
    } catch (NoSuchAlgorithmException e) {
    }
    this.messageType = messageType;
    this.ttl = DEFAULT_TTL;
  }
  protected Message(byte[] messageId, byte messageType) {
    this(messageType);
    this.messageId = messageId;
  }
  protected void decrementTimeToLiveCounter() throws MessageExpiredException {
    if (ttl-- <= 0) {
      throw new MessageExpiredException();
    }
  }
  @Override
  public boolean equals(Object obj) {
	if (this == obj) {
		return true;
	}
	if (!(obj instanceof Message)) {
		return false;
	}
	Message other = (Message) obj;
	return Arrays.equals(this.messageId, other.messageId);
  }
  @Override
  public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + Arrays.hashCode(messageId);
	return result;
  }
  
}