package com.swifta.mats.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Contants;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class BackgroundServices extends IntentService{

	@SuppressWarnings("deprecation")
	private HttpClient httpclient;
	private String reponseContent = "";
	
	public BackgroundServices() {
		super(Contants.SERVICE_NAME);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int job = intent.getIntExtra(Contants.JOB_IDENTITY, 0);
		JSONObject data = new JSONObject();
		try {
			data = new JSONObject(intent.getStringExtra(Contants.JOB_DATA));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch(job){
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
			default:
				//do nothing
				break;
		}
		// TODO Auto-generated method stub
		
	}
	
	private void publishResults() {
		//push a json string response to the UI for it to handle.. the UI converts the string to JSON
	    Intent intent = new Intent(Contants.SERVICE_NOTIFICATION);
	    intent.putExtra(Contants.JOB_RESPONSE, this.reponseContent);
	    sendBroadcast(intent);
	}
	
	@SuppressWarnings("deprecation")
	private void performLogin(JSONObject obj){
		String url = Contants.API_URL+"perform/authentication?";
		HttpParams httpParameters = new BasicHttpParams(); 
		int timeoutConnection = 10000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		httpclient = new DefaultHttpClient(httpParameters);
		
	    try {
	    	url+="username="+obj.getString("username")+"&password="+obj.getString("password");
	        HttpResponse response = httpclient.execute(new HttpGet(url));
	        int status = response.getStatusLine().getStatusCode();
	        String result = "";
	        if (status == 200) {
	            result = EntityUtils.toString(response.getEntity());    
	        }
	        else{
	        	result = "Cannot Process Request, Try again("+status+")";
	        }
	        this.reponseContent = result;
	        //System.out.println("Status = "+status+" and "+result);
	        
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	    finally{
	    	publishResults();
	    }
	}

	@SuppressLint("DefaultLocale")
	@SuppressWarnings("deprecation")
	private void withdrawalDealerAccount(JSONObject obj){
		String url = Contants.API_URL+"perform/cashoutrequest";
	    try {
			//url+="?receiver="+obj.getString("receiver").toLowerCase()+"&amount="+
			//		obj.getInt("amount")+"&orginatingresourceid="+obj.getString("mmo")+
			//		"&reference="+obj.getString("reference").replace(" ", "%20")+
			//		"&agentId="+obj.getString("agentId")+
			//		"&agentPin="+obj.getString("agentPin")+
			//		"&teasypin="+obj.getInt("teasypin")+
			//		"&transactionType=cashout";
			
			url+="?orginatingresourceid="+obj.getString("mmo")+
					"&destinationresourceid="+obj.getString("agentId")+
					"&amount="+obj.getInt("amount")+
					"&agentpassword="+obj.getString("agentPin")+
					"&transactionid="+Contants.TRANSACTION_ID+
					"&mmo="+obj.getString("mmo")+
					"&paymentreference="+obj.getString("receiver")+
					"&teasypin="+obj.getInt("teasypin");
			
			System.out.println(url);
			
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			httpclient = new DefaultHttpClient(httpParameters);
			
	    	HttpPost httpPost = new HttpPost(url);
	    	//httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

	        HttpResponse response = httpclient.execute(httpPost);
	        Log.d("Http Post Response:", response.toString());
	        
	        int status = response.getStatusLine().getStatusCode();
	        String result = "";
	        JSONObject myResponse = new JSONObject();
	        myResponse.put("request", Contants.CASH_OUT_COMPLETED);
	        if (status == 200) {
	            result = EntityUtils.toString(response.getEntity());
	            myResponse.put("success", true);
	            myResponse.put("message", "okay");
	            myResponse.put("psa", new JSONObject(result));
	        }
	        else{
	        	result = "Cannot Process Request, Try again("+status+") and "+EntityUtils.toString(response.getEntity());
	            myResponse.put("success", false);
	            myResponse.put("message", result);
	            myResponse.put("psa", "{}");
	        }
	        this.reponseContent = myResponse.toString();
	        System.out.println("Status = "+status+" and "+result);
	        
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	    finally{
	    	publishResults();
	    }
	}
	
	@SuppressLint("DefaultLocale")
	@SuppressWarnings("deprecation")
	private void performCompleteDepositFloat(JSONObject obj){
		String url = Contants.API_URL+"perform/depositcompleterequest";
	    try {
			url+="?orginatingresourceid="+obj.getString("username").toLowerCase()+"&destinationresourceid="+
					obj.getString("dealer")+"&amount="+obj.getString("amount")+
					"&agentpassword="+obj.getString("password")+
					"&transactionid="+obj.getInt("transaction_id")+
					"&otp="+obj.getInt("otp");
			
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			httpclient = new DefaultHttpClient(httpParameters);
			
	    	HttpPost httpPost = new HttpPost(url);
	    	//httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

	        HttpResponse response = httpclient.execute(httpPost);
	        Log.d("Http Post Response:", response.toString());
	        
	        int status = response.getStatusLine().getStatusCode();
	        String result = "";
	        JSONObject myResponse = new JSONObject();
	        myResponse.put("request", Contants.OTP_COMPLETE_REQUEST);
	        if (status == 200) {
	            result = EntityUtils.toString(response.getEntity());
	            myResponse.put("success", true);
	            myResponse.put("message", "okay");
	            myResponse.put("psa", new JSONObject(result));
	        }
	        else{
	        	result = "Cannot Process Request, Try again("+status+") and "+EntityUtils.toString(response.getEntity());
	            myResponse.put("success", false);
	            myResponse.put("message", result);
	            myResponse.put("psa", "{}");
	        }
	        this.reponseContent = myResponse.toString();
	        System.out.println("Status = "+status+" and "+result);
	        
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	    finally{
	    	publishResults();
	    }
	}
	
	@SuppressWarnings("deprecation")
	private void performDepositFloat(JSONObject obj){
		String url = Contants.API_URL+"perform/generateotp";
		
	    try {		
/*			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(10);
			nameValuePair.add(new BasicNameValuePair("orginatingresourceid", obj.getString("username").toLowerCase()));		
			nameValuePair.add(new BasicNameValuePair("destinationresourceid", obj.getString("dealer")));
			nameValuePair.add(new BasicNameValuePair("amount", obj.getString("amount")));			
			nameValuePair.add(new BasicNameValuePair("sendingdescription", 
					obj.getString("description").replace(" ", "%20")));
			nameValuePair.add(new BasicNameValuePair("receivingdescription", 
					Contants.RECEIVING_DESCRIPTION.replace(" ", "%20")));
			nameValuePair.add(new BasicNameValuePair("agentpassword", obj.getString("password")));
			nameValuePair.add(new BasicNameValuePair("transactiontypeid", Contants.TRANSACTION_TYPE_ID));
			nameValuePair.add(new BasicNameValuePair("transactionid", Contants.TRANSACTION_ID));
			nameValuePair.add(new BasicNameValuePair("transactionchannelid", Contants.TRANSACTION_CHANNEL_ID));
			nameValuePair.add(new BasicNameValuePair("transactionstatusid", Contants.TRANSACTION_STATUS_ID));*/
			
			//System.out.println(nameValuePair.toString());
			//chai, this will be stressful to construct url
			url+="?orginatingresourceid="+obj.getString("username").toLowerCase()+"&destinationresourceid="+
					obj.getString("dealer")+"&amount="+obj.getString("amount")+"&sendingdescription="+
					obj.getString("description").replace(" ", "%20")+"&receivingdescription="+
					Contants.RECEIVING_DESCRIPTION.replace(" ", "%20")+"&agentpassword="+obj.getString("password")+
					"&transactiontypeid="+Contants.TRANSACTION_TYPE_ID+"&transactionid="+Contants.TRANSACTION_ID+
					"&transactionchannelid="+Contants.TRANSACTION_CHANNEL_ID+"&transactionstatusid="+Contants.TRANSACTION_STATUS_ID;
			
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			httpclient = new DefaultHttpClient(httpParameters);
			
	    	HttpPost httpPost = new HttpPost(url);
	    	//httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

	        HttpResponse response = httpclient.execute(httpPost);
	        Log.d("Http Post Response:", response.toString());
	        
	        int status = response.getStatusLine().getStatusCode();
	        String result = "";
	        JSONObject myResponse = new JSONObject();
	        myResponse.put("request", Contants.OTP_REQUEST);
	        if (status == 200) {
	            result = EntityUtils.toString(response.getEntity());
	            myResponse.put("success", true);
	            myResponse.put("message", "okay");
	            myResponse.put("psa", new JSONObject(result));
	        }
	        else{
	        	result = "Cannot Process Request, Try again("+status+") and "+EntityUtils.toString(response.getEntity());
	            myResponse.put("success", false);
	            myResponse.put("message", result);
	            myResponse.put("psa", "{}");
	        }
	        this.reponseContent = myResponse.toString();
	        //System.out.println("Status = "+status+" and "+result);
	        
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	    finally{
	    	publishResults();
	    }
	}

}
