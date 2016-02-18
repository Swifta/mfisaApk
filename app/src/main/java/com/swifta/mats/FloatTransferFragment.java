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

import com.swifta.mats.forms.CompleteDepositFloatActivity;
import com.swifta.mats.forms.DepositFloatActivity;
import com.swifta.mats.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class FloatTransferFragment extends Fragment {

    private FloatTransferFragment self = this;
    private String myName = "";
    private boolean btn_clicked = true;

    private Button depositBtn;
    private Button completeDepositBtn;

    private SharedPreferences sharedPref;

    public FloatTransferFragment() {
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
        View v = inflater.inflate(R.layout.activity_float_transfer, container, false);

        sharedPref = getActivity().getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.float_transfer));
        initEvents(v);
        btn_clicked = false;

        return v;
    }

    private void initEvents(View v) {
        depositBtn = (Button) v.findViewById(R.id.deposit_float);
        completeDepositBtn = (Button) v.findViewById(R.id.complete_deposit_float);

        depositBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                btn_clicked = true;
                Intent actIntent = new Intent(getActivity(), DepositFloatActivity.class);
                self.startActivity(actIntent);
            }

        });

        completeDepositBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    JSONObject obj = new JSONObject(sharedPref.getString(Constants.TMP_DEPOSIT_FLOAT_DATA, "{}"));
                    if (obj.has("transaction_id")) {
                        //resume transaction
                        btn_clicked = true;
                        Intent actIntent = new Intent(getActivity(), CompleteDepositFloatActivity.class);
                        self.startActivity(actIntent);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_uncompleted_transactions), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException jE) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_resuming_transactions), Toast.LENGTH_LONG).show();
                    jE.printStackTrace();
                }
            }

        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.account, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
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
        actIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
    }

}
