package com.swifta.mats;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.swifta.mats.forms.ProcessServiceProviderActivity;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.InternetCheck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BillPaymentFragment extends Fragment {

    private BillPaymentFragment self = this;
    private boolean busy = false;
    private String myName;
    private LinearLayout dstvButton;
    private LinearLayout gotvButton;
    private LinearLayout startimesButton;
    private LinearLayout irokotvButton;
    private LinearLayout mtn;
    private LinearLayout glo;
    private LinearLayout etisalat;
    private LinearLayout airtel;
    private LinearLayout smile;
    private LinearLayout spectranet;

    private ProgressDialog progressDialog;
    public static String vendorId;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog.hide();
            Bundle bundle = intent.getExtras();
            try {
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                if (responseJson.getInt("request") == Constants.GET_SERVICE_PROVIDER_DETAILS) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");

                        Log.e("psa",psaResponse.toString());

                        JSONObject psaTranResponse = psaResponse.getJSONObject("getserviceproviderdetails");
                        Object json = psaTranResponse.get("getserviceproviderdetail");

                        // If the service provider is a telco, go straight to the form
                        if (json instanceof JSONObject) {
                            Intent objectIntent = new Intent(getActivity(), ProcessServiceProviderActivity.class);
                            objectIntent.putExtra("type", "telco");
                            objectIntent.putExtra("vendorid", vendorId);
                            objectIntent.putExtra("servicename", psaTranResponse.getJSONObject("getserviceproviderdetail").getString("servicename"));
                            startActivity(objectIntent);
                        } else if (json instanceof JSONArray) {
                            // If not, open up a list of searchable service names with prefixed prices
                            Intent newIntent = new Intent(getActivity(), ServiceProviderDetailsActivity.class);
                            newIntent.putExtra("data", json.toString());
                            newIntent.putExtra("vendorid", vendorId);
                            startActivity(newIntent);
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setMessage("Request failed : " + showReport);
                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
                Toast.makeText(getActivity(), getResources().getString(R.string.retry_uncompleted_request), Toast.LENGTH_LONG).show();
            } finally {
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
        View v = inflater.inflate(R.layout.activity_bill_payment, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.bill_payment));
        initEvents(v);

        return v;
    }

    private void initEvents(View v) {
        dstvButton = (LinearLayout) v.findViewById(R.id.dstv);
        gotvButton = (LinearLayout) v.findViewById(R.id.gotv);
        startimesButton = (LinearLayout) v.findViewById(R.id.startimes);
        irokotvButton = (LinearLayout) v.findViewById(R.id.irokotv);
        mtn = (LinearLayout) v.findViewById(R.id.mtn);
        glo = (LinearLayout) v.findViewById(R.id.glo);
        etisalat = (LinearLayout) v.findViewById(R.id.etisalat);
        airtel = (LinearLayout) v.findViewById(R.id.airtel);
        smile = (LinearLayout) v.findViewById(R.id.smile);
        spectranet = (LinearLayout) v.findViewById(R.id.spectranet);
        progressDialog = new ProgressDialog(getActivity());

        dstvButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.DSTV_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        gotvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.GOTV_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        startimesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.STARTIMES_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        irokotvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.IROKO_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.MTN_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        glo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.GLO_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        etisalat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.ETISALAT_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        airtel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.AIRTEL_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        smile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.SMILE_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        spectranet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(getActivity())) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.SPECTRANET_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getActivity(), BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    getActivity().startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.account, menu);
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
}
