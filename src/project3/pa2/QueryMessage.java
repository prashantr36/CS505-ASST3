package project3.pa2;

class QueryMessage extends Message {
  private static final long serialVersionUID = 1L;
  public static final byte TYPE_ID = (byte) 0x80;
  protected String[] keyWords;
  public QueryMessage(String[] keyWords) {
    super(TYPE_ID);
    this.keyWords = keyWords;
  }
}