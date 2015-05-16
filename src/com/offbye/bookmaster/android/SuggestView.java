package com.offbye.bookmaster.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.offbye.bookmaster.android.util.Constants;

public class SuggestView extends Activity {
	private static final String TAG = "SuggestView";

	private StringBuffer urlsb = null;
	private ProgressDialog pd;
	private final static int NOTIFICATION_GETDATA_FINISH = 0;
	private final static int NOTIFICATION_NODATA_ERROR = 1;
	private final static int NOTIFICATION_NETWORK_ERROR = 2;
	private EditText content,email;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestview);
        
        this.getWindow().setBackgroundDrawableResource(R.drawable.bg);
        
        content = (EditText) this.findViewById(R.id.content);
        email = (EditText) this.findViewById(R.id.email);
        
    	urlsb =new StringBuffer(128);
		urlsb.append(Constants.suggesturl);    	 
		
		Button submit = (Button) this.findViewById(R.id.submit);
		submit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if(checkEmail(email.getText().toString())){
						urlsb.append("?c=");
				    	urlsb.append(URLEncoder.encode(content.getText().toString()));
				    	urlsb.append("&e=");
				    	urlsb.append(URLEncoder.encode(email.getText().toString()));
				    	//Log.v(TAG,urlsb.toString());
				    	getDataInitialize();
					}
					else{
						new AlertDialog.Builder(SuggestView.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.invalidemail)
						.setMessage(R.string.invalidemail)
						.setPositiveButton(R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).show();
					}
					
				}
			});
		        
    }
    public boolean checkEmail(String mail){   
        String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";   
        Pattern   p   =   Pattern.compile(regex);   
        Matcher   m   =   p.matcher(mail);   
        return m.find();   
    }   
    
    public  String saveSuggest(String weburl) {
    	String re="";
		try {
			URL url = new URL(weburl);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(20000);
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String line = "";
			StringBuffer sb = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			if (sb.length() > 0 ){
				re=sb.toString();
			}
			//Log.v(TAG,re);

		} catch (IOException e) {
			progressHandler.sendEmptyMessage(NOTIFICATION_NETWORK_ERROR);
			Log.d(TAG, "network err");
		} 
		return re;
	}

	public void getDataInitialize() {
		// 1. to start ProgressDialog
		pd = ProgressDialog.show(this, getString(R.string.msg_loading), getString(R.string.msg_wait), true, false);
		pd.setIcon(R.drawable.icon);
		// 2. to start an Thread to do getDataThread working
		GetDataBody myWork = new GetDataBody();
		Thread getDataThread = new Thread(myWork);
		// 3. to call start() to do getDataThread working
		getDataThread.start();
	}

	private class GetDataBody implements Runnable {
		public void run() {
			// 4. the working
			String re= saveSuggest(urlsb.toString());
			//Log.v(TAG, "load from web");
			if (re.equals("ok")) {
				// 5. to call sendEmptyMessage() to notificate this working
				// is done
				progressHandler.sendEmptyMessage(NOTIFICATION_GETDATA_FINISH);
			}
			else{
				progressHandler.sendEmptyMessage(NOTIFICATION_NODATA_ERROR);
			}
		}
	}

	// Define the Handler that receives messages from the thread calculation
	private Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NOTIFICATION_GETDATA_FINISH:
				pd.dismiss();
				Toast.makeText(SuggestView.this, "成功提交建议", 5).show();
				content.setText("");
				break;
			case NOTIFICATION_NETWORK_ERROR:
				pd.dismiss();
				//Log.v(TAG, "NOTIFICATION_NETWORK_ERROR");
				Toast.makeText(SuggestView.this, "网络传输错误", 5).show();
				break;
			case NOTIFICATION_NODATA_ERROR:
				pd.dismiss();
				//Log.v(TAG, "NOTIFICATION_NODATA_ERROR");
				Toast.makeText(SuggestView.this, "提交建议失败", 5).show();
				break;
		
			default:
				Toast.makeText(SuggestView.this, "网络传输错误", 5).show();
			}
		}
	};
  
	
}