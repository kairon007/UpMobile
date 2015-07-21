package ru.johnlife.lifetoolsmp3.equalizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.equalizer.widget.Utils;
import ru.johnlife.lifetoolsmp3.equalizer.widget.VerticalSeekBar;


public abstract class MyEqualizer extends Activity implements OnSeekBarChangeListener, BaseConstants {

//	public static final String SERVICECMD = "com.android.music.musicservicecommand";
//	public static final String CMDNAME = "command";
//	public static final String CMDSTOP = "stop";
//	public static final String CMDTOGGLEPAUSE = "togglepause";
//	public static final String CMDPAUSE = "pause";
//	public static final String CMDPREVIOUS = "previous";
//	public static final String CMDNEXT = "next";

	private VerticalSeekBar sb1, sb2, sb3, sb4, sb5;
	private TextView sbP1, sbP2, sbP3, sbP4, sbP5;
//	private SeekArc sk1, sk2;
	private SeekBar sk1, sk2;
	private Button dontTouch;
	private int sk1pgs, sk2pgs;

	private Equalizer equalizer;
	private BassBoost bassBoost;
	private Virtualizer virtualizer;
//	private AssetFileDescriptor descriptor;
//	private AudioTrack audioTrack;
	private SharedPreferences prefs;

	private Spinner presetsSpinner;
	private ArrayAdapter<String> presetsAdapter;
	private ImageView onOffBtn;

	short bands, minEQLevel, maxEQLevel, nrOfPresets;
	final short band1 = 0;
	final short band2 = 1;
	final short band3 = 2;
	final short band4 = 3;
	final short band5 = 4;

	private ProgressDataSource myProgressDataSource;
	private List<ProgressClass> values;

	protected abstract BassBoost getBassBoost();

	protected abstract Virtualizer getVirtualizer();

	protected abstract Equalizer getEqualizer();

