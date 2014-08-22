package com.example.musicequalizer;

import java.util.ArrayList;
import java.util.List;
import widgets.Utils;
import widgets.VerticalSeekBar;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle; 
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


public class MyEqualizer extends Activity implements
		OnSeekBarChangeListener {// , OnSeekArcChangeListener {

	public static final String SERVICECMD = "com.android.music.musicservicecommand";
	public static final String CMDNAME = "command";
	public static final String CMDSTOP = "stop";
	public static final String CMDTOGGLEPAUSE = "togglepause";
	public static final String CMDPAUSE = "pause";
	public static final String CMDPREVIOUS = "previous";
	public static final String CMDNEXT = "next";

	private VerticalSeekBar sb1, sb2, sb3, sb4, sb5;
	private TextView sbP1, sbP2, sbP3, sbP4, sbP5;
	// private SeekArc sk1, sk2;
	private SeekBar sk1, sk2;
	private Button dontTouch;
	private int sk1pgs, sk2pgs;

	private ArrayList<SeekBar> sbList;
	private ArrayList<TextView> sbPList;

	private MediaPlayer player;
	private Equalizer equalizer;
	private AudioManager manager;
	private BassBoost bassBoost;
	private Virtualizer virtualizer;
	private AssetFileDescriptor descriptor;
	private AudioTrack audioTrack;

	private Button presetsButton;
	private ImageView onOffBtn;

	private String musicStyles[];

	short bands, minEQLevel, maxEQLevel, nrOfPresets;
	final short band1 = 0;
	final short band2 = 1;
	final short band3 = 2;
	final short band4 = 3;
	final short band5 = 4;

	private boolean ok;

	private ProgressDataSource myProgressDataSource;
	private List<ProgressClass> values;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);

		startService(new Intent(this, MyService.class));

		initApp();

		initEqualizer();

		if (Utils.isOn) {
			enableAll();
		} else {
			disableAll();

			onOffBtn.setImageResource(R.drawable.off_btn);
			Utils.isOn = false;

			Log.e("eqActive", Boolean.toString(equalizer.getEnabled()));
			dontTouch.setVisibility(View.VISIBLE);
			equalizer.release();
			bassBoost.release();
			virtualizer.release();
			disableAll();
		}
		super.onCreate(savedInstanceState);
	}

	private void initApp() {

		sb1 = (VerticalSeekBar) findViewById(R.id.seekBar1);
		sb2 = (VerticalSeekBar) findViewById(R.id.seekBar2);
		sb3 = (VerticalSeekBar) findViewById(R.id.seekBar3);
		sb4 = (VerticalSeekBar) findViewById(R.id.seekBar4);
		sb5 = (VerticalSeekBar) findViewById(R.id.seekBar5);

		sk1 = (SeekBar) findViewById(R.id.seekArc1);
		sk2 = (SeekBar) findViewById(R.id.seekArc2);

		presetsButton = (Button) findViewById(R.id.presets);

		onOffBtn = (ImageView) findViewById(R.id.on_off);

		if (!Utils.isOn) {
			onOffBtn.setImageResource(R.drawable.off_btn);
			disableAll();
		} else {
			onOffBtn.setImageResource(R.drawable.on_btn);
			enableAll();
		}

		onOffBtn.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:

					if (!Utils.isOn)
						onOffBtn.setImageResource(R.drawable.off_btn_pressed);
					else
						onOffBtn.setImageResource(R.drawable.on_btn_pressed);
					return true;

				case MotionEvent.ACTION_MOVE:
					return true;

				case MotionEvent.ACTION_UP:

					if (!Utils.isOn) {
						onOffBtn.setImageResource(R.drawable.on_btn);
						Utils.isOn = true;
						dontTouch.setVisibility(View.GONE);
						initEqualizer();
						enableAll();

					} else {
						onOffBtn.setImageResource(R.drawable.off_btn);
						Utils.isOn = false;

						Log.e("eqActive",
								Boolean.toString(equalizer.getEnabled()));
						dontTouch.setVisibility(View.VISIBLE);
						equalizer.release();
						bassBoost.release();
						virtualizer.release();
						disableAll();

					}
					return true;

				}

				return false;
			}
		});

		dontTouch = (Button) findViewById(R.id.not_touch);
		if (!Utils.isOn) {
			dontTouch.setVisibility(View.VISIBLE);
		} else {
			dontTouch.setVisibility(View.GONE);
		}
	}

	public void initBassBoost() {

		try {
			bassBoost = new BassBoost(0, 0);
			bassBoost.setEnabled(true);
			BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
			BassBoost.Settings bassBoostSetting = new BassBoost.Settings(
					bassBoostSettingTemp.toString());
			bassBoostSetting.strength = 0;
			bassBoost.setProperties(bassBoostSetting);

			Log.e("bbSetTemp", bassBoost.getProperties().toString());
			ok = true;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void initVirtualizer() {

		try {
			virtualizer = new Virtualizer(0, 0);
			virtualizer.setEnabled(true);
			virtualizer.setStrength((short) 0);
		} catch (Exception localException) {
			localException.printStackTrace();
		}

	}

	public void setEqualizerPreset(Short preset, int p1, int p2, int p3,
			int p4, int p5) {

		equalizer.usePreset(preset);

		sb1.setProgressAndThumb(15 + p1);
		sb2.setProgressAndThumb(15 + p2);
		sb3.setProgressAndThumb(15 + p3);
		sb4.setProgressAndThumb(15 + p4);
		sb5.setProgressAndThumb(15 + p5);

	}

	public void setPresets() {

		nrOfPresets = equalizer.getNumberOfPresets();

		musicStyles = new String[nrOfPresets];

		for (int k = 0; k < nrOfPresets; k++) {
			musicStyles[k] = equalizer.getPresetName((short) k);
			Log.e("preset", musicStyles[k]);
		}
	}

	private void testMethod() {

		try {

			manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			equalizer = new Equalizer(5, 0);
			int val = equalizer.setEnabled(true);
			if (val != Equalizer.SUCCESS)
				Log.e("A", "EQUALIZER NON ATTIVO");
			setVolumeControlStream(AudioManager.STREAM_MUSIC);

			bands = equalizer.getNumberOfBands();
			minEQLevel = equalizer.getBandLevelRange()[0];
			maxEQLevel = equalizer.getBandLevelRange()[1];

			initBassBoost();

			initVirtualizer();

			initDB();

			showEqLevels();

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initDB() {

		myProgressDataSource = new ProgressDataSource(this);
		myProgressDataSource.open();

		values = myProgressDataSource.getAllPgs();

		Log.e("valSize", Integer.toString(values.size()));

		if (values.size() == 0)
			myProgressDataSource.createProgress(0, 0, 0, 0, 0, "Custom", 0, 0);
		else {
			Log.e("valuesPgs", Integer.toString(values.get(0).getProgress(1)));
			int progress;
			presetsButton.setText(values.get(0).getUser());

			progress = values.get(0).getProgress(1);
			if (presetsButton.getText().equals("Custom")) {
				sbP1.setText(Integer.toString(progress - 15));
				sb1.setProgress(progress);
				changeAtBand(band1, progress - 15);
			} else {
				sbP1.setText(Integer.toString(progress));
				sb1.setProgress(progress + 15);
				changeAtBand(band1, progress);
			}

			progress = values.get(0).getProgress(2);
			if (presetsButton.getText().equals("Custom")) {
				sbP2.setText(Integer.toString(progress - 15));
				sb2.setProgress(progress);
				changeAtBand(band2, progress - 15);
			} else {
				sbP2.setText(Integer.toString(progress));
				sb2.setProgress(progress + 15);
				changeAtBand(band2, progress);
			}

			progress = values.get(0).getProgress(3);
			if (presetsButton.getText().equals("Custom")) {
				sbP3.setText(Integer.toString(progress - 15));
				sb3.setProgress(progress);
				changeAtBand(band3, progress - 15);
			} else {
				sbP3.setText(Integer.toString(progress));
				sb3.setProgress(progress + 15);
				changeAtBand(band3, progress);
			}

			progress = values.get(0).getProgress(4);
			if (presetsButton.getText().equals("Custom")) {
				sbP4.setText(Integer.toString(progress - 15));
				sb4.setProgress(progress);
				changeAtBand(band4, progress - 15);
			} else {
				sbP4.setText(Integer.toString(progress));
				sb4.setProgress(progress + 15);
				changeAtBand(band4, progress);
			}

			progress = values.get(0).getProgress(5);
			if (presetsButton.getText().equals("Custom")) {
				sbP5.setText(Integer.toString(progress - 15));
				sb5.setProgress(progress);
				changeAtBand(band5, progress - 15);
			} else {
				sbP5.setText(Integer.toString(progress));
				sb5.setProgress(progress + 15);
				changeAtBand(band5, progress);
			}

			progress = values.get(0).getArc(1);
			Log.e("dbSK1", Integer.toString(progress));
			sk1.setProgress(progress);
			sk1pgs = progress;
			bassBoost.setStrength((short) (progress * 10));

			progress = values.get(0).getArc(2);
			Log.e("dbSK2", Integer.toString(progress));
			sk2.setProgress(progress);
			sk2pgs = progress;
			virtualizer.setStrength((short) (progress * 10));

		}

		for (ProgressClass i : values) {

			Log.e("user", "blabla " + i.getUser());

		}

		myProgressDataSource.close();
	}

	private void dbChangePg(int p1, int p2, int p3, int p4, int p5,
			String user, int arc1, int arc2) {

		myProgressDataSource.open();
		values = myProgressDataSource.getAllPgs();

		myProgressDataSource.deleteComment(values.get(0));
		myProgressDataSource.createProgress(p1, p2, p3, p4, p5, user, arc1,
				arc2);

		myProgressDataSource.close();
	}

	public void initEqualizer() {

		sbList = new ArrayList();
		sbPList = new ArrayList();

		sbList.add(sb1);
		sbList.add(sb2);
		sbList.add(sb3);
		sbList.add(sb4);
		sbList.add(sb5);

		sbP1 = (TextView) findViewById(R.id.textViewSB1);
		sbP2 = (TextView) findViewById(R.id.textViewSB2);
		sbP3 = (TextView) findViewById(R.id.textViewSB3);
		sbP4 = (TextView) findViewById(R.id.textViewSB4);
		sbP5 = (TextView) findViewById(R.id.textViewSB5);

		sbPList.add(sbP1);
		sbPList.add(sbP2);
		sbPList.add(sbP3);
		sbPList.add(sbP4);
		sbPList.add(sbP5);

		sb1.setOnSeekBarChangeListener(this);
		sb2.setOnSeekBarChangeListener(this);
		sb3.setOnSeekBarChangeListener(this);
		sb4.setOnSeekBarChangeListener(this);
		sb5.setOnSeekBarChangeListener(this);

		// sk1.setOnSeekArcChangeListener(this);
		// sk2.setOnSeekArcChangeListener(this);

		sk1.setOnSeekBarChangeListener(this);
		sk2.setOnSeekBarChangeListener(this);

		testMethod();
		setPresets();
		presetsButton.setOnClickListener(new View.OnClickListener() {

			@SuppressLint("NewApi") @Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(MyEqualizer.this, presetsButton);
				popup.getMenuInflater().inflate(R.menu.popup_menu,
						popup.getMenu());

				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {

						if (item.getTitle().equals("Normal")) {
							setEqualizerPreset((short) (0), 6, 2, 0, 2, 6);
							presetsButton.setText(R.string.normal);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (0)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(6, 2, 0, 2, 6, "Normal", 0, 0);
						} else if (item.getTitle().equals("Classical")) {
							setEqualizerPreset((short) (1), 10, 6, -4, 8, 8);
							presetsButton.setText(R.string.classical);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (1)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(10, 6, -4, 8, 8, "Classical", 0, 0);
						} else if (item.getTitle().equals("Dance")) {
							setEqualizerPreset((short) (2), 12, 0, 4, 8, 2);
							presetsButton.setText(R.string.dance);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (2)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(12, 0, 4, 8, 2, "Dance", 0, 0);
						} else if (item.getTitle().equals("Flat")) {
							setEqualizerPreset((short) (3), 0, 0, 0, 0, 0);
							presetsButton.setText(R.string.flat);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (3)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(0, 0, 0, 0, 0, "Flat", 0, 0);
						} else if (item.getTitle().equals("Folk")) {
							setEqualizerPreset((short) (4), 6, 0, 0, 4, -2);
							presetsButton.setText(R.string.folk);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (4)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(6, 0, 0, 4, -2, "Folk", 0, 0);
						} else if (item.getTitle().equals("Metal")) {
							setEqualizerPreset((short) (5), 8, 2, 15, 6, 0);
							presetsButton.setText(R.string.metal);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (5)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(8, 2, 15, 6, 0, "Metal", 0, 0);
						} else if (item.getTitle().equals("HipHop")) {
							setEqualizerPreset((short) (6), 10, 6, 0, 2, 6);
							presetsButton.setText(R.string.hiphop);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (6)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(10, 6, 0, 2, 6, "HipHop", 0, 0);
						} else if (item.getTitle().equals("Jazz")) {
							setEqualizerPreset((short) (7), 8, 4, -4, 4, 10);
							presetsButton.setText(R.string.jazz);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (7)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(8, 4, -4, 4, 10, "Jazz", 0, 0);
						} else if (item.getTitle().equals("Pop")) {
							setEqualizerPreset((short) (8), -2, 4, 10, 2, -4);
							presetsButton.setText(R.string.pop);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (8)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(-2, 4, 10, 2, -4, "Pop", 0, 0);
						} else if (item.getTitle().equals("Rock")) {
							setEqualizerPreset((short) (9), 10, 6, -2, 6, 10);
							presetsButton.setText(R.string.rock);
							Toast.makeText(getApplicationContext(),
									equalizer.getPresetName((short) (9)),
									Toast.LENGTH_SHORT).show();
							dbChangePg(10, 6, -2, 6, 10, "Rock", 0, 0);
						}

						sk1.setProgress(0);
						sk2.setProgress(0);

						return true;
					}
				});

				popup.show();// showing popup menu

			}
		});

	}

	private void disableAll() {

		sb1.setEnabled(false);
		sb2.setEnabled(false);
		sb3.setEnabled(false);
		sb4.setEnabled(false);
		sb5.setEnabled(false);

//		sk1.setVisibility(View.GONE);
//		sk2.setVisibility(View.GONE);
		sk1.setEnabled(false);
		sk2.setEnabled(false);

		presetsButton.setEnabled(false);

	}

	private void enableAll() {

		sb1.setEnabled(true);
		sb2.setEnabled(true);
		sb3.setEnabled(true);
		sb4.setEnabled(true);
		sb5.setEnabled(true);

//		sk1.setVisibility(View.VISIBLE);
//		sk2.setVisibility(View.VISIBLE);
		sk1.setEnabled(true);
		sk2.setEnabled(true);

		presetsButton.setEnabled(true);

	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub

		seekBar.setProgress(progress);

		// ===========================================temporary
		if (seekBar == sk1) {

			bassBoost.setStrength((short) (progress * 10));
			if (progress != 0)
				presetsButton.setText(R.string.user);
			sk1pgs = progress;

		} else if (seekBar == sk2) {

			virtualizer.setStrength((short) (progress * 10));
			if (progress != 0)
				presetsButton.setText(R.string.user);
			sk2pgs = progress;

		}

		// ===============================================/temporary

		if (progress == 15)
			progress = 0;
		else
			progress = progress - 15;

		if (seekBar == sb1) {

			sbP1.setText(Integer.toString(progress));
			presetsButton.setText(R.string.user);
			changeAtBand(band1, progress);

		} else if (seekBar == sb2) {

			sbP2.setText(Integer.toString(progress));
			presetsButton.setText(R.string.user);
			changeAtBand(band2, progress);

		} else if (seekBar == sb3) {

			sbP3.setText(Integer.toString(progress));
			presetsButton.setText(R.string.user);
			changeAtBand(band3, progress);

		} else if (seekBar == sb4) {

			sbP4.setText(Integer.toString(progress));
			presetsButton.setText(R.string.user);
			changeAtBand(band4, progress);

		} else if (seekBar == sb5) {

			sbP5.setText(Integer.toString(progress));
			presetsButton.setText(R.string.user);
			changeAtBand(band5, progress);
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

		if (presetsButton.getText().equals("Custom")) {
			dbChangePg(sb1.getProgress(), sb2.getProgress(), sb3.getProgress(),
					sb4.getProgress(), sb5.getProgress(), presetsButton
							.getText().toString(), sk1pgs, sk2pgs);
		}

	}

	@Override
	protected void onPause() {
		myProgressDataSource.close();
		super.onPause();
		// if (isFinishing() && player != null) {
		// equalizer.release();
		// }
	}

	@Override
	protected void onResume() {
		if (Utils.isOn)
			myProgressDataSource.open();
		super.onResume();
	}

	public void changeAtBand(final short band, int progress) {

		short level = 0;
		if (progress >= 0) {
			level = (short) (progress * 100); // + maxEQLevel);
			equalizer.setBandLevel(band, level);
		} else if (progress < 0) {
			level = (short) (progress * 100); // + minEQLevel);
			equalizer.setBandLevel(band, level);
		}

	}

	// @Override
	// public void onProgressChanged(SeekArc seekArc, int progress,
	// boolean fromUser) {
	// // TODO Auto-generated method stub
	//
	// if (seekArc == sk1) {
	//
	// bassBoost.setStrength((short) (progress * 10));
	// if (progress != 0)
	// presetsButton.setText(R.string.user);
	// sk1pgs = progress;
	//
	// } else if (seekArc == sk2) {
	//
	// virtualizer.setStrength((short) (progress * 10));
	// if (progress != 0)
	// presetsButton.setText(R.string.user);
	// sk2pgs = progress;
	//
	// }
	//
	// }
	//
	// @Override
	// public void onStartTrackingTouch(SeekArc seekArc) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onStopTrackingTouch(SeekArc seekArc) {
	// // TODO Auto-generated method stub
	// Log.e("arc", "flag");
	// Log.e("arcPgS",
	// "flag " + Integer.toString(sk1pgs) + " & "
	// + Integer.toString(sk2pgs));
	// dbChangePg(sb1.getProgress(), sb2.getProgress(), sb3.getProgress(),
	// sb4.getProgress(), sb5.getProgress(), presetsButton.getText()
	// .toString(), sk1pgs, sk2pgs);
	// }

	public void showEqLevels() {

		for (short i = 0; i < 5; i++)
			Log.e("band" + Short.toString(i),
					Short.toString(equalizer.getBandLevel(i)));

	}

}
