package com.lajv;

import peersim.core.Node;

class NodeWrapper {
	Node node;
	int age;

	public NodeWrapper(Node node) {
		this.node = node;
		this.age = 0;
	}

	@Override
	protected Object clone() {
		NodeWrapper nw = null;
		try { nw = (NodeWrapper) super.clone(); }
		catch( CloneNotSupportedException e ) {} // never happens
		nw.node = this.node;
		nw.age = this.age;
		return nw;
	}
}
