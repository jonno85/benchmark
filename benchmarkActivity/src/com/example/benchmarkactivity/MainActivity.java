package com.example.benchmarkactivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

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
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.benchmarkservice.IActivityListener;
import com.example.benchmarkservice.IBenchMarkService;

public class MainActivity extends Activity {

	private int		loss;
	private long	gap;
	private float	rate;
	private long	servGap;
	
	//Service Variables
	private static AIDLConnection	BoundAIDLConnection;
	private IBenchMarkService		IBenchService;
	private boolean					state = false;
	
	private byte[] buffer;
	private String opts, s, unit;
	private static int N_PACKETS = 5000;
	
	private TextView		textResultService;
	private TextView		textResultStats;
	private TextView		textViewPayload;
	private Widget			widget;
	private Button			bStartAIDLService;
	private Button			bStopAIDLService;
	private ToggleButton	bPriority;
	
	private long 		counter;
	private Intent		intent;
	private BReceiver	bcRec;
	private long		begin, stop;
	private Handler		handler;
	
	private ObjectOutputStream	oos;
	private ObjectInputStream	ois;
	private static final String FILE = "./statistics";
	private HashMap<Integer, HashMap<String, LinkedList<DataContainer>>> stats;
	private int mode; //contain which mode used to evaluate benchmark
	
	private IActivityListener.Stub iListener = new IActivityListener.Stub(){
		@Override
		public void onReadyValue(byte[] load) throws RemoteException {
			buffer = load.clone();
			counter++;
			if(counter % N_PACKETS == 0)
				UpdateTextView();
		}
	};

