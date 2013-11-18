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

	public static void main(String[] args) {
		new Forecast();
	}

	public Forecast() {
		OpenRServeConnection();

		Run();

		CloseRServeConnection();
	}

	public void OpenRServeConnection() {
		try {
			connection = new RConnection();
			engine = (REngine) connection;

			connection.eval("library(forecast)");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void CloseRServeConnection() {
		if (connection != null) {
			connection.close();
			engine.close();
		}
	}

	public void Run() {
		double[] x = GenerateSamples();

		System.out.println("RCaller forecast:");
		RunRCaller(x);
		System.out.println();

		System.out.println("RServe  forecast:");
		RunRServe(x);
		System.out.println();
	}

	public void RunRServe(double[] stockClosePrices) {
		try {
			connection.assign("x", stockClosePrices);
			final double[] predicted= connection
					.eval("f = forecast(auto.arima(x), h=10, level=90); res=as.numeric(f$mean);res")
					.asDoubles();

			// on screen
			System.out.println(Arrays.toString(predicted));

		} catch (Exception e) {
			System.out.println(e.toString());
		}

	}

	public double[] GenerateSamples() {
		Random random = new Random(12345);
		double[] stockClosePrices = new double[100];
		stockClosePrices[0] = 0;
		for (int i = 1; i < stockClosePrices.length; i++) {
			stockClosePrices[i] = 0.5 + 1 * stockClosePrices[i - 1]
					+ random.nextGaussian();
		}
		return stockClosePrices;
	}

	public void RunRCaller(double[] stockClosePrices) {
		try {

			// define code to be run
			RCode code = new RCode();

			code.addDoubleArray("x", stockClosePrices);
			code.R_require("forecast");
			code.addRCode("f= forecast(auto.arima(x), h=10, level=90); res=predicted=as.numeric(f$mean)");

			// set and run the caller
			RCaller caller = new RCaller();

			caller.setRCode(code);
			caller.setRscriptExecutable("/usr/bin/Rscript");
			caller.runAndReturnResult("res");

			double[] predicted = caller.getParser().getAsDoubleArray("res");

			// on screen
			System.out.println(Arrays.toString(predicted));

		} catch (Exception e) {
			System.out.println(e.toString());
		}

	}
}
