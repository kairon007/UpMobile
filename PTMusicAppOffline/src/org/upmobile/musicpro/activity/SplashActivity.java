package org.upmobile.musicpro.activity;

import java.util.List;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.modelmanager.ModelManager;
import org.upmobile.musicpro.modelmanager.ModelManagerListener;
import org.upmobile.musicpro.object.CategoryMusic;
import org.upmobile.musicpro.util.LanguageUtil;
import org.upmobile.musicpro.util.MySharedPreferences;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class SplashActivity extends FragmentActivity {
	private ModelManager modelManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		LanguageUtil.setLocale(new MySharedPreferences(this).getLanguage(),
				this);

		GlobalValue.constructor(this);
		modelManager = new ModelManager(this);
		modelManager.getBaseUrl(new ModelManagerListener() {
			@Override
			public void onSuccess(Object object) {
				getListMusicType();
			}

			@Override
			public void onError() {
				getListMusicType();
			}
		});
	}

	private void getListMusicType() {
		modelManager.getListMusicTypes(new ModelManagerListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object object) {
				GlobalValue.listCategoryMusics
						.addAll((List<CategoryMusic>) object);
				startMainActivity();
			}

			@Override
			public void onError() {
				Toast.makeText(
						SplashActivity.this,
						"There is an error with the internet connection. Music data cannot be loaded.",
						Toast.LENGTH_LONG).show();
				startMainActivity();
			}
		});
	}

	private void startMainActivity() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(getBaseContext(), MainActivity.class));
				overridePendingTransition(R.anim.slide_in_left,
						R.anim.slide_out_left);
				finish();
			}
		}, 2000);
	}

	@Override
	public void onBackPressed() {
	}
}
