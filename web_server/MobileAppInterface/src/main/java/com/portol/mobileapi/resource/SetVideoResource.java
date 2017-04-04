package com.portol.mobileapi.resource;

import javax.ws.rs.Path;

import com.portol.mobileapi.repository.PlayerRepository;
import com.portol.mobileapi.repository.UserRepository;
import com.portol.mobileapi.service.LoadbalCommunicator;

@Path("/v0/setvideo")
public class SetVideoResource {

	public SetVideoResource(PlayerRepository playerrepo,
			UserRepository userrepo, LoadbalCommunicator comm) {
	}

}
