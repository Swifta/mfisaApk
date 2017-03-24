package com.swifta.mats.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import com.swifta.mats.R;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

public class BackgroundServices extends IntentService {

    @SuppressWarnings("deprecation")
    private DefaultHttpClient httpclient;
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
            case ApiJobs.CASH_IN_REQUEST:
                processCashInRequest(data);
                break;
            case ApiJobs.COMPLETE_CASHOUT_REQUEST:
                processCompleteCashOutRequest(data);
                break;
            case ApiJobs.CASH_OUT_UNREGISTERED_CUSTOMER:
                processUnregisteredCashout(data);
                break;
            case ApiJobs.GET_SERVICE_PROVIDER_DETAILS:
                getServiceProviderDetails(data);
                break;
            case ApiJobs.PAYBILL_REQUEST:
                payBillRequest(data);
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
        int timeoutSocket = 10000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // THis bypasses expired SSL
        byPassSSL(httpParameters);


        try {
            url += "username=" + obj.getString("username") + "&password=" + obj.getString("password");
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HTTP.CONTENT_TYPE,
                    "application/x-www-form-urlencoded;charset=UTF-8");

            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";

            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            } else if (status == 500) {
                result = getResources().getString(R.string.server_error);
            } else {
                result = getResources().getString(R.string.unprocessed_request);
            }
            this.reponseContent = result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }

    private void byPassSSL(HttpParams httpParameters) {
        HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        DefaultHttpClient client = new DefaultHttpClient(httpParameters);

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme("https", socketFactory, 443));
        SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
        httpclient = new DefaultHttpClient(mgr, client.getParams());

        // Set verifier
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("deprecation")
    private void withdrawalDealerAccount(JSONObject obj) {
        String url = Constants.API_URL + "service/cashoutrequest";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 15000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 15000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("agentId").toLowerCase() +
                    "&destinationresourceid=" + obj.getString("dealerId") +
                    "&amount=" + obj.getString("amount") +
                    "&agentpassword=" + obj.getString("agentPin") +
                    "&transactionid=" + Constants.TRANSACTION_ID +
                    "&mmo=" + obj.getString("mmo") +
                    "&paymentreference=" + obj.getString("receiver") +
                    "&teasypin=" + obj.getString("teasypin");

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
                result = getResources().getString(R.string.unprocessed_request);
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

            // THis bypasses expired SSL
            byPassSSL(httpParameters);

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
                result = getResources().getString(R.string.unprocessed_request);
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

            // THis bypasses expired SSL
            byPassSSL(httpParameters);

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
                result = getResources().getString(R.string.unprocessed_request);
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

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("username")
                    + "&newpassword=" + obj.getString("newpassword")
                    + "&oldpassword=" + obj.getString("oldpassword");

            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                result = getResources().getString(R.string.unprocessed_request);
            }
            this.reponseContent = result;
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

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("username");
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                result = getResources().getString(R.string.unprocessed_request);
            }
            this.reponseContent = result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            publishResults();
        }
    }

    @SuppressWarnings("deprecation")
    private void processCashInRequest(JSONObject obj) {
        String url = Constants.API_URL + "service/cashinrequest";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("orginatingresourceid").toLowerCase() +
                    "&destinationresourceid=" + obj.getString("destinationresourceid") +
                    "&amount=" + obj.getInt("amount") +
                    "&frommessage=" + obj.getString("frommessage") +
                    "&transactionid=" + obj.getString("transactionid") +
                    "&mmo=" + obj.getString("mmo") +
                    "&paymentreference=" + obj.getString("paymentreference");

            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.CASH_IN_COMPLETED);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = getResources().getString(R.string.unprocessed_request);
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
    private void processCompleteCashOutRequest(JSONObject obj) {
        String url = Constants.API_URL + "service/verifycashoutcompleted";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("orginatingresourceid").toLowerCase() +
                    "&destinationresourceid=" + obj.getString("destinationresourceid") +
                    "&amount=" + obj.getInt("amount") +
                    "&agentpassword=" + obj.getString("agentpassword") +
                    "&mmo=" + obj.getString("mmo") +
                    "&paymentreference=" + obj.getString("paymentreference") +
                    "&referencenumber=" + obj.getInt("referencenumber");

            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.COMPLETE_CASH_OUT_COMPLETED);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = getResources().getString(R.string.unprocessed_request);
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
    private void processUnregisteredCashout(JSONObject obj) {
        String url = Constants.API_URL + "service/cashoutunregisteredcustomer";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("orginatingresourceid").toLowerCase() +
                    "&destinationresourceid=" + obj.getString("destinationresourceid") +
                    "&amount=" + obj.getString("amount") +
                    "&redeemcode=" + obj.getString("redeemcode") +
                    "&mmo=" + obj.getString("mmo") +
                    "&paymentreference=" + obj.getString("paymentreference") +
                    "&referencenumber=" + obj.getString("referencenumber");

            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.UNREGISTERED_CASH_OUT);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = getResources().getString(R.string.unprocessed_request);
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
    private void getServiceProviderDetails(JSONObject obj) {
        String url = Constants.API_URL + "service/getserviceproviderdetails";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?serviceprovidercode=" + obj.getString(Constants.VENDOR_ID);

            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.GET_SERVICE_PROVIDER_DETAILS);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = getResources().getString(R.string.unprocessed_request);
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
    private void payBillRequest(JSONObject obj) {
        String url = Constants.API_URL + "service/paybillrequest";
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // THis bypasses expired SSL
        byPassSSL(httpParameters);

        try {
            url += "?orginatingresourceid=" + obj.getString("orginatingresourceid").toLowerCase() +
                    "&amount=" + obj.getString("amount") +
                    "&frommessage=" + obj.getString("frommessage") +
                    "&vendorid=" + obj.getString("vendorid") +
                    "&vendoraccount=" + obj.getString("vendoraccount") +
                    "&vendorservicename=" + obj.getString("vendorservicename") +
                    "&vendorparam1=" + obj.getString("vendorparam1") +
                    "&vendorparam2=" + obj.getString("vendorparam2");

            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();
            String result = "";
            JSONObject myResponse = new JSONObject();
            myResponse.put("request", Constants.PAY_BILL_REQUEST);
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity());
                myResponse.put("success", true);
                myResponse.put("message", "okay");
                myResponse.put("psa", new JSONObject(result));
            } else {
                result = getResources().getString(R.string.unprocessed_request);
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
}
