package com.portol.common.model.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.StreamState;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.user.User;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Player implements Serializable, Comparable<Player>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6144452656037406537L;

	private Long referrerId;
	
	private String userAgent;

	private String ApiKey; // unused as of yet, could be used for the 'free
							// blogger views' idea
	
	private String videoKey;
	
	@Id
	public String playerId = UUID.randomUUID().toString();
	
	private StreamState sState;
	
	// uninitialized: value when new player queries system for first time
	// splash screen: data for splash screen has been sent to the player
	// streaming: payment received for video, should be connecting/connected to
	// cloud player. The watch again screen has not yet been transmitted
	// Repeat_screen: player has finished at least one streaming session, and
	// the watch again screen data has been transmitted
	// Done streaming: successful transaction completed, this can be marked for
	// later garbage collection if desired
	// Failure stream: Payment was successful, but stream was cut short. Refund
	// should be issued
	// Failure_payment: Bad payment, maybe a double spend, should begin watching IP for bad behavior
	
	public Preview getPreviewStatus() {
		return previewStatus;
	}

	public void setPreviewStatus(Preview previewStatus) {
		this.previewStatus = previewStatus;
	}

	public void setId(String id) {
		this.playerId = id;
	}

	public enum Status {
		UNINITIALIZED, PREVIEW_TIMEOUT, PREVIEW_STREAMING, SPLASH_SCREEN, STREAMING, REPEAT_SCREEN, DONE_STREAMING, FAILURE_STREAM, FAILURE_PAYMENT, PAUSED, STOPPED, DEAD, TIMEOUT
	}
	
	public enum Preview {
		NONE, REQUESTED, STREAMING, DONE
	}
	
	private boolean locked; 
	
	private String playerIP;
	
	private String qrURL; 

	private Status status = Status.UNINITIALIZED;

	private PortolPlatform hostPlatform; 
	
	private Preview previewStatus = Preview.NONE;

	private Type profile;
	
	//time the payment went through + video playback started
	private long timeStarted;

	private User loggedIn; 
	
	private long initialConnect;
	
	//time when the payment period paid for is over
	private long timeExpire;
	
	private Payment playerPayment; 
	
	//used to quickly find a player when a payment comes through

	private String btcAddress; 
	

	//time of last requested segment
	private long lastRequest;

	//tracks the number of cloud players used 
	private int numPlayersUsed = 0;
	
	private ArrayList<String> players_used; //keep references to the cloud instances that were matches to this player
	
	//null when no cloud player in use
	private String currentCloudPlayerId = null;
	private String currentSourceIP = null;
	
	private int numPlays;

	
	public int getNumPlayersUsed() {
		return numPlayersUsed;
	}

	public void setNumPlayersUsed(int numPlayersUsed) {
		this.numPlayersUsed = numPlayersUsed;
	}

	public int getNumPlays() {
		return numPlays;
	}

	public void setNumPlays(int numPlays) {
		this.numPlays = numPlays;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Long getReferrerId() {
		return referrerId;
	}

	public void setReferrerId(Long referrerId) {
		this.referrerId = referrerId;
	}

	public void addCloudPlayerId(long cloudPlayerId){
		//TODO
		//add to arrayList
	}

	public long getTimeStarted() {
		return timeStarted;
	}

	public void setTimeStarted(long timeStarted) {
		this.timeStarted = timeStarted;
	}

	public long getTimeExpire() {
		return timeExpire;
	}

	public void setTimeExpire(long timeExpire) {
		this.timeExpire = timeExpire;
	}

	public long getLastRequest() {
		return lastRequest;
	}

	public void setLastRequest(long lastRequest) {
		this.lastRequest = lastRequest;
	}

	public String getPlayerId() {
		return playerId;
	}

	public Player(String playerIP, Long playerId, String apiKey, String videoKey) {
		super();
		this.playerIP = playerIP;
		this.referrerId = playerId;
		ApiKey = apiKey;
		this.videoKey = videoKey;
		this.players_used = new ArrayList<String>();
		
	}
	
	public Player(){
		super();
		this.players_used = new ArrayList<String>();
	}

	public String addPlayerUsed(String playerid){
		players_used.add(playerid);
		return playerid;
	}
	
	public String getPlayerIP() {
		return playerIP;
	}

	public void setPlayerIP(String playerIP) {
		this.playerIP = playerIP;
	}
	
	public String getVideoKey() {
		return videoKey;
	}

	public void setVideoKey(String videoKey) {
		this.videoKey = videoKey;
	}

	public String getApiKey() {
		return ApiKey;
	}

	public void setApiKey(String apiKey) {
		ApiKey = apiKey;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Type getProfile() {
		return profile;
	}

	public void setProfile(Type profile) {
		this.profile = profile;
	}

	public Payment getPlayerPayment() {
		return playerPayment;
	}
	public long getInitialConnect() {
		return initialConnect;
	}

	public void setInitialConnect(long initialConnect) {
		this.initialConnect = initialConnect;
	}
	public void setPlayerPayment(Payment playerPayment) {
		this.playerPayment = playerPayment;
	}

	public String getBtcAddress() {
		return btcAddress;
	}

	public void setBtcAddress(String btcAddress) {
		this.btcAddress = btcAddress;
	}

	public String getCurrentCloudPlayerId() {
		return currentCloudPlayerId;
	}

	public void setCurrentCloudPlayerId(String currentCloudPlayerId) {
		this.currentCloudPlayerId = currentCloudPlayerId;
	}

	public String getCurrentSourceIP() {
		return currentSourceIP;
	}

	public void setCurrentSourceIP(String currentSourceIP) {
		this.currentSourceIP = currentSourceIP;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public User getLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(User loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getQrURL() {
		return qrURL;
	}

	public void setQrURL(String qrURL) {
		this.qrURL = qrURL;
	}

	public StreamState getsState() {
		return sState;
	}

	public void setsState(StreamState sState) {
		this.sState = sState;
	}

	public PortolPlatform getHostPlatform() {
		return hostPlatform;
	}

	public void setHostPlatform(PortolPlatform hostPlatform) {
		this.hostPlatform = hostPlatform;
	}

	@Override
	public int compareTo(Player another) {
		if(this.getLastRequest() > another.getLastRequest()){
			//this should be ahead
			return -1;
			
		}

		if(this.getLastRequest() < another.getLastRequest()){
			//this should be behind
			return 1;

		}
		
		return 0;
	}
	
}
