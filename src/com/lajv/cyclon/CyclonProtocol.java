package com.lajv.cyclon;

import java.util.LinkedList;

import com.lajv.NetworkNode;
import com.lajv.NodeWrapper;
import com.lajv.vivaldi.VivaldiCoordinate;
import com.lajv.vivaldi.VivaldiProtocol;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

public class CyclonProtocol implements CDProtocol, Linkable {

	// =============== static fields =======================================
	// =====================================================================

	// We are using static temporary arrays to avoid garbage collection
	// of them. these are used by all SimpleNewscast protocols included
	// in the protocol array so its size is the maximum of the cache sizes

	/**
	 * Cache size.
	 * 
	 * @config
	 */
	private static final String PAR_CACHE = "cache";

	/**
	 * Shuffle size.
	 * 
	 * @config
	 */
	private static final String PAR_L = "l";

	/**
	 * The Vivaldi protocol which holds the coordinate
	 * 
	 * @config
	 */
	private static final String PAR_VIVALDI_PROT = "vivaldi_prot";

	// =================== fields ==========================================
	// =====================================================================

	/** Neighbors currently in the cache */
	private LinkedList<NodeWrapper> cache;

	private static int maxCacheSize;
	private static int l;

	private String prefix;
	protected VivaldiCoordinate coord;

	public CyclonProtocol(String prefix) {
		this.prefix = prefix;
		maxCacheSize = Configuration.getInt(prefix + "." + PAR_CACHE);
		l = Configuration.getInt(prefix + "." + PAR_L);
		cache = new LinkedList<NodeWrapper>();
	}

	// --------------------------------------------------------------------

	@Override
	public Object clone() {
		CyclonProtocol c = null;
		try {
			c = (CyclonProtocol) super.clone();
		} catch (CloneNotSupportedException e) {
		} // never happens
		c.cache = new LinkedList<NodeWrapper>();
		return c;
	}

	// ====================== helper methods ==============================
	// ====================================================================

	private NodeWrapper increasePeerAgeAndRemoveOldest() {
		int maxAge = 0;
		int oldest = cache.size() - 1;

		if (cache.size() == 0)
			return null;

		for (int i = 0; i < cache.size(); i++) {
			cache.get(i).age++;
			if (cache.get(i).age > maxAge) {
				oldest = i;
				maxAge = cache.get(i).age;
			}
		}
		return cache.remove(oldest);
	}

	private void addShuffledPeers(long selfId, LinkedList<NodeWrapper> shufflePeers,
			LinkedList<NodeWrapper> splicedPeers) {
		// Add new peers if they do not already exist
		add_shuffle: while (shufflePeers.size() > 0) {
			NodeWrapper nw = shufflePeers.pop();
			if (nw.node.getID() == selfId)
				continue;
			for (int i = 0; i < cache.size(); i++)
				if (nw.node == cache.get(i).node)
					continue add_shuffle;
			cache.add(nw);
		}

		// Fill up with spliced peers if necessary
		add_spliced: while (cache.size() < maxCacheSize && splicedPeers.size() > 0) {
			NodeWrapper splicedNW = splicedPeers.pop();
			for (NodeWrapper cacheNW : cache) {
				if (splicedNW.node == cacheNW.node)
					continue add_spliced;
			}
			cache.add(splicedNW);
		}
	}

	private LinkedList<NodeWrapper> randomSplice(int num) {
		LinkedList<NodeWrapper> randomNodes = new LinkedList<NodeWrapper>();
		NodeWrapper nw;

		while (num > 0) {
			nw = cache.remove(CommonState.r.nextInt(cache.size()));
			randomNodes.add(nw);
			num--;
		}

		return randomNodes;
	}

