package com.portol.paymentserver.service;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

public class BalanceMonitor implements Managed{

	public interface OnRelevantAddressChangeListener{
		public void onPayment(String btcAddress, long amountReceived);
	}

	private class WatchedAddress{
		Timer expirationTimer; 
		Address watching;
	}

	//1 hour timeout
	public static final long EXPIRATION_TIME_MS = 3600 * 1000;

	private OnRelevantAddressChangeListener callback;

	private static final Logger logger = LoggerFactory.getLogger(BalanceMonitor.class);

	private ConcurrentHashMap<String, WatchedAddress> currentAddrs = new ConcurrentHashMap<String, WatchedAddress>();
	final NetworkParameters params = TestNet3Params.get();
	WalletAppKit kit;

	File walletFile = new File("balanceCheckerWallet");

	public void setAddressChangeListener(OnRelevantAddressChangeListener listener){
		this.callback = listener; 
	}

	private Address onTimerExpire(String expiredAddress){
		logger.info("address: " + expiredAddress + " is expiring");

		WatchedAddress expiring = currentAddrs.remove(expiredAddress);
		expiring.expirationTimer.cancel();

		kit.wallet().removeWatchedAddress(expiring.watching);
		return expiring.watching;

	}

	public BalanceMonitor(){
		//initialize files and stuff here

		kit = new WalletAppKit(params,  walletFile, "_wak"); 
		kit.setAutoSave(true); 
		kit.startAsync(); 

		kit.awaitRunning();
		kit.peerGroup().addPeerDiscovery(new DnsDiscovery(params)); 
		kit.wallet().addEventListener(new AbstractWalletEventListener() {
			@Override
			public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
				super.onCoinsReceived(wallet, tx, prevBalance, newBalance);
				if(callback != null){
					long received = newBalance.value-prevBalance.value;

					for (TransactionOutput txop : tx.getOutputs()){
						Address address = txop.getScriptPubKey().getToAddress(params);
						logger.debug("checking address: " + address.toString());

						if(currentAddrs.containsKey(address.toString())){
							callback.onPayment(address.toString(), received);
						} else {
							//we dont care about the address 
							try{
								kit.wallet().removeWatchedAddress(address);
							} catch(Exception e){
								logger.error("error unwatching address",e );
							}
						}

					}

				}

			}
		});


	}

	public Address unWatchAddress(String addrToUnwatch){
		//basically fake a timeout
		return onTimerExpire(addrToUnwatch);
	}

	public Address watchNewAddress(String btcAddr){
		Address addy = null;
		try {
			addy =  new Address(params, btcAddr);


		} catch (AddressFormatException e) {

			logger.error("error watching new address", e);

		}

		if(addy == null){
			logger.error("null address, no longer watching");
		}

		kit.wallet().addWatchedAddress(addy);
		WatchedAddress me = new WatchedAddress();

		Timer thisTimer = new Timer();
		thisTimer.schedule(this.generateNewTimeoutTask(btcAddr), EXPIRATION_TIME_MS);
		long now = System.currentTimeMillis(); 

		Date nowDate = new Date(now);
		Date expiryDate = new Date(now + EXPIRATION_TIME_MS);
		logger.info("current time is: " + nowDate + ", scheduled expiry time: " + expiryDate);

		me.expirationTimer = thisTimer;
		me.watching = addy;

		WatchedAddress existing = currentAddrs.put(addy.toString(), me);

		if(existing != null){
			if(existing.expirationTimer != null){
				existing.expirationTimer.cancel();
			}
		}

		return addy;
	}

	private TimerTask generateNewTimeoutTask(String btcAddr){

		final String toLoad = btcAddr;
		TimerTask myTask = new TimerTask() {

			@Override
			public void run() {
				//do action
				logger.info("bitcoin address: " + toLoad + "timed out");
				onTimerExpire(toLoad);

			}
		};

		return myTask;
	}

	@Override
	public void start() throws Exception {
		logger.info("balancemonitor started");

	}

	@Override
	public void stop() throws Exception {
		logger.info("killing wallet and removing wallet file");
		kit.stopAsync();
		FileUtils.deleteQuietly(walletFile);

	}



}
