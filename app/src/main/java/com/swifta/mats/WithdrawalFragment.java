package com.swifta.mats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.swifta.mats.forms.CompleteCashOutActivity;
import com.swifta.mats.util.Constants;

public class WithdrawalFragment extends Fragment {

    private WithdrawalFragment self = this;
    private String myName = "";

    private Button dealer;
    private Button completeCashout;
    private Button unregisteredCustomer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_withdrawal, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.cash_out));
        initEvents(v);

        return v;
    }

    private void initEvents(View v) {
        dealer = (Button) v.findViewById(R.id.dealer_account);
        completeCashout = (Button) v.findViewById(R.id.complete_cash_out);
        unregisteredCustomer = (Button) v.findViewById(R.id.unregistered_customer);
        dealer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Fragment fragment = new MMOperatorsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PREVIOUS_ACTIVITY, Constants.DEALER_ACCOUNT);
                fragment.setArguments(bundle);

                android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, fragment);
                fragmentTransaction.commit();
            }
        });

        completeCashout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirms a pending cashout transaction before opening the Activity
                SharedPreferences sharedPref = getActivity().getSharedPreferences(Constants.STORE_CASHOUT_DATA,
                        Context.MODE_PRIVATE);
                String destinationresourceid = sharedPref.getString("destinationresourceid", Constants.UNKNOWN);
                if (destinationresourceid.equals(Constants.UNKNOWN)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_uncompleted_transactions),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent actIntent = new Intent(getActivity(), CompleteCashOutActivity.class);
                    startActivity(actIntent);
                }
            }
        });

        unregisteredCustomer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new MMOperatorsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PREVIOUS_ACTIVITY, Constants.UNREGISTERED_CUSTOMER);
                fragment.setArguments(bundle);

                android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, fragment);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.account, menu);
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
}