	private String[] sourcesItems = new String[]{"One Shot", "Listener", "Intent"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setAndRestore();
		mapObjectAndListener();

//		ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
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
		try {
			oos.writeObject(stats);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		LinearLayout l = (LinearLayout)findViewById(R.id.layerwidget);
		widget				= (Widget)		l.findViewById(R.id.Widget);
		bPriority			= (ToggleButton)l.findViewById(R.id.toggleButton1);
		l					= (LinearLayout)findViewById(R.id.layerButtons);
		bStartAIDLService	= (Button)		l.findViewById(R.id.buttonStartAIDL);
		bStopAIDLService	= (Button)		l.findViewById(R.id.buttonStopAIDL);
		l					= (LinearLayout)findViewById(R.id.layerTexts);
		textResultService	= (TextView)	l.findViewById(R.id.textView1);
		textResultStats		= (TextView)	l.findViewById(R.id.textView2);
		l					= (LinearLayout)findViewById(R.id.layerPayload);
		textViewPayload		= (TextView)	l.findViewById(R.id.textViewPayload);

		bStartAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!state){
					rate = gap = stop = counter = servGap = loss = 0;
					showChoices();
				}
			}
		});
		
		bStopAIDLService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(state){
					try{
						servGap = IBenchService.stopRunning();
						stop	= System.currentTimeMillis();
						state	= false;
						handler	= null;
						loss	= (int) (IBenchService.getNPackets() - counter);
						gap		= stop - begin;
						rate	= ((float)counter)/((float)gap);
						textResultService.setText("ELAPSED TIME ["+unit+"]:" +
												"\nactivity: " + gap + 
												"\nservice: " + servGap + 
												"\n\nPACKETS COUNTED:" +
												"\nactivity: " + counter +
												"\nservice: " + IBenchService.getNPackets() +
												"\n\nTotal sended: " + ((counter * widget.getIntValue())/1024) + " KByte" +
												"\n\n PACKET'S RATE [pks/"+unit+"]:" +
												"\nActivity rate: " + rate + 
												"\nService rate: " + (float)(IBenchService.getNPackets()/servGap) +
												"\nPackets loss [n]: " + loss +
												((loss > 0)? "\nPackets loss rate [n/"+unit+"]: "+ (float)(loss/gap) : "" ));
						saveStatElement();
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

		bPriority.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
				} else {
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
				}
			}
		});
	}

	
	public void setAndRestore(){
		try {
			ois = new ObjectInputStream(new FileInputStream(FILE));
			stats = (HashMap<Integer, HashMap<String, LinkedList<DataContainer>>>) ois.readObject();
		} catch (ClassNotFoundException ex) {
			initStats();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(stats == null)
			initStats();
	}

	private void initStats(){
		stats = new HashMap<Integer, HashMap<String,LinkedList<DataContainer>>>();
		for(int i=0; i<Widget.values.length; i++){
			HashMap<String, LinkedList<DataContainer>> subMap = new HashMap<String, LinkedList<DataContainer>>(3);
			for(int j=0; j<sourcesItems.length; j++){
				subMap.put(sourcesItems[j], new LinkedList<DataContainer>());
			}
			stats.put(Widget.values[i], subMap);
		}
	}

	private void saveStatElement(){
		HashMap<String, LinkedList<DataContainer>> map = stats.get(widget.getIntValue());
		LinkedList<DataContainer> list = map.get(sourcesItems[mode]);
		float avg = 0;
		Iterator<DataContainer> i = list.iterator();
		int index=0;
		while(i.hasNext()){
			avg += i.next().getRate();
			index++;
		}
//		avg /= index;
		
		MemoryInfo info = new MemoryInfo();
		Debug.getMemoryInfo(info);
		
		list.add(new DataContainer(gap,counter,rate,avg,loss,getCpuUsage(),Debug.getPss()));
		textResultStats.setText("Avg: " + avg +
								"\ncpu usage: " + getCpuUsage() + " clock ticks " +
								"\nmemory usage: " + (info.nativePrivateDirty) + 
								"\npriority: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
	}

	private long getCpuUsage(){

		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/"+android.os.Process.myPid()+"/stat", "r");
			
			String load = reader.readLine();

			String[] toks = load.split(" ");

			long userScheduled		= Long.parseLong(toks[13]);
			long kernelScheduled	= Long.parseLong(toks[14]);
			return userScheduled+kernelScheduled;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * show up dialog to choose which method to trigger
	 */
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
					long gap = System.currentTimeMillis() - begin;
					long n = counter;
					print(gap, n, 3);
				}
			}, 1000);
		}
	}

	/**
	 * print partial data and reschedule show process
	 * modes:
	 * 0 - singleShot
	 * 1 - listener
	 * 2 - intent
	 * 3 - listener & intent (same for both)
	 * @param gap
	 * @param counter
	 */
	private void print(long gap, long counter, int mode) {
		if(state){
			opts	= (mode != 0)? ("\n\nTotal sended: "
					+ ((counter * widget.getIntValue())/1024)
					+ " KByte\n\nPACKET'S RATE [pks/"+unit+"]:"
					+ (float)(counter/gap))
					: "";

			textResultService.setText("ELAPSED TIME ["+unit+"]:" + gap
								+ "\n\nPACKETS COUNTED: " + counter
								+ opts);
	
			if(buffer.length > 0){
				s = "";
				for(int i=0; i<buffer.length; i++){
					s += (buffer[i] + " | ");
				}
				textViewPayload.setText("\nsize: " + buffer.length + "\nPayload: " + s);
			}
			if(mode != 0)
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
			mode = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
			unit = (mode != 0)? "ms" : "ns";
			switch(mode){
			case 0:	//ONE SHOT
				try {
					IBenchService.setOneShotPacketSize(widget.getIntValue());
					begin	= System.nanoTime();
					buffer	= IBenchService.getOneShotPacket();
					stop	= System.nanoTime();
					print(stop-begin, 1, 0);
					state = false;
					saveStatElement();
				} catch (RemoteException e) {
					Log.e("AIDL", "START SEND ERROR");
				}
				break;
			case 1:	//LISTENER
				try {
					IBenchService.setOneShotPacketSize(widget.getIntValue());
					IBenchService.bindClientListener(iListener);
					begin = System.currentTimeMillis();
					IBenchService.startListenerRunning();
				} catch (RemoteException e) {
					Log.e("AIDL", "START SEND ERROR");
				}
				break;
			case 2: //INTENT
				try {
					begin = System.currentTimeMillis();
					IBenchService.startRunning();
					show();
				} catch (RemoteException e) {
					Log.e("AIDL", "START SEND ERROR");
				}
				break;
			}
		};
	};

	/**
	 * set up thread to show partial result, associated to IActivityListener
	 */
	private void UpdateTextView(){
		handler.post(new Runnable() {
			@Override
			public void run() {
				gap = System.currentTimeMillis() - begin;
				rate = ((float)counter)/((float)gap);
				s = "";
				for(int i=0; i<buffer.length; i++){
					s += (buffer[i] + " | ");
				}
				textResultService.setText("ELAPSED TIME ["+unit+"]:" + gap + 
										"\n\nPACKETS COUNTED: " + counter +
										"\n\nTotal sended: " + ((counter * widget.getIntValue())/1024) + " KByte" +
										"\n\nPACKET'S RATE [pks/"+unit+"]:" + rate);
				textViewPayload.setText("\nsize: " + buffer.length + "\nPayload: " + s);
			}
		});
	}
}
