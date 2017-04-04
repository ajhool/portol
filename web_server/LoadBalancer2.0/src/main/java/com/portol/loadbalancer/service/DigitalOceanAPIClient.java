package com.portol.loadbalancer.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Account;
import com.myjeeva.digitalocean.pojo.Delete;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Droplets;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Images;
import com.myjeeva.digitalocean.pojo.Key;
import com.myjeeva.digitalocean.pojo.Keys;
import com.myjeeva.digitalocean.pojo.Networks;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Size;
import com.portol.common.model.instance.BackendInstance;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.instance.Instance;
import com.portol.loadbalancer.config.CloudProviderConfig;

public class DigitalOceanAPIClient extends CloudProviderAPIClient {

	public final static String NAME = "REDACTED";
	private final static String authToken = "REDACTED";
	private final Logger logger = LoggerFactory.getLogger(DigitalOceanAPIClient.class);
	private final DigitalOceanClient apiClient;
	private Image edgeImage;
	private Image backendImage;

	private static final String EDGE_PRODUCTION_TAG = "EDGE_PRODUCTION";
	private static final String BACKEND_PRODUCTION_TAG = "BACKEND_PRODUCTION";

	public DigitalOceanAPIClient(CloudProviderConfig config, HttpClient hClient)
			throws DigitalOceanException, RequestUnsuccessfulException {
		super(config, hClient, NAME);

		apiClient = new DigitalOceanClient("v2", authToken, hClient);

		// lets print some debug info to make sure we have a good connection
		logger.debug("Connected to digitalocean API. Current droplets running:");

		Droplets currentDroplets = apiClient.getAvailableDroplets(1);

		int i = 1;
		for (Droplet drop : currentDroplets.getDroplets()) {
			String name = drop.getName();
			String image = drop.getImage().toString();
			String id = drop.getId().toString();
			logger.debug("Droplet " + i + ": Name: " + name + ", Image: " + image + ", id: " + id + "\n");
			i++;
		}
		logger.debug("Account information:");
		Account myInfo = apiClient.getAccountInfo();

		String email = myInfo.getEmail();

		logger.debug("Email for this auth token: " + email);

		logger.debug("Images available: ");
		Images imgs = apiClient.getUserImages(1);

		for (Image cur : imgs.getImages()) {
			logger.debug("searching image:" + cur.getName());

			if (cur.getName().contains(EDGE_PRODUCTION_TAG)) {
				// then we will use this as the new production image
				this.edgeImage = cur;

				break;
			}
		}

		for (Image cur : imgs.getImages()) {
			logger.debug("searching image:" + cur.getName());

			if (cur.getName().contains(BACKEND_PRODUCTION_TAG)) {

				this.backendImage = cur;
				break;
			}
		}

	}

	@Override
	public String destroyCloud(Instance toKill) {
		Delete death = new Delete();
		try {
			death = apiClient.deleteDroplet(Integer.parseInt(toKill.getApiId()));
		} catch (NumberFormatException | DigitalOceanException | RequestUnsuccessfulException e) {
			e.printStackTrace();
		}

		logger.debug(
				"Delete request for droplet: " + toKill.getApiId() + " is successful: " + death.getIsRequestSuccess());
		return null;
	}

