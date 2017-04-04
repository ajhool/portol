package com.portol.paymentserver.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.KeyCrypter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public abstract class ECKeyModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 373210924048822134L;

	@JsonProperty("privateKeyEncoded")
	private  byte[] priv;
	
	@JsonProperty("wholeKeyEncoded")
	private byte[] encodedKey;
	
	private KeyCrypter keyCrypter; 
	
	//maintain public key hash here for quick reference
	private String pubkey;

	private byte[] encryptedPrivKey;
	
	private Date created; 
	
	public ECKeyModel(ECKey toUse, NetworkParameters params){
		this.priv = toUse.getPrivKey().toByteArray();
		this.created = new Date(System.currentTimeMillis());
		this.encodedKey =   toUse.toASN1();
		this.pubkey = toUse.toAddress(params).toString();
	}
	
	public ECKeyModel(){
		this.created = new Date(System.currentTimeMillis());
	}
	
	@JsonIgnore
	public void setEncodedKey(ECKey toSet) {
		this.encodedKey = toSet.toASN1();
	}
	
	@JsonIgnore
	public ECKey getKey(){
		return ECKey.fromASN1(encodedKey);
	}
	
	public Date getCreated() {
		return created;
	}

	public String getPubkey() {
		return pubkey;
	}

	public void setPubkey(String pubkey) {
		this.pubkey = pubkey;
	}

	@JsonIgnore
	public BigInteger getPriv() {
		return new BigInteger(priv);
	}

	@JsonIgnore
	public void setPriv(BigInteger priv) {
		this.priv = priv.toByteArray();
	}

	public KeyCrypter getKeyCrypter() {
		return keyCrypter;
	}

	public void setKeyCrypter(KeyCrypter keyCrypter) {
		this.keyCrypter = keyCrypter;
	}

	public byte[] getEncryptedPrivKey() {
		return encryptedPrivKey;
	}

	public void setEncryptedPrivKey(byte[] encryptedPrivKey) {
		this.encryptedPrivKey = encryptedPrivKey;
	}
	
}
