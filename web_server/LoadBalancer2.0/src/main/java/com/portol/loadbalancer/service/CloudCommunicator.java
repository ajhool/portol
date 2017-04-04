package com.portol.loadbalancer.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.portol.common.model.CloudCommand;
import com.portol.common.model.CloudCommand.Type;
import com.portol.common.model.content.Content;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance;
import com.portol.common.model.player.Player;

public class CloudCommunicator {
	final Client cClient; 
	private String addPlayerPath;
	private String deletePlayerPath;
	private int port;
	private String setContentPath;
	private String setLiveContentPath;
	private String loginAlertPath; 
	private String taskPath;
	private static final String protocol = "http://";
	private String previewExtension = "/preview";
	private String killPath = "/api/v0/kill";
	private static final Logger logger = LoggerFactory.getLogger(CloudCommunicator.class);

	public CloudCommunicator(Client jClient, String addPlayerPath,
			String deletePlayerPath, int port, String setContentPath,
			String taskPath, String setLiveContentPath, String loginAlertPath) {
		this.cClient = jClient;
		this.addPlayerPath = addPlayerPath;
		this.loginAlertPath = loginAlertPath; 
		this.deletePlayerPath = deletePlayerPath;
		this.port = port;
		this.setLiveContentPath = setLiveContentPath;
		this.setContentPath = setContentPath;
		this.taskPath = taskPath;
	}


	public String initEdgeFullCloud(EdgeInstance readyCloudPlayer,
			Content toPlay) throws URISyntaxException {

		URI uri = new URI(protocol + readyCloudPlayer.getLocation() + ":" + port + setContentPath);
		String targetURL = uri.toString();
		CloudCommand cmd = new CloudCommand("bar", CloudCommand.Type.SET_CONTENT_ALL);
		cmd.setContent(toPlay);
		cmd.setSource(readyCloudPlayer.getLocalSource());
		Content resp = cClient.target(uri).request().post(Entity.json(cmd), Content.class);

		return resp.getId();

	}
	
	public String initEdgePreviewCloud(EdgeInstance readyCloudPlayer,
			Content toPlay) throws URISyntaxException {

		URI uri = new URI(protocol + readyCloudPlayer.getLocation() + ":" + port + setContentPath);
		String targetURL = uri.toString();
		CloudCommand cmd = new CloudCommand("bar", CloudCommand.Type.SET_CONTENT_PREVIEW);
		cmd.setContent(toPlay);
		cmd.setSource(readyCloudPlayer.getLocalSource());
		Content resp = cClient.target(uri).request().post(Entity.json(cmd), Content.class);

		return resp.getId();

	}
	


	public String addnewClient(EdgeInstance readyCloudPlayer, Player newClient) throws URISyntaxException {

		URI uri = new URI(protocol + readyCloudPlayer.getLocation() + ":" + port + addPlayerPath);
		String targetURL = uri.toString();
		CloudCommand cmd = new CloudCommand("bar", CloudCommand.Type.ADD_PLAYER);
		cmd.setPlayer(newClient);
		Player resp = cClient.target(uri).request().post(Entity.json(cmd), Player.class);

		return resp.getPlayerId();

	}

	public EdgeInstance sigKill(EdgeInstance toKill) {
		
		CloudCommand kill = new CloudCommand();
		kill.setType(Type.KILL);
		UriBuilder uribuild = UriBuilder.fromUri(toKill.getProtocol() + toKill.getLocation() + ":" + port + killPath);

		EdgeInstance lastKnownState = cClient.target(uribuild).request().post(Entity.json(kill), EdgeInstance.class);

		return lastKnownState;
	}

	public BackendInstance sigKill(BackendInstance toKill) {
		UriBuilder uribuild = UriBuilder.fromUri(toKill.getLocation() + ":" + port + killPath);

		BackendInstance resp = cClient.target(uribuild).request().post(Entity.json(toKill), BackendInstance.class);

		return resp;
	}


	public BackendInstance initLiveStreamingBackend(
			BackendInstance readyCloudPlayer, Content toPlay) throws URISyntaxException {

		URI uri = new URI(protocol + readyCloudPlayer.getLocation() + ":" + port + setLiveContentPath);
		String targetURL = uri.toString();
		CloudCommand cmd = new CloudCommand("bar", CloudCommand.Type.START_LIVE_BACKEND);
		cmd.setContent(toPlay);
		BackendInstance resp = cClient.target(uri).request().post(Entity.json(cmd), BackendInstance.class);

		return resp;
	}


	public boolean alertLogin(Player active){
		if(active.getCurrentSourceIP() == null) return false;
		try {
		URI uri = new URI(protocol + active.getCurrentSourceIP() + ":" + port + loginAlertPath);
		String targetURL = uri.toString();
		CloudCommand cmd = new CloudCommand("bar", CloudCommand.Type.ALERT_LOGIN);
		cmd.setPlayer(active);
		Player resp = cClient.target(uri).request().post(Entity.json(cmd), Player.class);
		} catch (Exception e){
			logger.error("Error alerting cloud @ " + active.getCurrentSourceIP(), e);
			return false;
		}
		return true;
	}
}
