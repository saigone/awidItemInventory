package com.emmt.plus.device;

import java.util.ArrayList;

public class TagRequestQueue {
	private final ArrayList<String> queue = new ArrayList<String>();

	public synchronized String[] getTags() {
		while (queue.size() <= 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String[] tags = new String[queue.size()];
		queue.toArray(tags);
		queue.clear();

		return tags;
	}

	public synchronized void putTag(String tag) {
		if (isUnstoredTag(tag)) {
			queue.add(tag);
			notifyAll();
		}
	}
	
	private boolean isUnstoredTag(String tag) {
		if(queue.contains(tag))
			return false;
		
		return true;
	}
}
