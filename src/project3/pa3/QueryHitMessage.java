package project3.pa3;

import java.util.Arrays;

import project3.pa3.FileRepository.FileRepositoryFile;

public class QueryHitMessage extends Message {
	protected QueryHitMessage(byte[] messageId, String[] matches, String leaf_node_hostname, String leaf_node_port) {
		super(TYPE_ID);
		this.query_hit_node_port = leaf_node_port;
		this.matches = matches;
		this.query_hit_node_address = leaf_node_hostname;
		this.messageId = messageId;
	}

	private static final long serialVersionUID = 1L;
	protected String query_hit_node_address;
	protected String query_hit_node_port;
	protected String[] matches;
	protected int[] sizes;
	public static final byte TYPE_ID = (byte) 0x81;

	@Override
	public String toString() {
		return "QueryHitMessage [query_hit_origin=" + query_hit_node_address + ", query_hit_origin_port=" + query_hit_node_port
				+ ", matches=" + Arrays.toString(matches) +
				" TTL " + ttl +"]";
	}
}