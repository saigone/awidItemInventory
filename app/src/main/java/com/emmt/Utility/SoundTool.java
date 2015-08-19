package com.emmt.Utility;

import com.emmt.awiditeminventory.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundTool {
	private SoundPool soundPool;
	private int soundID;

	public SoundTool(Context context, int soundSource) {
		// 這裏指定聲音池的最大音頻流數目為10，聲音品質為5大家可以自己測試感受下效果
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		// 載入音頻流
		soundID = soundPool.load(context, soundSource, 0);
	}

	public void play() {
		// 播放音頻，可以對左右音量分別設置，還可以設置優先級，循環次數以及速率
		// 速率最低0.5最高為2，1代表正常速度
		soundPool.play(soundID, 1, 1, 0, 0, 1);
	}

	public void stop() {
		soundPool.stop(soundID);
	}

	public void release() {
		soundPool.release();
	}

}
