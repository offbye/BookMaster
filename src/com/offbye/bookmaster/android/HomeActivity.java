package com.offbye.bookmaster.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.offbye.bookmaster.android.douban.BookActivity;
import com.offbye.bookmaster.android.douban.BookEntry;
import com.offbye.bookmaster.android.douban.BookListActivity;
import com.offbye.bookmaster.android.douban.RecommendAdapter;
import com.offbye.bookmaster.android.util.Constants;


public class HomeActivity extends Activity {
	private static final String TAG = "HomeActivity";
	private EditText bookname;
	private Gallery g;
	private ArrayList<BookEntry> books = new ArrayList<BookEntry>();
	private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeview);
        
        this.getWindow().setBackgroundDrawableResource(R.drawable.bg);

        
        bookname = (EditText) this.findViewById(R.id.bookname);        
        bookname.setBackgroundResource(R.xml.textfield_search);
        ImageButton search = (ImageButton) this.findViewById(R.id.icon_search);
        search.setBackgroundResource(R.xml.btn_search_dialog);
        search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(bookname.getText().toString().trim().length()>0){
					Intent i = new Intent(HomeActivity.this,BookListActivity.class);
	                i.putExtra("q", bookname.getText().toString());
					startActivity(i);
				}
				else{
					new AlertDialog.Builder(HomeActivity.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.msg_searchlimittitle)
					.setMessage(R.string.msg_searchlimit)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).show();
				}
				
			}
		});
   
        ImageButton btn_scan = (ImageButton) this.findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(HomeActivity.this,CaptureActivity.class);
				startActivity(i);
				//finish();
			}
		});
        
        ImageButton btn_search = (ImageButton) this.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(HomeActivity.this,SearchActivity.class);
				startActivity(i);
			}
		});
        
        g = (Gallery) findViewById(R.id.gallery);
        ConnectivityManager cwjManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo info = cwjManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()){
			getDataInitialize();
		}
        else{
        	new AlertDialog.Builder(HomeActivity.this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.msg_no_connenction)
			.setMessage(R.string.msg_no_connenction_detail)
			.setPositiveButton(R.string.alert_dialog_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

						}
					}).show();
        }
        

    }
    
    
    
    public  String getBookInfo(String weburl) {
		String returnstr="";
		try {
			URL url = new URL(weburl);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(20000);
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
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

	public void getDataInitialize() {
		// 1. to start ProgressDialog
		pd = ProgressDialog.show(this, null,getString(R.string.msg_loading), true, true);
		//pd.setIcon(R.drawable.zxing_icon);
		// 2. to start an Thread to do getDataThread working
		GetDataBody myWork = new GetDataBody();
		Thread getDataThread = new Thread(myWork);
		// 3. to call start() to do getDataThread working
		getDataThread.start();
	}
	
	
	private class GetDataBody implements Runnable {
		public void run() {
			// 4. the working
			String re =getBookInfo(Constants.recommendUrl);
			if(re.length()>0){
				try {
					JSONArray jsonBooks = new JSONArray(getBookInfo(Constants.recommendUrl));
					for (int i = 0; i < jsonBooks.length(); i++) {
						JSONArray jp = jsonBooks.getJSONArray(i);
						BookEntry book = new BookEntry();
						book.id = jp.getString(0);
						book.linkimage = jp.getString(1);
						book.title = jp.getString(2);
						books.add(book);
					}
					progressHandler.sendEmptyMessage(R.string.notify_succeeded);
				} catch (JSONException e) {
					e.printStackTrace();
					progressHandler.sendEmptyMessage(R.string.notify_service_exception);
				}
			}
		}

	}

	// Define the Handler that receives messages from the thread
	private Handler progressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.string.notify_succeeded:
				pd.dismiss();
				  g.setAdapter(new RecommendAdapter(HomeActivity.this,R.layout.book_row, books));
				  g.setSelection(2);
			      g.setOnItemClickListener(new OnItemClickListener() { 
			           public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			        	   Intent i = new Intent(HomeActivity.this, BookActivity.class); 
			        	   i.putExtra("id", books.get(position).id);
			        	   startActivity(i); 
			           }
			       });
				
				//Toast.makeText(HomeActivity.this,R.string.notify_succeeded, 5).show();
				break;
			case R.string.notify_network_error:
				pd.dismiss();
				Toast.makeText(HomeActivity.this,R.string.notify_network_error, 5).show();
				break;
			case R.string.notify_service_exception:
				pd.dismiss();
				Toast.makeText(HomeActivity.this, R.string.notify_service_exception, 5).show();
				break;

			case R.string.notify_no_result:
				pd.dismiss();
				Toast.makeText(HomeActivity.this, R.string.notify_no_result, 5).show();
				break;
		
			default:
				Toast.makeText(HomeActivity.this, R.string.notify_service_exception, 5).show();
			}
		}
	};
	
	
	
  
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, this.getText(R.string.menu_about)).setIcon(R.drawable.icon);
		menu.add(0, 1, 1, this.getText(R.string.submitsuggest)).setIcon(R.drawable.suggest);
		menu.add(0, 2, 2,  this.getText(R.string.search)).setIcon(android.R.drawable.ic_menu_search);

		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.icon)
					.setTitle(R.string.menu_about)
					.setMessage(R.string.abouttext)
					.setPositiveButton(this.getText(R.string.alert_dialog_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).show();

			break;
		case 1:
			startActivity(new Intent(this,SuggestView.class));
			break;
		case 2:
			//onSearchRequested();
			Intent i = new Intent(HomeActivity.this,SearchActivity.class);
			startActivity(i);
			break;	
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onSearchRequested() {

		Bundle appDataBundle = null;

		appDataBundle = new Bundle();
		appDataBundle.putString("demo_key", "test");

		// Now call the Activity member function that invokes the Search Manager UI.
		startSearch("", false, appDataBundle, false); 
		// Returning true indicates that we did launch the search, instead of blocking it.
		return true;
	}
	
}