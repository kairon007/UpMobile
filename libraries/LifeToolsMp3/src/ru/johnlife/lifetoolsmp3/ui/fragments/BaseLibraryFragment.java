package ru.johnlife.lifetoolsmp3.ui.fragments;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BaseAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.special.utils.UISwipableList;

public abstract class BaseLibraryFragment<T> extends Fragment{

	protected UISwipableList listView;
	private LayoutInflater inflater;
	private Class<? extends Activity> baseActivity;

	protected ContentObserver observer;
	private Handler uiHandler;

	protected abstract View createView(LayoutInflater inflater, Bundle savedInstanceState);
	
	protected abstract String getFolderPath();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.base_fragment, null);
		View customView = createView(inflater, savedInstanceState);
		if (null != customView) {
			view.addView(customView, 0);
		}
		return view;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		baseActivity = null;
	}

	public Class<? extends Activity> getBaseActivity() {
		return baseActivity;
	}

	public LayoutInflater getLayoutInflater() {
		return inflater;
	}
	
	private ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		Cursor cursor = buildQuery(getActivity().getContentResolver(), getFolderPath());
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			return result;
		}
		MusicData d = new MusicData();
		d.populate(cursor);
		result.add(d);
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);
			result.add(data);
		}
		cursor.close();
		return result;
	}
	
	private Cursor buildQuery(ContentResolver resolver, String folderFilter) {
		String selection = MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%'";
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
	}

}
