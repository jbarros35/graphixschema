package com.graphixschema.database.vo;

public class ConnectionProperties implements java.io.Serializable {

	private String url = "";
	private String user = "";
	private String password = "";
	private String schema = "";
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}

	
	
}
