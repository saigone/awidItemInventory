package com.emmt.awiditeminventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

public class WriteOrReaderTextFile {

	private final static String DIR_NAME = "TimeCheck";
	private final static String FILE_NAME = "timeRecord.txt";
	private final static long TOTAL_USING_TIME = 6 * 60 * 60; // 6 hours = 21600 sec
	private long mStartTime;
	private String TAG = WriteOrReaderTextFile.class.getSimpleName();
	private String mDirPath;

	public WriteOrReaderTextFile() {
		mStartTime = System.currentTimeMillis();
	}

	public void createDirectory() throws Exception {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			throw new Exception("There is no sd card in the device");

		File sdCardDir = Environment.getExternalStorageDirectory();
		File dir = new File(sdCardDir, DIR_NAME);
		if (!dir.isDirectory()) { 
			if (!dir.mkdir()) { 
				throw new Exception("Creating Dir fail");
			}
		}

		mDirPath = dir.getPath();
		Log.v(TAG, "DIR: " + mDirPath);
	}

	public String readFile() throws Exception {
		try {
			File readFile = new File(mDirPath, FILE_NAME);
			Log.v(TAG, readFile.getAbsolutePath());
			if (!readFile.exists())
				readFile.createNewFile();
			FileInputStream fIn = new FileInputStream(readFile);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));

			String timeRecord = myReader.readLine();
			myReader.close();
			if(timeRecord == null)
				return "0";
			else
				return timeRecord;
		} catch (IOException ex) {
			throw new IOException(ex.toString());
		}
	}

	public void writeFile() throws Exception {
		long plusTime = Long.parseLong(readFile());
		Log.v(TAG, "plusTime: " + plusTime);
		
		File writeFile = new File(mDirPath, FILE_NAME);
		if (!writeFile.exists())
			writeFile.createNewFile();

		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(writeFile, false),
					"utf-8");
			long timeDiff = (System.currentTimeMillis() - mStartTime) / 1000;
			Log.v(TAG, "timeDiff: " + timeDiff);
			out.append(String.valueOf(timeDiff + plusTime));
			out.flush();
			out.close();
		} catch (IOException ex) {
			throw new IOException(ex.toString());
		}
	}
	
	public boolean isOverUsingTime() throws Exception {
		long currentUsingTime =  Long.parseLong(readFile());
		if(currentUsingTime > TOTAL_USING_TIME)
			return true;
		
		return false;
	}

}
