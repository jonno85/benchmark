package com.job.benchmarkservice;

import com.job.benchmarkservice.SerFD;
import com.job.benchmarkservice.SerInpStr;

interface IMemoryFile{
	SerFD getFileDescriptor();
	SerInpStr	getInputStream();
}