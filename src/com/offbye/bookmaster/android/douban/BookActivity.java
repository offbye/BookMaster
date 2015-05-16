package com.offbye.bookmaster.android.douban;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gdata.client.douban.DoubanService;
import com.google.gdata.data.Person;
import com.google.gdata.data.douban.Attribute;
import com.google.gdata.data.douban.SubjectEntry;
import com.google.gdata.data.douban.Tag;
import com.google.gdata.util.ServiceException;
import com.offbye.bookmaster.android.HomeActivity;
import com.offbye.bookmaster.android.R;
import com.offbye.bookmaster.android.util.Constants;
import com.offbye.bookmaster.android.util.UrlImage;

public class BookActivity extends Activity {
	private static final String TAG = "BookActivity";

	private String bookId = "2023018";
	private SubjectEntry subjectEntry;;
	
	private ProgressDialog pd;
	private ImageView image;
	private ImageButton returnhome;
	private Button readreviews,compareprices;
	private TextView title,author,tranlator,isbn,pages,price,publisher,pubdate,summary,authorintro,tags;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book);
		
		Bundle extras = getIntent().getExtras();
		if(extras.getString("id")!=null){
			bookId = extras.getString("id");
		}		
		
		returnhome = (ImageButton) this.findViewById(R.id.returnhome);
		readreviews = (Button) this.findViewById(R.id.readreviews);
		compareprices = (Button) this.findViewById(R.id.compareprices);

		image = (ImageView) this.findViewById(R.id.bookimage);
		title = (TextView) this.findViewById(R.id.title);
		author = (TextView) this.findViewById(R.id.author);
		tranlator = (TextView) this.findViewById(R.id.tranlator);
		isbn = (TextView) this.findViewById(R.id.isbn);
		pages = (TextView) this.findViewById(R.id.pages);
		price = (TextView) this.findViewById(R.id.price);
		publisher = (TextView) this.findViewById(R.id.publisher);
		pubdate = (TextView) this.findViewById(R.id.pubdate);
		tags = (TextView) this.findViewById(R.id.tags);
		
		summary = (TextView) this.findViewById(R.id.summary);
		authorintro = (TextView) this.findViewById(R.id.authorintro);
		
		returnhome.setOnClickListener(buttonReturnhomeListener);
		readreviews.setOnClickListener(buttonReadreviewsListener);
		compareprices.setOnClickListener(buttonComparePricesListener);

		getDataInitialize();
	}

	private final Button.OnClickListener buttonReturnhomeListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(BookActivity.this,HomeActivity.class);
			startActivity(i);
		}
	};
	private final Button.OnClickListener buttonReadreviewsListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(BookActivity.this,ReadReviewActivity.class);
            i.putExtra("id",bookId);
			startActivity(i);
		}
	};
	private final Button.OnClickListener buttonComparePricesListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(BookActivity.this,ComparePricesActivity.class);
            i.putExtra("id",bookId);
			startActivity(i);
		}
	};		  

	public void getDataInitialize() {
		// 1. to start ProgressDialog
		pd = ProgressDialog.show(this,getString(R.string.msg_loading), getString(R.string.msg_wait),true, false);
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
				subjectEntry = myService.getBook(bookId);
				
				if(subjectEntry==null){
					progressHandler.sendEmptyMessage(R.string.notify_no_result);
				}
				progressHandler.sendEmptyMessage(R.string.notify_succeeded);
				////Log.v(TAG,"subjectEntry"+subjectEntry.getTitle().getPlainText());
			} catch (IOException e) {
				//e.printStackTrace();
				progressHandler.sendEmptyMessage(R.string.notify_network_error);
			} catch (ServiceException e) {
				//e.printStackTrace();
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
				showBook(subjectEntry);
				
				break;
			case R.string.notify_network_error:
				pd.dismiss();
				Toast.makeText(BookActivity.this, R.string.notify_network_error, 5).show();
				break;
			case R.string.notify_service_exception:
				pd.dismiss();
				Toast.makeText(BookActivity.this, R.string.notify_service_exception, 5).show();
				break;
			case R.string.notify_no_result:
				pd.dismiss();
				Toast.makeText(BookActivity.this, R.string.notify_no_result, 5).show();
				break;
			default:
				title.setText(R.string.notify_service_exception);
			}
		}
	};
	
	protected void showBook(SubjectEntry subjectEntry){
		title.setText(subjectEntry.getTitle().getPlainText());
		StringBuffer authorsb=new StringBuffer();
		for(Person p:subjectEntry.getAuthors()){
			authorsb.append(p.getName()).append(" ");
		}
		author.setText(authorsb.toString());
		
		for (Attribute attr : subjectEntry.getAttributes()) {
			if(attr.getName().equals("isbn13")){
				isbn.setText(attr.getContent());
			}
			if(attr.getName().equals("pages")){
				tranlator.setText(attr.getContent());
			}
			if(attr.getName().equals("pages")){
				pages.setText(attr.getContent());
			}
			if(attr.getName().equals("price")){
				price.setText(attr.getContent());
			}
			if(attr.getName().equals("publisher")){
				publisher.setText(attr.getContent());
			}
			if(attr.getName().equals("pubdate")){
				pubdate.setText(attr.getContent());
			}
			if(attr.getName().equals("author-intro")){
				authorintro.setText(attr.getContent());
			}
			////Log.v(TAG,attr.getName() + " : " + attr.getContent());
		}
		
		StringBuffer tagsb=new StringBuffer(128);
		tagsb.append("Tags: ");
		for (Tag tag : subjectEntry.getTags()) {
			tagsb.append(tag.getName()).append("(").append(tag.getCount()).append(")  ");
		}
		tags.setText(tagsb.toString());
		if(subjectEntry.getSummary()!=null){
			summary.setText(subjectEntry.getSummary().getPlainText());
		}

		if(subjectEntry.getLink("image",null)!=null){
			image.setImageBitmap(UrlImage.returnBitMap(subjectEntry.getLink("image",null).getHref().replace("spic", "mpic")));
		}
		
	}

}