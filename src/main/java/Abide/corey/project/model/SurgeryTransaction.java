package Abide.corey.project.model;

public class SurgeryTransaction {

	String surgeryId ;
	Double spent = 0.00;
	String postcode;
	String region;

	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getSurgeryId() {
		return surgeryId;
	}
	public void setSurgeryId(String surgeryId) {
		this.surgeryId = surgeryId;
	}
	public Double getSpent() {
		return spent;
	}
	public void setSpent(Double spent) {
		this.spent = spent + this.spent;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	
	
}
