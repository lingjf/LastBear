package com.lastbear;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;

public class GroupActivity extends Activity {
	private String groupId = null;
	private LibraryGroup libraryGroup = null;
	private GroupView groupView = null;
	private Assistant assistant = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Toolbox.getInstance(getApplicationContext());
		ScoreBuilder.getInstance(getApplicationContext());
		LibraryBuilder.getInstance(getApplicationContext());

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Intent intent = getIntent();
		groupId = intent.getStringExtra("groupId");
		libraryGroup = LibraryBuilder.getInstance().getGroup(groupId);

		setContentView(R.layout.group);

		groupView = (GroupView) findViewById(R.id.group_view);
		groupView.initailze(libraryGroup, new GroupView.OnListener() {
			public void onMission(String groupId, int missionIndex) {
				Intent intent = new Intent();
				intent.setClass(GroupActivity.this, TangramActivity.class);
				intent.putExtra("groupId", groupId);
				intent.putExtra("missionIndex", missionIndex);
				GroupActivity.this.startActivity(intent);
			}

			public void onBackward() {
				GroupActivity.this.finish();
				libraryGroup.resetMissions();
			}
		});

		assistant = (Assistant) findViewById(R.id.library_assistant);
		assistant.initialize("right bottom", 160, 130,
				new Assistant.OnListener() {
					public void onAction(int action) {
						switch (action) {
						case Assistant.ACTION_CLEAR:
							for (int i = 0; i < libraryGroup.getMissions()
									.size(); i++) {
								LibraryMission libraryMission = libraryGroup
										.getMissions().get(i);
								libraryMission.setScore(0);
							}
							break;
						case Assistant.ACTION_SOUND:
							MediaPlayer sound = Toolbox.getInstance()
									.getBacksound();
							if (sound.isPlaying()) {
								sound.pause();
								assistant.setAction(
										action,
										null,
										Toolbox.getInstance().getBitmap(
												R.drawable.menu_mute));
							} else {
								sound.start();
								assistant.setAction(
										action,
										null,
										Toolbox.getInstance().getBitmap(
												R.drawable.menu_sound));
							}
							break;
						case Assistant.ACTION_CREDITS:
							assistant.close(true);
							Intent intent = new Intent();
							intent.setClass(GroupActivity.this,
									CreditActivity.class);
							GroupActivity.this.startActivity(intent);
							break;
						}
					}
				});
		assistant.addAction(Assistant.ACTION_CLEAR, "Score", Toolbox
				.getInstance().getBitmap(R.drawable.menu_clear));
		assistant.addAction(Assistant.ACTION_SOUND, "Sound", Toolbox
				.getInstance().getBitmap(R.drawable.menu_sound));
		assistant.addAction(Assistant.ACTION_CREDITS, "Credits", Toolbox
				.getInstance().getBitmap(R.drawable.menu_credit));
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
	}
}
