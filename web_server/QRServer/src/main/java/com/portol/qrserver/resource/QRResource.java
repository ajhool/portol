package com.portol.qrserver.resource;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.portol.common.model.QRReply;
import com.portol.common.model.QRRequest;
import com.portol.qrserver.config.QRConfiguration;

@Path("/api/v0/qr")
public class QRResource {

	private File qrDirRoot;
	private String qrDirHost;
	private int port;
	private String protocol;
	private String fileType = "png";


	private static final Logger logger = LoggerFactory.getLogger(QRResource.class);

	public QRResource(QRConfiguration qrConf) {
		qrDirRoot = new File(qrConf.qrDirRoot);
		qrDirRoot.mkdirs();
		qrDirHost = qrConf.hostAddr;
		this.port = qrConf.port;
		this.protocol = qrConf.protocol;

	}

	@POST
	@Path("/qrmake")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public QRReply makeQR(QRRequest incomingReq, @Context HttpServletRequest req){

		if(incomingReq == null){
			throw new BadRequestException();
		}

		if(!incomingReq.getApiKey().equalsIgnoreCase("foo")){
			throw new ForbiddenException();
		}

		String qrContents = ""; 
		if(!incomingReq.isComplete()){
			StringBuffer qrContentBuilder = new StringBuffer();

			qrContentBuilder.append(incomingReq.getProtocol());
			qrContentBuilder.append(incomingReq.getAddress());
			qrContentBuilder.append("?amount=");
			qrContentBuilder.append(incomingReq.getAmount());
			qrContentBuilder.append("&r=");
			qrContentBuilder.append(incomingReq.getPaymentReqServer());
			qrContentBuilder.append("?%");
			qrContentBuilder.append(incomingReq.getServerParams());
			qrContents = qrContentBuilder.toString();


		} else {
			//we have a complete URL
			qrContents = incomingReq.getCompleteURL();
		}

		logger.debug("Complete string to be encoded into QR format: " + qrContents);
		boolean success = makeQRFile(incomingReq, qrContents);

		if(success){
			QRReply reply = new QRReply();

			reply.setQrURL(protocol + qrDirHost + ":" + port + "/" + incomingReq.id + "." + fileType);
			return reply;
		} else return null;

	}

	private boolean makeQRFile(QRRequest incomingReq, String qrContents) {
		int size = 600;

		File myFile = new File(qrDirRoot + "/" + incomingReq.id + "." + this.fileType);
		try {
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(qrContents,BarcodeFormat.QR_CODE, size, size, hintMap);
			int myWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(myWidth, myWidth,
					BufferedImage.TYPE_INT_RGB);
			image.createGraphics();

			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, myWidth, myWidth);
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < myWidth; i++) {
				for (int j = 0; j < myWidth; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			ImageIO.write(image, fileType, myFile);
			return true;
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
