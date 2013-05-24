/** This is code that I found in a tutorial at
 *  http://techdroid.kbeanie.com/2010/09/expandablelistview-on-android.html
 *  This class is used in the OzToll Text Activity, so that the user can select the
 *  streets from a list to get the cost of their trip.
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * @author bugman
 *
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
	private Context appContext;
	private ArrayList<String> tollways;
	private ArrayList<ArrayList<String>> streets;
	private String start="";
	
	public ExpandableListAdapter(Context context, ArrayList<String> tollways,
								 ArrayList<ArrayList<String>> streets){
		appContext = context;
		this.tollways=tollways;
		this.streets=streets;
	}

	public void resetView(){
		tollways=new ArrayList<String>();
		streets=new ArrayList<ArrayList<String>>();
		start="";
	}
	
	public void addTollway(String tollway){
		boolean tollwayFound=false;
		for (int twc=0; twc < tollways.size(); twc++){
			if (tollways.get(twc).equalsIgnoreCase(tollway))
				tollwayFound=true;
			if (!tollwayFound)
				tollways.add(tollway);
		}
	}
	
	public void addStart(String tollway, String street) {
		start=street;
		
		if (!tollways.contains(tollway)){
			tollways.add(tollway);
		}
		int index=tollways.indexOf(tollway);
		if (streets.size()<index+1){
			streets.add(new ArrayList<String>());
		}
		if (!streets.get(index).contains(street))
			streets.get(index).add(0,street);
	}

	public void addStreet(String tollway, String street){
		if (!tollways.contains(tollway)){
			tollways.add(tollway);
		}
		int index=tollways.indexOf(tollway);
		if (streets.size()<index+1){
			streets.add(new ArrayList<String>());
		}
		if (!streets.get(index).contains(street))
			streets.get(index).add(street);
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return streets.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		String street = (String) getChild(groupPosition,childPosition);
		if (convertView == null){
			LayoutInflater infalInflator = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflator.inflate(R.layout.list_street,null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.streetName);
		if (street.equalsIgnoreCase(start)){
			tv.setBackgroundColor(Color.BLUE);
		} else {
			tv.setBackgroundColor(Color.TRANSPARENT);
		}
		tv.setText(street);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return streets.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return tollways.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return tollways.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String tollway = (String) getGroup(groupPosition);
		if (convertView == null){
			LayoutInflater infalInflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_street, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.streetName);
		tv.setText("     "+tollway);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
