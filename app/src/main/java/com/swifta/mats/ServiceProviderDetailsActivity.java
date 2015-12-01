package com.swifta.mats;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.swifta.mats.adapters.ServiceProviderDetailsAdapter;
import com.swifta.mats.adapters.ServiceProviderDetailsModel;
import com.swifta.mats.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServiceProviderDetailsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private RecyclerView mRecyclerView;
    private ServiceProviderDetailsAdapter mAdapter;
    private List<ServiceProviderDetailsModel> mModels;

    private static String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cable);

        SharedPreferences sharedPref = getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(myName);

        mRecyclerView = (RecyclerView) findViewById(R.id.service_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mModels = new ArrayList<>();

        String data = getIntent().getStringExtra("data");
        try {
            JSONArray serviceProviderDetail = new JSONArray(data);
            for (int i = 0; i < serviceProviderDetail.length(); i++) {
                JSONObject serviceDetails = serviceProviderDetail.getJSONObject(i);

                mModels.add(new ServiceProviderDetailsModel(
                        serviceDetails.getString("servicename"),
                        serviceDetails.getString("amount")
                ));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        String vendorId = getIntent().getStringExtra("vendorid");
        mAdapter = new ServiceProviderDetailsAdapter(this, vendorId, mModels);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cable, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<ServiceProviderDetailsModel> filteredModelList = filter(mModels, newText);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private List<ServiceProviderDetailsModel> filter(List<ServiceProviderDetailsModel> models, String query) {
        query = query.toLowerCase();

        final List<ServiceProviderDetailsModel> filteredModelList = new ArrayList<>();
        for (ServiceProviderDetailsModel model : models) {
            final String text = model.getServiceName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }
}
