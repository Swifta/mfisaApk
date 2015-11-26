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

import com.swifta.mats.forms.CashInActivity;
import com.swifta.mats.forms.CashOutUnregisteredCustomerActivity;
import com.swifta.mats.forms.WithdrawMMActivity;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.Dealers;

public class MMOperatorsActivity extends AppCompatActivity {

    private MMOperatorsActivity self = this;
    private String myName = "";

    private Button ready_cash;
    private Button fets;
    private Button teasy_mobile;
    private Button paga;
    private Button fortis;
    private Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmoperators);
        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();
        getSupportActionBar();
        setTitle(myName);
        initEvents();
    }

    private void initEvents() {
        ready_cash = (Button) findViewById(R.id.ready_cash);
        fets = (Button) findViewById(R.id.fets);
        teasy_mobile = (Button) findViewById(R.id.teasy_mobile);
        paga = (Button) findViewById(R.id.paga);
        fortis = (Button) findViewById(R.id.fortis);

        ready_cash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bundle.putString("dealer", Dealers.READY_CASH.name());
                openForm(Dealers.READY_CASH.name());
            }
        });
        fets.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bundle.putString("dealer", Dealers.FETS.name());
                openForm(Dealers.FETS.name());
            }
        });
        teasy_mobile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bundle.putString("dealer", Dealers.TEASY_MOBILE.name());
                openForm(Dealers.TEASY_MOBILE.name());
            }
        });
        paga.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bundle.putString("dealer", Dealers.PAGA.name());
                openForm(Dealers.PAGA.name());
            }
        });
        fortis.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bundle.putString("dealer", Dealers.FORTIS.name());
                openForm(Dealers.FORTIS.name());
            }
        });
    }

    /**
     * Determines what activity to open based on the previous activity.
     **/
    private void openForm(String mmo) {
        String previousActivity = getIntent().getStringExtra(Constants.PREVIOUS_ACTIVITY);

        switch (previousActivity) {
            case (Constants.CASH_IN):
                Intent cashInIntent = new Intent(self, CashInActivity.class);
                cashInIntent.putExtra("dealer", mmo);
                startActivity(cashInIntent);
                break;
            case (Constants.DEALER_ACCOUNT):
                Intent actIntent = new Intent(self, WithdrawMMActivity.class);
                actIntent.putExtras(bundle);
                startActivity(actIntent);
                break;
            case (Constants.UNREGISTERED_CUSTOMER):
                Intent unregisteredIntent = new Intent(self, CashOutUnregisteredCustomerActivity.class);
                unregisteredIntent.putExtra("dealer", mmo);
                startActivity(unregisteredIntent);
                break;
        }
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
