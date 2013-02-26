package com.example.benchmarkactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.TextView;

import com.example.benchmarkservice.IActivityListener;
import com.example.benchmarkservice.IBenchMarkService;

public class MainActivity extends Activity {

	//Service Variables
	private static AIDLConnection	BoundAIDLConnection;
	private IBenchMarkService		IBenchService;
	private boolean					state = false;
	
	private byte[] buffer;
	private static int N_PACKETS = 10000;
	
	private TextView	textResultService;
	private TextView	textViewPayload;
	private Widget		widget;
	private Button		bStartAIDLService;
	private Button		bStopAIDLService;
	
	private long 		counter;
	private Intent		intent;
	private BReceiver	bcRec;
	private long		begin, stop;
	private Handler		handler;

	private IActivityListener.Stub iListener = new IActivityListener.Stub(){
		@Override
		public void onReadyValue(byte[] load) throws RemoteException {
			buffer = load;
			counter++;
			if(counter%N_PACKETS == 0)
				UpdateTextView();
		}
	};

	private String[] sourcesItems = new String[]{"One Shot", "Listener", "Intent"};

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
		LinearLayout	  l = (LinearLayout)findViewById(R.id.layerButtons);
		bStartAIDLService	= (Button)		l.findViewById(R.id.buttonStartAIDL);
		bStopAIDLService	= (Button)		l.findViewById(R.id.buttonStopAIDL);
		textResultService	= (TextView)	findViewById(R.id.textView1);
		textViewPayload		= (TextView)	findViewById(R.id.textViewPayload);

		bStartAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!state){
					begin = stop = counter = 0;
					showChoices();
				}
			}
		});
		
		bStopAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(state){
					try{
						double servDiff = IBenchService.stopRunning();
						stop = System.nanoTime();
						state = false;
						handler = null;
						int loss = (int) (IBenchService.getNPackets() - counter);
						double gap = stop - begin;
						textResultService.setText("ELAPSED TIME [ns]:\nservice: " + servDiff + 
												"\nactivity: " + gap + 
												"\n\nPACKETS COUNTED:\n activity: " + counter +
												"\nservice: " + IBenchService.getNPackets() +
												"\n\nTotal sended: " + (counter * widget.getIntValue()) + " Byte" +
												"\n\n PACKET'S RATE [pks/ns]:" +
												"\nActivity rate: " + (double)(counter/gap) + 
												"\nService rate: " + (double)(IBenchService.getNPackets()/servDiff) +
												"\nPackets loss [n]: " + loss +
												((loss > 0)? "\nPackets loss rate [n/ns]: "+ (loss/gap) : "" ));
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

	protected void showChoices() {
		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.selectSource)
		.setSingleChoiceItems(sourcesItems, 0, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {}})
		.setPositiveButton("go", yesListener)
		.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {}})
		.show();
	}

	/**
	 * define and schedule partial data 
	 */
	private void show(){
		if(handler != null){
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					double gap = System.nanoTime() - begin;
					long n = counter;
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
	private void print(double gap, long counter) {
		if(state){
			textResultService.setText("ELAPSED TIME [ns]:" + gap + 
								"\n\nPACKETS COUNTED: " + counter +
								"\n\nTotal sended: " + (counter * widget.getIntValue()) + " Byte" +
								"\n\n PACKET'S RATE [pks/ns]:" + (double)(counter/gap));
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

	final DialogInterface.OnClickListener yesListener = new DialogInterface.OnClickListener(){

		@Override
		public void onClick(DialogInterface dialog, int arg1) {
			state	= true;
			counter	= 0;
			handler	= new Handler();
			switch(((AlertDialog)dialog).getListView().getCheckedItemPosition()){
			case 0:	//ONE SHOT
				try {
					IBenchService.setOneShotPacketSize(widget.getIntValue());
					begin = System.nanoTime();
					buffer = IBenchService.getOneShotPacket();
					stop = System.nanoTime();
					print(stop-begin, 1);
					state = false;
				} catch (RemoteException e) {
					Log.e("AIDL", "START SEND ERROR");
				}
				break;
			case 1:	//LISTENER
				Log.e("yes", "case 1");
				try {
					IBenchService.setOneShotPacketSize(widget.getIntValue());
					IBenchService.bindClientListener(iListener);
					begin = System.nanoTime();
					IBenchService.startListenerRunning();
				} catch (RemoteException e) {
					Log.e("AIDL", "START SEND ERROR");
				}
				break;
			case 2: //INTENT
				try {
					begin = System.nanoTime();
					IBenchService.startRunning();
					show();
				} catch (RemoteException e) {
					Log.e("AIDL", "START SEND ERROR");
				}
				break;
			}
		};
	};

	private void UpdateTextView(){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				long gap = System.nanoTime() - begin;
				textResultService.setText("ELAPSED TIME [ns]:" + gap + 
						"\n\nPACKETS COUNTED: " + counter +
						"\n\nTotal sended: " + (counter * widget.getIntValue()) + " Byte" +
						"\n\nPACKET'S RATE [pks/ns]:" + (double)(counter/gap));
			}
		});
	}
}
