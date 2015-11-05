package com.swifta.mats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.swifta.mats.forms.CompleteDepositFloatActivity;
import com.swifta.mats.forms.DepositFloatActivity;
import com.swifta.mats.util.Contants;

import org.json.JSONException;
import org.json.JSONObject;

public class FloatTransferActivity extends AppCompatActivity {

    private FloatTransferActivity self = this;
    private String myName = "";
    private boolean btn_clicked = true;

    private Button depositBtn;
    private Button completeDepositBtn;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float_transfer);
        sharedPref = self.getSharedPreferences(Contants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", "UNKNOWN").toUpperCase();

        getSupportActionBar();
        setTitle("Welcome " + myName);
        initEvents();
        btn_clicked = false;
    }

    private void initEvents() {
        depositBtn = (Button) findViewById(R.id.deposit_float);
        completeDepositBtn = (Button) findViewById(R.id.complete_deposit_float);

        depositBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                btn_clicked = true;
                Intent actIntent = new Intent(self, DepositFloatActivity.class);
                self.startActivity(actIntent);
            }

        });

        completeDepositBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    JSONObject obj = new JSONObject(sharedPref.getString(Contants.TMP_DEPOSIT_FLOAT_DATA, "{}"));
                    if (obj.has("transaction_id")) {
                        //resume transaction
                        btn_clicked = true;
                        Intent actIntent = new Intent(self, CompleteDepositFloatActivity.class);
                        self.startActivity(actIntent);
                    } else {
                        Toast.makeText(self, "You do not have an UNCOMPLETED Transaction", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException jE) {
                    Toast.makeText(self, "Error Resuming Previos Transaction", Toast.LENGTH_LONG).show();
                    jE.printStackTrace();
                }

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //System.out.println("Back was pressed with ID:"+item.getTitle());
        if (id == R.id.logout) {
            //logout clicked
            onLogoutPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

/*	@Override
    public void onDestroy(){
		super.onDestroy();
		if(!btn_clicked){
			logout();
		}
	}*/


    public void onLogoutPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        logout();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //builder
                    }

                }).show();
    }

    private void logout() {
        Intent actIntent = new Intent(self, MainActivity.class);
        actIntent.setFlags(actIntent.FLAG_ACTIVITY_NEW_TASK | actIntent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
        finish();
    }

}