	public static void setEqualizer(Context context, int audioSessionId) {
		if (Utils.getEqPrefs(context)) {
			Equalizer equalizer = new Equalizer(1, audioSessionId);
			BassBoost bassBoost = new BassBoost(2, audioSessionId);
			Virtualizer virtualizer = new Virtualizer(3, audioSessionId);
			equalizer.setEnabled(true);
			bassBoost.setEnabled(true);
			virtualizer.setEnabled(true);
			ProgressDataSource myProgressDataSource = new ProgressDataSource(context);
			myProgressDataSource.open();
			List<ProgressClass> values = myProgressDataSource.getAllPgs();
			if (values.size() == 0) {
				myProgressDataSource.createProgress(0, 0, 0, 0, 0, 0, 0, 0);
			} else {
				//Set equalizer
				Utils.changeAtBand(equalizer, (short)0, values.get(0).getProgress(1) - 15);
				Utils.changeAtBand(equalizer, (short)1, values.get(0).getProgress(2) - 15);
				Utils.changeAtBand(equalizer, (short)2, values.get(0).getProgress(3) - 15);
				Utils.changeAtBand(equalizer, (short)3, values.get(0).getProgress(4) - 15);
				Utils.changeAtBand(equalizer, (short)4, values.get(0).getProgress(5) - 15);
				//Set bassboost
				bassBoost.setStrength((short)(values.get(0).getArc(1) * 10));
				//Set virtualizer
				virtualizer.setStrength((short)(values.get(0).getArc(2) * 10));
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.equalizer_main);
		if (null == getEqualizer() || null == getVirtualizer() || null == getBassBoost()) finish();
		startService(new Intent(this, MyService.class));
		prefs = getPreferences(MODE_PRIVATE);

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
			equalizer.setEnabled(false);

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

		presetsSpinner = (Spinner) findViewById(R.id.presets);

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
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (!Utils.isOn) {
							onOffBtn.setImageResource(R.drawable.off_btn_pressed);
						} else {
							onOffBtn.setImageResource(R.drawable.on_btn_pressed);
						}
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
							Utils.setEqPrefs(MyEqualizer.this, true);
							if (prefs.getBoolean(EQUALIZER_SAVE, false)) {
								String preset = prefs.getString(EQUALIZER_PRESET, "Flat");
								if ("Custom".equals(preset)) {
									setSavedPreset();
								} else {
									setPreset(preset);
								}
							}
						} else {
							savePreset();
							setPreset("Flat");
							onOffBtn.setImageResource(R.drawable.off_btn);
							Utils.isOn = false;
							Log.e("eqActive", Boolean.toString(equalizer.getEnabled()));
							dontTouch.setVisibility(View.VISIBLE);
							equalizer.release();
							bassBoost.release();
							virtualizer.release();
							disableAll();
							Utils.setEqPrefs(MyEqualizer.this, false);
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

	public void setSavedPreset(){
		final int sbOne, sbTwo, sbThree, sbFour, sbFive, skOne, skTwo;
		if ("Custom".equals(presetsSpinner.getSelectedItem())) {
			sbOne = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_ONE, 0);
			sbTwo = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_TWO, 0);
			sbThree = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_THREE, 0);
			sbFour = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_FOUR, 0);
			sbFive = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_FIVE, 0);
			skOne = prefs.getInt(EQUALIZER_SEEKBAR_CUSTOM_ONE, 0);
			skTwo = prefs.getInt(EQUALIZER_SEEKBAR_CUSTOM_TWO, 0);
		} else {
			sbOne = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_ONE, 0);
			sbTwo = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_TWO, 0);
			sbThree = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_THREE, 0);
			sbFour = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_FOUR, 0);
			sbFive = prefs.getInt(EQUALIZER_VERTICAL_SEEKBAR_FIVE, 0);
			skOne = prefs.getInt(EQUALIZER_SEEKBAR_ONE, 0);
			skTwo = prefs.getInt(EQUALIZER_SEEKBAR_TWO, 0);
		}
		sb1.setProgressAndThumb(sbOne);
		sb2.setProgressAndThumb(sbTwo);
		sb3.setProgressAndThumb(sbThree);
		sb4.setProgressAndThumb(sbFour);
		sb5.setProgressAndThumb(sbFive);
		Utils.changeAtBand(equalizer, band1, sbOne - 15);
		Utils.changeAtBand(equalizer, band2, sbTwo - 15);
		Utils.changeAtBand(equalizer, band2, sbThree - 15);
		Utils.changeAtBand(equalizer, band3, sbFour - 15);
		Utils.changeAtBand(equalizer, band4, sbFive - 15);
		presetsSpinner.setSelection(0);
		Toast.makeText(getApplicationContext(), R.string.user, Toast.LENGTH_SHORT).show();
		sk1.setProgress(skOne);
		sk2.setProgress(skTwo);
		dbChangePg(sbOne, sbTwo, sbThree, sbFour, sbFive, 0, skOne, skTwo);
	}

	public void savePreset() {
		final int sbOne = sb1.getProgress();
		final int sbTwo = sb2.getProgress();
		final int sbThree = sb3.getProgress();
		final int sbFour = sb4.getProgress();
		final int sbFive = sb5.getProgress();
		final int skOne = sk1.getProgress();
		final int skTwo = sk2.getProgress();
		final String preset = presetsSpinner.getSelectedItem().toString().trim();
		Editor editor = prefs.edit();
		editor.putBoolean(EQUALIZER_SAVE, true);
		editor.putString(EQUALIZER_PRESET, preset);
		if ("Custom".equals(preset)) {
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_ONE, sbOne);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_TWO, sbTwo);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_THREE, sbThree);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_FOUR, sbFour);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_FIVE, sbFive);
			editor.putInt(EQUALIZER_SEEKBAR_CUSTOM_ONE, skOne);
			editor.putInt(EQUALIZER_SEEKBAR_CUSTOM_TWO, skTwo);
		} else {
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_ONE, sbOne);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_TWO, sbTwo);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_THREE, sbThree);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_FOUR, sbFour);
			editor.putInt(EQUALIZER_VERTICAL_SEEKBAR_FIVE, sbFive);
			editor.putInt(EQUALIZER_SEEKBAR_ONE, skOne);
			editor.putInt(EQUALIZER_SEEKBAR_TWO, skTwo);
		}
		editor.apply();
	}

	public void initBassBoost() {
		try {
			bassBoost = getBassBoost();
			if (null != bassBoost) {
				bassBoost.setEnabled(true);
				BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
				BassBoost.Settings bassBoostSetting = new BassBoost.Settings(bassBoostSettingTemp.toString());
				bassBoostSetting.strength = 0;
				bassBoost.setProperties(bassBoostSetting);
				Log.e("bbSetTemp", bassBoost.getProperties().toString());
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void initVirtualizer() {
		try {
			virtualizer = getVirtualizer();
			if (null != virtualizer) {
				virtualizer.setEnabled(true);
				virtualizer.setStrength((short) 0);
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public void setEqualizerPreset(Short preset, int p1, int p2, int p3, int p4, int p5) {
		try {
			equalizer.usePreset(preset);
		} catch(Throwable e) {
			e.printStackTrace();
		}
		sb1.setProgressAndThumb(15 + p1);
		sb2.setProgressAndThumb(15 + p2);
		sb3.setProgressAndThumb(15 + p3);
		sb4.setProgressAndThumb(15 + p4);
		sb5.setProgressAndThumb(15 + p5);
	}

	public void setPresets() {
		equalizer.setEnabled(true);
		nrOfPresets = equalizer.getNumberOfPresets();

		String[] musicStyles = new String[nrOfPresets];

		for (int k = 0; k < nrOfPresets; k++) {
			musicStyles[k] = equalizer.getPresetName((short) k);
			Log.e("preset", musicStyles[k]);
		}
	}

	private void testMethod() {
		try {
			equalizer = getEqualizer();
			if (null != equalizer) {
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
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	private void initDB() {

		final int DEF = 15;
		myProgressDataSource = new ProgressDataSource(this);
		myProgressDataSource.open();

		values = myProgressDataSource.getAllPgs();

		Log.e("valSize", Integer.toString(values.size()));

		if (values.size() == 0) {
			myProgressDataSource.createProgress(DEF, DEF, DEF, DEF, DEF, 0, 0, 0);
		} else {
			Log.e("valuesPgs", Integer.toString(values.get(0).getProgress(1)));
			int progress;
			presetsSpinner.setSelection(values.get(0).getUser());

			ProgressClass value = values.get(0);
			setValues(sbP1, sb1, band1, value.getProgress(1));
			setValues(sbP2, sb2, band2, value.getProgress(2));
			setValues(sbP3, sb3, band3, value.getProgress(3));
			setValues(sbP4, sb4, band4, value.getProgress(4));
			setValues(sbP5, sb5, band5, value.getProgress(5));
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
		myProgressDataSource.close();
	}

	private void setValues(TextView sbP, VerticalSeekBar sb, short band, int progress) {
		if ("Custom".equals(presetsSpinner.getSelectedItem())) {
			sbP.setText(Integer.toString(progress - 15));
			sb.setProgress(progress);
			Utils.changeAtBand(equalizer, band, progress - 15);
		} else {
			sbP.setText(Integer.toString(progress));
			sb.setProgress(progress + 15);
			Utils.changeAtBand(equalizer, band, progress);
		}
	}

	private void dbChangePg(int p1, int p2, int p3, int p4, int p5, int presetPosInArray, int arc1, int arc2) {
		myProgressDataSource.open();
		values = myProgressDataSource.getAllPgs();
		myProgressDataSource.deleteComment(values.get(0));
		myProgressDataSource.createProgress(p1, p2, p3, p4, p5, presetPosInArray, arc1, arc2);
		myProgressDataSource.close();
	}

	public void initEqualizer() {

		sbP1 = (TextView) findViewById(R.id.textViewSB1);
		sbP2 = (TextView) findViewById(R.id.textViewSB2);
		sbP3 = (TextView) findViewById(R.id.textViewSB3);
		sbP4 = (TextView) findViewById(R.id.textViewSB4);
		sbP5 = (TextView) findViewById(R.id.textViewSB5);

		sb1.setOnSeekBarChangeListener(this);
		sb2.setOnSeekBarChangeListener(this);
		sb3.setOnSeekBarChangeListener(this);
		sb4.setOnSeekBarChangeListener(this);
		sb5.setOnSeekBarChangeListener(this);

		sk1.setOnSeekBarChangeListener(this);
		sk2.setOnSeekBarChangeListener(this);
		if (getEqualizer() == null || getBassBoost() == null || getVirtualizer() == null) return;
		presetsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.presets));
		presetsSpinner.setAdapter(presetsAdapter);
		presetsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (null != view) {
					setPreset(((TextView) view).getText().toString());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		testMethod();
		setPresets();
	}

	private void setPreset(String preset) {
		switch (preset) {
			case "Custom":
				setSavedPreset();
				presetsSpinner.setSelection(0);
				break;
			case "Normal":
				setEqualizerPreset((short) (0), 6, 2, 0, 2, 6);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (0)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(1);
				dbChangePg(6, 2, 0, 2, 6, 1, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Classical":
				setEqualizerPreset((short) (1), 10, 6, -4, 8, 8);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (1)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(2);
				dbChangePg(10, 6, -4, 8, 8, 2, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Dance":
				setEqualizerPreset((short) (2), 12, 0, 4, 8, 2);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (2)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(3);
				dbChangePg(12, 0, 4, 8, 2, 3, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Flat":
				setEqualizerPreset((short) (3), 0, 0, 0, 0, 0);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (3)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(4);
				dbChangePg(0, 0, 0, 0, 0, 4, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Folk":
				setEqualizerPreset((short) (4), 6, 0, 0, 4, -2);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (4)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(5);
				dbChangePg(6, 0, 0, 4, -2, 5, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Metal":
				setEqualizerPreset((short) (5), 8, 2, 15, 6, 0);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (5)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(6);
				dbChangePg(8, 2, 15, 6, 0, 6, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "HipHop":
				setEqualizerPreset((short) (6), 10, 6, 0, 2, 6);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (6)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(7);
				dbChangePg(10, 6, 0, 2, 6, 7, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Jazz":
				setEqualizerPreset((short) (7), 8, 4, -4, 4, 10);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (7)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(8);
				dbChangePg(8, 4, -4, 4, 10, 8, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Pop":
				setEqualizerPreset((short) (8), -2, 4, 10, 2, -4);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (8)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(9);
				dbChangePg(-2, 4, 10, 2, -4, 9, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
			case "Rock":
				setEqualizerPreset((short) (9), 10, 6, -2, 6, 10);
				Toast.makeText(getApplicationContext(),
						equalizer.getPresetName((short) (9)),
						Toast.LENGTH_SHORT).show();
				presetsSpinner.setSelection(10);
				dbChangePg(10, 6, -2, 6, 10, 10, 0, 0);
				sk1.setProgress(0);
				sk2.setProgress(0);
				break;
		}
	}

	private void disableAll() {

		sb1.setEnabled(false);
		sb2.setEnabled(false);
		sb3.setEnabled(false);
		sb4.setEnabled(false);
		sb5.setEnabled(false);

		sk1.setEnabled(false);
		sk2.setEnabled(false);

		presetsSpinner.setEnabled(false);
	}

	private void enableAll() {

		sb1.setEnabled(true);
		sb2.setEnabled(true);
		sb3.setEnabled(true);
		sb4.setEnabled(true);
		sb5.setEnabled(true);

		sk1.setEnabled(true);
		sk2.setEnabled(true);

		presetsSpinner.setEnabled(true);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
								  boolean fromUser) {

		seekBar.setProgress(progress);

		// ===========================================temporary
		if (seekBar == sk1) {

			BassBoost.Settings settings = bassBoost.getProperties();
			settings.strength = ((short)(progress * 10));
			bassBoost.setProperties(settings);
			if (progress != 0) {
				presetsSpinner.setSelection(0);
			}
			sk1pgs = progress;

		} else if (seekBar == sk2) {

			virtualizer.setStrength((short) (progress * 10));
			if (progress != 0) {
				presetsSpinner.setSelection(0);
			}
			sk2pgs = progress;

		}

		// ===============================================/temporary

		if (progress == 15)
			progress = 0;
		else
			progress = progress - 15;

		if (seekBar == sb1) {

			sbP1.setText(Integer.toString(progress));
			presetsSpinner.setSelection(0);
			Utils.changeAtBand(equalizer, band1, progress);

		} else if (seekBar == sb2) {

			sbP2.setText(Integer.toString(progress));
			presetsSpinner.setSelection(0);
			Utils.changeAtBand(equalizer, band2, progress);

		} else if (seekBar == sb3) {

			sbP3.setText(Integer.toString(progress));
			presetsSpinner.setSelection(0);
			Utils.changeAtBand(equalizer, band3, progress);

		} else if (seekBar == sb4) {

			sbP4.setText(Integer.toString(progress));
			presetsSpinner.setSelection(0);
			Utils.changeAtBand(equalizer, band4, progress);

		} else if (seekBar == sb5) {

			sbP5.setText(Integer.toString(progress));
			presetsSpinner.setSelection(0);
			Utils.changeAtBand(equalizer, band5, progress);
		}
		Utils.setEqPrefs(MyEqualizer.this, true);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (presetsSpinner.getSelectedItem().equals("Custom")) {
			savePreset();
			dbChangePg(sb1.getProgress(), sb2.getProgress(), sb3.getProgress(),
					sb4.getProgress(), sb5.getProgress(), presetsSpinner.getSelectedItemPosition(), sk1pgs, sk2pgs);
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
			if (null != myProgressDataSource) {
				myProgressDataSource.open();
			}
		super.onResume();
	}

	public void showEqLevels() {

		for (short i = 0; i < 5; i++)
			Log.e("band" + Short.toString(i),
					Short.toString(equalizer.getBandLevel(i)));

	}
}