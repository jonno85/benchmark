package com.example.benchmarkactivity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Pipe;

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
import android.widget.TextView;

import com.example.benchmarkservice.*;

public class MainActivity extends Activity {

	//Service Variables
	private static AIDLConnection	BoundAIDLConnection;
	private IBenchMarkService		IBenchService;
	private boolean					state = false;
	
	private TextView	textResultService;
	private TextView	textResPipeService;
	private Widget		widget;
	private Button		bStartAIDLService;
	private Button		bStopAIDLService;
	private Button		bStartPipeService;
	
	private int 		counter;
	private Intent		intent;
	private BReceiver	bcRec;
	private long		begin, stop;
	private Handler		handler;
	
	private MappedByteBuffer	mem;
	private Pipe.SourceChannel	sPipe;
	private IPipeService		pipeService;
	private PipeConnection		pipeConnection;
	private ByteBuffer 			buffer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mapObjectAndListener();
		/*
				try {
			mem = new RandomAccessFile("/mnt/sdcard/mapped.txt", "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 2000);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	@Override
	protected void onStart() {
		super.onStart();

		BoundAIDLConnection = new AIDLConnection();
		intent = new Intent("com.example.benchmarkservice.IBenchMarkService");
		bindService(intent, BoundAIDLConnection, Context.BIND_AUTO_CREATE);
	
		pipeConnection = new PipeConnection();
		intent = new Intent("com.example.benchmarkservice.IPipeService");
		bindService(intent, pipeConnection, Context.BIND_AUTO_CREATE);
		
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
		unbindService(pipeConnection);
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
			Log.i("BINDER", "CONNECTED");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			IBenchService = null;
			Log.i("BINDER", "DISCONNECTED");
		}
	}

	/**
	 * Class definition to connect on Pipe binder Service
	 * @author F31999A
	 *
	 */
	class PipeConnection implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pipeService = IPipeService.Stub.asInterface(service);
			Log.e("PIPE SERVICE", "CONNECTED");
			buffer = ByteBuffer.allocate(100);
			try {
				sPipe = pipeService.getSourcePipe().source();
				Log.e("PIPE SERVICE", "SOURCE PIPE ASSOCIATED");
			} catch (RemoteException e1) {
				Log.e("PIPE SERVICE", "GetSourcePipe");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			pipeService = null;
			Log.e("PIPE SERVICE", "DISCONNECTED");
		}
	}

	public class BReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			counter++;
		}
	}

	/**
	 * Initialize all the object from the ContentView and associate 
	 * their relative listeners
	 */
	private void mapObjectAndListener(){
		widget				= (Widget)	findViewById(R.id.Widget);
		bStartAIDLService	= (Button)	findViewById(R.id.buttonStartAIDL);
		bStopAIDLService	= (Button)	findViewById(R.id.buttonStopAIDL);
		bStartPipeService	= (Button)	findViewById(R.id.buttonStartPipe);
		textResultService	= (TextView)findViewById(R.id.textView1);
		textResPipeService	= (TextView)findViewById(R.id.textViewPipe);

		bStartPipeService.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				try {
					Log.e("PIPE SERVICE", "start run");
					//pipeService.run();
				} catch (RemoteException e1) {
					Log.e("PIPE SERVICE", "run");
				}
				*/
				try {
					int n = sPipe.read(buffer);
					textResPipeService.setText("Get from pipe: " + buffer.array());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

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
					IBenchService.setBurstSize(widget.sub());
				} catch (RemoteException e) {
					Log.e("AIDL", "SETBURST SIZE -");
				}
			}
		});
		
		widget.addRightClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					IBenchService.setBurstSize(widget.add());
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
		textResultService.setText("ELAPSED TIME [ms]:" + 
								"\nactivity: " + gap + 
								"\n\nPACKETS COUNTED:\n activity: " + counter +
								"\n\nTotal sended: " + (counter * widget.getIntValue()) + " Byte" +
								"\n\n PACKET'S RATE [pks/ms]:" +
								"\nActivity rate: " + (double)(counter/gap));
		show();
	}
}
