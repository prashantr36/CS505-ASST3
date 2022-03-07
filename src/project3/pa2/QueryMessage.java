package project3.pa2;

import java.util.Arrays;

public class QueryMessage extends Message {
  private static final long serialVersionUID = 1L;
  public static final byte TYPE_ID = (byte) 0x80;
  protected String key;
  public QueryMessage(String key) {
    super(TYPE_ID);
    this.key = key;
  }
	@Override
	public String toString() {
		return "QueryMessage [key=" + key
				+ " TTL " + ttl + "]";
	}
}