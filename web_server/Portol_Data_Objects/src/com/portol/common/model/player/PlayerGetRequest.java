package com.portol.common.model.player;

import java.io.Serializable;

/**
 * Created by alex on 9/2/15.
 */
public class PlayerGetRequest implements Serializable {


    /**
	 * 
	 */
	private static final long serialVersionUID = -8007221321083803479L;
	public final String TEXT_ID = "TEXT";
    public final String QR_ID = "QR_CODE";
    public static final String PLAYER_ID ="PLAYER_ID";
    private String paringCode;
    private String pairingType;


    public String getPairingType() {
        return pairingType;
    }

    public void setPairingType(String pairingType) {
        this.pairingType = pairingType;
    }

    public PlayerGetRequest( String pairingCode) {
        this.paringCode = pairingCode;
    }

    public String getParingCode() {
        return paringCode;
    }

    public void setParingCode(String paringCode) {
        this.paringCode = paringCode;
    }

    public PlayerGetRequest() {
    }
}
