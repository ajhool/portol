package com.portol.common.model.player;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.content.ContentMetadata;
import com.portol.common.model.payment.Payment.Type;
import com.portol.common.model.player.Player.Status;
import com.portol.common.model.user.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerReply implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7865813231625044459L;

	private String playerId;

	private String dedicatedCloudHost;

	private User loggedIn; 
	private String qrURL;

	public String getQrURL() {
		return qrURL;
	}
	private PortolPlatform hostPlatform; 
	public void setQrURL(String qrURL) {
		this.qrURL = qrURL;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	private String videoKey;

	private Status newStatus; 

	private String btcPaymentAddr; //address under our control

	private int totRequested; //amt in bits requested

	private int totReceived; //amt in bits received

	//payment type
	private Type type; 

	private String color; 

	public ContentMetadata metaData;

	public ContentMetadata getMetaData() {
		return metaData;
	}

	public void setMetaData(ContentMetadata metaData) {
		this.metaData = metaData;
	}

	//used to check if server has received payments 
	private boolean mpdAuthorized = false;

	public ServerReply(String videoKey,
			boolean mpdAuthorized) {
		super();

		this.videoKey = videoKey;
		this.mpdAuthorized = mpdAuthorized;
	}

	public ServerReply() {
		super();

	}

	public String getVideoKey() {
		return videoKey;
	}

	public void setVideoKey(String videoKey) {
		this.videoKey = videoKey;
	}

	public boolean isMpdAuthorized() {
		return mpdAuthorized;
	}

	public void setMpdAuthorized(boolean mpdAuthorized) {
		this.mpdAuthorized = mpdAuthorized;
	}

	public Status getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(Player.Status newStatus) {
		this.newStatus = newStatus;
	}

	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();

		buf.append("  Printing state of serverreply object @ " + this.hashCode() + ":\n\n");
		buf.append("  playerid: " + playerId +"\n");
		buf.append("BTC address: " + this.getBtcPaymentAddr());
		buf.append("  videokey: " + videoKey +"\n");
		buf.append("  newstatus: " + newStatus +"\n");
		buf.append("  mpdauthorized: " + mpdAuthorized +"\n");
		buf.append("  Address of QR code: " + this.qrURL);

		return buf.toString();
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getTotReceived() {
		return totReceived;
	}

	public void setTotReceived(int totReceived) {
		this.totReceived = totReceived;
	}

	public int getTotRequested() {
		return totRequested;
	}

	public void setTotRequested(int totRequested) {
		this.totRequested = totRequested;
	}

	public String getBtcPaymentAddr() {
		return btcPaymentAddr;
	}

	public void setBtcPaymentAddr(String btcPaymentAddr) {
		this.btcPaymentAddr = btcPaymentAddr;
	}

	public String getDedicatedCloudHost() {
		return dedicatedCloudHost;
	}

	public void setDedicatedCloudHost(String dedicatedCloudHost) {
		this.dedicatedCloudHost = dedicatedCloudHost;
	}

	public User getLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(User loggedIn) {
		this.loggedIn = loggedIn;
	}

	public void setColor(String hexColor) {
		this.color = hexColor;

	}

	public String getColor(){
		return this.color;
	}

	public PortolPlatform getHostPlatform() {
		return hostPlatform;
	}

	public void setHostPlatform(PortolPlatform hostPlatform) {
		this.hostPlatform = hostPlatform;
	}

}
