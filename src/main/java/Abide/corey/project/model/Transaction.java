package Abide.corey.project.model;

public class Transaction {
	String surgeryId;
	String bnfCode;
	String bnfName;
	Integer quantity;
	Double price;
	
	public String getSurgeryId() {
		return surgeryId;
	}
	public void setSurgeryId(String surgeryId) {
		this.surgeryId = surgeryId;
	}
	public String getBnfCode() {
		return bnfCode;
	}
	public void setBnfCode(String bnfCode) {
		this.bnfCode = bnfCode;
	}
	public String getBnfName() {
		return bnfName;
	}
	public void setBnfName(String bnfName) {
		this.bnfName = bnfName;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	
	
}
