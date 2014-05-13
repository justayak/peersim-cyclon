package com.lajv.cyclon;

import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

public class CyclonObserver implements Control {

	private static final String CYCLON_PROT = "lnk";

	private LinkedList<Long> peersWithEmptyCache;

	public CyclonObserver(String prefix) {
		peersWithEmptyCache = new LinkedList<Long>();
	}

	@Override
	public boolean execute() {

		int pid = Configuration.lookupPid(CYCLON_PROT);
		peersWithEmptyCache.clear();

		for (int i = 0; i < Network.size(); i++) {
			GeneralNode n = (GeneralNode) Network.get(i);
			CyclonProtocol cyclonNode = (CyclonProtocol) n.getProtocol(pid);

			if(cyclonNode.getCache().size() == 0) {
				peersWithEmptyCache.add(n.getID());
			}
		}

		System.err.println("No peers in nodes: " + peersWithEmptyCache.toString());
		return false;
	}

}
