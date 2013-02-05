package com.example.benchmarkservice;

import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class BenchMarkService extends Service{

	private static final String TAG = "LOAD";
	private long start, stop;
	private int counter = 0;

	@Override
	public IBinder onBind(Intent intent) {
		Log.e("SERVICE", "ON BIND");
		return apiEndPoint;
	}

	@Override
	public void onDestroy() {
		apiEndPoint = null;
		super.onDestroy();
	}

	IBenchMarkService.Stub apiEndPoint = new IBenchMarkService.Stub() {
		
		private boolean running	= false;
		private byte[] burst	= new byte[500];
		private Intent load		= new Intent("com.example.benchmarkactivity.MainActivity");
		private Thread sender;
		
		private void stopSender() {
			sender = null;
		}

		private void startSender() {
			if(sender == null){
				counter = 0;
				sender = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(running){
							sendBroadcast(load);
							counter++;
						}
					}
				});
			}
			sender.start();
		}
		
		@Override
		public synchronized void setBurstSize(int size) throws RemoteException {
			burst = new byte[size];
			Log.e("AIDL", "SETBURST SIZE " + size);
			Random r = new Random(size);
			for(int i=0; i<size; i++){
				burst[i] = (byte)r.nextInt();
			}
		}

		@Override
		public void startRunning() throws RemoteException {
			this.running = true;
			load.putExtra(TAG, burst);
			start = System.currentTimeMillis();
			startSender();
		}

		@Override
		public long stopRunning() throws RemoteException {
			this.running = false;
			stopSender();
			stop = System.currentTimeMillis();
			return stop-start;
		}

		@Override
		public int getNPackets() throws RemoteException {
			return counter;
		}
	};
}
