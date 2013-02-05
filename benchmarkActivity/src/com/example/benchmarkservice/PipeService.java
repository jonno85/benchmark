package com.example.benchmarkservice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PipeService extends Service {

	private final IBinder mBinder = new LocalBinder();
	private ByteBuffer buffer;
	private byte[] burst = new byte[DEFAULT];
	public static final int DEFAULT = 500;
	public static Pipe pipe;
	public Pipe.SinkChannel sinkChannel;
	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
	public class LocalBinder extends Binder{
		public PipeService getService(){
			// Return this instance of BinderService so clients can call 
			// public and protected methods
			return PipeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		setUp();
		return mBinder;
	}

	public synchronized void setBurstSize(int size){
		burst = new byte[size];
		Log.e("AIDL", "SETBURST SIZE " + size);
		loadRandomData(size);
	}

	private void setUp() {
		try {
			buffer = ByteBuffer.allocate(DEFAULT);
			loadRandomData(DEFAULT);
			pipe		= Pipe.open();
			sinkChannel	= pipe.sink();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadRandomData(int size){
		Random r = new Random(size);
		for(int i=0; i<size; i++){
			burst[i] = (byte)r.nextInt();
		}
		for(int i=0; i<size; i++){
			buffer.put(i, (byte)i);
		}
		
	}
	
	public void run(){
		try {
			sinkChannel.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
