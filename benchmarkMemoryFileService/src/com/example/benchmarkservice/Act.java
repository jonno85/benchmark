package com.example.benchmarkservice;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.job.benchmarkservice.IMemoryFile;
import com.job.benchmarkservice.IMemoryFile.Stub;
import com.job.benchmarkservice.MemoryFileService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

public class Act extends Activity {

	private MemoryFile mFile;
	private MFConnection connection;
	private IMemoryFile.Stub iService;
	private Intent intent;
	private byte[] buffer = new byte[1024];
	private TextView t1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
//		try {
//			mFile = new MemoryFile("dump", 1024);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try {
//			Log.e("Test", "File Descriptor " + mFile.getFileDescriptor());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		t1 = (TextView)findViewById(R.id.text1);
		TextView t2 = (TextView)findViewById(R.id.text2);
		TextView t3 = (TextView)findViewById(R.id.text3);
		
		
//		t1.setText(" getParcelFileDescriptor: "+MemoryFileService.getParcelFileDescriptor(mFile).getStatSize());
//		t2.setText(" getFileDescriptor: ");
	}


	@Override
	protected void onStart() {
		super.onStart();
		
		connection = new MFConnection();
		intent = new Intent("com.job.benchmarkservice.MemoryFileService");
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}


	@Override
	protected void onStop() {
		unbindService(connection);
		super.onStop();
	}
	
	private class MFConnection implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			iService = (Stub) IMemoryFile.Stub.asInterface(service);
			try {
				FileInputStream fis = new FileInputStream(iService.getFileDescriptor().getmFD());
//				FileDescriptor fd = MemoryFileService.getFileDescriptor(mFile);
//				Log.e("Test", "File Descriptor " + fd);
//				Log.e("Test", "Input Stream byte readed: " + in.read(buffer));
				fis.read(buffer);
				String s = "";
				for(int i=0; i<buffer.length; i++){
					s += buffer[i] + " ";
				}
				t1.setText("READ: " + s);
				
				
				
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("Test", "Input Stream cannot be read");
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			iService = null;
		}
		
	}
}
