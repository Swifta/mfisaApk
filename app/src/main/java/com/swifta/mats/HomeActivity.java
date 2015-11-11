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

import com.swifta.mats.util.Constants;

public class HomeActivity extends AppCompatActivity {

    private HomeActivity self = this;
    private String myName = "";
    private Button withdrawalButton;
    private Button floatTransferButton;
    private Button readMiniStatementButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", "UNKNOWN").toUpperCase();

        getSupportActionBar();
        setTitle(myName);
        initEvents();
    }

    private void initEvents() {
        withdrawalButton = (Button) findViewById(R.id.withdrawal);
        floatTransferButton = (Button) findViewById(R.id.float_transfer);
        readMiniStatementButton = (Button) findViewById(R.id.mini_statement);

        withdrawalButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent actIntent = new Intent(self, WithdrawalActivity.class);
                self.startActivity(actIntent);
            }

        });

        floatTransferButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent actIntent = new Intent(self, FloatTransferActivity.class);
                self.startActivity(actIntent);
            }

        });

        readMiniStatementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(self, MiniStatementActivity.class);
                startActivity(intent);
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
            confirmLogout();
            return true;
        } else if (id == R.id.my_account) {
            openAccountActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Confirms that the user really wants to logout
     */
    public void confirmLogout() {
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
        actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
        finish();
    }

    private void openAccountActivity() {
        Intent actIntent = new Intent(self, AccountActivity.class);
        startActivity(actIntent);
        finish();
    }


}
