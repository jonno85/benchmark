package com.example.benchmarkservice;

import com.example.benchmarkservice.IActivityListener;

interface IBenchMarkService{
	void startRunning();
	long stopRunning();
	void setBurstSize(in int size);
	long getNPackets();
	
	boolean setOneShotPacketSize(in int size);
	byte[] getOneShotPacket();

	void startListenerRunning();
	void bindClientListener(IActivityListener listener);
}