package com.natalinobusa.Java2R;

import java.util.Random;
import java.util.Arrays;

import com.mhsatman.rcaller.RCaller;
import com.mhsatman.rcaller.RCode;

import org.rosuda.rengine.*;
import org.rosuda.rserve.*;

public class Forecast {

  RConnection connection = null;
  REngine engine = null;
	  
  public void main(String[] args) {
	try {
		connection = new RConnection();
		engine = (REngine) connection;
	} catch (Exception e) {
	      System.out.println(e.toString());
    }
    
    Forecast forecaster = new Forecast();
    
    forecaster.RCaller();
    forecaster.RServe();
    
    if (connection!= null){
        connection.close();
        engine.close();
    }
  }
  
  public void RServe() {
    try {
    	final double x[] = connection.eval("rnorm(100)").asDoubles();
        System.out.println(Arrays.toString(x));
    } catch (Exception e) {
	      System.out.println(e.toString());
    }
  }
  
  public void RCaller() {
    Random random = new Random(12345);
    double[] stockClosePrices = new double[100];
    stockClosePrices[0] = 0;
    for (int i = 1; i < stockClosePrices.length; i++) {
      stockClosePrices[i] = 0.5 + 1 * stockClosePrices[i - 1] + random.nextGaussian();
    }
    RunRScript(stockClosePrices);
  }

  public void RunRScript(double[] stockClosePrices) {
    try {

      // define code to be run
      RCode code = new RCode();

      code.addDoubleArray("x", stockClosePrices);
      code.R_require("forecast");
      code.addRCode("ww<-auto.arima(x)");
      code.addRCode("tt<-forecast(ww,h=20)");

      code.addRCode("myResult <- list(upper=tt$upper, lower=tt$lower, fitted = as.double(tt$fitted))");

      // set and run the caller
      RCaller caller = new RCaller();

      caller.setRCode(code);
      caller.setRscriptExecutable("/usr/bin/Rscript");
      caller.runAndReturnResult("myResult");

      double[] fitted = caller.getParser().getAsDoubleArray("fitted");

      System.out.println(Arrays.toString(fitted));
    } catch (Exception e) {
      System.out.println(e.toString());
    }

  }
}
