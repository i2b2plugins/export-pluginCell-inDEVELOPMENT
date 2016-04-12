/*
 * Copyright (c) 2015 Biomeris s.r.l. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Matteo Gabetta
 */
package com.biomeris.i2b2.export.engine.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import com.biomeris.i2b2.export.engine.ExportConstants;
import com.biomeris.i2b2.export.engine.io.misc.Concept;
import com.biomeris.i2b2.export.engine.session.WObservation;

public class ObservationAggregator {
	private Mean mean;
	private Median median;
	private StandardDeviation standardDeviation;
	
	private Concept concept;
	private String patientId;

	private int count = 0;

	private int numbers = 0;
	private int strings = 0;
	
	private List<Double> numericValues;
	private List<String> stringValues;

	public ObservationAggregator() {
		super();
		mean = new Mean();
		median = new Median();
		standardDeviation = new StandardDeviation();
		
		numericValues = new ArrayList<>();
		stringValues  = new ArrayList<>();
	}

	public void clearAndSetUp(Concept concept, String patientId) {
		count = 0;

		numbers = 0;
		strings = 0;

		numericValues.clear();
		stringValues.clear();

		this.concept = concept;
		this.patientId = patientId;
	}

	public boolean hasData() {
		return (count > 0);
	}

	public void addObservation(WObservation observation, String name) {
		if (concept.getType().equals(ExportConstants.LEAF)) {
			if (observation.getValueTypeCd().equals(ExportConstants.VALTYPE_N)) {
				//only if operator is "equals" (=)
				if(observation.getTvalChar().equals("E")){
					count++;
					numbers++;
					numericValues.add(observation.getNvalNum());
				}
			} else {
				count++;
				strings++;
				stringValues.add(observation.getTvalChar());
			}
		} else{
			count++;
			stringValues.add(name);
		}
	}

	public ConceptAggregate makeAggregate() {
		ConceptAggregate output = new ConceptAggregate();
		
		output.setConcept(concept);
		output.setPatientId(patientId);
		output.setCount(count);
		
		if (concept.getType().equals(ExportConstants.LEAF)) {
			if (getValType().equals(ExportConstants.VALTYPE_N)) {
				output.setValType(ExportConstants.VALTYPE_N);
				
				double[] values = createNumericArray(numericValues);
				
				output.setMean(mean.evaluate(values));
				output.setMedian(median.evaluate(values));
				output.setSd(standardDeviation.evaluate(values));
			} else if(getValType().equals(ExportConstants.VALTYPE_T)){
				output.setValType(ExportConstants.VALTYPE_T);
				
				output.setMode(mode(stringValues));
			}
		} else{
			output.setValType(ExportConstants.FOLDER);
			output.setMode(mode(stringValues));
		}
		
		return output;
	}
	
	public String getPatientId() {
		return patientId;
	}

	private double[] createNumericArray(List<Double> list) {
		double[] output = new double[list.size()];
		for(int i=0; i<list.size(); i++){
			output[i] = list.get(i);
		}
		return output;
	}

	private String getValType(){
		if(numbers==strings){
			System.out.println("");
		}
		
		if(numbers>=strings){
			return ExportConstants.VALTYPE_N;
		} else if(strings > numbers){
			return ExportConstants.VALTYPE_T;
		} else{
			return null;
		}
	}
	
	private String mode(List<String> list){
		Map<String, Integer> counts = new HashMap<>();
		
		int maxCount = 0;
		String mostFreq  = null;
		
		for(String s : list){
			if(counts.containsKey(s)){
				Integer newCount = counts.get(s)+1;
				counts.remove(s);
				counts.put(s, newCount);
				
				if(newCount > maxCount){
					maxCount = newCount;
					mostFreq = s;
				}
			} else{
				counts.put(s, 1);
				
				if(1 > maxCount){
					maxCount = 1;
					mostFreq = s;
				}
			}
		}
		
		return mostFreq;
	}
}
