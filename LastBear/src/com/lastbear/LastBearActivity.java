package com.lastbear;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;

public class LastBearActivity extends Activity {

	private ArrayList<LibraryGroup> libraryGroups = null;
	private LastBearView lastBearView = null;
	private Assistant assistant = null;
	private BearSprite bearSprite = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Toolbox.getInstance(getApplicationContext());
		ScoreBuilder.getInstance(getApplicationContext());
		Preferences.getInstance(getApplicationContext());
		LibraryBuilder.getInstance(getApplicationContext());

		setContentView(R.layout.main);

		libraryGroups = LibraryBuilder.getInstance().getGroups();
		lastBearView = (LastBearView) findViewById(R.id.lastbear_view);
		lastBearView.initailze(libraryGroups, new LastBearView.OnListener() {
			public void onLibrary(String groupId) {
				Intent intent = new Intent();
				intent.setClass(LastBearActivity.this, GroupActivity.class);
				intent.putExtra("groupId", groupId);
				LastBearActivity.this.startActivity(intent);
			}
		});

		bearSprite = (BearSprite) findViewById(R.id.lastbear_bear);
		bearSprite.initailze();

		assistant = (Assistant) findViewById(R.id.lastbear_assistant);
		assistant.initialize("left bottom", 20, -130,
				new Assistant.OnListener() {
					public void onAction(int action) {
						switch (action) {
						case Assistant.ACTION_FINISH:
							LastBearActivity.this.finish();
							// Process.killProcess(Process.myPid());
							// System.exit(0);
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
								Preferences
										.getInstance()
										.setPreference(
												Preferences.library_background_sound,
												0);
							} else {
								sound.start();
								assistant.setAction(
										action,
										null,
										Toolbox.getInstance().getBitmap(
												R.drawable.menu_sound));
								Preferences
										.getInstance()
										.setPreference(
												Preferences.library_background_sound,
												1);
							}
							break;
						case Assistant.ACTION_CREDITS:
							assistant.close(true);
							Intent intent = new Intent();
							intent.setClass(LastBearActivity.this,
									CreditActivity.class);
							LastBearActivity.this.startActivity(intent);
							break;
						}
					}
				});
		;
		assistant.addAction(Assistant.ACTION_FINISH, "Finish", Toolbox
				.getInstance().getBitmap(R.drawable.menu_shutdown));

		Bitmap soundbmp = Toolbox.getInstance().getBitmap(R.drawable.menu_mute);
		if (Preferences.getInstance().getPreference(
				Preferences.library_background_sound) > 0) {
			Toolbox.getInstance().getBacksound().start();
			soundbmp = Toolbox.getInstance().getBitmap(R.drawable.menu_sound);
		}

		assistant.addAction(Assistant.ACTION_SOUND, "Sound", soundbmp);

		assistant.addAction(Assistant.ACTION_CREDITS, "Credits", Toolbox
				.getInstance().getBitmap(R.drawable.menu_coach));
	}
}