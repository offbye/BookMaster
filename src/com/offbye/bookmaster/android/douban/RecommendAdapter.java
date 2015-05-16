package com.offbye.bookmaster.android.douban;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.offbye.bookmaster.android.R;
import com.offbye.bookmaster.android.util.UrlImage;

public class RecommendAdapter extends ArrayAdapter<BookEntry> {
	
	int mGalleryItemBackground;
	private static final String TAG = "RecommendAdapter";
	int resource;
	Context context;
	
    public RecommendAdapter(Context _context, int _resource,
    		ArrayList<BookEntry> _items) {
		super(_context, _resource,  _items);
		context = _context;
		resource = _resource;
		
		TypedArray a = context.obtainStyledAttributes(R.styleable.Gallery1);
		mGalleryItemBackground = a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
		a.recycle();
	}


    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(context);

        BookEntry bookEntry = getItem(position);
        i.setImageBitmap(UrlImage.returnBitMap(bookEntry.linkimage));
        i.setScaleType(ImageView.ScaleType.FIT_XY);
        i.setLayoutParams(new Gallery.LayoutParams(90, 120));
        
        // The preferred Gallery item background
        i.setBackgroundResource(mGalleryItemBackground);
        
        return i;
    }

}