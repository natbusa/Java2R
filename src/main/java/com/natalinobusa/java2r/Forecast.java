package com.natalinobusa.java2r;

import java.util.Random;

import com.mhsatman.rcaller.RCaller;
import com.mhsatman.rcaller.RCode;

public class Forecast {

  public static void main(String[] args) {
    new Forecast();
  }

  public Forecast() {
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

      /*
       * It is good to have a look at the XML file
       * for having info about which variables are passed to result
       */
      System.out.println(caller.getParser().getXMLFileAsString());

      double[] upValues = caller.getParser().getAsDoubleArray("upper");
      double[] loValues = caller.getParser().getAsDoubleArray("lower");
      double[] fitted = caller.getParser().getAsDoubleArray("fitted");

      System.out.println("success");
    } catch (Exception e) {
      System.out.println(e.toString());
    }

  }
}
