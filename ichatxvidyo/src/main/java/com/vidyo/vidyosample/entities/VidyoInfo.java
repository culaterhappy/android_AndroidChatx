package com.vidyo.vidyosample.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.google.gson.Gson;

public class VidyoInfo implements Parcelable {

	String vidyoUsername;
	String vidyoPassword;
	String vidyoPAK;
	String vidyoHost;
	String vidyoRoomId;
	String calledDestUser;
	String calledDestEntityID;

	private String guest_host;
	private int guest_port;
	private String guest_key;
	private String guest_userName;
	private String guest_pin;

	public String getCalledDestUser() {
		return calledDestUser;
	}

	public void setCalledDestUser(String dest) {
		this.calledDestUser = dest;
	}

	public String getCalledDestEntityID() {
		return calledDestEntityID;
	}

	public void setCalledDestEntityID(String dest) {
		this.calledDestEntityID = dest;
	}


	public String getVidyoUsername() {
		return vidyoUsername;
	}
	
	public void setVidyoUsername(String vidyoUsername) {
		this.vidyoUsername = vidyoUsername;
	}
	
	public String getVidyoPassword() {
		return vidyoPassword;
	}
	
	public void setVidyoPassword(String vidyoPassword) {
		this.vidyoPassword = vidyoPassword;
	}
	
	public String getVidyoPAK() {
		return vidyoPAK;
	}
	
	public void setVidyoPAK(String vidyoPAK) {
		this.vidyoPAK = vidyoPAK;
	}
	
	public String getVidyoHost() {
		return vidyoHost;
	}
	
	public void setVidyoHost(String vidyoHost) {
		this.vidyoHost = vidyoHost;
	}
	
	public String getVidyoRoomId() {
		return vidyoRoomId;
	}
	
	public void setVidyoRoomId(String vidyoRoomId) {
		this.vidyoRoomId = vidyoRoomId;
	}
	
	public String getEncodedUsernamePassword() {
		 final String userPass = vidyoUsername + ":" + vidyoPassword;
		 return Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		final Gson gson = new Gson();
		dest.writeString(gson.toJson(this));
	}

    public static final Parcelable.Creator<VidyoInfo> CREATOR = new Parcelable.Creator<VidyoInfo>() {
        @Override
		public VidyoInfo createFromParcel(final Parcel in) {
        	final Gson gson = new Gson();
            return gson.fromJson(in.readString(), VidyoInfo.class);
        }

        @Override
		public VidyoInfo[] newArray(final int size) {
            return new VidyoInfo[size];
        }
    };

	public String getGuest_host() {
		return guest_host;
	}

	public void setGuest_host(String guest_host) {
		this.guest_host = guest_host;
	}

	public int getGuest_port() {
		return guest_port;
	}

	public void setGuest_port(int guest_port) {
		this.guest_port = guest_port;
	}

	public String getGuest_key() {
		return guest_key;
	}

	public void setGuest_key(String guest_key) {
		this.guest_key = guest_key;
	}

	public String getGuest_userName() {
		return guest_userName;
	}

	public void setGuest_userName(String guest_userName) {
		this.guest_userName = guest_userName;
	}

	public String getGuest_pin() {
		return guest_pin;
	}

	public void setGuest_pin(String guest_pin) {
		this.guest_pin = guest_pin;
	}
}
