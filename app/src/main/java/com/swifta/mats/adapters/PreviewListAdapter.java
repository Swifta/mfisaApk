package com.swifta.mats.adapters;

import com.swifta.mats.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PreviewListAdapter extends ArrayAdapter<String>{
	 private Activity context;
	 private String[] left;
	 private String[] right;
	 
	 public PreviewListAdapter(Activity p, String[] l, String[] r){
		 super(p, R.layout.my_list, l);
		 this.context = p;
		 this.left = l;
		 this.right = r;
	 }
	 
	 public void setList(String l[], String r[]){
		 this.left = l;
		 this.right = r;
	 }
	 
	 public View getView(int position, View view,ViewGroup parent) {
		 LayoutInflater inflater = context.getLayoutInflater();
		 View rowView = inflater.inflate(R.layout.my_list, null,true);
		 
		 TextView txtLeft = (TextView) rowView.findViewById(R.id.left);
		 TextView txtRight = (TextView) rowView.findViewById(R.id.right);
		 
		 //System.out.println("I counted "+this.getCount());
		 
		 txtLeft.setText(left[position]);
		 txtRight.setText(right[position]);
		 
		 return rowView;
		 
	 }
}
