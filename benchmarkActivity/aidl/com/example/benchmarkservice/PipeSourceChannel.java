package com.example.benchmarkservice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class PipeSourceChannel extends Pipe implements Parcelable {

	private Pipe pipe;

	public PipeSourceChannel() {
		super();
		try {
			pipe = Pipe.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PipeSourceChannel(Pipe pipe){
		this.pipe = pipe;
		try {
			this.pipe.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final Parcelable.Creator<PipeSourceChannel> CREATOR = 
			new Creator<PipeSourceChannel>() {
				
				@Override
				public PipeSourceChannel[] newArray(int size) {
					return new PipeSourceChannel[size];
				}
				
				@Override
				public PipeSourceChannel createFromParcel(Parcel source) {
					return new PipeSourceChannel();
				}
			};


	@Override
	public SinkChannel sink() {
		return pipe.sink();
	}

	@Override
	public SourceChannel source() {
		return pipe.source();
	}

	public void writeToParcel(Parcel reply, int parcelableWriteReturnValue) {
		reply.writeParcelable((Parcelable) pipe, 0);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
}
