package com.example.benchmarkactivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.benchmarkservice.*;

public class MainActivity extends Activity {

	//Service Variables
	private static AIDLConnection	BoundAIDLConnection;
	private IBenchMarkService		IBenchService;
	private boolean					state = false;
	
	private byte[] buffer;
	
	private TextView	textResultService;
	private TextView	textViewPayload;
	private Widget		widget;
	private Button		bStartAIDLService;
	private Button		bStopAIDLService;
	
	private int 		counter;
	private Intent		intent;
	private BReceiver	bcRec;
	private long		begin, stop;
	private Handler		handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mapObjectAndListener();
	}

	@Override
	protected void onStart() {
		super.onStart();

		BoundAIDLConnection = new AIDLConnection();
		intent = new Intent("com.example.benchmarkservice.IBenchMarkService");
		bindService(intent, BoundAIDLConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bcRec = new BReceiver();
		registerReceiver(bcRec, new IntentFilter("com.example.benchmarkactivity.MainActivity"));
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onStop() {
		unregisterReceiver(bcRec);
		unbindService(BoundAIDLConnection);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Class definition to connect on AIDL Service
	 * @author F31999A
	 *
	 */
	class AIDLConnection implements ServiceConnection{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IBenchService = IBenchMarkService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			IBenchService = null;
		}
	}

	public class BReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			counter++;
			writeByteArray(intent.getByteArrayExtra("LOAD"));
		}
	}

	/**
	 * Initialize all the object from the ContentView and associate 
	 * their relative listeners
	 */
	private void mapObjectAndListener(){
		widget				= (Widget)		findViewById(R.id.Widget);
		LinearLayout 	  l = (LinearLayout)findViewById(R.id.layerButtons);
		bStartAIDLService	= (Button)		l.findViewById(R.id.buttonStartAIDL);
		bStopAIDLService	= (Button)		l.findViewById(R.id.buttonStopAIDL);
		textResultService	= (TextView)	findViewById(R.id.textView1);
		textViewPayload		= (TextView)	findViewById(R.id.textViewPayload);

		bStartAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!state){
					try {
						state = true;
						counter = 0;
						begin = System.currentTimeMillis();
						IBenchService.startRunning();
						handler = new Handler();
						show();
					} catch (RemoteException e) {
						Log.e("AIDL", "START SEND ERROR");
					}
				}
			}
		});
		
		bStopAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(state){
					try{
						double servDiff = IBenchService.stopRunning();
						stop = System.currentTimeMillis();
						state = false;
						handler = null;
						double gap = stop - begin;
						textResultService.setText("ELAPSED TIME [ms]:\nservice: " + servDiff + 
												"\nactivity: " + gap + 
												"\n\nPACKETS COUNTED:\n activity: " + counter +
												"\nservice: " + IBenchService.getNPackets() +
												"\n\nTotal sended: " + (counter * widget.getIntValue()) + " Byte" +
												"\n\n PACKET'S RATE [pks/ms]:" +
												"\nActivity rate: " + (double)(counter/gap) + 
												"\nService rate: " + (double)(IBenchService.getNPackets()/servDiff));
					} catch(RemoteException e){
						Log.e("AIDL", "STOP SEND ERROR");
					}
				}
			}
		});
		
		widget.addLeftClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					IBenchService.setBurstSize(widget.getIntValue());
				} catch (RemoteException e) {
					Log.e("AIDL", "SETBURST SIZE -");
				}
			}
		});
		
		widget.addRightClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					IBenchService.setBurstSize(widget.getIntValue());
				} catch (RemoteException e) {
					Log.e("AIDL", "SETBURST SIZE +");
				}
			}
		});
	}

	/**
	 * define and schedule partial data 
	 */
	private void show(){
		if(handler != null){
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					double gap = System.currentTimeMillis() - begin;
					int n = counter;
					print(gap, n);
				}
			}, 1000);
		}
	}

	/**
	 * print partial data and reschedule show process
	 * @param gap
	 * @param counter
	 */
	private void print(double gap, int counter) {
		if(state){
			textResultService.setText("ELAPSED TIME [ms]:" + 
								"\nactivity: " + gap + 
								"\n\nPACKETS COUNTED:\n activity: " + counter +
								"\n\nTotal sended: " + (counter * widget.getIntValue()) + " Byte" +
								"\n\n PACKET'S RATE [pks/ms]:" +
								"\nActivity rate: " + (double)(counter/gap));
			String s = "";
			for(int i=0; i<buffer.length; i++)
				s += (buffer[i] + " | ");
			textViewPayload.setText("\nsize: " + buffer.length + "\nPayload: " + s);
			show();
		}
	}

	private synchronized void writeByteArray(byte[] bufferIn){
		buffer = bufferIn.clone();
	}
}
