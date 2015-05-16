package com.offbye.bookmaster.android.douban;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import com.google.gdata.client.douban.DoubanService;
import com.google.gdata.data.douban.SubjectEntry;
import com.google.gdata.data.douban.SubjectFeed;
import com.google.gdata.util.ServiceException;
import com.offbye.bookmaster.android.HomeActivity;
import com.offbye.bookmaster.android.R;
import com.offbye.bookmaster.android.SearchActivity;
import com.offbye.bookmaster.android.SuggestView;
import com.offbye.bookmaster.android.util.Constants;



public class BookListActivity extends Activity {
	private static final String TAG = "BookListActivity";

	private String q = "世界";
	private String searchtag = "";
	private SubjectFeed feeds;;
	private SubjectEntry seleteditem =new SubjectEntry();
	
	private ProgressDialog pd;
	private ImageButton returnhome;
	private ListView optionsListView;
	private Button previous,next;

	private int current=1;
	private int start=1;
	private int pagenum=5;   //每页显示个数
	private int returnsize=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_list);
		Bundle extras = getIntent().getExtras();
		if(extras !=null && !("").equals(extras.getString("q"))){
			q = extras.getString("q");
		}		
		if(extras !=null && !("").equals(extras.getString("searchtag"))){
			searchtag = extras.getString("searchtag");
		}
		
		final Intent queryIntent = getIntent(); 
	    final String queryAction = queryIntent.getAction(); 
	    if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(queryIntent, "onCreate()");
        }


		optionsListView = (ListView) this.findViewById(R.id.listview);

		previous = (Button) this.findViewById(R.id.previous);
		next = (Button) this.findViewById(R.id.next);
		
		if(current==1){
			//不显示上一页下一页
			previous.setVisibility(View.INVISIBLE);
			next.setVisibility(View.INVISIBLE);
			getDataInitialize();
		}
		returnhome = (ImageButton) this.findViewById(R.id.returnhome);
		returnhome.setOnClickListener(buttonReturnhomeListener);
		previous.setOnClickListener(buttonPrevousListener);
		next.setOnClickListener(buttonNextListener);
	}
    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        
        // get and process search query here
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(queryIntent, "onNewIntent()");
        }
        else {
        	Toast.makeText(BookListActivity.this, "onNewIntent(), but no ACTION_SEARCH intent", 5).show();
        }
    }

	private void doSearchQuery(final Intent queryIntent, final String entryPoint) {
		// The search query is provided as an "extra" string in the query intent
		final String queryString = queryIntent
				.getStringExtra(SearchManager.QUERY);
		q = queryString;
	}


	private final Button.OnClickListener buttonPrevousListener = new Button.OnClickListener() {
		public void onClick(View view) {
			if(current>1){
				current = current-1;
				start = current * pagenum;
				getDataInitialize();
			}
		}
	};
	private final Button.OnClickListener buttonNextListener = new Button.OnClickListener() {
		public void onClick(View view) {
			current = current+1;
			start = current * pagenum;
			getDataInitialize();
		}
	};
	
	private final Button.OnClickListener buttonReturnhomeListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(BookListActivity.this,HomeActivity.class);
			startActivity(i);
		}
	};
	
		  

	public void getDataInitialize() {
		// 1. to start ProgressDialog
		pd = ProgressDialog.show(this, getString(R.string.msg_loading), getString(R.string.msg_wait), true, false);
		//pd.setIcon(R.drawable.zxing_icon);
		// 2. to start an Thread to do getDataThread working
		GetDataBody myWork = new GetDataBody();
		Thread getDataThread = new Thread(myWork);
		// 3. to call start() to do getDataThread working
		getDataThread.start();
	}


	
	private class GetDataBody implements Runnable {
		public void run() {
			DoubanService myService = new DoubanService("subApplication",  Constants.apiKey,Constants.secret);
			try {
				feeds = myService.findBook(q, searchtag, start, pagenum);

				returnsize=feeds.getEntries().size();
				if(returnsize==0){
					progressHandler.sendEmptyMessage(R.string.notify_no_result);
				}
				progressHandler.sendEmptyMessage(R.string.notify_succeeded);
			} catch (IOException e) {
				e.printStackTrace();
				progressHandler.sendEmptyMessage(R.string.notify_network_error);
			} catch (ServiceException e) {
				e.printStackTrace();
				progressHandler.sendEmptyMessage(R.string.notify_service_exception);
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
				//显示分页按钮判断
				if(returnsize<pagenum){
					next.setVisibility(View.INVISIBLE);
				}
				else{
					next.setVisibility(View.VISIBLE);
				}

				if(current>1){
					previous.setVisibility(View.VISIBLE);
				}
				else{
					previous.setVisibility(View.INVISIBLE);
				}
				
				BookAdapter pa = new BookAdapter(
						BookListActivity.this, R.layout.book_row, feeds.getEntries());
				optionsListView.setAdapter(pa);
//				optionsListView.setOnItemClickListener(new OnItemClickListener() {
//        			public void onItemClick(AdapterView<?> arg0, View arg1,int position, long id)
//        			{
//        				arg1.findViewById(R.id.title).setBackgroundColor(Color.RED);
//        			}
//				});
				
				optionsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
        			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long id)
        			{
        				seleteditem = feeds.getEntries().get(position);
        				
        				new AlertDialog.Builder(BookListActivity.this)
        	                .setTitle(R.string.select_dialog)
        	                .setItems(R.array.bookoptions, new DialogInterface.OnClickListener() {
        	                    public void onClick(DialogInterface dialog, int which) {
        	                       if(which==0){
        	           	            	Intent i = new Intent(BookListActivity.this, BookActivity.class); 
        	           	            	//Log.v(TAG,"getId"+seleteditem.getId());
        	           	            	i.putExtra("id", seleteditem.getId().substring(35));
        	           					startActivity(i); 
        	                       }
        	                       if(which==1){
       	           	            		Intent i = new Intent(BookListActivity.this, ReadReviewActivity.class); 
       	           	            		//Log.v(TAG,"getId"+seleteditem.getId());
       	           	            		//http://api.douban.com/book/subject/1029617 取最后的数字
       	           	            		i.putExtra("id", seleteditem.getId().substring(35));
       	           	            		startActivity(i); 
        	                       }
        	                       if(which==2){
       	           	            		Intent i = new Intent(BookListActivity.this, ComparePricesActivity.class); 
       	           	            		i.putExtra("id", seleteditem.getId().substring(35));
       	           	            		startActivity(i); 
        	                       }
        	                       if(which==3){
        	                    	   Intent intent = new Intent();
        	                    	   intent.setAction(Intent.ACTION_WEB_SEARCH);
        	                    	   intent.putExtra(SearchManager.QUERY,seleteditem.getTitle().getPlainText());
        	                    	   startActivity(intent);
        	                       }
        	                       if(which==4){
        	                    	   Intent i = new Intent(Intent.ACTION_VIEW);   
        	                    	   i.putExtra("sms_body", seleteditem.getTitle().getPlainText());   
        	                    	   i.setType("vnd.android-dir/mms-sms");   
        	                    	   startActivity(i);
        	                       }

        	                    }
        	                }).show();
						return true;
        			}
        		});
				
				break;
			case R.string.notify_network_error:
				pd.dismiss();
				Toast.makeText(BookListActivity.this, R.string.notify_network_error, 5).show();
				break;
			case R.string.notify_service_exception:
				pd.dismiss();
				Toast.makeText(BookListActivity.this, R.string.notify_service_exception, 5).show();
				break;
			case R.string.notify_no_result:
				pd.dismiss();
				Toast.makeText(BookListActivity.this, R.string.notify_no_result, 5).show();
				break;
			default:
				Toast.makeText(BookListActivity.this, R.string.notify_service_exception, 5).show();
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
			Intent i = new Intent(this,SearchActivity.class);
			startActivity(i);
			break;	
	

		}
		return super.onOptionsItemSelected(item);
	}
	


}