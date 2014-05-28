package com.lajv;

import java.text.DecimalFormat;

import com.lajv.vivaldi.VivaldiCoordinate;
import com.lajv.vivaldi.dim2d.Dim2DVivaldiCoordinate;

import peersim.core.Node;

public class NodeWrapper {
	public Node node;
	public int age;
	public VivaldiCoordinate coord;
	public double distance;
	public double uploadCapacity;
	public boolean recommendable;
	public double responsibilityValue;
	public boolean superPeer;

	public NodeWrapper(Node node) {
		this.node = node;
		this.age = 0;
		this.recommendable = false;
		responsibilityValue = 0;
		superPeer = false;
		coord = new Dim2DVivaldiCoordinate();
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
		DecimalFormat df = new DecimalFormat("0.00");
		return "{ id: " + node.getID() + ", coord: " + coord + ", distance: " + df.format(distance)
				+ "}";
	}
}
