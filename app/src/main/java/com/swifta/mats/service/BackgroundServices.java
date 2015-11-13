package com.swifta.mats.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class BackgroundServices extends IntentService {

    @SuppressWarnings("deprecation")
    private HttpClient httpclient;
    private String reponseContent = "";

    public BackgroundServices() {
        super(Constants.SERVICE_NAME);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int job = intent.getIntExtra(Constants.JOB_IDENTITY, 0);
        JSONObject data = new JSONObject();
        try {
            data = new JSONObject(intent.getStringExtra(Constants.JOB_DATA));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        switch (job) {
            case ApiJobs.LOGIN:
                performLogin(data);
                break;
            case ApiJobs.DEPOSIT_FLOAT:
                performDepositFloat(data);
                break;
            case ApiJobs.COMPLETE_DEPOSIT_FLOAT:
                performCompleteDepositFloat(data);
                break;
            case ApiJobs.WITHDRAWAL_DEALER_ACCOUNT:
                withdrawalDealerAccount(data);
                break;
            case ApiJobs.CHANGE_PASSWORD:
                changePassword(data);
                break;
            case ApiJobs.GET_MINI_STATEMENT:
                getMiniStatement(data);
                break;

            default:
                //do nothing
                break;
        }
        // TODO Auto-generated method stub

    }

    private void publishResults() {
        //push a json string response to the UI for it to handle.. the UI converts the string to JSON
        Intent intent = new Intent(Constants.SERVICE_NOTIFICATION);
        intent.putExtra(Constants.JOB_RESPONSE, this.reponseContent);
        sendBroadcast(intent);
    }

    @SuppressWarnings("deprecation")
    private void performLogin(JSONObject obj) {
        String url = Constants.API_URL + "service/authentication?";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        httpclient = new DefaultHttpClient(httpParameters);

        try {
            url += "username=" + obj.getString("username") + "&password=" + obj.getString("password");
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                result = "Cannot process your request. Please try again";
            }
            this.reponseContent = result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("deprecation")
    private void withdrawalDealerAccount(JSONObject obj) {
        String url = Constants.API_URL + "service/cashoutrequest";
        try {

            url += "?orginatingresourceid=" + obj.getString("mmo") +
                    "&destinationresourceid=" + obj.getString("agentId") +
                    "&amount=" + obj.getInt("amount") +
                    "&agentpassword=" + obj.getString("agentPin") +
                    "&transactionid=" + Constants.TRANSACTION_ID +
                    "&mmo=" + obj.getString("mmo") +
                    "&paymentreference=" + obj.getString("receiver") +
                    "&teasypin=" + obj.getInt("teasypin");

            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 10000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.CASH_OUT_COMPLETED);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = "Cannot process request. Please try again(" + status + ") and " + EntityUtils.toString(response.getEntity());
                myResponse.put("success", false);
                myResponse.put("message", result);
                myResponse.put("psa", "{}");
            }
            this.reponseContent = myResponse.toString();
            System.out.println("Status = " + status + " and " + result);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("deprecation")
    private void performCompleteDepositFloat(JSONObject obj) {
        String url = Constants.API_URL + "service/floatrequestcompleted";
        try {
            url += "?orginatingresourceid=" + obj.getString("username").toLowerCase() + "&destinationresourceid=" +
                    obj.getString("dealer") + "&amount=" + obj.getString("amount") +
                    "&agentpassword=" + obj.getString("password") +
                    "&transactionid=" + obj.getInt("transaction_id") +
                    "&otp=" + obj.getInt("otp");

            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 10000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            httpclient = new DefaultHttpClient(httpParameters);

            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.OTP_COMPLETE_REQUEST);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = "Cannot process your request, please try again.";
                myResponse.put("success", false);
                myResponse.put("message", result);
                myResponse.put("psa", "{}");
            }
            this.reponseContent = myResponse.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }

    @SuppressWarnings("deprecation")
    private void performDepositFloat(JSONObject obj) {
        String url = Constants.API_URL + "service/floattransfer";

        try {
            url += "?orginatingresourceid=" + obj.getString("username").toLowerCase() + "&destinationresourceid=" +
                    obj.getString("dealer") + "&amount=" + obj.getString("amount") + "&sendingdescription=" +
                    obj.getString("description").replace(" ", "%20") + "&receivingdescription=" +
                    Constants.RECEIVING_DESCRIPTION.replace(" ", "%20") + "&agentpassword=" + obj.getString("password") +
                    "&transactiontypeid=" + Constants.TRANSACTION_TYPE_ID + "&transactionid=" + Constants.TRANSACTION_ID +
                    "&transactionchannelid=" + Constants.TRANSACTION_CHANNEL_ID + "&transactionstatusid=" + Constants.TRANSACTION_STATUS_ID;

            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 10000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            httpclient = new DefaultHttpClient(httpParameters);

            HttpPost httpPost = new HttpPost(url);

            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.OTP_REQUEST);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = "We cannot process your request, please try again.";
                myResponse.put("success", false);
                myResponse.put("message", result);
                myResponse.put("psa", "{}");
            }
            this.reponseContent = myResponse.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }

    @SuppressWarnings("deprecation")
    private void changePassword(JSONObject obj) {
        String url = Constants.API_URL + "service/changepassword";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        httpclient = new DefaultHttpClient(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("username")
                    + "&newpassword=" + obj.getString("newpassword")
                    + "&oldpassword=" + obj.getString("oldpassword");
            // HttpResponse response = httpclient.execute(new HttpPost(url));

            HttpPost httpPost = new HttpPost(url);
            //httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                result = "Cannot process request. Please try again(" + status + ")";
            }
            this.reponseContent = result;
            //System.out.println("Status = "+status+" and "+result);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }

    @SuppressWarnings("deprecation")
    private void getMiniStatement(JSONObject obj) {
        String url = Constants.API_URL + "service/getministatement";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        httpclient = new DefaultHttpClient(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("username");
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                result = "Cannot process request. Please try again(" + status + ")";
            }
            this.reponseContent = result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }
}
