package com.portol.common.model.content;


import java.io.File;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.commons.io.FileUtils;
import org.mongojack.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portol.common.model.MovieFact;
import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.ObjectFactory;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType.S;
import com.portol.common.model.payment.Payment;
import com.portol.common.utils.Marshal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Content implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5161597960375457758L;

	public enum Status{
		NEW, UNUSED, UNAVAILABLE, DELETED, AVAILABLE
	}
	public enum DistributionLevel{
		LIMITED, APP, UNAVAILABLE, ALL
	}
	public enum Type {
		LIVE, VOD, RADIO
	}


	@JsonIgnore
	private static final Logger logger = LoggerFactory.getLogger(Content.class);

	@Id
	private String id = UUID.randomUUID().toString();

	private ContentMetadata metaData;
	//used when storing in DB

	private HashMap<String, Integer> previewSegments = new HashMap<String, Integer>();

	public HashMap<String, Integer> getPreviewSegments() {
		return previewSegments;
	}

	private long availabilityTime;

	@JsonIgnore
	public void recalculateFreeSegments(){
		previewSegments.clear();
		//calculate the number of segments to download for each representation 
		for(AdaptationSetType adapt : this.getMPDInfo().getPeriod().get(0).getAdaptationSet()){
			//Step 1: find out how many segments in this adaptation set will be a part of the preview
			long timescale = adapt.getSegmentTemplate().getTimescale();
			
			//30 seconds above what is allowed for caching
			long upperBound = timescale * (this.getFreeSeconds() + 30);

			int segNum;
			for(segNum = 0; segNum < adapt.getSegmentTemplate().getSegmentTimeline().getS().size(); segNum++){
				S s = adapt.getSegmentTemplate().getSegmentTimeline().getS().get(segNum);
				if(s.getT() > upperBound){
					break;
				}

			}

			//segnum now holds the number of free segments for this adaptation set



			//Step 2: map all the represnetation types in this adaptation set to that max free number
			
			for(RepresentationType rep : adapt.getRepresentation()){
				String safe = stripPeriods(rep.getId());
				
				previewSegments.put(safe, segNum);
			}
		}
	}

	@JsonIgnore
	public static String stripPeriods(String id2) {
		return id2.replace(".", "_");
		
	}

	public long getAvailabilityTime() {
		return availabilityTime;
	}

	public void setAvailabilityTime(long availabilityTime) {
		this.availabilityTime = availabilityTime;
	}

	private String splashDataId;
	private boolean hasPreview;

	private int dataSize_mb; 

	public class FileNamingScheme{
		private String dataFileExtension = "mp4";
		private String initSegmentTemplate = "$RepresentationID$_init";
		private String segmentTemplate = "$RepresentationID$-$Time$";

		public String getDataFileExtension() {
			return dataFileExtension;
		}

		public void setDataFileExtension(String dataFileExtension) {
			this.dataFileExtension = dataFileExtension;
		}

		public String getInitSegmentTemplate() {
			return initSegmentTemplate;
		}

		public void setInitSegmentTemplate(String initSegmentTemplate) {
			this.initSegmentTemplate = initSegmentTemplate;
		}

		public String getSegmentTemplate() {
			return segmentTemplate;
		}

		public void setSegmentTemplate(String segmentTemplate) {
			this.segmentTemplate = segmentTemplate;
		}
	}


	private FileNamingScheme nameScheme;  
	private String previewId;

	private int freeSeconds;  

	private ArrayList<ArrayList<String>> previewSegmentIds;
	private ArrayList<ArrayList<String>> mainSegmentIds;

	private MPDtype MPDInfo;

	private MPDtype previewMPD; 
	public void setPreviewMPD(MPDtype previewMPD) {
		this.previewMPD = previewMPD;
	}

	//private MPDtype mainMPDInfo;
	private Status status;
	//length of content for VOD
	//may be used in future for ready queue cloud instance selection
	private int lengthInSec;
	private int previewLengthInSec;
	private Content.Type type;
	private DistributionLevel distLvl = Content.DistributionLevel.ALL;

	private String channelOrVideoTitle;

	//both of these can be used to ID the owner
	private String ownerKey;

	private ArrayList<MovieFact> movieFax; 

	private boolean isPreview; 
	//master address where all btc from this video playing are aggregated
	//@DBRef(db= "masters")
	private Payment correspondingMaster;

	private int numBitrates; 

	private Date created;
	private Date last_accessed;
	// this is used by the players to request this piece of content
	private String contentKey;
	
	//sets the grace period as a multiple of the overall time of the content
	private float gracePeriod = 2.0F; 



	private ContentSource globalSrc;



	public boolean addmainSegmentListForBitrate(ArrayList<String> bitrate_segment_array) throws Exception{
		mainSegmentIds.add(0, bitrate_segment_array);
		return true;
	}

	public ArrayList<String> getmainSegmentIdsForBitrate(int index){
		return mainSegmentIds.get(index);
	}

	public boolean addPreviewSegmentListForBitrate(ArrayList<String> bitrate_segment_array) throws Exception{
		previewSegmentIds.add(0, bitrate_segment_array);
		return true;
	}

	public ArrayList<String> getPreviewSegmentIdsForBitrate(int index){
		return previewSegmentIds.get(index);
	}

	public Content(){
		previewSegmentIds = new ArrayList<ArrayList<String>>();
		mainSegmentIds = new ArrayList<ArrayList<String>>();
		if(this.getNameScheme() == null){
			this.nameScheme = new FileNamingScheme();
		}
	}


	public boolean isHasPreview() {
		return hasPreview;
	}
	public void setHasPreview(boolean hasPreview) {
		this.hasPreview = hasPreview;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}

	public DistributionLevel getDistLvl() {
		return distLvl;
	}
	public void setDistLvl(DistributionLevel distLvl) {
		this.distLvl = distLvl;
	}
	public Date getLast_accessed() {
		return last_accessed;
	}
	public void setLast_accessed(Date last_accessed) {
		this.last_accessed = last_accessed;
	}
	public String getContentKey() {
		return contentKey;
	}
	public void setContentKey(String contentKey) {
		this.contentKey = contentKey;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getOwnerKey() {
		return ownerKey;
	}
	public void setOwnerKey(String ownerKey) {
		this.ownerKey = ownerKey;
	}

	public Payment getCorrespondingMaster() {
		return correspondingMaster;
	}
	public void setCorrespondingMaster(Payment correspondingMaster) {
		this.correspondingMaster = correspondingMaster;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getSplashDataId() {
		return splashDataId;
	}
	public void setSplashDataId(String splashDataId) {
		this.splashDataId = splashDataId;
	}
	public MPDtype getMPDInfo() {
		return MPDInfo;
	}
	public void setMPDInfo(MPDtype MPDInfo) {
		this.MPDInfo = MPDInfo;
	}

	public String getPreviewId() {
		return previewId;
	}
	public void setPreviewId(String previewId) {
		this.previewId = previewId;
	}

	public int getNumBitrates() {
		return numBitrates;
	}

	public void setNumBitrates(int numBitrates) {
		this.numBitrates = numBitrates;
	}

	public boolean isPreview() {
		return isPreview;
	}

	public void setPreview(boolean isPreview) {
		this.isPreview = isPreview;
	}

	public int getDataSize_mb() {
		return dataSize_mb;
	}

	public void setDataSize_mb(int dataSize_mb) {
		this.dataSize_mb = dataSize_mb;
	}

	public ContentSource getGlobalSrc() {
		return globalSrc;
	}

	public void setGlobalSrc(ContentSource globalSrc) {
		this.globalSrc = globalSrc;
	}

	public ContentMetadata getMetaData() {
		return metaData;
	}

	public void setMetaData(ContentMetadata metaData) {
		this.metaData = metaData;
	}



	public String getChannelOrVideoTitle() {
		return channelOrVideoTitle;
	}

	public void setChannelOrVideoTitle(String channelOrVideoTitle) {
		this.channelOrVideoTitle = channelOrVideoTitle;
	}

	public FileNamingScheme getNameScheme() {
		return nameScheme;
	}

	public void setNameScheme(FileNamingScheme nameScheme) {
		this.nameScheme = nameScheme;
	}

	public ArrayList<MovieFact> getMovieFax() {
		return movieFax;
	}

	public void setMovieFax(ArrayList<MovieFact> movieFax) {
		this.movieFax = movieFax;
	}

	public int getFreeSeconds() {
		return freeSeconds;
	}


	public void setFreeSeconds(int freeSeconds) {
		this.freeSeconds = freeSeconds;
	}

	public MPDtype getPreviewMPD() {
		return previewMPD;
	}

	@JsonIgnore
	public void regeneratePreviewMPD() throws Exception{

		if(this.MPDInfo == null){
			throw new Exception("error - must have valid regular MPD before preview can be generated");
		}

		//step 0: clone MPD by marshalling it into a string and parsing it back out
		String marshalled = this.marshalMPD(this.MPDInfo);
		File tmp = new File("xml" + Math.random() + ".tmp");
		FileUtils.writeStringToFile(tmp, marshalled);


		MPDtype generated = Marshal.parseMPD(tmp.getAbsolutePath());

		FileUtils.deleteQuietly(tmp);

		//step 1: tweak duration of period
		Duration previewDur = DatatypeFactory.newInstance().newDuration(freeSeconds * 1000);

		generated.getPeriod().get(0).setDuration(previewDur);

		//step 2: chop segment timeline
		this.recalculateFreeSegments();


		for(AdaptationSetType adapt : generated.getPeriod().get(0).getAdaptationSet()){
			int numSegments = 0;
			for(RepresentationType rep: adapt.getRepresentation()){

				numSegments = this.previewSegments.get(stripPeriods(rep.getId()));
			}


			List<S> allSegs = adapt.getSegmentTemplate().getSegmentTimeline().getS();

			while(allSegs.size() > numSegments + 1){
				allSegs.remove(allSegs.size() -1);
			}

			//now, segment timeline should be the right size
			adapt.getSegmentTemplate().getSegmentTimeline().setS(allSegs);

		}


		this.previewMPD = generated;
		Date now = new Date();
		long duration = (long) (((double)this.previewMPD.getPeriod().get(0).getDuration().getTimeInMillis(now)) / (double)1000.00);
	
		
		this.previewLengthInSec = (int) duration;

	}

	@JsonIgnore
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

	public float getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(float gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public int getLengthInSec() {
		return lengthInSec;
	}

	public void setLengthInSec(int lengthInSec) {
		this.lengthInSec = lengthInSec;
	}

	public int getPreviewLengthInSec() {
		return previewLengthInSec;
	}

	public void setPreviewLengthInSec(int previewLengthInSec) {
		this.previewLengthInSec = previewLengthInSec;
	}


}


