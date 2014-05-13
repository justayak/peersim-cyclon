package com.lajv;

import peersim.core.Node;

public class NodeWrapper {
	public Node node;
	public int age;

	public NodeWrapper(Node node) {
		this.node = node;
		this.age = 0;
	}
	
	public NodeWrapper cyclonCopy() {
		NodeWrapper nw = new NodeWrapper(node);
		nw.age = age;
		return nw;
	}
}