	private Droplet makeNewEdgeDroplet() {
		Droplet newDroplet = new Droplet();

		newDroplet.setName("edge-server-created-" + System.currentTimeMillis());

		Region region = new Region("nyc3");
		newDroplet.setRegion(region);

		String dropletSize = "1gb";
		newDroplet.setSize(dropletSize);

		newDroplet.setImage(edgeImage);

		newDroplet.setEnableBackup(Boolean.FALSE);
		newDroplet.setEnableIpv6(Boolean.FALSE);
		newDroplet.setEnablePrivateNetworking(Boolean.FALSE);

		Keys keys = new Keys();
		try {
			keys = apiClient.getAvailableKeys(1);
		} catch (DigitalOceanException | RequestUnsuccessfulException e1) {
			e1.printStackTrace();
		}
		List<Key> allKeys = keys.getKeys();

		for (Key key : allKeys) {
			logger.debug("adding key: " + key.getName() + " with fingerprint: " + key.getFingerprint());
		}

		newDroplet.setKeys(allKeys);
		Droplet droplet = null;
		try {
			droplet = apiClient.createDroplet(newDroplet);
		} catch (DigitalOceanException e) {
			e.printStackTrace();
		} catch (RequestUnsuccessfulException e) {
			e.printStackTrace();
		}

		Droplet copy = null;
		int trys = 1;
		while (copy == null || copy.getNetworks().getVersion4Networks().isEmpty()) {
			try {
				copy = apiClient.getDropletInfo(droplet.getId());
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {
				e.printStackTrace();
			}

			Networks copynetworks = copy.getNetworks();

			Networks origNetoworks = droplet.getNetworks();

			logger.debug("try: " + trys + "checking size of networks returned from droplet"
					+ copy.getNetworks().getVersion4Networks().size());

			trys++;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		logger.debug("finally obtained network info for droplet");
		logger.debug("IP of new cloud: " + copy.getNetworks().getVersion4Networks().get(0).getIpAddress());

		return copy;
	}

	@Override
	public EdgeInstance spinUpNewEdge() {

		Droplet droplet = this.makeNewEdgeDroplet();

		EdgeInstance newCloud = new EdgeInstance();
		newCloud.setBootTime(droplet.getCreatedDate());
		newCloud.setLocation(droplet.getNetworks().getVersion4Networks().get(0).getIpAddress());
		newCloud.setMax_viewers(15);
		newCloud.setData_cap_mb(30000);
		newCloud.setStatus(EdgeInstance.Status.NEW);
		newCloud.setApiId(droplet.getId().toString());

		return newCloud;
	}

	private Droplet makeNewBackendDroplet() {
		// insert digitalocean API calls here
		// OK, i will
		Droplet newDroplet = new Droplet();

		newDroplet.setName("backend-created-" + System.currentTimeMillis());

		Region region = new Region("nyc3");
		newDroplet.setRegion(region);

		String dropletSize = "1gb";
		newDroplet.setSize(dropletSize);

		newDroplet.setImage(backendImage);

		newDroplet.setEnableBackup(Boolean.FALSE);
		newDroplet.setEnableIpv6(Boolean.FALSE);
		newDroplet.setEnablePrivateNetworking(Boolean.FALSE);

		Keys keys = new Keys();
		try {
			keys = apiClient.getAvailableKeys(1);
		} catch (DigitalOceanException | RequestUnsuccessfulException e1) {
			e1.printStackTrace();
		}
		List<Key> allKeys = keys.getKeys();

		for (Key key : allKeys) {
			logger.debug("adding key: " + key.getName() + " with fingerprint: " + key.getFingerprint());
		}

		newDroplet.setKeys(allKeys);
		Droplet droplet = null;
		try {
			droplet = apiClient.createDroplet(newDroplet);
		} catch (DigitalOceanException e) {
			e.printStackTrace();
		} catch (RequestUnsuccessfulException e) {
			e.printStackTrace();
		}

		Droplet copy = null;
		int trys = 1;
		while (copy == null || copy.getNetworks().getVersion4Networks().isEmpty()) {
			try {
				copy = apiClient.getDropletInfo(droplet.getId());
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {

				e.printStackTrace();
			}

			Networks copynetworks = copy.getNetworks();

			Networks origNetoworks = droplet.getNetworks();

			logger.debug("try: " + trys + "checking size of networks returned from droplet"
					+ copy.getNetworks().getVersion4Networks().size());

			trys++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		logger.debug("finally obtained network info for droplet");
		logger.debug("IP of new cloud: " + copy.getNetworks().getVersion4Networks().get(0).getIpAddress());

		return copy;
	}

	@Override
	public EdgeInstance shutdownAndResize(EdgeInstance toresize, int sizelevel) {
		return null;
	}

	@Override
	public BackendInstance spinUpNewBackend() {

		Droplet droplet = this.makeNewBackendDroplet();

		BackendInstance newCloud = new BackendInstance();
		newCloud.setBootTime(droplet.getCreatedDate());
		newCloud.setLocation(droplet.getNetworks().getVersion4Networks().get(0).getIpAddress());
		newCloud.setStatus(EdgeInstance.Status.NEW);
		newCloud.setApiId(droplet.getId().toString());

		return newCloud;
	}

}
