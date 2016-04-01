package com.swifta.mats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.swifta.mats.forms.CashInActivity;
import com.swifta.mats.forms.CashOutUnregisteredCustomerActivity;
import com.swifta.mats.forms.WithdrawMMActivity;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.Dealers;

public class MMOperatorsFragment extends Fragment {

    private MMOperatorsFragment self = this;
    private String myName = "";

    private LinearLayout ready_cash;
    private LinearLayout fets;
    private LinearLayout teasy_mobile;
    private LinearLayout paga;
    private LinearLayout fortis;
    private Bundle bundle = new Bundle();

    public MMOperatorsFragment() {
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
        View v = inflater.inflate(R.layout.activity_mmoperators, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        Bundle bundle = this.getArguments();
        String toolbarTitle = bundle.getString("toolbar_title", getResources().getString(R.string.cash_in));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(toolbarTitle);

        initEvents(v);
        return v;
    }

    private void initEvents(View v) {
        ready_cash = (LinearLayout) v.findViewById(R.id.ready_cash);
        fets = (LinearLayout) v.findViewById(R.id.fets);
        teasy_mobile = (LinearLayout) v.findViewById(R.id.teasy_mobile);
        paga = (LinearLayout) v.findViewById(R.id.paga);
        fortis = (LinearLayout) v.findViewById(R.id.fortis);

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
        bundle = this.getArguments();
        String previousActivity = bundle.getString(Constants.PREVIOUS_ACTIVITY);

        switch (previousActivity) {
            case (Constants.CASH_IN):
                Intent cashInIntent = new Intent(getActivity(), CashInActivity.class);
                cashInIntent.putExtra("dealer", mmo);
                startActivity(cashInIntent);
                break;
            case (Constants.DEALER_ACCOUNT):
                Intent actIntent = new Intent(getActivity(), WithdrawMMActivity.class);
                actIntent.putExtra("dealer", mmo);
                startActivity(actIntent);
                break;
            case (Constants.UNREGISTERED_CUSTOMER):
                Intent unregisteredIntent = new Intent(getActivity(), CashOutUnregisteredCustomerActivity.class);
                unregisteredIntent.putExtra("dealer", mmo);
                startActivity(unregisteredIntent);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
