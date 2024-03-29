package com.horsehour.metric;

import java.util.List;

/**
 * Kendall Tau Distance是衡量两个列表差异性的测度,tau越小,一致性越强
 * tau属于[0,1]，也可以使用1-tau作为度量相似性的标准 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 2012/12/07
 */
public class KendallTau extends Metric{
	
	@Override
	public double measure(List<Integer> desire, List<Double> predict) {
		return 1 - tauDistance(desire, predict);
	}

	//计算两列表的tau距离
	public double tauDistance(List<Integer> desire, List<Double> predict){
		double distance = 0;
		int len = desire.size(), discordant = 0;
		for(int i = 0; i < len - 1; i++){
			for(int j = i + 1; j < len; j++){
				discordant += 
						(desire.get(i) - desire.get(j)) * (predict.get(i) - predict.get(j)) > 0
						? 0 : 1;
			}
		}
		
		distance = (double) 2 * discordant / (len * (len - 1)); 
		return distance;
	}
	
	public double tauDistance(float[] list1, float[] list2){
		float distance = 0;
		int len = list1.length, discordant = 0;
		for(int i = 0; i < len - 1; i++)
			for(int j = i + 1; j < len; j++){
				discordant += 
						(list1[i] - list1[j]) * (list2[i] - list2[j]) > 0 ? 0 : 1;
			}
		
		distance = (float)2 * discordant / ((len - 1) * len);
		return distance;
	}
	
	@Override
	public String name() {
		return "KendallTauMetric";
	}
}
