package com.offbye.bookmaster.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.offbye.bookmaster.android.douban.BookEntry;
import com.offbye.bookmaster.android.douban.ComparePricesActivity;
import com.offbye.bookmaster.android.douban.ReadReviewActivity;
import com.offbye.bookmaster.android.util.UrlImage;

public class BookView extends Activity {
	private static final String TAG = "BookView";
	
	private String bookisbn = "2023018";
	private String bookId = "2023018";	
	private BookEntry book=new BookEntry();
	

	private StringBuffer urlsb  =null;
	private ProgressDialog pd;
	private ImageButton returnhome;
	private Button readreviews,compareprices;
	private ImageView image;
	private TextView title,author,tranlator,isbn,pages,price,publisher,pubdate,summary,authorintro,tags;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book);

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
		
		Bundle extras = getIntent().getExtras();
		bookisbn = extras.getString("isbn");

		urlsb = new StringBuffer();

		urlsb.append("http://api.douban.com/book/subject/isbn/").append(bookisbn).append("?alt=json");

		returnhome.setOnClickListener(buttonReturnhomeListener);
		readreviews.setOnClickListener(buttonReadreviewsListener);
		compareprices.setOnClickListener(buttonComparePricesListener);
		getDataInitialize();
	}
	private final Button.OnClickListener buttonReturnhomeListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(BookView.this,HomeActivity.class);
			startActivity(i);
		}
	};
	private final Button.OnClickListener buttonReadreviewsListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(BookView.this,ReadReviewActivity.class);
            i.putExtra("id",bookId);
			startActivity(i);
		}
	};
	private final Button.OnClickListener buttonComparePricesListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent i = new Intent(BookView.this,ComparePricesActivity.class);
            i.putExtra("id",bookId);
			startActivity(i);
		}
	};		  
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
		pd = ProgressDialog.show(this, getString(R.string.msg_loading), getString(R.string.msg_wait),true, false);
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

			try {
				JSONObject jsonBook = new JSONObject(getBookInfo(urlsb.toString()));
				book.title = jsonBook.getJSONObject("title").getString("$t");
				int count=0;
				
				book.summary = jsonBook.getJSONObject("summary").getString("$t");
				JSONArray jsonLinks= jsonBook.getJSONArray("link");
				count = jsonLinks.length();
				for(int i=0;i<count;i++){
					if(jsonLinks.getJSONObject(i).getString("@rel").equals("self")){
						book.linkself= jsonLinks.getJSONObject(i).getString("@href");
					}
					if(jsonLinks.getJSONObject(i).getString("@rel").equals("image")){
						book.linkimage= jsonLinks.getJSONObject(i).getString("@href");
					}
					if(jsonLinks.getJSONObject(i).getString("@rel").equals("alternate")){
						book.linkalternate= jsonLinks.getJSONObject(i).getString("@href");
					}
					if(jsonLinks.getJSONObject(i).getString("@rel").equals("collection")){
						book.linkcollection= jsonLinks.getJSONObject(i).getString("@href");
					}
				}
				
				JSONArray dbattribute= jsonBook.getJSONArray("db:attribute");
				count = dbattribute.length();
				for(int i=0;i<count;i++){
					if(dbattribute.getJSONObject(i).getString("@name").equals("isbn10")){
						book.isbn10= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("isbn13")){
						book.isbn13= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("title")){
						book.title= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("pages")){
						book.pages= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("translator")){
						book.tranlator= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("price")){
						book.price= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("author")){
						book.author= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("translator")){
						book.tranlator= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("publisher")){
						book.publisher= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("binding")){
						book.binding= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("pubdate")){
						book.pubdate= dbattribute.getJSONObject(i).getString("$t");
					}
					if(dbattribute.getJSONObject(i).getString("@name").equals("author-intro")){
						book.authorintro= dbattribute.getJSONObject(i).getString("$t");
					}
					
				}
				
				JSONArray jsonAuthor= jsonBook.getJSONArray("author");
				count = jsonAuthor.length();
				if(count>1){
					StringBuffer author=new StringBuffer();
					for(int i=0;i< count;i++){
						author.append(jsonAuthor.getJSONObject(i).getJSONObject("name").getString("$t")).append(" ");
					}
				}
				
				JSONArray jsonTag= jsonBook.getJSONArray("db:tag");
				count = jsonTag.length();
				for(int i=0;i< count;i++){
					book.tags.add(jsonTag.getJSONObject(i).getString("@name") + " " + jsonTag.getJSONObject(i).getString("@count"));
				}
				
				book.min = jsonBook.getJSONObject("gd:rating").getInt("@min");
				book.max = jsonBook.getJSONObject("gd:rating").getInt("@max");
				book.numRaters = jsonBook.getJSONObject("gd:rating").getInt("@numRaters");
				book.average = jsonBook.getJSONObject("gd:rating").getDouble("@average");
				
				book.id = jsonBook.getJSONObject("id").getString("$t");
				//设置查询的bookID
				bookId = book.id.substring(35);
				progressHandler.sendEmptyMessage(R.string.notify_succeeded);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				progressHandler.sendEmptyMessage(R.string.notify_service_exception);
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
				showBook(book);
				Toast.makeText(BookView.this,R.string.notify_succeeded, 5).show();
				break;
			case R.string.notify_network_error:
				pd.dismiss();
				Toast.makeText(BookView.this,R.string.notify_network_error, 5).show();
				break;
			case R.string.notify_service_exception:
				pd.dismiss();
				Toast.makeText(BookView.this, R.string.notify_service_exception, 5).show();
				break;

			case R.string.notify_no_result:
				pd.dismiss();
				Toast.makeText(BookView.this, R.string.notify_no_result, 5).show();
				break;
		
			default:
				Toast.makeText(BookView.this, R.string.notify_service_exception, 5).show();
			}
		}
	};
	
	protected void showBook(BookEntry book){
		title.setText(book.title);
		author.setText(book.author);
		tranlator.setText(book.tranlator);
		isbn.setText(book.isbn13);
		pages.setText(book.pages);
		price.setText(book.price);
		publisher.setText(book.publisher);
		pubdate.setText(book.pubdate);
		
		StringBuffer sbtag=new StringBuffer();
		for(String tag : book.tags){
			sbtag.append(tag);
			sbtag.append("  ");
		}
		tags.setText(tags.getText()+ sbtag.toString());
		
		summary.setText(book.summary);
		authorintro.setText(book.authorintro);
		
		//\\反斜线字符。在书写时要写为\\\\。（注意：因为java在第一次解析时,把\\\\解析成正则表达式\\，在第二次解析时再解析为\，所以凡是不是1.1列举到的转义字符，包括1.1的\\,而又带有\的都要写两次）
		String bookimage =book.linkimage.replaceAll("\\\\", "").replace("spic", "mpic");
		//Log.v(TAG,"bookimage="+ bookimage);
		
		image.setImageBitmap(UrlImage.returnBitMap(bookimage));
	}
	

	


}