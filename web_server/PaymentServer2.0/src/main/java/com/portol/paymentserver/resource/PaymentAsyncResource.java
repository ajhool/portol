package com.portol.paymentserver.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

import com.portol.common.model.payment.Payment;
import com.portol.common.model.player.Player;
import com.portol.common.model.player.Player.Status;
import com.portol.paymentserver.repository.PlayerRepository;
import com.portol.paymentserver.service.BalanceCheckerClient;
import com.portol.paymentserver.service.BalanceMonitor;
import com.portol.paymentserver.service.BalanceMonitor.OnRelevantAddressChangeListener;
import com.portol.paymentserver.service.LoadbalCommunicator;

public class PaymentAsyncResource implements Managed, OnRelevantAddressChangeListener {

	private BalanceMonitor balMon; 
	private PlayerRepository playerRepo; 
	private LoadbalCommunicator lbComm; 
	private BalanceCheckerClient balCheck;

	private static final Logger logger = LoggerFactory.getLogger(PaymentAsyncResource.class);

	public PaymentAsyncResource(BalanceMonitor monitor,
			PlayerRepository playerRepo, LoadbalCommunicator lbComm, BalanceCheckerClient balcheck) {
		super();

		monitor.setAddressChangeListener(this);
		this.balCheck = balcheck;
		this.balMon = monitor;
		this.playerRepo = playerRepo;
		this.lbComm = lbComm;
	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void onPayment(String btcAddress, long amountReceivedSatoshi) {
		//update player in playerRepo. if player is dead/missing, trigger address removal
		int amtReceivedBits = (int) ((double)amountReceivedSatoshi / (double)1000);

		Player hasAddress = null;
		try {
			hasAddress = playerRepo.findOneByAddress(btcAddress);
		} catch (Exception e) {
			logger.error("error retreiving player for comparison", e);
		}

		if(hasAddress == null || hasAddress.getStatus() == Status.DEAD){
			balMon.unWatchAddress(btcAddress);
			return;
		}

		if(hasAddress.getPlayerPayment().getStatus() == Payment.Status.COMPLETE){
			logger.error("making payment to a complete address, should trigger automatic refund here");
		}
		
		int prevBal = hasAddress.getPlayerPayment().getTotReceived();

		prevBal += amtReceivedBits;

		hasAddress.getPlayerPayment().setTotReceived(prevBal);

		//if the amount received is enough, fire off an alert to the loadbal async start
		Payment playerPayment = hasAddress.getPlayerPayment(); 

		//this is the range we accept in bits
		int epsilon = 1;
		if(playerPayment.getTotReceived() >= playerPayment.getTotRequested() - epsilon){
			//then we have a successful payment that went through
			//update status
			playerPayment.setStatus(Payment.Status.COMPLETE);
		}

		hasAddress.setPlayerPayment(playerPayment);

		playerRepo.save(hasAddress);
		lbComm.earlyLoad(hasAddress);


	}
}
