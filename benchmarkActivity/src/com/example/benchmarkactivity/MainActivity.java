package com.example.benchmarkactivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
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
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.benchmarkservice.IActivityListener;
import com.example.benchmarkservice.IBenchMarkService;

public class MainActivity extends Activity {

	/**
	 * Statistics
	 */
	private float	rate;
	private long 	counter, begin, stop, gap, loss;
	
	/**
	 * CPU Statistics
	 */
	private long work, workAM, total;
	private long workBefore, totalBefore, workAMBefore;
	private RandomAccessFile reader;
	
	/**
	 * Graphic widget
	 */
	static int WIDTH_INTERVAL = 1;
	static int READ_INTERVAL, UPDATE_INTERVAL;
	static boolean HAPPYICONS, MEMFREE_R, BUFFERS_R, CACHED_R, ACTIVE_R, INACTIVE_R, SWAPTOTAL_R, DIRTY_R, CPUP_R, CPUTOTALP_R, CPUAMP_R, CPURESTP_R, 
			DRAW, MEMFREE_D, BUFFERS_D, CACHED_D, ACTIVE_D, INACTIVE_D, SWAPTOTAL_D, DIRTY_D, CPUTOTALP_D, CPUAMP_D, CPURESTP_D, RECORD;
	static int TOTAL_INTERVALS = 100; // Default value to initialice the vector. Afterwards will be modified automatically.
	private AnGraphic graphWidget;
	
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
	private RadioButton		rbPriority0;
	private RadioButton		rbPriority1;
	private RadioButton		rbPriority2;
	private RadioButton		rbPriority3;

	private Intent		intent;
	private BReceiver	bcRec;
	private Handler		handler;
	
	/**
	 * Serialize variables
	 */
	private ObjectOutputStream	oos;
	private ObjectInputStream	ois;
	private String statFile;
	private HashMap<Integer, HashMap<String, LinkedList<DataContainer>>> stats;
	
	private int mode = 0; //contain which mode used to evaluate benchmark
	
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
		statFile = getFilesDir().getPath().toString() + "/statistics";
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
			oos = new ObjectOutputStream(new FileOutputStream(statFile));
			oos.writeObject(stats);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			super.onDestroy();
		}
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
		rbPriority0			= (RadioButton)	l.findViewById(R.id.priority0);
		rbPriority1			= (RadioButton)	l.findViewById(R.id.priority1);
		rbPriority2			= (RadioButton)	l.findViewById(R.id.priority2);
		rbPriority3			= (RadioButton)	l.findViewById(R.id.priority3);
		l					= (LinearLayout)findViewById(R.id.layerButtons);
		bStartAIDLService	= (Button)		l.findViewById(R.id.buttonStartAIDL);
		bStopAIDLService	= (Button)		l.findViewById(R.id.buttonStopAIDL);
		l					= (LinearLayout)findViewById(R.id.layerTexts);
		textResultService	= (TextView)	l.findViewById(R.id.textView1);
		textResultStats		= (TextView)	l.findViewById(R.id.textView2);
		graphWidget			= (AnGraphic)	l.findViewById(R.id.graphWidget);
		l					= (LinearLayout)findViewById(R.id.layerPayload);
		textViewPayload		= (TextView)	l.findViewById(R.id.textViewPayload);

		rbPriority0.setChecked(true);
		
		bStartAIDLService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!state){
					rate = gap = stop = counter = 0;
					loss = 0;
					showChoices();
				}
			}
		});
		
		bStopAIDLService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(state){
					try{
//						servGap = 
						IBenchService.stopRunning();
						stop	= System.currentTimeMillis();
						state	= false;
						handler	= null;
						loss	= (IBenchService.getNPackets() - counter);
						loss	= (loss>0)? loss : 0;
						gap		= stop - begin;
						rate	= ((float)counter)/((float)gap);
						textResultService.setText("ELAPSED TIME ["+unit+"]:" +
												"\nactivity: " + gap + 
//												"\nservice: " + servGap + 
												"\n\nPACKETS COUNTED:" +
												"\nactivity: " + counter +
												"\nservice: " + IBenchService.getNPackets() +
												"\n\nTotal sended: " + ((counter * widget.getIntValue())/1024) + " KByte" +
												"\n\n PACKET'S RATE [pks/"+unit+"]:" +
												"\nActivity rate: " + rate + 
//												"\nService rate: " + (float)(IBenchService.getNPackets()/servGap) +
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

		setRadioButtonListener();
	}

	private void setRadioButtonListener() {
		rbPriority0.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
			}
		});
		
		rbPriority1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
			}
		});
		
		rbPriority2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
			}
		});
		
		rbPriority3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void setAndRestore(){
		try {
			ois = new ObjectInputStream(new FileInputStream(statFile));
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
		float avgRate		= 0;
		float avgPackets	= 0;
		float avgLoss		= 0;
		
		HashMap<String, LinkedList<DataContainer>> map = stats.get(widget.getIntValue());
		LinkedList<DataContainer> list = map.get(sourcesItems[mode]);

		Iterator<DataContainer> i = list.iterator();
		float index=0;
		while(i.hasNext()){
			DataContainer c = i.next();
			avgRate		+= c.getRate();
			avgPackets	+= c.getPackets();
			avgLoss		+= c.getLoss();
			index++;
		}
		avgRate		/= index;
		avgPackets	/= index;
		avgLoss		/= index;
		
		
//		MemoryInfo info = new MemoryInfo();
//		Debug.getMemoryInfo(info);
		Log.e("SAVED", "gap "+gap + " counter " + counter +" rate " + rate + " avg " + avgRate +" loss " + loss + " getCpuUsage() " +getCpuUsage());
		list.add(new DataContainer(gap, counter, rate, avgRate, loss, getCpuUsage()));
		textResultStats.setText("AVG (Set "+index+"):" +
								"\nRate: " 		+ avgRate		+
								"\nPackets: "	+ avgPackets	+
								"\nLost: "		+ avgLoss		+ 
								"\ncpu usage: " + getCpuUsage() + " clock ticks " +
//								"\nmemory usage: " + (info.nativePrivateDirty) + 
								"\npriority: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
	}

	private long getCpuUsage(){
		String[] tok;
		long workT	 = 0;
		long totalT	 = 0;
		long workAMT = 0;
		try {
			reader = new RandomAccessFile("/proc/stat","r");
			tok = reader.readLine().split("[ ]+", 9);
			
			work	= Long.parseLong(tok[1]) +
					  Long.parseLong(tok[2]) +
					  Long.parseLong(tok[3]);
			total	= work +
					  Long.parseLong(tok[4]) +
					  Long.parseLong(tok[5]) +
					  Long.parseLong(tok[6]) +
					  Long.parseLong(tok[7]);
			
			reader = new RandomAccessFile("/proc/"+android.os.Process.myPid()+"/stat","r");
			tok = reader.readLine().split("[ ]+", 18);
			
			workAM	= Long.parseLong(tok[13]) +
					  Long.parseLong(tok[14]) +
					  Long.parseLong(tok[15]) +
					  Long.parseLong(tok[16]);
			if(totalBefore != 0){
				workT	= work - workBefore;
				totalT	= total - totalBefore;
				workAMT	= workAM - workAMBefore;
			}
			workBefore = work;
			totalBefore = total;
			workAMBefore = workAM;
			Log.e("SAVED", "workT "+workT + " totalT " + totalT +" workAMT " + workAMT + " workAM " + workAM);
			
			return workAMT;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		.setSingleChoiceItems(sourcesItems, mode, new DialogInterface.OnClickListener() {
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
