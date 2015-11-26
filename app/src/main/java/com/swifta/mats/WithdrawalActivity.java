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

import com.swifta.mats.forms.CompleteCashOutActivity;
import com.swifta.mats.util.Constants;

public class WithdrawalActivity extends AppCompatActivity {

    private WithdrawalActivity self = this;
    private String myName = "";

    private Button dealer;
    private Button completeCashout;
    private Button unregisteredCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);
        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        getSupportActionBar();
        setTitle(myName);
        initEvents();
    }

    private void initEvents() {
        dealer = (Button) findViewById(R.id.dealer_account);
        completeCashout = (Button) findViewById(R.id.complete_cash_out);
        unregisteredCustomer = (Button) findViewById(R.id.unregistered_customer);
        dealer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent actIntent = new Intent(self, MMOperatorsActivity.class);
                actIntent.putExtra(Constants.PREVIOUS_ACTIVITY, Constants.DEALER_ACCOUNT);
                startActivity(actIntent);
            }
        });

        completeCashout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirms a pending cashout transaction before opening the Activity
                SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_CASHOUT_DATA,
                        Context.MODE_PRIVATE);
                String destinationresourceid = sharedPref.getString("destinationresourceid", Constants.UNKNOWN);
                if (destinationresourceid.equals(Constants.UNKNOWN)) {
                    Toast.makeText(self, getResources().getString(R.string.no_uncompleted_transactions),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent actIntent = new Intent(self, CompleteCashOutActivity.class);
                    startActivity(actIntent);
                }
            }
        });

        unregisteredCustomer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(self, MMOperatorsActivity.class);
                intent.putExtra(Constants.PREVIOUS_ACTIVITY, Constants.UNREGISTERED_CUSTOMER);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account, menu);
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

    public void onLogoutPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.logout_confirmation))
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
        Intent actIntent = new Intent(self, LoginActivity.class);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
        finish();
    }
}
