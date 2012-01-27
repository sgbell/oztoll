/**
 * 
 */
package com.bg.oztoll;

/**
 * @author bugman
 *
 */
public class ScreenCoordinates {
	private float x,y;

	public ScreenCoordinates(float x, float y){
		this.x=x; 
		this.y=y;
	}
	
	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}

	public void updateX(float x2) {
		x+=x2;
	}

	public void updateY(float y2) {
		y+=y2;
	}

	public void reset() {
		x=y=0;
	}
}
