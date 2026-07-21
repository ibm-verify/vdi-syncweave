package com.ibm.di.test.framework.perf.result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Map.Entry;

public class ResultsHolder {

	private final PerfResult[] results;
	private Properties avgResult;

	public ResultsHolder(PerfResult... results) {
		this.results = results;
		this.avgResult = null;
	}

	public ResultsHolder(File storedPerfResults) {
		this.results = null;
		this.avgResult = loadResultsFrom(storedPerfResults);
	}

	/**
	 * @return the results
	 */
	public PerfResult[] getResults() {
		return results;
	}

	/**
	 * @return the avgResult
	 */
	public Properties getAvgResult() {
		if (avgResult == null && results != null) {
			avgResult = calculateAverageTimes();
		}
		return avgResult;
	}

	/**
	 * @param avg
	 * @param baseLineFile
	 */
	public void storeResultsTo(File baseLineFile) {
		Properties avg = getAvgResult();

		FileOutputStream f = null;
		OutputStreamWriter o = null;
		try {
			f = new FileOutputStream(baseLineFile);
			// o = new OutputStreamWriter(f, "UTF-8");
			avg.store(f, "Performance results");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (o != null) {
				try {
					o.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (f != null) {
				try {
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param results
	 * @param repeatCount
	 */
	private Properties calculateAverageTimes() {
		long sum = 0L;
		Properties result = new Properties();
		String key = null;

		for (Entry<String, Long> time : results[0].getTimes().entrySet()) {
			key = time.getKey();
			sum = time.getValue();

			for (int j = 1; j < results.length; j++) {
				sum += results[j].getTimes().get(key);
			}

			sum /= results.length;
			result.put(key, Long.toString(sum));
		}

		return result;
	}

	/**
	 * @param avgOld
	 * @param avgNew
	 * @return
	 */
	public void compareResultsWith(ResultsHolder oldRes, int equalityPrecisionInPercents) {
		Properties newAvg = getAvgResult();
		Properties oldAvg = oldRes.getAvgResult();
		StringBuilder slowerMsg = new StringBuilder("");
		StringBuilder fasterMsg = new StringBuilder("");

		if (newAvg.size() != oldAvg.size()) {
			throw new RuntimeException("Different number of tests in both stores!");
		} else {
			String key = null;
			long oldTime = 0L;
			long newTime = 0L;
			long avrPercent = 0;
			
			int numTests = 0;
			boolean hasSlower = false;
			boolean hasFaster = false;
			
			for (Entry<Object, Object> time : newAvg.entrySet()) {
				key = (String) time.getKey();
				newTime = getLong((String) time.getValue());
				oldTime = getLong(oldAvg.getProperty(key));
				long percent = 0;
				
				if (isNewTimeLower(oldTime, newTime, equalityPrecisionInPercents) && oldTime > 0) {
					percent = (long)((oldTime - newTime)*100.0)/oldTime;
					fasterMsg.append("Test: " + key + "\n\tOldTime: " + oldTime + "ms.\n\tNewTime: " + newTime + "ms.");
					if (percent >0) {
						fasterMsg.append("\n\tPercent faster: " +percent+"%\n\n");	
					} else {
						fasterMsg.append("\n\n");
					}
					hasFaster = true;
				} else {
					percent = (long)((newTime - oldTime)*100.0)/oldTime;
					slowerMsg.append("Test: " + key + "\n\tOldTime: " + oldTime + "ms.\n\tNewTime: " + newTime + "ms.\n\tPercent slower: " +percent+"%\n\n");
					hasSlower = true;
				}
				avrPercent+=percent;
				numTests++;
			}
			// add average slower/faster percent
			if(hasSlower ^ hasFaster) {
				avrPercent/=numTests;
				if(hasSlower) {
					slowerMsg.append("\n\nAvarage percent slower: " + avrPercent + "% \n\n");
				} else {
					fasterMsg.append("\n\nAvarage percent faster: " + avrPercent + "% \n\n");
				}
			} 
			if (!hasSlower) {
				// all test are faster
				System.out.println("\n\n"+fasterMsg.toString());
			} else {
				// some tests are slower
				throw new IllegalStateException("New server is slower than the old server! The following tests failed:\n\n"
						+ slowerMsg);

			}
		}
	}

	private static long getLong(String s) {

		if (s != null) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException nfe) {
			}
		}

		return 0L;
	}

	private static boolean isNewTimeLower(long oldTime, long newTime, int equalityPrecisionInPercents) {
		double oldTimePlusExtra = ((equalityPrecisionInPercents / 100.0) * oldTime) + oldTime;
		return ((newTime < oldTime ) || (newTime < oldTimePlusExtra ));
	}

	/**
	 * @param baseLineFile
	 * @return
	 */
	private static Properties loadResultsFrom(File baseLineFile) {

		Properties result = new Properties();

		FileInputStream f = null;
		InputStreamReader i = null;
		try {
			f = new FileInputStream(baseLineFile);
			// i = new InputStreamReader(f, "UTF-8");
			result.load(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (i != null) {
				try {
					i.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (f != null) {
				try {
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

}
