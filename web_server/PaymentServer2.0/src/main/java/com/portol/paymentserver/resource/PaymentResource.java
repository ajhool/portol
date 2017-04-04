package com.portol.paymentserver.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitpay.common.controller.BitPay;
import com.bitpay.common.controller.BitPayException;
import com.bitpay.common.model.Invoice;
import com.bitpay.common.model.PortolInvoice;
import com.codahale.metrics.annotation.Timed;
import com.portol.common.model.payment.Payment;
import com.portol.common.model.payment.Payment.Status;
import com.portol.common.model.payment.PaymentInfoRequest;
import com.portol.paymentserver.service.BalanceCheckerClient;
import com.portol.paymentserver.service.BalanceMonitor;

@Path("/api/v0/payment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

	public static final String net = "test";



	public static final int MAX_USAGE = 100;
	public static final int MAX_MASTER_TO_GET = 20;
	public static final int MIN_UNUSED = 120;
	final static Logger logger = LoggerFactory.getLogger(PaymentResource.class);

	private final Client jClient; 
	private BitPay bitpay; 
	private BalanceMonitor balMon; 

	private final BalanceCheckerClient balCheck; 

	private static final String clientName = "portol1";


	public PaymentResource(BalanceCheckerClient bCheck, Client jClient, BalanceMonitor monitor) throws Exception {

		logger.info("in constructor for contentresource");

		this.jClient = jClient;

		this.balCheck = bCheck;

		this.balMon = monitor;

		logger.debug("Setting up bitpay connection");

		bitpay = new BitPay(clientName);

		if (!bitpay.clientIsAuthorized(BitPay.FACADE_POS))
		{
			// Get POS facade authorization code.
			String pairingCode = bitpay.requestClientAuthorization(BitPay.FACADE_POS);

			// Signal the device operator that this client needs to be paired with a merchant account.
			System.out.println("Info: Pair this client with your merchant account using the pairing code: " + pairingCode);
			throw new BitPayException("Error: client is not authorized for pos facade.");
		}

		logger.info("finished constructor for contentresource");
	}

	@POST
	@Path("/newpayment")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public List<Payment> getbtcaddrs(PaymentInfoRequest request) throws Exception {

		if(!request.getApiKey().equalsIgnoreCase("foo")){
			return null;
		}

		int numToMake = request.getNumAddrRequested();
		int[] vals = request.getValuesRequested();

		ArrayList<Payment> addr_list = new ArrayList<Payment>();

		//bitpay based implementation
		for(int i = 0; i < numToMake; i++){
			//since the price is in bits
			Invoice temp = new Invoice((double)(vals[i] / (double) 100000), "BTC");
			temp.setFullNotifications(false);
			temp.setExpirationTime(System.currentTimeMillis() + 9999*1000);
			temp.setPosData(System.nanoTime() + "");

			int numTries = 5;

			boolean successful = false;

			while (!successful){
				try {
					temp = this.bitpay.createInvoice(temp);
					successful = true;
				} catch (Exception e1){
					logger.warn("bitpay server failed to respond", e1);
					int waitTime = 1000;
					logger.info("waiting " + waitTime + " ms and retrying");
					successful = false;
					Thread.sleep(waitTime);
					numTries --;
					if(numTries < 0 || e1.getMessage().contains("limit")){
						throw new Exception("Max number of tries exceeded");
					}

				}

			}

			Payment pmt = new Payment();

			String bip21 = temp.getPaymentUrls().getBIP21();
			String btcAddr = bip21.substring(bip21.indexOf(":") + 1, bip21.indexOf("?"));
			pmt.setBtcPaymentAddr(btcAddr);
			pmt.setBitPayinvoice(new PortolInvoice(temp));
			int startingBal = balCheck.addressBalanceGetter(pmt.getBtcPaymentAddr());
			pmt.setOldBalance(startingBal/ 1000);
			pmt.setTotRequested(vals[i]);
			pmt.setStatus(Status.UNUSED);
			balMon.watchNewAddress(btcAddr);

			addr_list.add(pmt);


		}

		return addr_list;
	}





}
