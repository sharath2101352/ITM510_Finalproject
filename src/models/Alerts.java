package models;

public class Alerts {

	private String id;
	private String alert;
	private String status;
	private String timestamp;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAlert() {
		return alert;
	}
	public void setAlert(String alert) {
		this.alert = alert;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCreatedTimestamp() {
		return timestamp;
	}
	public void setCreatedTimestamp(String createdTimestamp) {
		this.timestamp = createdTimestamp;
	}


}
