package com.bean;

import java.sql.Timestamp;

public class Pay {

	private String ownername, no, bat, amt;
	private String payMode, email;
	private int slotId;
	private Timestamp startTime, endTime;

	public String getOwnername() { return ownername; }
	public void setOwnername(String ownername) { this.ownername = ownername; }

	public String getNo() { return no; }
	public void setNo(String no) { this.no = no; }

	public String getBat() { return bat; }
	public void setBat(String bat) { this.bat = bat; }

	public String getAmt() { return amt; }
	public void setAmt(String amt) { this.amt = amt; }

	public String getPayMode() { return payMode; }
	public void setPayMode(String payMode) { this.payMode = payMode; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public int getSlotId() { return slotId; }
	public void setSlotId(int slotId) { this.slotId = slotId; }

	public Timestamp getStartTime() { return startTime; }
	public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

	public Timestamp getEndTime() { return endTime; }
	public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

}