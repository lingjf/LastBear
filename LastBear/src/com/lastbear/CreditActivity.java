package com.lastbear;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;

import com.shared.GIFView;

public class CreditActivity extends Activity {
	private MediaPlayer mMediaPlayer;
	private CreditView cv = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.credit);

		cv = (CreditView) findViewById(R.id.credit_view);

		Assistant av = (Assistant) findViewById(R.id.credit_assistant);
		av.initialize("center", 0, -270, null);
		av.addAction(Assistant.ACTION_CLEAR, "Score", null);
		av.addAction(Assistant.ACTION_SOUND, "Sound", null);
		av.addAction(Assistant.ACTION_CREDITS, "Credits", null);
		av.addAction(Assistant.ACTION_CLEAR, "Score", null);
		av.addAction(Assistant.ACTION_SOUND, "Sound", null);
		av.addAction(Assistant.ACTION_CREDITS, "Credits", null);
		av.addAction(Assistant.ACTION_CLEAR, "Score", null);
		av.addAction(Assistant.ACTION_SOUND, "Sound", null);
		av.addAction(Assistant.ACTION_CREDITS, "Credits", null);
		av.addAction(Assistant.ACTION_CLEAR, "Score", null);
		av.addAction(Assistant.ACTION_SOUND, "Sound", null);
		av.addAction(Assistant.ACTION_CREDITS, "Credits", null);

		mMediaPlayer = MediaPlayer.create(this, R.raw.musik);

		GIFView gv = (GIFView) findViewById(R.id.credit_river);
		gv.setGifImage(R.drawable.river);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		cv.getRefreshThread().setState(CreditView.RefreshThread.STATE_RUNING);
		mMediaPlayer.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		cv.getRefreshThread().setState(CreditView.RefreshThread.STATE_PAUSED);
		mMediaPlayer.pause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
