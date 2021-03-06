package com.example.benchmarkactivity;

import java.util.ArrayList;
import java.util.List;

import com.example.benchmarkactivity.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Widget extends LinearLayout {

	private Button				buttonLeft;
	private Button				buttonRight;
	private TextView		 	textValue;
	private ListClickListener	leftClickListeners;
	private ListClickListener	rightClickListeners;
	private List<OnTouchListener> registeredListener = new ArrayList<OnTouchListener>();
	public static final int DEFAULT_INDEX = 0;
	public static final int values[] = {
		100, 300, 400, 500, 600, 700, 800, 1000, 1200, 1300, 1500, 1700, 2000};
	private int index = DEFAULT_INDEX;
	
/*** PERSONAL CALLBACKS ***/
	public interface OnProgressListener{
		public void onChangeProgress(int progress);
	}
/*** 					***/
	
/*** CONSTRUCTORS ***/
	public Widget(Context context) {
		super(context);
		inflate(context);
		setClicksListeners();
	}

	public Widget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context);
		setClicksListeners();
	}
/*** 			***/
	
	private void inflate(Context context){
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		inflater.inflate(R.layout.widgetlayout, this, true);
		
		buttonLeft	= (Button)	findViewById(R.id.IBLeft);
		textValue	= (TextView)findViewById(R.id.text);
		buttonRight	= (Button)	findViewById(R.id.IBRight);
		
		setValueByIndex(index);
	}
	
	/**
	 * setUp the default behaviour for click events on two directions buttons
	 */
	private void setClicksListeners(){
		leftClickListeners	= new ListClickListener();
		rightClickListeners	= new ListClickListener();
		
		leftClickListeners.registerClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sub();
			}
		});
		
		rightClickListeners.registerClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				add();
			}
		});
		
		buttonLeft.setOnClickListener(leftClickListeners);
		buttonRight.setOnClickListener(rightClickListeners);
	}
	
	public void addLeftClickListener(OnClickListener l){
		leftClickListeners.registerClickListener(l);
	}
	
	public void removeLeftClickListener(OnClickListener l){
		leftClickListeners.unRegisterClickListener(l);
	}
	
	public void addRightClickListener(OnClickListener l){
		rightClickListeners.registerClickListener(l);
	}
	
	public void removeRightClickListener(OnClickListener l){
		leftClickListeners.unRegisterClickListener(l);
	}
	
	/**
	 * It allows to manage lists of clickListener for the same click event
	 * @author F31999A
	 *
	 */
	private class ListClickListener implements OnClickListener {
		private List<OnClickListener> listClickListeners = new ArrayList<OnClickListener>(2);
		
		public void registerClickListener(OnClickListener l){
			listClickListeners.add(l);
		}
		
		public void unRegisterClickListener(OnClickListener l){
			listClickListeners.remove(l);
		}
		
		@Override
		public void onClick(View v) {
			for (OnClickListener click : listClickListeners) {
				click.onClick(v);
			}
		}
	}
	
	public synchronized void setValueByIndex(int index) {
		this.index = index;
		textValue.setText(""+values[this.index] + " Byte");
	}

	/**
	 * Actually it implements a listener list phylosofy
	 * @param l
	 */
	@Override
	public void setOnTouchListener(OnTouchListener l) {
		registeredListener.add(l);
	}

	/**
	 * It allows to unregister a Touchlistener
	 * @param l
	 */
	public void removeOnTouchListener(OnTouchListener l){
		registeredListener.remove(l);
	}

	public int getIndex(){
		return index;
	}

	public int getIntValue(){
		return values[index];
	}

	public String getValue() {
		return ""+values[index];
	}

	public int sub() {
		if(index > 0){
			--index;
		}
		textValue.setText(""+values[this.index] + " Byte");
//		Log.e("WIDGET", "" + values[index]);
		return values[index];
	}

	public int add() {
		if(index < values.length - 1){
			++index;
		} else {
			index = values.length-1;
		}
		textValue.setText(""+values[this.index] + " Byte");
//		Log.e("WIDGET", "" + values[index]);
		return values[index];
	}
}
