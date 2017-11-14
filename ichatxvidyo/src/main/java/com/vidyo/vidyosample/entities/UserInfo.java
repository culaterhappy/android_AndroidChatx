package com.vidyo.vidyosample.entities;

public class UserInfo {

	private String server;
	private String name;
	private String password;
	private String entityID;

	private static UserInfo userInfo = null;

	public static UserInfo getInstance() {
		if (userInfo == null) {
			synchronized (UserInfo.class) {
				if (userInfo == null) {
					userInfo = new UserInfo("", "", "", "");
				}
			}
			return userInfo;
		} else {
			return userInfo;
		}

	}

	private UserInfo(String server, String name, String password,
			String entityID) {
		super();
		this.server = server;
		this.name = name;
		this.password = password;
		this.entityID = entityID;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEntityID() {
		return entityID;
	}

	public void setEntityID(String entityID) {
		this.entityID = entityID;
	}

	@Override
	public String toString() {
		return "UserInfo [server=" + server + ", name=" + name + ", password="
				+ password + "]";
	}

}
