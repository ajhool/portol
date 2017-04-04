package com.portol.cloudplayer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.portol.common.model.dash.jaxb.AdaptationSetType;
import com.portol.common.model.dash.jaxb.BaseURLType;
import com.portol.common.model.dash.jaxb.MPDtype;
import com.portol.common.model.dash.jaxb.ObjectFactory;
import com.portol.common.model.dash.jaxb.PeriodType;
import com.portol.common.model.dash.jaxb.PresentationType;
import com.portol.common.model.dash.jaxb.ProgramInformationType;
import com.portol.common.model.dash.jaxb.RepresentationType;
import com.portol.common.model.dash.jaxb.SegmentTemplateType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType;
import com.portol.common.model.dash.jaxb.SegmentTimelineType.S;


public class Marshal {

	public static void main(String[] args) throws JAXBException, SAXException, IOException, ParserConfigurationException, DatatypeConfigurationException{

		MPDtype output = parseMPD("live.mpd");

		File outfile = new File("portolRedbullMPD.mpd");
		JAXBContext jaxbContext = JAXBContext.newInstance(MPDtype.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
		ObjectFactory mpdObjFac = new ObjectFactory();
		//System.out.println("number of periods: " + output.getPeriod().size());
		jaxbMarshaller.marshal(mpdObjFac.createMPD(output), outfile );
		jaxbMarshaller.marshal(mpdObjFac.createMPD(output), System.out );

	}

	public static synchronized MPDtype parseMPD(String xmlToParse) throws SAXException, IOException, ParserConfigurationException, DatatypeConfigurationException {

		//Get the DOM Builder Factory
		DocumentBuilderFactory factory = 
				DocumentBuilderFactory.newInstance();

		//Get the DOM Builder
		DocumentBuilder builder = factory.newDocumentBuilder();
		DatatypeFactory  dtFactory = DatatypeFactory.newInstance(); 
		//Load and Parse the XML document
		//document contains the complete XML as a Tree.
		Document document = 
				builder.parse(new InputSource(new ByteArrayInputStream(xmlToParse.getBytes("utf-8"))));

		NodeList nodeList = document.getElementsByTagName("MPD");

		Element mpdGeneral = (Element) nodeList.item(0);
		//System.out.println("Minbuffertime: " + mpdGeneral.getAttribute("minBufferTime"));

		///System.out.println("All attributes of mpd general: ");
		NamedNodeMap mpdAttrs = mpdGeneral.getAttributes();
		iterate(mpdAttrs);
		MPDtype mpd = new MPDtype();


		//load top level MPD info
		Duration minBufferTime = dtFactory.newDuration(mpdAttrs.getNamedItem("minBufferTime").getNodeValue());
		//System.out.println("Loading minbuffertime: " + minBufferTime.toString());
		mpd.setMinBufferTime(minBufferTime);


		Node duration = mpdAttrs.getNamedItem("mediaPresentationDuration");
		if(duration != null){
			String durationValue = duration.getNodeValue();
			Duration mpDuration = dtFactory.newDuration(durationValue);
			//System.out.println("Loading mediaPresentationDuration: " + mpDuration.toString());
			mpd.setMediaPresentationDuration(mpDuration);

		}
		Node start = mpdAttrs.getNamedItem("availabilityStartTime");
		if(start != null){
			String durationValue = start.getNodeValue();
			XMLGregorianCalendar mpDuration = dtFactory.newXMLGregorianCalendar(durationValue);
			//System.out.println("Loading availability: " + mpDuration.toString());
			
			mpd.setAvailabilityStartTime(mpDuration);

		}

		String profiles = mpdAttrs.getNamedItem("profiles").getNodeValue();
		//System.out.println("Loading profiles: " + profiles.toString());
		mpd.setProfiles(profiles);

		PresentationType type = PresentationType.fromValue(mpdAttrs.getNamedItem("type").getNodeValue());
		//System.out.println("Loading type: " + type.value());
		mpd.setType(type);


		System.out.println();

		//Load Program info

		nodeList = document.getElementsByTagName("ProgramInformation");
		//System.out.println("Number of program info elements found: " + nodeList.getLength());
		for(int q = 0; q < nodeList.getLength(); q++){
			ProgramInformationType progInfo = new ProgramInformationType();
			Element title = (Element) ((Element) nodeList.item(q)).getElementsByTagName("Title").item(0);

			//System.out.println("attributes of title:");
			iterate(title.getAttributes());

			String titleString = nodeList.item(0).getTextContent();

			progInfo.setTitle(titleString);

			//System.out.println("node value: " + titleString);

			progInfo.setSource("Portol DASH Engine");

			mpd.getProgramInformation().add(progInfo);
		}

		//load base URL

		nodeList = document.getElementsByTagName("BaseURL");
		//System.out.println("number of BAseURLs: " + nodeList.getLength());

		for(int r = 0; r < nodeList.getLength(); r ++){
			BaseURLType baseurlType = new BaseURLType();

			baseurlType.setValue(nodeList.item(r).getTextContent());

			mpd.getBaseURL().add(baseurlType);
			///System.out.println("loaded base URL: " + baseurlType.getValue());
		}

		System.out.println();

		//load periods
		nodeList = document.getElementsByTagName("Period");
		//System.out.println("Number of periods found: " + nodeList.getLength());

		for(int i = 0; i < nodeList.getLength(); i++){
			Element period = (Element) nodeList.item(i);

			PeriodType curPeriod = new PeriodType();
			String perDur = period.getAttribute("duration");
			if(perDur != null && perDur.length() > 0){
				Duration periodDuration = dtFactory.newDuration(perDur);

				//System.out.println("Parsing information for period with duration: " + periodDuration);

				curPeriod.setDuration(periodDuration);
			}
			NodeList adaptSet = period.getElementsByTagName("AdaptationSet");

			for(int f = 0; f < adaptSet.getLength(); f++){
				//System.out.println("number of adaptation sets found: " + adaptSet.getLength() + ". Attributes:");
				Node thisAdapt = adaptSet.item(0);
				NamedNodeMap adaptAttrs = thisAdapt.getAttributes();
				iterate(adaptAttrs);

				AdaptationSetType tempAdapt = new AdaptationSetType();

				Node attrsAdapt = adaptAttrs.getNamedItem("segmentAlignment");
				if(attrsAdapt != null){
					String segAlign = attrsAdapt.getNodeValue();
					//System.out.println("storing segment alignment: " + segAlign);
					tempAdapt.setSegmentAlignment(segAlign);
				}

				Node attrsAdapt2 = adaptAttrs.getNamedItem("group");
				if(attrsAdapt2 != null){
					long group = Long.parseLong(attrsAdapt2.getNodeValue());
					//System.out.println("storing group: " + group);
					tempAdapt.setGroup(group);
				}
				Node attrsAdapt3 = adaptAttrs.getNamedItem("maxWidth");
				if(attrsAdapt3 != null){
					long maxWidth = Long.parseLong(attrsAdapt3.getNodeValue());
					System.out.println("storing maxWidth: " + maxWidth);
					tempAdapt.setMaxWidth(maxWidth);
				}
				Node attrsAdapt4 = adaptAttrs.getNamedItem("maxHeight");
				if(attrsAdapt4 != null){
					long maxHeight = Long.parseLong(attrsAdapt4.getNodeValue());
					//System.out.println("storing maxHeight: " + maxHeight);
					tempAdapt.setMaxHeight(maxHeight);
				}

				Node attrsAdapt5 = adaptAttrs.getNamedItem("maxFrameRate");
				if(attrsAdapt5 != null){
					String maxFr = attrsAdapt5.getNodeValue();
					//System.out.println("storing maxFrameRate: " + maxFr);
					tempAdapt.setMaxFrameRate(maxFr);

				}

				Node attrsAdapt6 = adaptAttrs.getNamedItem("par");

				if(attrsAdapt6 != null){
					String par = attrsAdapt6.getNodeValue();
					//System.out.println("storing par: " + par);
					tempAdapt.setPar(par);
					//System.out.println();
				}

				//load segment template
				SegmentTemplateType tempSegTemplType = new SegmentTemplateType();
				//we know there is only 1 segment template per adaptation set acording to the dash schema
				Node segTemp = ((Element)thisAdapt).getElementsByTagName("SegmentTemplate").item(0);
				NamedNodeMap segTempAttrs = segTemp.getAttributes();

				//System.out.println("segment template attributes: ");
				iterate(segTempAttrs);

				Node segTempAttrs1 = segTempAttrs.getNamedItem("duration");
				if(segTempAttrs1 != null){
					long dur = Long.parseLong(segTempAttrs1.getNodeValue());
					//System.out.println("storing duration: " + dur);
					tempSegTemplType.setDuration(dur);

				}

				long timescale = Long.parseLong(segTempAttrs.getNamedItem("timescale").getNodeValue());
				//System.out.println("storing timescale: " + timescale);
				tempSegTemplType.setTimescale(timescale);

				Node segTempAttrs2 = segTempAttrs.getNamedItem("startNumber");
				if(segTempAttrs2 != null){
					long startnum = Long.parseLong(segTempAttrs2.getNodeValue());
					//System.out.println("storing startnumber: " + startnum);
					tempSegTemplType.setStartNumber(startnum);
				}


				String initialization = segTempAttrs.getNamedItem("initialization").getNodeValue();
				//System.out.println("storing initialization: " + initialization);
				tempSegTemplType.setInit(initialization);

				String media = segTempAttrs.getNamedItem("media").getNodeValue();
				//System.out.println("storing media: " + media);
				tempSegTemplType.setMedia(media);



				NodeList timeLine = segTemp.getChildNodes();
				Node segTimeLine = null;
				for(int k = 0; k < timeLine.getLength(); k++){
					//System.out.println(timeLine.item(k).getNodeName());
					if(timeLine.item(k).getNodeName().equalsIgnoreCase("SegmentTimeline")){
						segTimeLine = timeLine.item(k);
					}
				}






				if(segTimeLine != null){
					tempSegTemplType.setSegmentTimeline(new SegmentTimelineType());
					for(int l = 0; l < segTimeLine.getChildNodes().getLength(); l ++){
						Node timelineItem = segTimeLine.getChildNodes().item(l);

						//System.out.println("item: " + l + ": " + timelineItem.getNodeName());

						if(timelineItem.getNodeName().equalsIgnoreCase("S")){
							S seg = new S();

							NamedNodeMap segAttrs = timelineItem.getAttributes();

							Node dNode = segAttrs.getNamedItem("d");
							if(dNode != null){
								String d = dNode.getNodeValue();
								long D = Long.parseLong(d);
								seg.setD(D);

							}				

							Node tNode = segAttrs.getNamedItem("t");
							if(tNode != null){
								String t = tNode.getNodeValue();
								long T = Long.parseLong(t);
								seg.setT(T);

							}	

							Node rNode = segAttrs.getNamedItem("r");
							if(rNode != null){
								String r = rNode.getNodeValue();
								long R = Long.parseLong(r);
								seg.setR(R);

							}	


							tempSegTemplType.getSegmentTimeline().getS().add(seg);

						}
					}
				}


				tempAdapt.setSegmentTemplate(tempSegTemplType);

				//System.out.println();

				//load representations
				NodeList reps = period.getElementsByTagName("Representation");
				//System.out.println("Number of reps found: " + reps.getLength());

				ArrayList<RepresentationType> repObjs = new ArrayList<RepresentationType>();
				for(int j = 0; j < reps.getLength(); j++){
					//System.out.println("attributes for representation: " + j);
					NamedNodeMap attrs = reps.item(j).getAttributes();
					iterate(attrs);


					RepresentationType tempRep = new RepresentationType();

					//load values in
					String id = attrs.getNamedItem("id").getNodeValue();
					//System.out.println("storing id: " + id);
					tempRep.setId(id);

					String mime = attrs.getNamedItem("mimeType").getNodeValue();
					//System.out.println("storing mimetype: " + mime);
					tempRep.setMimeType(mime);

					String codecs = attrs.getNamedItem("codecs").getNodeValue();
					//System.out.println("storing codecs: " + codecs);
					tempRep.setCodecs(codecs);

					Node width1 = attrs.getNamedItem("width");

					if(width1 != null){
						long width = Long.parseLong(width1.getNodeValue());
						//System.out.println("storing width: " + width);
						tempRep.setWidth(width);

					}

					Node height1 = attrs.getNamedItem("height");
					if(height1 != null){
						long height = Long.parseLong(height1.getNodeValue());
						//System.out.println("storing height: " + height);
						tempRep.setHeight(height);
					}

					Node fRate1 = attrs.getNamedItem("frameRate");
					if(fRate1 != null){
						String fRate = fRate1.getNodeValue();
						//System.out.println("storing frameRate: " + fRate);
						tempRep.setFrameRate(fRate);

					}

					Node sar1 = attrs.getNamedItem("sar");
					if(sar1 != null){
						String sar = sar1.getNodeValue();
						//System.out.println("storing sar: " + sar);
						tempRep.setSar(sar);
					}

					Node sap1 = attrs.getNamedItem("startWithSAP");
					if(sap1 != null){
						long sap = Long.parseLong(sap1.getNodeValue());
						//System.out.println("storing startWithSAP: " + sap);
						tempRep.setStartWithSAP(sap);
					}

					long bandwidth = Long.parseLong(attrs.getNamedItem("bandwidth").getNodeValue());
					//System.out.println("storing bandwidth: " + bandwidth);
					tempRep.setBandwidth(bandwidth);

					//store in adaptationset
					tempAdapt.getRepresentation().add(tempRep);


				}


				curPeriod.getAdaptationSet().add(tempAdapt);
			}

			mpd.getPeriod().add(curPeriod);

		}

		return mpd;
	}

	private static void iterate(NamedNodeMap attributesList) {
		for (int j = 0; j < attributesList.getLength(); j++) {
			//System.out.println("Attribute: "
	//				+ attributesList.item(j).getNodeName() + " = "
	//				+ attributesList.item(j).getNodeValue());
		}
		//System.out.println();
	}

}
