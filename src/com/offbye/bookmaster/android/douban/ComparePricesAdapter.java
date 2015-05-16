package com.offbye.bookmaster.android.douban;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.offbye.bookmaster.android.douban.BuyLink;
import com.offbye.bookmaster.android.R;

public class ComparePricesAdapter extends ArrayAdapter<BuyLink> {
	private static final String TAG = "ComparePricesAdapter";
	int resource;
	Context context;

	public ComparePricesAdapter(Context _context, int _resource,
			ArrayList<BuyLink> _items) {
		super(_context, _resource, _items);
		context = _context;
		resource = _resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LinearLayout todoView;
		BuyLink buyLink = getItem(position);

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

		TextView sitenameView = (TextView) todoView.findViewById(R.id.sitename);
		TextView sitepriceView = (TextView) todoView.findViewById(R.id.siteprice);
		TextView savedpriceView = (TextView) todoView.findViewById(R.id.savedprice);
		ImageView sitelogoImage = (ImageView) todoView.findViewById(R.id.sitelogo);
		
		sitenameView.setText(buyLink.sitename);
		sitepriceView.setText("售价"+buyLink.siteprice);
		savedpriceView.setText("节省"+buyLink.savedprice);
		
		if("dangdang".equals(buyLink.sitelogo)){
			sitelogoImage.setImageResource(R.drawable.dangdang);
		}
		if("bookschina".equals(buyLink.sitelogo)){
			sitelogoImage.setImageResource(R.drawable.bookschina);
		}
		if("chinapub".equals(buyLink.sitelogo)){
			sitelogoImage.setImageResource(R.drawable.chinapub);
		}
		if("beifa".equals(buyLink.sitelogo)){
			sitelogoImage.setImageResource(R.drawable.beifa);
		}
		if("joyo".equals(buyLink.sitelogo)){
			sitelogoImage.setImageResource(R.drawable.joyo);
		}
		if("99read".equals(buyLink.sitelogo)){
			sitelogoImage.setImageResource(R.drawable.read99);
		}
		if("xinhua".equals(buyLink.sitelogo)){
			sitelogoImage.setImageResource(R.drawable.xinhua);
		}
		


		return todoView;
	}
	
	

}
