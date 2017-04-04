package com.portol.common.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.mongojack.Id;

/**
 * Created by alex on 6/8/15.
 *
 * Used to offer dynamically changing categories
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category implements Serializable  {

    /**
	 * 
	 */
	private static final long serialVersionUID = -435723044313107458L;

	public static final String VIDEO = "VIDEO";
    public static final String MUSIC = "MUSIC";
    
    private String version;
    
    private String iconEncoded;
    private String desc;
    private String name;

    @Id
    private String categoryId = UUID.randomUUID().toString();

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private int position;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    private Date validDate;

    private Date expirationDate;


    public Category() {
    }

    public Category(String name){

    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String id) {
        this.categoryId = id;
    }

    public Date getValidDate() {
        return validDate;
    }

    public void setValidDate(Date validDate) {
        this.validDate = validDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIconEncoded() {
		return iconEncoded;
	}

	public void setIconEncoded(String icon) {
		this.iconEncoded = icon;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}



}
