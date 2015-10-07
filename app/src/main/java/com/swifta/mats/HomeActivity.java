package com.swifta.mats;

import com.swifta.mats.util.Contants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class HomeActivity extends Activity {

	private HomeActivity self = this;
	private String myName = "";
	private boolean btn_clicked = true;
	
	private Button withdrawalBtn;
	private Button float_transferBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		SharedPreferences sharedPref = self.getSharedPreferences(Contants.STORE_USERNAME_KEY, 
				Context.MODE_PRIVATE);
		myName = sharedPref.getString("username", "UNKNOWN").toUpperCase();
		self.setTitle("Welcome "+myName);
		self.getActionBar().setLogo(R.drawable.ic_person_pin_black_24dp);
		initEvents();
		btn_clicked = false;
	}
	
	private void initEvents(){
		withdrawalBtn = (Button) findViewById(R.id.withdrawal);
		float_transferBtn = (Button) findViewById(R.id.float_transfer);
		
		withdrawalBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btn_clicked = true;
				Intent actIntent = new Intent(self, WithdrawalActivity.class);
				self.startActivity(actIntent);				
			}
			
		});
		
		float_transferBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btn_clicked = true;
				Intent actIntent = new Intent(self, FloatTransferActivity.class);
				self.startActivity(actIntent);				
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//System.out.println("Back was pressed with ID:"+item.getTitle());
		if (id == R.id.logout) {
			//logout clicked
			onBackPressed();
			return true;
		}
		if(id == android.R.id.home){
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
/*	public void onDestroy(){
		super.onDestroy();
		if(!btn_clicked){
			logout();
		}
	}*/
	
    public void onBackPressed(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Do you want to logout?")
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				logout();
			}
    		
    	})
    	.setNegativeButton("No", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//builder
			}
    		
    	}).show();
    }
    
    private void logout(){
		 Intent actIntent = new Intent(self, MainActivity.class);
		 actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		 startActivity(actIntent);
		 finish();
    }
   
}
