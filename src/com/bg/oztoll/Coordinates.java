/**
 * 
 */
package com.bg.oztoll;

/**
 * @author bugman
 *
 */
public class Coordinates {
	private long x, y;
	
	public Coordinates (long x, long y){
		this.x=x;
		this.y=y;
	}
	
	public Coordinates() {
	}

	public void setX(long x){
		this.x=x;
	}
	
	public void setY(long y){
		this.y=y;
	}
	
	public long getX(){
		return x;
	}
	
	public long getY(){
		return y;
	}
}
