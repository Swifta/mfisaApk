package com.swifta.mats;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity {
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        container = (LinearLayout) findViewById(R.id.container);

        String name = getIntent().getStringExtra("name");
        String amount = String.valueOf(getIntent().getIntExtra("amount", 0));
        ArrayList<Statement> arrayList = getIntent().getParcelableArrayListExtra("arraylist");

        getSupportActionBar().setTitle(name + " (" + amount + ")");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        changeViews(arrayList);
    }


    /**
     * Updates the layout with data from the arraylist
     */
    private void changeViews(ArrayList<Statement> arrayList) {
        for (Statement values : arrayList) {

            // Sets up the layout to display the data from the JSON response
            View v = getLayoutInflater().inflate(R.layout.mini_statement_item, null);
            container.addView(v);

            TextView transactionType = (TextView) v.findViewById(R.id.transactiontype);
            TextView amount = (TextView) v.findViewById(R.id.amount);
            TextView receiver = (TextView) v.findViewById(R.id.receiver);
            TextView status = (TextView) v.findViewById(R.id.status);

            String transactionTypeValue = values.getTransactionTypeValue();
            String dateValue = values.getDateValue();
            int amountValue = values.getAmountValue();
            String receiverValue = values.getReceiverValue();
            String statusValue = values.getStatusValue();


            transactionType.setText(transactionTypeValue.replace("_", " ") + " on "
                    + getDayFromDate(dateValue) + " at " + getTimeFromDate(dateValue));

            amount.setText("Amount: " + amountValue);

            receiver.setText("Receiver: " + receiverValue);

            // If the status failed, creates a visual cue by setting the text color to red,
            // sets the text color to green if successful and to yellow if pending
            switch (statusValue) {
                case "SUCCESSFUL":
                    status.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    break;
                case "PENDING":
                    status.setTextColor(ContextCompat.getColor(this, R.color.yellow));
                    break;
                case "FAILED":
                    status.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                    break;
                case "NOT_ENOUGH_FUNDS":
                    status.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                    break;
                default:
                    status.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
            status.setText(statusValue);
        }
    }

    /**
     * Retrieves the transaction day from the date
     */
    private String getDayFromDate(String date) {
        return date.substring(0, 10);
    }

    /**
     * Retrieves the transaction time from the date
     */
    private String getTimeFromDate(String date) {
        String dateValue = date.substring(11, 19);

        // Determines whether to append a "PM" prefix or "AM"
        if (Integer.parseInt(dateValue.substring(0, 2)) >= 12) {
            return dateValue += " PM";
        } else {
            return dateValue += " AM";
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
