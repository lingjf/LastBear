package com.lastbear;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class CoachActivity extends Activity {
	private CoachView coachView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("CoachActivity.onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		coachView = new CoachView(this);
		setContentView(coachView);
	}

	@Override
	protected void onStart() {
		System.out.println("CoachActivity.onStart()");

		super.onStart();
	}

	@Override
	protected void onRestart() {
		System.out.println("CoachActivity.onRestart()");
		super.onRestart();
	}

	@Override
	protected void onPause() {
		System.out.println("CoachActivity.onPause()");
		super.onPause();
		/*
		 * The following call pauses the rendering thread. If your OpenGL
		 * application is memory intensive, you should consider deallocating
		 * objects that consume significant memory here.
		 */
		coachView.onPause();
	}

	@Override
	protected void onResume() {
		System.out.println("CoachActivity.onResume()");
		super.onResume();
		/*
		 * The following call resumes a paused rendering thread. If you
		 * deallocated graphic objects for onPause() this is a good place to
		 * re-allocate them.
		 */
		coachView.onResume();
	}

	@Override
	protected void onStop() {
		System.out.println("CoachActivity.onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		System.out.println("CoachActivity.onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		System.out.println("CoachActivity.onBackPressed()");
		super.onBackPressed();
	}
}
