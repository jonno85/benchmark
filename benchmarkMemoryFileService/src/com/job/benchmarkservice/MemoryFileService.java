package com.job.benchmarkservice;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.RemoteException;
import android.util.Log;


public class MemoryFileService extends Service {

	public static final String TAG = "com.job.benchmarkservice.MemoryFileService";
	
	private Timer		timer = new Timer("timer");
	private TimerTask	task;
	public MemoryFile mFile;
	private byte[] buffer = new byte[512];
	
//	private static Method sMethodGetParcelFileDescriptor;
	private static Method sMethodGetFileDescriptor;
	static{
//		sMethodGetParcelFileDescriptor	= get("getParcelFileDescriptor");
		sMethodGetFileDescriptor 		= get("getFileDescriptor");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		
		for(int i=0; i<buffer.length; i++){
			buffer[i] = (byte)i;
		}
		
		try {
			mFile = new MemoryFile("dump", 1024);
			Log.e("Test", "File Input Stream " + mFile.getInputStream());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return apiEndPoint;
	}

	IMemoryFile.Stub apiEndPoint = new IMemoryFile.Stub(){

		@Override
		public SerFD getFileDescriptor() throws RemoteException {
			SerFD fd = null;
			try {
				fd = new SerFD(mFile.getFileDescriptor());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return fd;
		}

		@Override
		public SerInpStr getInputStream() throws RemoteException {
			SerInpStr inputStream = new SerInpStr(mFile.getInputStream());
			triggerTimer();
			return inputStream;
		}
		
	};
	
//	public static ParcelFileDescriptor getParcelFileDescriptor(MemoryFile file) {
//		try{
//			return (ParcelFileDescriptor) sMethodGetParcelFileDescriptor.invoke(file);
//		}catch(IllegalAccessException e) {
//			throw new RuntimeException(e);
//		} catch (InvocationTargetException e) {
//			throw new RuntimeException(e);
//		}
//	}

	public static FileDescriptor getFileDescriptor(MemoryFile file) {
		try {
			return (FileDescriptor) sMethodGetFileDescriptor.invoke(file);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	protected void triggerTimer() {
		task = new TimerTask() {
			@Override
			public void run() {
				try {
					mFile.writeBytes(buffer, 0, 30, buffer.length);
					Log.e("Test", "Memory File write ");
				} catch (IOException e) {
					Log.e("Test", "Memory File Exceptipn ");
					e.printStackTrace();
				}
			}
		};
		timer.schedule(task, 20);
	}

	private static Method get(String name) {
		try {
			return MemoryFile.class.getDeclaredMethod(name);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
