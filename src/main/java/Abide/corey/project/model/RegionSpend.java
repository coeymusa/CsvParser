package Abide.corey.project.model;

import java.math.BigDecimal;

public class RegionSpend {


	String postcode;
	
	BigDecimal spend=BigDecimal.valueOf(0);
	
	public RegionSpend(String postcode, BigDecimal spend){
		setPostcode(postcode);
		setSpend(spend);
	}
	
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public BigDecimal getSpend() {
		return spend;
	}
	public void setSpend(BigDecimal spend) {
		this.spend = spend;
	}



}
