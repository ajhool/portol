package com.portol.common.model.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.Cookie;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.PortolPlatform;
import com.portol.common.model.PortolToken;
import com.portol.common.model.bookmark.Bookmark;
import com.portol.common.model.content.HistoryItem;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1463842831615797875L;

	public static final String PALETTE = "F44336#E91E63#9C27B0#673AB7#3F51B5#2196F3#03A9F4#00BCD4#009688#4CAF50#8BC34A#CDDC39#FFEB3B#FFC107#FF5722#795548#9E9E9E#607D8B";

	public static final int HISTORY_SIZE = 10;

	private int paletteIndex = -1;

	private List<Account> userAccounts; 

	public int getPaletteIndex() {
		return paletteIndex;
	}

	public void setPaletteIndex(int paletteIndex) {
		this.paletteIndex = paletteIndex;
	}

	@Id
	private String userId = UUID.randomUUID().toString();

	//sha 512 + salt
	private String hashedPass; 

	private UserIcon userImg;

	private String userName; 

	private String name; 

	private String firstName;

	private String email;

	private int numConnections = 0;

	private PortolToken currentToken;

	private String lastName;

	private Date signUpDate;

	private Date lastSeen;

	private List<PortolPlatform> platforms = new ArrayList<PortolPlatform>();

	public int getNumFavorites() {
		return numFavorites;
	}

	public void setNumFavorites(int numFavorites) {
		this.numFavorites = numFavorites;
	}

	private int numFavorites; 


	private String loggedInPlatformId; 

	private Date loggedInPlatformExpire; 

	private UserFunds funds;


	private List<HistoryItem> history;

	private List<Bookmark> bookmarked;

	public User(String id, String hashedPass, UserIcon userImg,
			String userName, String firstName, String email,
			PortolToken currentToken, String lastName, Date signUpDate,
			Date lastSeen, UserFunds userShards, List<HistoryItem> history) {
		super();
		this.userId = id;
		this.hashedPass = hashedPass;
		this.userImg = userImg;
		this.userName = userName;
		this.firstName = firstName;
		this.email = email;
		this.currentToken = currentToken;
		this.lastName = lastName;
		this.signUpDate = signUpDate;
		this.lastSeen = lastSeen;
		this.funds = userShards;
		this.history = history;
		//		if(userCookies == null){
		//			userCookies = new Cookie[1];
		//		}
	}

	public User(String hashedPass, String firstName, String email,
			String lastName, UserFunds shards) {
		super();
		this.hashedPass = hashedPass;
		this.firstName = firstName;
		this.email = email;
		this.lastName = lastName;
		this.signUpDate = new Date(System.currentTimeMillis());
		this.lastSeen = signUpDate;
		this.setFunds(shards);
		//		if(userCookies == null){
		//			userCookies = new Cookie[1];
		//		}

	}

	public User(){
		super();
		//		if(userCookies == null){
		//			userCookies = new Cookie[1];
		//		}
	}

	private UserSettings settings;

	private String locale; 

	public String getLocale(){
		return locale;
	}
	public String getHashedPass() {
		return hashedPass;
	}

	public void setHashedPass(String hashedPass) {
		this.hashedPass = hashedPass;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getSignUpDate() {
		return signUpDate;
	}

	public void setSignUpDate(Date signUpDate) {
		this.signUpDate = signUpDate;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public List<HistoryItem> getHistory() {
		return history;
	}

	public PortolToken getCurrentToken() {
		return currentToken;
	}

	public void setCurrentToken(PortolToken currentToken) {
		this.currentToken = currentToken;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public void setUserId(String id) {
		this.userId = id;
	}

	public void setHistory(List<HistoryItem> history) {
		this.history = history;
	}

	public UserIcon getUserImg() {
		return userImg;
	}

	public void setUserImg(UserIcon userImg) {
		this.userImg = userImg;
	}

	public UserFunds getFunds() {
		return funds;
	}

	public void setFunds(UserFunds funds) {
		this.funds = funds;
	}

	public List<PortolPlatform> getPlatforms() {

		return platforms;
	}

	public void setPlatforms(List<PortolPlatform> platforms) {
		this.platforms = platforms;
	}

	public String getLoggedInPlatformId() {
		return loggedInPlatformId;
	}

	public void setLoggedInPlatformId(String loggedInPlatformId) {
		this.loggedInPlatformId = loggedInPlatformId;
	}

	public Date getLoggedInPlatformExpire() {
		return loggedInPlatformExpire;
	}

	public void setLoggedInPlatformExpire(Date loggedInPlatformExpire) {
		this.loggedInPlatformExpire = loggedInPlatformExpire;
	}

	public List<Bookmark> getBookmarked() {
		return bookmarked;
	}

	public void setBookmarked(List<Bookmark> bookmarked) {
		this.bookmarked = bookmarked;
	}

	@JsonIgnore
	public PortolPlatform findPlatformById(
			String value) {

		for(PortolPlatform plat : this.platforms){
			if(plat.getPlatformId().equals(value)){
				return plat;
			}
		}

		return null;
	}

	@JsonIgnore
	public String getNextColor(){
		String[] colors = PALETTE.split("#");
		if(paletteIndex < 0){
			Random rand = new Random();
			paletteIndex = rand.nextInt(colors.length);

		}

		String nextColor = colors[paletteIndex];

		paletteIndex++;
		paletteIndex %= (colors.length - 1);

		return nextColor;


	}

	@JsonIgnore
	public PortolPlatform adopt(PortolPlatform orphan) {
		String color = this.getNextColor();

		if(numConnections > 0){
			orphan.setPaired(true);
		}

		orphan.setPlatformColor(color);
		this.platforms.add(0, orphan);
		
		return orphan;
	}

	public int getNumConnections() {
		return numConnections;
	}

	public void setNumConnections(int numConnections) {
		this.numConnections = numConnections;
	}

	public PortolPlatform orphanPlayer(String platformId) {

		PortolPlatform orphaned = null;
		int index = 0;
		for(PortolPlatform plat : this.platforms){
			if(plat.getPlatformId().equals(platformId)){
				orphaned = this.platforms.remove(index);
				break;
			}
			index++;
		}

		orphaned.setPaired(false);
		orphaned.setPlatformColor("orphaned");
		return orphaned;
	}

	public UserSettings getSettings() {
		return settings;
	}

	public void setSettings(UserSettings settings) {
		this.settings = settings;
	}

	public List<Account> getUserAccounts() {
		return userAccounts;
	}

	public void setUserAccounts(List<Account> userAccounts) {
		this.userAccounts = userAccounts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocale(String locale) {
		this.locale = locale;


	}

	@JsonIgnore
	public PortolPlatform findPlatformById(javax.servlet.http.Cookie[] cookies) {
		for(Cookie cook : cookies){
			String value = cook.getValue();
			for(PortolPlatform plat : this.platforms){
				if(plat.getPlatformId().equals(value)){
					return plat;
				}
			}

		}
		return null;
	}
}
