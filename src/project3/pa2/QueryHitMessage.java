package project3.pa2;

import java.util.Arrays;

public class QueryHitMessage extends Message {
	protected QueryHitMessage(byte[] messageId, String[] matches, String leaf_node_hostname, String leaf_node_port) {
		super(TYPE_ID);
		this.leaf_node_port = leaf_node_port;
		this.matches = matches;
		this.leaf_node_address = leaf_node_hostname;
		this.messageId = messageId;
	}

	private static final long serialVersionUID = 1L;
	protected String leaf_node_address;
	protected String leaf_node_port;
	protected String[] matches;
	protected int[] sizes;
	public static final byte TYPE_ID = (byte) 0x81;

	@Override
	public String toString() {
		return "QueryHitMessage [leaf_node_address=" + leaf_node_address + ", leaf_node_port=" + leaf_node_port
				+ ", matches=" + Arrays.toString(matches) + ", sizes=" + Arrays.toString(sizes) + 
				" TTL " + ttl + " messageId= " + Arrays.toString(messageId) + "]";
	}
}