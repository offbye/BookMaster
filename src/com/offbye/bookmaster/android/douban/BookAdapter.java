package com.offbye.bookmaster.android.douban;

import java.util.List;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gdata.data.douban.Attribute;
import com.google.gdata.data.douban.SubjectEntry;
import com.offbye.bookmaster.android.R;
import com.offbye.bookmaster.android.util.UrlImage;

public class BookAdapter extends ArrayAdapter<SubjectEntry> {
	private static final String TAG = "BookAdapter";
	int resource;
	Context context;

	public BookAdapter(Context _context, int _resource,
			List<SubjectEntry> _items) {
		super(_context, _resource, _items);
		context = _context;
		resource = _resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LinearLayout todoView;
		SubjectEntry subjectEntry = getItem(position);

		if (convertView == null)
		{
			todoView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi;
			vi = (LayoutInflater) getContext().getSystemService(inflater);
			vi.inflate(resource, todoView, true);
		}
		else
		{
			todoView = (LinearLayout) convertView;
		}

		TextView titleView = (TextView) todoView.findViewById(R.id.title);
		TextView authorView = (TextView) todoView.findViewById(R.id.author);
		TextView tranlatorView = (TextView) todoView.findViewById(R.id.tranlator);
		
		TextView priceView = (TextView) todoView.findViewById(R.id.price);
		
		TextView publisherView = (TextView) todoView.findViewById(R.id.publisher);
		TextView pubdateView = (TextView) todoView.findViewById(R.id.pubdate);
		
		ImageView bookImage = (ImageView) todoView.findViewById(R.id.bookimage);
		RatingBar ratingBar = (RatingBar)todoView.findViewById(R.id.Ratingbar);
		TextView ratingscoreView = (TextView) todoView.findViewById(R.id.ratingscore);
		TextView ratingnumView = (TextView) todoView.findViewById(R.id.ratingnum);
		
		bookImage.setImageBitmap(UrlImage.returnBitMap(subjectEntry.getLink("image", null).getHref()));
		
		ratingBar.setMax(5);
		ratingBar.setRating(subjectEntry.getRating().getAverage().floatValue()/2);
		titleView.setText(subjectEntry.getTitle().getPlainText());

		if (!subjectEntry.getAuthors().isEmpty()) {
			authorView.setText(subjectEntry.getAuthors().get(0).getName());
		}
		////Log.v(TAG,"subjectEntry : " + subjectEntry.getId());
		for (Attribute attr : subjectEntry.getAttributes()) {
		    ////Log.v(TAG,attr.getName() + " : " + attr.getContent());
			if(attr.getName().equals("translator")){
				tranlatorView.setText(getContext().getString(R.string.booktranlator)+" "+attr.getContent());
			}
			if(attr.getName().equals("price")){
				priceView.setText(getContext().getString(R.string.bookprice)+" "+attr.getContent());
			}
			if(attr.getName().equals("publisher")){
				publisherView.setText(attr.getContent());
			}
			if(attr.getName().equals("pubdate")){
				pubdateView.setText(attr.getContent()+getContext().getString(R.string.bookpub));
			}
		}
		ratingscoreView.setText(subjectEntry.getRating().getAverage().floatValue()+"");
		ratingnumView.setText("   ("+subjectEntry.getRating().getNumRaters().toString()+getContext().getString(R.string.bookvotes)+")");
		return todoView;
	}
	
}
