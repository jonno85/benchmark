package com.example.benchmarkservice;

interface IBenchMarkService{
	void startRunning();
	long stopRunning();
	void setBurstSize(int size);
	int getNPackets();
}