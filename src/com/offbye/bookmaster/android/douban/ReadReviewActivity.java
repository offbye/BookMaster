package com.offbye.bookmaster.android.douban;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.google.gdata.client.douban.DoubanService;
import com.google.gdata.data.douban.ReviewEntry;
import com.google.gdata.data.douban.ReviewFeed;
import com.google.gdata.util.ServiceException;
import com.offbye.bookmaster.android.HomeActivity;
import com.offbye.bookmaster.android.R;
import com.offbye.bookmaster.android.util.Constants;


public class ReadReviewActivity extends Activity {
	private static final String TAG = "ReadReviewActivity";

	private String bookId = "3108935";
	private ReviewFeed reviewFeed;;
	private ReviewEntry seleteditem;
	
	private ProgressDialog pd;
	private ImageButton returnhome;
	private ListView optionsListView;
	private Button previous,next;
	
	private int current=1;
	private int start=1;
	private int pagenum=10;   //每页评论数
	private int returnsize=0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_list);
		
		Bundle extras = getIntent().getExtras();
		if(extras !=null && !extras.getString("id").equals("")){
			//Log.v(TAG,"extras"+extras.getString("id"));
			bookId = extras.getString("id");
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
			Intent i = new Intent(ReadReviewActivity.this,HomeActivity.class);
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
			// 4. the working

			DoubanService myService = new DoubanService("subApplication",  Constants.apiKey,Constants.secret);
			
			try {
				reviewFeed = myService.getBookReviews(bookId, start, pagenum, "time");

				returnsize=reviewFeed.getEntries().size();
				if(returnsize==0){
					progressHandler.sendEmptyMessage(R.string.notify_no_result);
				}
				progressHandler.sendEmptyMessage(R.string.notify_succeeded);
//				//Log.v(TAG,"reviewFeed"+reviewFeed.getTitle().getPlainText());
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
				ReviewAdapter pa = new ReviewAdapter(
						ReadReviewActivity.this, R.layout.review_row, reviewFeed.getEntries());
				optionsListView.setAdapter(pa);
				optionsListView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,int position, long id)
					{
						if(arg1.findViewById(R.id.summary).getVisibility()==View.GONE){
							arg1.findViewById(R.id.summary).setVisibility(View.VISIBLE);
						}
						else{
							arg1.findViewById(R.id.summary).setVisibility(View.GONE);
						}
					}
			   });
				optionsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
        			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long id)
        			{
        				//arg1.findViewById(R.id.summary).setBackgroundColor(Color.RED);
        				seleteditem = reviewFeed.getEntries().get(position);
						new AlertDialog.Builder(ReadReviewActivity.this)
    	                .setTitle(R.string.select_dialog)
    	                .setItems(R.array.reviewoptions, new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog, int which) {
    	                       if(which==0){
    	                    	   Intent i = new Intent(Intent.ACTION_VIEW);   
    	                    	   i.putExtra("sms_body", "豆瓣精彩书评： \n"+seleteditem.getTitle().getPlainText()+"  \n"+seleteditem.getId().replace("api", "www")+" \n  通过淘书趣转发");   
    	                    	   i.setType("vnd.android-dir/mms-sms");   
    	                    	   startActivity(i);
    	                       }
    	                       if(which==1){
    	                    	   Intent returnIt = new Intent(Intent.ACTION_SEND);
    	                    	   returnIt.putExtra(Intent.EXTRA_SUBJECT, seleteditem.getTitle().getPlainText());
    	                    	   returnIt.putExtra(Intent.EXTRA_TEXT, "豆瓣精彩书评： \n"+seleteditem.getTitle().getPlainText()+"  \n"+seleteditem.getId().replace("api", "www")+" \n  通过淘书趣转发");
    	                    	   returnIt.setType("message/rfc882");
    	                    	   Intent.createChooser(returnIt, "Send Email");
    	                    	   startActivity(returnIt);
    	                       }
    	                       if(which==2){
    	                    	   Uri uri = Uri.parse(seleteditem.getId().replace("api", "www"));
    	                    	   Intent it  = new Intent(Intent.ACTION_VIEW,uri);
    	                    	   startActivity(it);
    	                       }
    	                    }
    	                }).show();
						return true;
        			}
				});
				break;
			case R.string.notify_network_error:
				pd.dismiss();
				Toast.makeText(ReadReviewActivity.this, R.string.notify_network_error, 5).show();
				break;
			case R.string.notify_service_exception:
				pd.dismiss();
				Toast.makeText(ReadReviewActivity.this, R.string.notify_service_exception, 5).show();
				break;
			case R.string.notify_no_result:
				pd.dismiss();
				next.setVisibility(View.INVISIBLE);
				Toast.makeText(ReadReviewActivity.this, R.string.notify_no_result, 5).show();
				break;
			default:
				Toast.makeText(ReadReviewActivity.this, R.string.notify_service_exception, 5).show();
			}
		}
	};

}