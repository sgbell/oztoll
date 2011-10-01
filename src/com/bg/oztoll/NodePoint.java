/**
 * 
 */
package com.bg.oztoll;

/**
 * @author bugman
 *
 */
public class NodePoint {
	private TollPoint a,
					  b;
	private float price;
	
	public NodePoint(TollPoint a, TollPoint b, float price){
		this.a = a;
		this.b = b;
		this.price = price;
	}
	
	public TollPoint getPointA(){
		return a;
	}
	
	public TollPoint getPointB(){
		return b;
	}
	
	public float getPrice(){
		return price;
	}
	
	public void setPointA(TollPoint newPoint){
		a = newPoint;
	}
	
	public void setPointB(TollPoint newPoint){
		b = newPoint;
	}
	
	public void setPrice(float newprice){
		price = newprice;
	}
}
