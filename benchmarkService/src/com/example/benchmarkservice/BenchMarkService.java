package com.example.benchmarkservice;

import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class BenchMarkService extends Service{

	public static final String TAG = "LOAD";
	private long start, stop;
	private long counter = 0;

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
		private byte[] burst	= new byte[100];
		private Intent load;
		private Thread sender;
		private IActivityListener listener;
		
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
			burst = null;
			burst = new byte[size];
//			Log.e("AIDL", "SETBURST SIZE " + size);
			load = getNewIntent();
		}

		@Override
		public void startRunning() throws RemoteException {
			this.running = true;
			load = getNewIntent();
			start = System.currentTimeMillis();
			startSender();
		}

		@Override
		public long stopRunning() throws RemoteException {
			this.running = false;
			stopSender();
			stop = System.currentTimeMillis();
			return (stop-start);
		}

		@Override
		public long getNPackets() throws RemoteException {
			return counter;
		}

		private Intent getNewIntent(){
			Intent i = new Intent("com.example.benchmarkactivity.MainActivity");
			i.putExtra(TAG, burst);
			putPayLoad(burst.length);
			return i;
		}
		
		private void putPayLoad(int size){
			Random r = new Random(size);
			for(int i=0; i<size; i++){
				burst[i] = (byte)r.nextInt();
			}
		}

		@Override
		public byte[] getOneShotPacket() throws RemoteException {
			return burst;
		}

		@Override
		public void bindClientListener(IActivityListener listener)
				throws RemoteException {
			this.listener = listener;
		}

		@Override
		public boolean setOneShotPacketSize(int size) throws RemoteException {
			if(size < 1)
				return false;
			burst = new byte[size];
			putPayLoad(size);
			return true;
		}

		@Override
		public void startListenerRunning() throws RemoteException {
			sender = null;
			counter = 0;
			this.running = true;
			Log.e("AIDL", "startListenerRunning");
			sender = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(running){
						try {
							listener.onReadyValue(burst);
							counter++;
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			});
			Log.e("AIDL", "start");
			sender.start();
		}
	};
}
