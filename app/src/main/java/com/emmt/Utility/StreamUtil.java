/*
 * Description
 *
 *   A brief description of the class/interface.
 *
 * History
 *
 *   yyyy-mm-dd Author        
 *              What has been changed.
 *
 * Copyright notice
 */
package com.emmt.Utility;

import java.io.Closeable;
import java.io.IOException;

public class StreamUtil {
	
	public static void close(Closeable... closeables ) {
		for(Closeable c : closeables) {
			if(c != null) {
				try {
					c.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
