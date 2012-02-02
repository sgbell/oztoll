/**
 * 
 */
package com.bg.oztoll;

/**
 * @author bugman
 *
 */
public class Connection extends Pathway {
	private String startTollway, endTollway;
	
	public Connection(Street newStart, Street newEnd) {
		super(newStart, newEnd);
		setStartTollway("");
		setEndTollway("");
	}

	public Connection(Street newStart, String startTollway, Street newEnd, String endTollway){
		super(newStart, newEnd);
		this.setStartTollway(startTollway);
		this.setEndTollway(endTollway);
	}

	/**
	 * @return the startTollway
	 */
	public String getStartTollway() {
		return startTollway;
	}

	/**
	 * @param startTollway the startTollway to set
	 */
	public void setStartTollway(String startTollway) {
		this.startTollway = startTollway;
	}

	/**
	 * @return the endTollway
	 */
	public String getEndTollway() {
		return endTollway;
	}

	/**
	 * @param endTollway the endTollway to set
	 */
	public void setEndTollway(String endTollway) {
		this.endTollway = endTollway;
	}
}
