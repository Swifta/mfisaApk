package com.swifta.mats;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private HomeFragment self = this;
    private String myName = "";
    private TextView transactionTitle;
    private TextView transactionType;
    private TextView transactionDate;
    private TextView transactionTime;
    private TextView transactionStatus;
    private LinearLayout container;
    private CardView lastFiveCardview;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                Bundle bundle = intent.getExtras();
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                JSONObject responseJson2 = responseJson.getJSONObject("TransactionResponses");

                JSONArray finalJson = responseJson2.getJSONArray("TransactionResponse");

                if (finalJson.length() != 0) {
                    // Automatically populates the views with data from the JSON response
                    updateViews(finalJson);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // Sets up the layout to handle null response
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_home, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.home));

        initEvents(v);

        return v;
    }

    private void initEvents(View v) {
        transactionTitle = (TextView) v.findViewById(R.id.transaction_title);
        transactionType = (TextView) v.findViewById(R.id.transaction_type);
        transactionDate = (TextView) v.findViewById(R.id.transaction_date);
        transactionTime = (TextView) v.findViewById(R.id.transaction_time);
        transactionStatus = (TextView) v.findViewById(R.id.transaction_status);
        container = (LinearLayout) v.findViewById(R.id.container);
        lastFiveCardview = (CardView) v.findViewById(R.id.last_five_cardview);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", Constants.UNKNOWN);

        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Intent intent = new Intent(getActivity(), BackgroundServices.class);
        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_MINI_STATEMENT);
        intent.putExtra(Constants.JOB_DATA, data.toString());
        getActivity().startService(intent);
    }

    /**
     * Updates the layout with data from the arraylist
     */
    private void updateViews(JSONArray finalJson) throws JSONException {

        lastFiveCardview.setVisibility(View.VISIBLE);

        // Automatically populates with data from the JSON response
        for (int i = 0; i < 6; i++) {

            JSONObject arrayValue = finalJson.getJSONObject(i);

            // Sets up the layout to display the data from the JSON response
            View v = getActivity().getLayoutInflater().inflate(R.layout.mini_statement_item, null);
            container.addView(v);

            TextView transactionType = (TextView) v.findViewById(R.id.transactiontype);
            TextView amount = (TextView) v.findViewById(R.id.amount);
            TextView receiver = (TextView) v.findViewById(R.id.receiver);
            TextView status = (TextView) v.findViewById(R.id.status);

            String transactionTypeValue = arrayValue.getString("transactiontype");
            String dateValue = arrayValue.getString("date");
            int amountValue = arrayValue.getInt("amount");
            String receiverValue = arrayValue.getString("receiver");
            String statusValue = arrayValue.getString("status");

            // Set the values for the last transaction only
            if (i == 0) {
                transactionTitle.setVisibility(View.VISIBLE);
                transactionType.setVisibility(View.VISIBLE);
                transactionType.setText(transactionTypeValue);
                transactionDate.setVisibility(View.VISIBLE);
                transactionDate.setText(getDayFromDate(dateValue));
                transactionTime.setVisibility(View.VISIBLE);
                transactionTime.setText(getTimeFromDate(dateValue));
                transactionStatus.setVisibility(View.VISIBLE);
            }

            transactionType.setText(transactionTypeValue.replace("_", " ") + " on "
                    + getDayFromDate(dateValue) + " at " + getTimeFromDate(dateValue));

            amount.setText("Amount: " + amountValue);

            receiver.setText("Receiver: " + receiverValue);

            // If the status failed, creates a visual cue by setting the text color to red,
            // sets the text color to green if successful and to yellow if pending
            switch (statusValue) {
                case "SUCCESSFUL":
                    status.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark));
                    break;
                case "PENDING":
                    status.setTextColor(ContextCompat.getColor(getActivity(), R.color.yellow));
                    break;
                case "FAILED":
                    status.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));
                    break;
                case "NOT_ENOUGH_FUNDS":
                    status.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));
                    break;
                default:
                    status.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
            }
            status.setText(statusValue);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.home, menu);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        Intent actIntent = new Intent(getActivity(), LoginActivity.class);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
    }

    private void openAccountActivity() {
        Intent actIntent = new Intent(getActivity(), AccountActivity.class);
        startActivity(actIntent);
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
        String dateValue = date.substring(11, 16);

        // Determines whether to append a "PM" prefix or "AM"
        if (Integer.parseInt(dateValue.substring(0, 2)) >= 12) {
            return dateValue += " PM";
        } else {
            return dateValue += " AM";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter(Constants.SERVICE_NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }
}
