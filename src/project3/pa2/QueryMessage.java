package project3.pa2;

/**
 * Il messaggio di ricerca per inoltrare la ricerca di una stringa attraverso la rete.
 * @author Michele Comignano
 */
class QueryMessage extends Message {
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;
  /**
   * L'identificatore del messaggio di pong.
   */
  public static final byte TYPE_ID = (byte) 0x80;
  /**
   * La stringa di ricerca per cui cercare corrispondenze.
   */
  protected String[] keyWords;
  /**
   * Crea un nuovo messaggio di query con ttl di dafault.
   * @param keyWords la chiave di ricerca.
   */
  public QueryMessage(String[] keyWords) {
    super(TYPE_ID);
    this.keyWords = keyWords;
  }
}