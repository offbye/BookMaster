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

import com.google.gdata.data.douban.ReviewEntry;
import com.google.gdata.data.extensions.Rating;
import com.offbye.bookmaster.android.R;
import com.offbye.bookmaster.android.util.UrlImage;

public class ReviewAdapter extends ArrayAdapter<ReviewEntry> {
	private static final String TAG = "ReviewAdapter";
	int resource;
	Context context;

	public ReviewAdapter(Context _context, int _resource,
			List<ReviewEntry> _items) {
		super(_context, _resource, _items);
		context = _context;
		resource = _resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LinearLayout todoView;
		ReviewEntry reviewEntry = getItem(position);

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
		TextView publishdateView = (TextView) todoView.findViewById(R.id.publisheddate);
		TextView summaryView = (TextView) todoView.findViewById(R.id.summary);
		ImageView avatarImage = (ImageView) todoView.findViewById(R.id.user_avatar);
		RatingBar ratingBar = (RatingBar)todoView.findViewById(R.id.Ratingbar);
		
		String icon = reviewEntry.getAuthors().get(0).getXmlBlob().getBlob();
		if(icon.indexOf("<link rel='icon' href='")>0){
			icon = icon.substring(icon.indexOf("<link rel='icon' href='")+23, icon.lastIndexOf("'/>"));
			avatarImage.setImageBitmap(UrlImage.returnBitMap(icon));
		}
		
		ratingBar.setMax(5);
		ratingBar.setRating(reviewEntry.getRating().getValue().floatValue());
		titleView.setText(reviewEntry.getTitle().getPlainText());
		if (!reviewEntry.getAuthors().isEmpty()) {
			authorView.setText(reviewEntry.getAuthors().get(0).getName());
		}
		publishdateView.setText(reviewEntry.getPublished().toUiString());
		summaryView.setText(reviewEntry.getSummary().getPlainText());
		
		return todoView;
	}
	
}
