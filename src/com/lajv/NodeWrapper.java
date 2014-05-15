package com.lajv;

import com.lajv.vivaldi.VivaldiCoordinate;

import peersim.core.Node;

public class NodeWrapper {
	public Node node;
	public int age;
	public VivaldiCoordinate coord;
	public double distance;
	public double uploadCapacity;
	public double responsibilityValue;
	public boolean superPeer;

	public NodeWrapper(Node node) {
		this.node = node;
		this.age = 0;
		responsibilityValue = 0;
		superPeer = false;
	}

	public NodeWrapper cyclonCopy() {
		NodeWrapper copy = new NodeWrapper(node);
		copy.age = age;
		copy.uploadCapacity = uploadCapacity;
		copy.coord = (VivaldiCoordinate) coord.clone();
		copy.responsibilityValue = responsibilityValue;
		copy.superPeer = superPeer;
		return copy;
	}

	public NodeWrapper closePeerCopy() {
		NodeWrapper copy = new NodeWrapper(node);
		copy.coord = (VivaldiCoordinate) coord.clone();
		copy.distance = distance;
		copy.uploadCapacity = uploadCapacity;
		copy.responsibilityValue = responsibilityValue;
		copy.superPeer = superPeer;
		return copy;
	}

	@Override
	public String toString() {
		return "(" + node.getID() + ", " + uploadCapacity + ", " + responsibilityValue + ")";
	}
}
