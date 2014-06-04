package com.lastbear;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class TangramActivity extends Activity {
	private String groupId = null;
	private int missionIndex = 0;
	private LibraryGroup libraryGroup = null;
	private LibraryMission libraryMission = null;

	TangramPlay tangramPlay = null;
	TangramDone tangramDone = null;
	Assistant assistant = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Intent intent = getIntent();
		groupId = intent.getStringExtra("groupId");
		missionIndex = intent.getIntExtra("missionIndex", 0);

		libraryGroup = LibraryBuilder.getInstance().getGroup(groupId);
		libraryMission = libraryGroup.getMission(missionIndex);

		setContentView(R.layout.tangram);

		tangramPlay = (TangramPlay) findViewById(R.id.tangram_play);
		tangramDone = (TangramDone) findViewById(R.id.tangram_done);
		assistant = (Assistant) findViewById(R.id.tangram_assistant);

		tangramPlay.initialize(libraryMission.getMission(),
				new TangramPlay.OnListener() {
					public void onMissionCompleted() {
						libraryMission.setScore(1);
						tangramDone.open();
					}
				});

		tangramDone.initialize(libraryMission, new TangramDone.OnListener() {
			public void onListLibrary() {
				TangramActivity.this.finish();
			}

			public void onMissionContinue() {
				tangramDone.close();
			}

			public void onMissionForward() {
				LibraryMission nextMission = libraryGroup
						.getMission(missionIndex + 1);
				if (nextMission != null) {
					Intent intent = new Intent();
					intent.setClass(TangramActivity.this, TangramActivity.class);
					intent.putExtra("groupId", groupId);
					intent.putExtra("missionIndex", nextMission.getIndex());
					TangramActivity.this.startActivity(intent);
				}
				TangramActivity.this.finish();
			}

			public void onDonate() {
				System.out.println("onDonate");
			}
		});

		assistant.initialize("left top", 110, -130, new Assistant.OnListener() {
			public void onAction(int action) {
				switch (action) {
				case Assistant.ACTION_BACKWARD:
					TangramActivity.this.finish();
					break;
				case Assistant.ACTION_SOUND:
					MediaPlayer sound = Toolbox.getInstance().getBacksound();
					if (sound.isPlaying()) {
						sound.pause();
						assistant.setAction(action, null, Toolbox.getInstance()
								.getBitmap(R.drawable.menu_mute));
					} else {
						sound.start();
						assistant.setAction(action, null, Toolbox.getInstance()
								.getBitmap(R.drawable.menu_sound));
					}
					break;
				case Assistant.ACTION_COACH:
					Intent intent = new Intent();
					intent.setClass(TangramActivity.this, CoachActivity.class);
					TangramActivity.this.startActivity(intent);
					break;
				case Assistant.ACTION_SOLVE:
					libraryMission.getMission().incrSolve();
					break;
				}
			}
		});
		assistant.addAction(Assistant.ACTION_BACKWARD, "Back", Toolbox
				.getInstance().getBitmap(R.drawable.menu_back));
		assistant.addAction(Assistant.ACTION_SOUND, "Sound", Toolbox
				.getInstance().getBitmap(R.drawable.menu_sound));
		assistant.addAction(Assistant.ACTION_COACH, "Coach", Toolbox
				.getInstance().getBitmap(R.drawable.menu_coach));
		assistant.addAction(Assistant.ACTION_SOLVE, "Solve", Toolbox
				.getInstance().getBitmap(R.drawable.menu_solve));
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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		/*
		 * if (assistant.isOpen()) { assistant.close(false); } else {
		 * assistant.open(false); }
		 */
	}
}
