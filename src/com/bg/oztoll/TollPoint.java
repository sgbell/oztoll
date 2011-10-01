/**
 * 
 */
package com.bg.oztoll;

/**
 * @author bugman
 *
 */
public class TollPoint {
	private int x,
				y;
	
	public TollPoint(int newx, int newy){
		x=newx;
		y=newy;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int x){
		this.x=x;
	}
	
	public void setY(int y){
		this.y=y;
	}
}
