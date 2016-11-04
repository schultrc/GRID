package edu.ucdenver.cse.GRIDmessages;

import java.io.Serializable;

import edu.ucdenver.cse.GRIDclient.GRIDrequest;


public class GRIDtimeMsg  implements GRIDrequest, Serializable {
	/**
	 * 
	 */
	
	
	private static final long serialVersionUID = 1L;
	
	private Long theTime;
	
	public Long getTheTime() {
		return theTime;
	}

	public void setTheTime(Long theTime) {
		this.theTime = theTime;
	}

	public GRIDtimeMsg(Long time) {
		this.theTime = time;
	}

	@Override
	public String toString() {
		return "GRIDtimeMsg [" +this.theTime + "]";
	}
}
