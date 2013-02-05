package com.example.benchmarkservice;

import com.example.benchmarkservice.IAdvisor;

interface IBenchMarkService{
	void startRunning();
	long stopRunning();
	void setBurstSize(int size);
	int getNPackets();
}