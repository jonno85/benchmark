package com.example.benchmarkactivity;

import com.example.benchmarkservice.IBenchMarkService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	//Service Variables
	private static AIDLConnection	BoundAIDLConnection;
	private IBenchMarkService		IBenchService;
	private boolean					mBound;
	private boolean					state = false;
	private TextView	textBarByte;
	private TextView	textResultService;
	private Widget		widget;
	private Button		bStartAIDLService;
	private Button		bStopAIDLService;
	private float		value = 0;
	private int 		counter = 0;
	private Intent		intent;
	private BReceiver	bcRec;
	private long		begin, stop;

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
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class AIDLConnection implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IBenchService = IBenchMarkService.Stub.asInterface(service);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			IBenchService = null;
			mBound = false;
		}
	}

	public class BReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			counter++;
			Log.i("RECEIVER", "received "+ counter + " element");
		}
	}

	private void mapObjectAndListener(){
		widget				= (Widget)	findViewById(R.id.Widget);
		textBarByte			= (TextView)findViewById(R.id.textViewByte);
		bStartAIDLService	= (Button)	findViewById(R.id.buttonStartAIDL);
		bStopAIDLService	= (Button)	findViewById(R.id.buttonStopAIDL);
		textResultService	= (TextView)findViewById(R.id.textView1);

		bStartAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!state)
				try {
					state = true;
					counter = 0;
					begin = System.currentTimeMillis();
					IBenchService.startRunning();
				} catch (RemoteException e) {
					Log.e("AIDL", "START SEND ERROR");
				}
			}
		});
		
		bStopAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(state)
					try{
						double servDiff = IBenchService.stopRunning();
						stop = System.currentTimeMillis();
						state = false;
						double gap = stop - begin;
						textResultService.setText("Elapsed time service: " + servDiff + 
												"\nElapsed time activity: " + gap + 
												"\nPackets counted: " + counter +
												"\nPackets sended from service: " + IBenchService.getNPackets() +
												"\nTotal sended: " + (counter * value) + " Byte" + 
												"\nActivity rate: " + (double)(counter/gap) + 
												"\nService rate: " + (double)(IBenchService.getNPackets()/servDiff));
					} catch(RemoteException e){
						Log.e("AIDL", "STOP SEND ERROR");
					}
			}
		});
		
		widget.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setTextBarByte();
				try {
					IBenchService.setBurstSize((int)value);
				} catch (RemoteException e) {
					Log.e("AIDL", "SETBURST SIZE");
				}
			}
		});
	}

	private void setTextBarByte(){
		textBarByte.setText("" + widget.getValue() + " Byte");
	}
}
