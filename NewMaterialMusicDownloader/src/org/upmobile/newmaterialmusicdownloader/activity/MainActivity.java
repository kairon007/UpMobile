package org.upmobile.newmaterialmusicdownloader.activity;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

public class MainActivity extends BaseMiniPlayerActivity {

	private Drawer.Result drawerResult = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		drawerResult = new Drawer()
				.withActivity(this)
				.withToolbar(toolbar)
				.withActionBarDrawerToggle(true)
				.withHeader(R.layout.drawer_header)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.tab_search).withIcon(R.drawable.ic_action_search).withIdentifier(1),
						new PrimaryDrawerItem().withName(R.string.tab_downloads).withIcon(FontAwesome.Icon.faw_gamepad),
						new PrimaryDrawerItem().withName(R.string.tab_playlist).withIcon(FontAwesome.Icon.faw_eye).withIdentifier(2),
						new PrimaryDrawerItem().withName(R.string.tab_library).withIcon(FontAwesome.Icon.faw_eye).withIdentifier(2),
						new SectionDrawerItem().withName(R.string.tab_settings), 
						new SecondaryDrawerItem().withName("one").withIcon(FontAwesome.Icon.faw_question))
				.withOnDrawerListener(new Drawer.OnDrawerListener() {
					@Override
					public void onDrawerOpened(View drawerView) {
						Util.hideKeyboard(MainActivity.this, drawerView);
					}

					@Override
					public void onDrawerClosed(View drawerView) {
					}
				})
				.withOnDrawerItemClickListener(
						new Drawer.OnDrawerItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
								if (drawerItem instanceof Nameable) {
									Toast.makeText(MainActivity.this, MainActivity.this.getString(((Nameable) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
								}
								if (drawerItem instanceof Badgeable) {
									Badgeable badgeable = (Badgeable) drawerItem;
									if (badgeable.getBadge() != null) {
										try {
											int badge = Integer.valueOf(badgeable.getBadge());
											if (badge > 0) {
												drawerResult.updateBadge(String.valueOf(badge - 1), position);
											}
										} catch (Exception e) {
											Log.d(getClass().getSimpleName(), "Hay la-la-la! :)");
										}
									}
								}
//								changeFragment(getFragments().get(position - 1), false);
							}
						})
				.withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
							@Override
							public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
								if (drawerItem instanceof SecondaryDrawerItem) {
									Toast.makeText(MainActivity.this, MainActivity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()),	Toast.LENGTH_SHORT).show();
								}
								return false;
							}
						}).build();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (service == null) {
			service = PlaybackService.get(this);
		}
		if (service.hasArray()) {
			out.putParcelableArrayList(ARRAY_SAVE, service.getArrayPlayback());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		super.onStart();
	}

	@Override
	protected void onResume() {
		if (null != service && service.isPlaying()) {
			showPlayerElement(true);
		} else if (PlaybackService.hasInstance()) {
			service = PlaybackService.get(this);
		}
		super.onResume();
	}
	
	
	
	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectory();
	}

	@Override
	protected int getMiniPlayerID() {
		return 0;
	}

	@Override
	protected int getMiniPlayerClickableID() {
		return R.id.mini_player_main;
	}

	@Override
	protected int getFakeViewID() {
		return 0;
	}

	@Override
	protected void showPlayerFragment() {
	}

	@Override
	protected void showPlayerElement(boolean flag) {
	}
}
