package com.portol.cloudplayer.resource;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.portol.cloudplayer.repository.LocalPlayerRepository;
import com.portol.cloudplayer.service.MPDService;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.BaseURLType;
import com.portol.common.model.dash.jaxb.DescriptorType;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.ObjectFactory;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType;
import com.portol.common.model.instance.EdgeInstance;
import com.portol.common.model.payment.Payment.Status;
import com.portol.common.model.player.Player;

@Path("/api/v0/mpd")
public class MPDResource {

	private EdgeInstance _this;
	private static Logger logger = LoggerFactory.getLogger(MPDResource.class);
	private MPDService mpdsvc;
	private LocalPlayerRepository localPlayers;


	public MPDResource(EdgeInstance _this, MPDService mpdSvc, LocalPlayerRepository locals){
		this._this = _this;
		this.mpdsvc = mpdSvc;
		this.localPlayers = locals;

	}

	@GET
	@Path("/vod/{playerId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public String sendvodMPD(@Context HttpServletRequest request, @PathParam("playerId") String playerId) throws Exception{

		Player wantsMPD = localPlayers.findOneById(playerId);

			String result = "";

			MPDtype mpd = _this.getServing().getMPDInfo();

			//delete any exisiting base urls
			if(!mpd.getBaseURL().isEmpty()){
				mpd.getBaseURL().clear();
			}

			BaseURLType temp = new BaseURLType();

			temp.setValue(_this.getProtocol() + _this.getLocation() + ":8901" + _this.getAssetParent() + "/" + _this.getServing().getGlobalSrc().getCollName() + "/");


			mpd.getBaseURL().add(temp);

			//add some ID information
			String mpdID = UUID.randomUUID().toString();

			final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(mpdID.getBytes(Charset.forName("UTF8")));
			final byte[] resultByte = messageDigest.digest();
			mpdID = new String(Hex.encodeHex(resultByte));

			mpd.setId(mpdID);

			result = marshalMPD(mpd);
			return result;
	}


	@GET
	@Path("/live/{playerId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public String sendliveMPD(@Context HttpServletRequest request) throws Exception{

		HashMap<String, SegmentTimelineType> mappingTime = new HashMap<String, SegmentTimelineType>();
		HashMap<String, XMLGregorianCalendar> mappingStart = new HashMap<String, XMLGregorianCalendar>();

		for(AdaptationSetType adapt : _this.getServing().getMPDInfo().getPeriod().get(0).getAdaptationSet()){
			RepresentationType rep = adapt.getRepresentation().get(0);
			SegmentTimelineType vidTimes = mpdsvc.getSegmentTimeline(rep, _this.getLocalSource().getLocation());
			XMLGregorianCalendar startTime = mpdsvc.getAvailabilityStart(rep, _this.getLocalSource().getLocation());

			mappingTime.put(rep.getId(), vidTimes);
			mappingStart.put(rep.getId(), startTime);
		}

		MPDtype mpd = makeMainMPDFile(_this.getServing());

		for(AdaptationSetType adapt : _this.getServing().getMPDInfo().getPeriod().get(0).getAdaptationSet()){
			RepresentationType key = adapt.getRepresentation().get(0);

			adapt.getSegmentTemplate().setSegmentTimeline(mappingTime.get(key.getId()));
		}

		mpd.setAvailabilityStartTime(mappingStart.values().iterator().next());
		String retVal = marshalMPD(mpd);

		return retVal;
	}


	private MPDtype makeMainMPDFile(Content targetContent) throws NoSuchAlgorithmException, JAXBException {
		String result = "";

		MPDtype mpd = targetContent.getMPDInfo();

		//delete any exisiting base urls
		if(!mpd.getBaseURL().isEmpty()){
			mpd.getBaseURL().clear();
		}

		BaseURLType temp = new BaseURLType();
		EdgeInstance thisCloud = _this;

		if(targetContent.getType() == Type.LIVE){
			temp.setValue("http://" + thisCloud.getLocation() + ":8901" + "/assets" + "/");
		} else {
			temp.setValue("http://" + thisCloud.getLocation() + ":8901" + "/assets" + "/" + targetContent.getGlobalSrc().getCollName() + "/");
		}

		mpd.getBaseURL().add(temp);
		//}
		//add some ID information
		String mpdID = targetContent.getId();

		final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.reset();
		messageDigest.update(mpdID.getBytes(Charset.forName("UTF8")));
		final byte[] resultByte = messageDigest.digest();
		mpdID = new String(Hex.encodeHex(resultByte));

		mpd.setId(mpdID);

		return mpd;
	}

	private String marshalMPD(MPDtype mpd) throws JAXBException {

		JAXBContext jaxbContext = JAXBContext.newInstance(MPDtype.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		StringWriter stringout = new StringWriter();
		jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
		ObjectFactory mpdObjFac = new ObjectFactory();
		logger.debug("number of periods being marshaled: " + mpd.getPeriod().size());
		jaxbMarshaller.marshal(mpdObjFac.createMPD(mpd), stringout);
		String out = stringout.toString();
		logger.debug("XML file written as string: " + out);

		return out;
	}
}
