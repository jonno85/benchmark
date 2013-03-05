package com.example.benchmarkactivity;

import java.io.Serializable;

public class DataContainer implements Serializable {

	/**
	 * 
	 */
	private  final long serialVersionUID = 1L;
	
	private long	timeGap;	//Time Gap
	private long	packets;	//Number of packets
	private float	avgPackets;	//avg packets with this one included
	private float	rate;		//rate in packets/timeGap
	private float	avgRate;	//avg rate with this one included
	private long	loss;		//packets loss
	private float	avgLoss;	//avg loss with this one included
	private long	cpuLoad;	//cpu load
	private int		prio;
	
	public long getLoss() {
		return loss;
	}

	public void setLoss(int loss) {
		this.loss = loss;
	}

	public float getAvgRate() {
		return avgRate;
	}

	public void setAvgRate(float AvgRate) {
		this.avgRate = AvgRate;
	}

	public DataContainer(){
	}

	public DataContainer(long timeGap, long packets,
						float rate, long loss, long load, int prio){
		this.timeGap	= timeGap;
		this.packets	= packets;
		this.rate		= rate;
		this.loss		= loss;
		this.cpuLoad	= load;
		this.prio		= prio;
	}
	
	public DataContainer(long timeGap, long packets, float avgPackets, float rate,
						float avgRate, long loss, float avgLoss, long load){
		this.timeGap	= timeGap;
		this.packets	= packets;
		this.setAvgPackets(avgPackets);
		this.rate		= rate;
		this.avgRate	= avgRate;
		this.loss		= loss;
		this.setAvgLoss(avgLoss);
		this.setCpuLoad(load);
	}

	public long getTimeGap() {
		return timeGap;
	}

	public void setTimeGap(long timeGap) {
		this.timeGap = timeGap;
	}

	public long getPackets() {
		return packets;
	}

	public void setPackets(long packets) {
		this.packets = packets;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	/**
	 * @return the avgPackets
	 */
	public float getAvgPackets() {
		return avgPackets;
	}

	/**
	 * @param avgPackets the avgPackets to set
	 */
	public void setAvgPackets(float avgPackets) {
		this.avgPackets = avgPackets;
	}

	/**
	 * @return the avgLoss
	 */
	public float getAvgLoss() {
		return avgLoss;
	}

	/**
	 * @param avgLoss the avgLoss to set
	 */
	public void setAvgLoss(float avgLoss) {
		this.avgLoss = avgLoss;
	}

	/**
	 * @return the cpuLoad
	 */
	public long getCpuLoad() {
		return cpuLoad;
	}

	/**
	 * @param cpuLoad the cpuLoad to set
	 */
	public void setCpuLoad(long cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	/**
	 * @return the prio
	 */
	public int getPrio() {
		return prio;
	}

	/**
	 * @param prio the prio to set
	 */
	public void setPrio(int prio) {
		this.prio = prio;
	}
}