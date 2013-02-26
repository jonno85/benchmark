package com.job.benchmarkservice;

import java.io.IOException;
import java.io.InputStream;

import android.os.Parcel;
import android.os.Parcelable;

public class SerInpStr extends InputStream implements Parcelable{

	private InputStream input;
	
	public static final Parcelable.Creator<SerInpStr> CREATOR = new Parcelable.Creator<SerInpStr>(){

		@Override
		public SerInpStr createFromParcel(Parcel source) {
			return new SerInpStr();
		}

		@Override
		public SerInpStr[] newArray(int size) {
			return new SerInpStr[size];
		}
		
	};
	
	public SerInpStr(){}
	
	public SerInpStr(InputStream in){
		super();
		input = in;
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}

}
