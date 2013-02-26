package com.example.benchmarkservice;

import com.example.benchmarkservice.IActivityListener;

interface IBenchMarkService{
	void startRunning();
	long stopRunning();
	void setBurstSize(int size);
	int getNPackets();
	
	boolean setOneShotPacketSize(int size);
	byte[] getOneShotPacket();

	void startListenerRunning();
	void bindClientListener(IActivityListener listener);
}