package com.offbye.bookmaster.android.douban;

import java.util.ArrayList;

public class BookEntry {
	public String id;
	public String subjectID;
	public String title;
	public String author;
	public String summary;
	public String isbn10;
	public String isbn13;
	public String pages;
	public String tranlator;
	public String price;
	public String publisher;
	public String binding;
	public String pubdate;
	public String authorintro;

	public String linkimage;
	public String linkself;
	public String linkalternate;
	public String linkcollection;
	
	public int numRaters;
	public double average;
	public int min;
	public int max;
	public ArrayList<String> tags =new ArrayList<String>();
}
