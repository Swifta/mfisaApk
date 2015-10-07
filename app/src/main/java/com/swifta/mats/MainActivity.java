package com.swifta.mats;

import org.json.JSONException;
import org.json.JSONObject;

import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Contants;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends Activity{

	private MainActivity self = this;
	private Button sign_in;
	private LinearLayout form_frame;
	private LinearLayout loading;
	private EditText usernameTxt;
	private EditText passwordTxt;
	private CheckBox show_pwd;
	
	
	private boolean busy = false;
	private boolean status = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        form_frame = (LinearLayout) findViewById(R.id.form_frame);
        loading = (LinearLayout) findViewById(R.id.loading);
        
        show_pwd = (CheckBox) findViewById(R.id.show_pwd);
        
        usernameTxt = (EditText) findViewById(R.id.username);
        passwordTxt = (EditText) findViewById(R.id.password);
        
        sign_in = (Button)findViewById(R.id.sign_in);
        sign_in.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(com.swifta.mats.util.InternetCheck.isNetworkAvailable(self)){
					if(usernameTxt.getText().toString().isEmpty() || 
							passwordTxt.getText().toString().isEmpty()){
						Toast.makeText(self, "Please supply your credentials", Toast.LENGTH_LONG).show();
					}
					else{
						form_frame.setVisibility(View.GONE);
						loading.setVisibility(View.VISIBLE);
						JSONObject data = new JSONObject();
						try {
							data.put("username", usernameTxt.getText().toString());
							data.put("password", passwordTxt.getText().toString());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}				
						Intent intent = new Intent(self, BackgroundServices.class);
					    intent.putExtra(Contants.JOB_IDENTITY, ApiJobs.LOGIN);
					    intent.putExtra(Contants.JOB_DATA, data.toString());
					    startService(intent);
					    busy = true;
					}
				}
				else{
					Toast.makeText(self, "Please connect to the Internet", Toast.LENGTH_LONG).show();
				}
			}
        	
        });
        
        show_pwd.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					passwordTxt.setInputType(InputType.TYPE_CLASS_TEXT);
					show_pwd.setEnabled(false);
					show_pwd.setClickable(false);
				}

			}
        	
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
      super.onResume();
      registerReceiver(receiver, new IntentFilter(Contants.SERVICE_NOTIFICATION));
    }
    
    @Override
    protected void onPause() {
      super.onPause();
      unregisterReceiver(receiver);
    }
    
    public void onBackPressed(){
        // do something here and don't write super.onBackPressed()
    	if(busy){
    		Toast.makeText(self, "Currently Processing a request, Please wait..", Toast.LENGTH_LONG).show();
    	}
    	else{
    		//super.onBackPressed();
    		this.finishActivity(0);
    		finish();
    		//System.exit(0);
    		
    	}
    }
    
    
    private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				Bundle bundle = intent.getExtras();
			
				JSONObject responseJson = new JSONObject(bundle.getString(Contants.JOB_RESPONSE, "{}"));
				JSONObject responseJson2 = responseJson.getJSONObject("TransactionResponses");
				JSONObject finalJson = responseJson2.getJSONObject("TransactionResponse");
				status = finalJson.getBoolean("responsemessage");				
				Toast.makeText(self, String.valueOf(status), Toast.LENGTH_LONG).show();
				
				if(status){
					SharedPreferences sharedPref = self.getSharedPreferences(Contants.STORE_USERNAME_KEY, 
							Context.MODE_PRIVATE);
					Editor edit = sharedPref.edit();
					edit.putString("username", usernameTxt.getText().toString());
					edit.putString("password", passwordTxt.getText().toString());
					edit.commit();
					//after commiting... then change activity
					Intent actIntent = new Intent(self, HomeActivity.class);
					self.startActivity(actIntent);
					self.finish();
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(self, "Request cannot be completed, Try again", Toast.LENGTH_LONG).show();
			}
			finally{
				if(!status){
					form_frame.setVisibility(View.VISIBLE);
					loading.setVisibility(View.GONE);
					busy = false;
				}
			}
		}
    	
    };
}
