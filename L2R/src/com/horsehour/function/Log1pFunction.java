package com.horsehour.function;

public class Log1pFunction implements ActivationFunction{
	public float base = 2;
	
	public Log1pFunction(float base){
		this.base = base;
	}
	
	@Override
	public double calc(double netInput) {
		return (Math.log(netInput + 1)/Math.log(base));
	}

	@Override
	public double calcDerivation(double input) {
		double ret = 0;
		if(input == 0){}
		else
			ret = (double)(1/Math.log(base) * (1 + input));
		return ret;
	}
}

