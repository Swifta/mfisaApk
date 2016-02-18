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
import android.widget.Toast;

import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MiniStatementFragment extends Fragment {

    private MiniStatementFragment self = this;
    private boolean busy = false;

    private TextView noStatement;
    private LinearLayout generalLayout;
    private LinearLayout container;

    private CardView countCardView;
    private TextView floatTextView;
    private TextView cashInTextView;
    private TextView cashOutTextView;
    private TextView paymentTextView;

    private LinearLayout floatLayout;
    private LinearLayout cashInLayout;
    private LinearLayout cashOutLayout;
    private LinearLayout paymentLayout;

    ArrayList<Statement> floatList = new ArrayList<Statement>();
    ArrayList<Statement> cashInList = new ArrayList<Statement>();
    ArrayList<Statement> cashOutList = new ArrayList<Statement>();
    ArrayList<Statement> paymentList = new ArrayList<Statement>();

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
                    // Automatically populates with data from the JSON response
                    for (int i = 0; i < finalJson.length(); i++) {
                        JSONObject arrayValue = finalJson.getJSONObject(i);

                        String transactionTypeValue = arrayValue.getString("transactiontype");
                        String date = arrayValue.getString("date");
                        int amount = arrayValue.getInt("amount");
                        String receiver = arrayValue.getString("receiver");
                        String status = arrayValue.getString("status");

                        // Adds to the respective arraylist based on the type of transaction being performed
                        switch (transactionTypeValue) {
                            case "FLOAT_TRANSFER":
                                floatList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                            case "CASH_IN":
                                cashInList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                            case "CASH_OUT":
                                cashOutList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                            case "PAYMENT":
                                paymentList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                        }
                    }

                    busy = false;
                    generalLayout.setVisibility(View.GONE);
                    noStatement.setVisibility(View.GONE);
                    countCardView.setVisibility(View.VISIBLE);

                    // Sets the numeric values beside each transaction type
                    floatTextView.setText(" (" + String.valueOf(floatList.size()) + ")");
                    cashInTextView.setText(" (" + String.valueOf(cashInList.size()) + ")");
                    cashOutTextView.setText(" (" + String.valueOf(cashOutList.size()) + ")");
                    paymentTextView.setText(" (" + String.valueOf(paymentList.size()) + ")");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // Sets up the layout to handle null response
                generalLayout.setVisibility(View.GONE);
                countCardView.setVisibility(View.GONE);
                noStatement.setVisibility(View.VISIBLE);
                busy = false;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_mini_statement, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.read_mini_statement));

        initEvent(v);

        return v;
    }

    private void initEvent(View v) {
        noStatement = (TextView) v.findViewById(R.id.no_statement);
        generalLayout = (LinearLayout) v.findViewById(R.id.general_layout);
        container = (LinearLayout) v.findViewById(R.id.container);

        countCardView = (CardView) v.findViewById(R.id.count_cardview);
        floatTextView = (TextView) v.findViewById(R.id.float_transfer_count);
        cashInTextView = (TextView) v.findViewById(R.id.cash_in_count);
        cashOutTextView = (TextView) v.findViewById(R.id.withdrawal_count);
        paymentTextView = (TextView) v.findViewById(R.id.payment_count);

        floatLayout = (LinearLayout) v.findViewById(R.id.float_layout);
        cashInLayout = (LinearLayout) v.findViewById(R.id.cash_in_layout);
        cashOutLayout = (LinearLayout) v.findViewById(R.id.withdrawal_layout);
        paymentLayout = (LinearLayout) v.findViewById(R.id.payment_layout);

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

        floatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(floatList, "Float Transfer");
            }
        });

        cashInLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(cashInList, "Cash In");
            }
        });

        cashOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(cashOutList, "Cash Out");
            }
        });

        paymentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(paymentList, "Bill Payment");
            }
        });

        Intent intent = new Intent(getActivity(), BackgroundServices.class);
        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_MINI_STATEMENT);
        intent.putExtra(Constants.JOB_DATA, data.toString());
        getActivity().startService(intent);
        busy = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.mini_statement, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void transactionsClickAction(ArrayList<Statement> arrayList, String string) {
        if (arrayList.size() > 0) {
            Intent i = new Intent(getActivity(), TransactionActivity.class);
            i.putExtra("name", string);
            i.putExtra("amount", arrayList.size());
            i.putParcelableArrayListExtra("arraylist", arrayList);
            startActivity(i);
        } else {
            Toast.makeText(getActivity(), "No " + string + " transaction available", Toast.LENGTH_SHORT).show();
        }
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
