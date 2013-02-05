package com.example.benchmarkservice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class PipeService extends Service {
	
	private ByteBuffer buffer;
	private byte[] burst = new byte[DEFAULT];
	public static final int DEFAULT = 100;
	public static Pipe pipe;
	private Pipe.SinkChannel sinkChannel;
	private PipeSourceChannel pipeSourceChannel;

//	public static final int INTERFACE_TRANSACTION = 1598968902;

	@Override
	public IBinder onBind(Intent intent) {
		setUp();
		return apiEndPoint;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setUp();
		run();
		Pipe.SourceChannel src = pipe.source();
		ByteBuffer buff2 = ByteBuffer.allocate(100);
		int read_n = 0;
		try {
			read_n = src.read(buff2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			pipe = Pipe.open();
			sinkChannel = pipe.sink();
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
			Log.e("PIPE SERVICE REMOTE", "start Run");
			sinkChannel.write(buffer);
		} catch (IOException e) {
			Log.e("PIPE SERVICE REMOTE run", "exceptipon");
		}
	}
	
	IPipeService.Stub apiEndPoint = new IPipeService.Stub(){

		@Override
		public PipeSourceChannel getSourcePipe() throws RemoteException {
			pipeSourceChannel = new PipeSourceChannel(pipe);
			PipeService.this.run();
			return pipeSourceChannel;
		}

		@Override
		public void run() throws RemoteException {
			PipeService.this.run();
		}
	};
}
