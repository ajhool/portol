package com.portol.common.model.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.portol.common.model.player.Player;

public class EdgeInstance extends Instance implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -534613556895261108L;

	// points to the content object that this cloud player is currently serving
	private String contentId;

	private int max_viewers;

	private int data_cap_mb;

	private boolean full;

	private String assetParent = "/assets";

	private BackendInstance localSource;

	private int numClients;

	// ID of client that was the source of the error
	private String badClient;

	// use this to track the clients when the cloud is done
	private ArrayList<String> inactiveClientIds;

	// set to null when storing in DBs... saves space
	// key can be btc address... fast lookup on auth requests
	private HashMap<String, Player> activeClients;

	public EdgeInstance() {
		// wrap JSON in here to spin up new instance
		setStatus(Status.NEW);

	}

	public synchronized String addActivePlayer(Player toAdd) {
		// lazy instantiation
		if (activeClients == null) {
			activeClients = new HashMap<String, Player>();
		}
		activeClients.put(toAdd.getBtcAddress(), toAdd);

		return toAdd.getBtcAddress();
	}

	public synchronized Player deleteActivePlayer(Player toDelete) {
		return toDelete;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (super.getServing() != null) {
			buf.append("Content being served by this cloud: " + super.getServing().getContentKey() + "\n");
		} else {
			buf.append("Content memeber is null\n");
		}

		buf.append("Cloud booted at: " + this.bootTime + "\n");
		buf.append("ID of content being served: " + this.contentId + "\n");
		buf.append("size of this cloud: " + this.data_cap_mb + "\n");
		buf.append("ID of this cloud: " + this.id + "\n");
		buf.append("Last report: " + this.getLastReport() + "\n");
		buf.append("IP of this cloud: " + this.location + "\n");
		buf.append("Cloud status: " + this.status + "\n");
		return buf.toString();

	}

	public ArrayList<String> getInactiveClientIds() {
		return inactiveClientIds;
	}

	public void setInactiveClientIds(ArrayList<String> inactiveClientIds) {
		this.inactiveClientIds = inactiveClientIds;
	}

	public String getBadClient() {
		return badClient;
	}

	public void setBadClient(String badClient) {
		this.badClient = badClient;
	}

	public int getNumClients() {
		return numClients;
	}

	public BackendInstance getLocalSource() {
		return localSource;
	}

	public void setLocalSource(BackendInstance toSet) {
		this.localSource = toSet;
	}

	public void setNumClients(int numClients) {
		this.numClients = numClients;
	}

	public String getAssetParent() {
		return assetParent;
	}

	public void setAssetParent(String assetParent) {
		this.assetParent = assetParent;
	}

	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public int getMax_viewers() {
		return max_viewers;
	}

	public void setMax_viewers(int max_viewers) {
		this.max_viewers = max_viewers;
	}

	public int getData_cap_mb() {
		return data_cap_mb;
	}

	public void setData_cap_mb(int data_cap_mb) {
		this.data_cap_mb = data_cap_mb;
	}
}
