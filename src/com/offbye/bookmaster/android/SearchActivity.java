package com.offbye.bookmaster.android;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.offbye.bookmaster.android.douban.BookListActivity;

public class SearchActivity extends Activity {
	private static final String TAG = "SearchActivity";

	private EditText q,searchtag;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        this.getWindow().setBackgroundDrawableResource(R.drawable.bg);
        
        q = (EditText) this.findViewById(R.id.q);
        searchtag = (EditText) this.findViewById(R.id.searchtag);

		
		Button search = (Button) this.findViewById(R.id.search);
		search.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if("".equals(q.getText().toString()) && "".equals(searchtag.getText().toString())){
						new AlertDialog.Builder(SearchActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.msg_title_adsearchlimit)
						.setMessage(R.string.msg_adsearchlimit)
						.setPositiveButton(R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).show();
					}
					else{
						Intent i = new Intent(SearchActivity.this,BookListActivity.class);
		                i.putExtra("q", q.getText().toString());
		                i.putExtra("searchtag", searchtag.getText().toString());
						startActivity(i);
					}
					
				}
			});
		        
    }
  
  
	
}