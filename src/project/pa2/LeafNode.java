package project.pa2;

import project.pa1.RMIServer;

public class LeafNode extends RMIServer {

	public LeafNode(String hostname, Integer portNumber) throws Exception {
		super(hostname, portNumber);
	}

}