	public LinkedList<NodeWrapper> shuffle(long selfId, LinkedList<NodeWrapper> shufflePeers) {
		// Select l random neighbors.
		int numPeersToShuffle = Math.min(l, cache.size());
		LinkedList<NodeWrapper> peersToShuffle = randomSplice(numPeersToShuffle);

		LinkedList<NodeWrapper> peersToSend = new LinkedList<NodeWrapper>();

		// Create list for returning to calling peer.
		for (NodeWrapper n : peersToShuffle) {
			peersToSend.add(n.cyclonCopy());
		}

		addShuffledPeers(selfId, shufflePeers, peersToShuffle);

		return peersToSend;
	}

	private NodeWrapper me(Node node) {
		NodeWrapper me = new NodeWrapper(node);
		if (coord == null) {
			int vivaldiProtID = Configuration.getPid(prefix + "." + PAR_VIVALDI_PROT);
			VivaldiProtocol vivaldiProt = (VivaldiProtocol) node.getProtocol(vivaldiProtID);
			coord = vivaldiProt.getCoord();
		}
		me.coord = (VivaldiCoordinate) coord.clone();
		me.uploadCapacity = ((NetworkNode) node).location.getUploadCapacity();
		return me;
	}

	// --------------------------------------------------------------------

	// ====================== Linkable implementation =====================
	// ====================================================================

	/**
	 * Does not check if the index is out of bound (larger than {@link #degree()})
	 */
	public Node getNeighbor(int i) {
		return cache.get(i).node;
	}

	// --------------------------------------------------------------------

	/** Might be less than cache size. */
	public int degree() {
		return cache.size();
	}

	// --------------------------------------------------------------------

	@Override
	public boolean addNeighbor(Node node) {
		if (cache.size() < maxCacheSize) {
			NodeWrapper nw = new NodeWrapper(node);
			nw.uploadCapacity = ((NetworkNode) node).location.getUploadCapacity();
			cache.add(nw);
		}
		return true;
	}

	// --------------------------------------------------------------------

	@Override
	public void pack() {
		// TODO Auto-generated method stub

	}

	// --------------------------------------------------------------------

	@Override
	public boolean contains(Node n) {
		for (NodeWrapper nw : cache) {
			if (nw.node == n)
				return true;
		}
		return false;
	}

	// --------------------------------------------------------------------

	@Override
	public void onKill() {
		cache = null;
	}

	// ===================== CDProtocol implementations ===================
	// ====================================================================

	@Override
	public void nextCycle(Node node, int protocolID) {
		NodeWrapper oldestPeer = increasePeerAgeAndRemoveOldest();
		if (oldestPeer == null)
			return;
		Node otherNode = oldestPeer.node;

		CyclonProtocol otherCyclonProt = (CyclonProtocol) (otherNode.getProtocol(protocolID));

		// 2. Select l - 1 other random neighbors.
		int numPeersToShuffle = Math.min(l - 1, cache.size());
		LinkedList<NodeWrapper> peersToShuffle = randomSplice(numPeersToShuffle);

		LinkedList<NodeWrapper> peersToSend = new LinkedList<NodeWrapper>();

		// Copy NodeWrappers into new list for shuffling
		for (NodeWrapper n : peersToShuffle) {
			peersToSend.add(n.cyclonCopy());
		}

		// 3. Add entry of age 0 and with self node
		peersToSend.add(me(node));

		// 4. Send the updated subset to peer Q.
		LinkedList<NodeWrapper> responsePeers = otherCyclonProt.shuffle(otherNode.getID(),
				peersToSend);

		addShuffledPeers(node.getID(), responsePeers, peersToShuffle);
	}

	// ===================== other public methods =========================
	// ====================================================================

	public String toString() {
		if (cache == null)
			return "DEAD!";

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < degree(); ++i) {
			sb.append(" (" + cache.get(i).node.getID() + "," + cache.get(i).age + ")");
		}
		return sb.toString();
	}

	public LinkedList<NodeWrapper> getCache() {
		return cache;
	}

	public void updateCoord(Node n, VivaldiCoordinate coord) {
		for (NodeWrapper nw : cache) {
			if (nw.node == n) {
				nw.coord.update(coord);
				break;
			}
		}
	}
}
