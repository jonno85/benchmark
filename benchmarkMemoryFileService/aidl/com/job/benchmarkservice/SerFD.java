package com.job.benchmarkservice;

import java.io.FileDescriptor;

import android.os.Parcel;
import android.os.Parcelable;

public class SerFD implements Parcelable{
	
	private FileDescriptor mFD;
	
	public SerFD(FileDescriptor in){
		this.mFD = in;
	}
	
	public static final Parcelable.Creator<SerFD> CREATOR = new Parcelable.Creator<SerFD>(){

		@Override
		public SerFD createFromParcel(Parcel source) {
			return new SerFD();
		}

		@Override
		public SerFD[] newArray(int size) {
			return new SerFD[size];
		}
		
	};

	public SerFD() {
		super();
		this.setmFD(new FileDescriptor());
	}

	/**
	 * @return the mFD
	 */
	public FileDescriptor getmFD() {
		return mFD;
	}

	/**
	 * @param mFD the mFD to set
	 */
	public void setmFD(FileDescriptor mFD) {
		this.mFD = mFD;
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
