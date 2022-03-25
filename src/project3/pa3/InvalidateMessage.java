package project3.pa3;

public class InvalidateMessage extends Message {
  private static final long serialVersionUID = 1L;
  public static final byte TYPE_ID = (byte) 0x82;
  protected String key;
  protected int versionNumber;
  public InvalidateMessage(String key, int versionNumber) {
    super(TYPE_ID);
    this.key = key;
    this.versionNumber = versionNumber;
  }
  @Override
  public String toString() {
	return "InvalidateMessage [key=" + key
			+ " versionNumber " + versionNumber + "]";
  }
  public String getKey() {
	  return key;
  }
  public int getVersionNumber() {
	  return versionNumber;
  }
}