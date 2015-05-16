package com.offbye.bookmaster.android.douban;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.offbye.bookmaster.android.HomeActivity;
import com.offbye.bookmaster.android.R;


public class ComparePricesActivity extends Activity {
	private static final String TAG = "ComparePricesActivity";

	private String bookId = "3108935";
	private BuyLink seleteditem;
	private ProgressDialog pd;
	private ImageButton returnhome;
	private ListView optionsListView;
	
	private ArrayList<BuyLink> bl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_list);

		Bundle extras = getIntent().getExtras();
		if(extras.getString("id")!=null && !extras.getString("id").equals("")){
			//Log.v(TAG,"extras"+extras.getString("id"));
			bookId = extras.getString("id");
		}
		returnhome = (ImageButton) this.findViewById(R.id.returnhome);
		returnhome.setOnClickListener(buttonReturnhomeListener);
		
		optionsListView = (ListView) this.findViewById(R.id.listview);

		getDataInitialize();
	}

	private final Button.OnClickListener buttonReturnhomeListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(ComparePricesActivity.this,HomeActivity.class);
			startActivity(i);
		}
	};

		  

	public void getDataInitialize() {
		// 1. to start ProgressDialog
		pd = ProgressDialog.show(this,getString(R.string.msg_loading), getString(R.string.msg_wait), true, false);
		//pd.setIcon(R.drawable.zxing_icon);
		// 2. to start an Thread to do getDataThread working
		GetDataBody myWork = new GetDataBody();
		Thread getDataThread = new Thread(myWork);
		// 3. to call start() to do getDataThread working
		getDataThread.start();
	}
	
	public  String getWebPage(String weburl) {
		String returnstr="";
		try {
			URL url = new URL(weburl);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(20000);
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream(),"UTF-8"));
			String line = "";
			StringBuffer sb = new StringBuffer(100000);
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			returnstr = sb.toString();
		
		} catch (IOException e) {
			progressHandler.sendEmptyMessage(R.string.notify_network_error);
			Log.d(TAG, "network err");
		}
		return returnstr;
	}

	
	private class GetDataBody implements Runnable {
		public void run() {
			String url="http://www.douban.com/subject/2350026/buylinks";
			String re = getWebPage(url.replace("2350026", bookId));
			String pricehtml = re.substring(re.indexOf("<br/><table class=\"olt\">"),re.indexOf("<br/><br/><br/><br/><br/>"));

			pricehtml=pricehtml.replaceAll("\"", "");
			pricehtml=pricehtml.replaceAll("</tr>", "</tr>\r\n"); //增加换行，否则模式匹配会得不到正确结果

			String regEx="<tr><td height=39><img src=http://t.douban.com/pics/icon/(.*).gif width=96/></td><td class=pl2>(.*)</td><td  class=pl2 align=right>(.*)</td>        <td class=pl2 align=right>(.*)</td></tr>"; 
			Pattern p=Pattern.compile(regEx); 
			Matcher m=p.matcher(pricehtml); 


			bl =new ArrayList<BuyLink>();
			
			while (m.find()){
				BuyLink bu =new BuyLink();
				bu.sitelogo = m.group(1);
				bu.sitename = getLinkText(m.group(2));
				bu.buylink = getLinkHref(m.group(2));
				bu.siteprice = getLinkText(m.group(3));
				bu.savedprice = m.group(4);
				bl.add(bu);
			}
			if(bl.isEmpty()){
				progressHandler.sendEmptyMessage(R.string.notify_no_result);
			}
			else{
				progressHandler.sendEmptyMessage(R.string.notify_succeeded);
			}
		}
	}

	// Define the Handler that receives messages from the thread
	private final Handler progressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.string.notify_succeeded:
				pd.dismiss();
				ComparePricesAdapter pa = new ComparePricesAdapter(
						ComparePricesActivity.this, R.layout.buylink_row, bl);
				optionsListView.setAdapter(pa);
				optionsListView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0,View arg1, int position, long id) {
						seleteditem = bl.get(position);
						new AlertDialog.Builder(ComparePricesActivity.this)
    	                .setTitle(R.string.select_dialog)
    	                .setItems(R.array.buyoptions, new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog, int which) {
    	                       if(which==0){
    	                    	    Uri uri = Uri.parse(seleteditem.buylink);
    	   							Intent it  = new Intent(Intent.ACTION_VIEW,uri);
    	   							startActivity(it);
    	                       }
    	                       if(which==1){
    	                    	   Intent i = new Intent(Intent.ACTION_VIEW);   
    	                    	   i.putExtra("sms_body", seleteditem.sitename+"  "+seleteditem.buylink);   
    	                    	   i.setType("vnd.android-dir/mms-sms");   
    	                    	   startActivity(i);
    	                       }
    	                       if(which==2){
    	                    	   Intent returnIt = new Intent(Intent.ACTION_SEND);
    	                    	   //String[] tos = { "demo@mail.com" };
    	                    	   //returnIt.putExtra(Intent.EXTRA_EMAIL, tos);
    	                    	   returnIt.putExtra(Intent.EXTRA_TEXT, seleteditem.sitename +"  售价:"+seleteditem.siteprice +"  \r\n "+ seleteditem.buylink);
    	                    	   returnIt.putExtra(Intent.EXTRA_SUBJECT, seleteditem.sitename);
    	                    	   returnIt.setType("message/rfc882");
    	                    	   Intent.createChooser(returnIt, "Send Email");
    	                    	   startActivity(returnIt);
    	                       }

    	                    }
    	                }).show();
						
						
					}
				});		
				
				break;
			case R.string.notify_network_error:
				pd.dismiss();
				Toast.makeText(ComparePricesActivity.this, R.string.notify_network_error, 5).show();
				break;
			case R.string.notify_service_exception:
				pd.dismiss();
				Toast.makeText(ComparePricesActivity.this, R.string.notify_service_exception, 5).show();
				break;
			case R.string.notify_no_result:
				pd.dismiss();
				Toast.makeText(ComparePricesActivity.this, R.string.notify_no_result, 5).show();
				break;
			default:
				Toast.makeText(ComparePricesActivity.this, R.string.notify_service_exception, 5).show();
			}
		}
	};
	
	
	
	private final String getLinkText(String link) {
		return link.substring(link.indexOf(">")+1, link.indexOf("</a>"));
	}
	private final String getLinkHref(String link) {
		String doubanurl="http://www.douban.com";
		String url= link.substring(link.indexOf("href=")+5, link.indexOf("onclick="));
		return doubanurl+url; 
	}

}