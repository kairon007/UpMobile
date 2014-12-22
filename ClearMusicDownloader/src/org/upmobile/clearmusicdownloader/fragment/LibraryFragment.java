package org.upmobile.clearmusicdownloader.fragment;

import java.util.ArrayList;
import java.util.HashSet;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.LibraryAdapter;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.special.R;
import com.special.menu.ResideMenu;
import com.special.utils.UISwipableList;

public class LibraryFragment extends Fragment implements Handler.Callback, OnScrollListener{

	private static final int MSG_FILL_ADAPTER = 1;
	private View parentView;
	private UISwipableList listView;
	private LibraryAdapter adapter;
	private ResideMenu resideMenu;
	private Handler uiHandler;
	private String folderFilter;
	private Animation anim;
	private ContentObserver observer = new ContentObserver(null) {
		
		@Override
		public void onChange(boolean selfChange) {
			ArrayList<MusicData> list = querySong();
			customList(list);
			Message msg = new Message();
			msg.what = MSG_FILL_ADAPTER;
			msg.obj = list;
			uiHandler.sendMessage(msg);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)){
				ArrayList<MusicData> list = querySong();
				customList(list);
				Message msg = new Message();
				msg.what = MSG_FILL_ADAPTER;
				msg.obj = list;
				uiHandler.sendMessage(msg);
			} 
		};
		
		private void customList(ArrayList<MusicData> list) {
			PlayerService service = PlayerService.get(getActivity());
			HashSet<MusicData> datas = adapter.getRemovingData();
			if (null != datas) {
				for (MusicData musicData : datas) {
					if (list.contains(musicData)) {
						list.get(list.indexOf(musicData)).turnOn(MusicData.MODE_VISIBLITY);
					} else {
						adapter.deleteRemovingData(musicData);
					}
				}
			}
			if(service.getPlayingPosition() >= 0 && service.isPlaying() && service.getPlayingSong().getClass() == MusicData.class){
				int i = service.getPlayingPosition();
				list.get(i).turnOn(MusicData.MODE_PLAYING);
			}
		};
		
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle state) {
		uiHandler = new Handler(this);
		getActivity().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, observer);
		parentView = inflater.inflate(R.layout.fragment_list_transition, container, false);
		MainActivity parentActivity = (MainActivity) getActivity();
		resideMenu = parentActivity.getResideMenu();
		((MainActivity) getActivity()).showTopFrame();
		init();
		settingListView();
		ArrayList<MusicData> srcList = querySong();
		if (null == srcList || !srcList.isEmpty()) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(srcList);
			PlayerService service = PlayerService.get(getActivity());
			if (service.isPlaying() && service.getPlayingSong().getClass() == MusicData.class) {
				int pos = service.getPlayingPosition();
				if (pos >= 0 && pos < list.size()) {
					((MusicData) list.get(pos)).turnOn(MusicData.MODE_PLAYING);
				}
			}
			adapter.addAll(srcList);
			listView.setAdapter(adapter);
			listView.setOnScrollListener(this);
		}
		return parentView;
	}

	private void settingListView() {
		listView.setActionLayout(R.id.hidden_view);
		listView.setItemLayout(R.id.front_layout);
		listView.setIgnoredViewHandler(resideMenu);
	}

	private void init() {
		folderFilter = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
		adapter = new LibraryAdapter(getActivity(), org.upmobile.clearmusicdownloader.R.layout.library_item);
		listView = (UISwipableList) parentView.findViewById(R.id.listView);
	}
	
	private ArrayList<MusicData> querySong() {
		ArrayList<MusicData> result = new ArrayList<MusicData>();
		Cursor cursor = buildQuery(getActivity().getContentResolver());
		if (!cursor.moveToFirst()) {
			return new ArrayList<MusicData>();
		}
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);	
			result.add(data);
		}
		cursor.close();
		return result;
	}
	
	private Cursor buildQuery(ContentResolver resolver) {
		String selection =  MediaStore.MediaColumns.DATA + " LIKE '" + folderFilter + "%'" ;
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_FILL_ADAPTER) {
			adapter.changeArray((ArrayList<MusicData>) msg.obj);
		}
		return true;
	}

	@Override
	public void onScrollStateChanged(AbsListView paramAbsListView, int paramInt) {
		for (final MusicData item : adapter.getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				int wantedPosition = adapter.getPosition(item);
				int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
				int wantedChild = wantedPosition - firstPosition;
				if (wantedChild < 0 || wantedChild >= listView.getChildCount()) return;
				anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
				anim.setDuration(200);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation paramAnimation) {
						adapter.cancelTimer();
					}

					@Override
					public void onAnimationRepeat(Animation paramAnimation) {
					}

					@Override
					public void onAnimationEnd(Animation paramAnimation) {
						item.reset(getActivity());
						adapter.remove(item);
						PlayerService.get(getActivity()).remove(item);
					}
				});
				listView.getChildAt(wantedChild).startAnimation(anim);
			}
		}
	}

	@Override
	public void onScroll(AbsListView paramAbsListView, int paramInt1, int paramInt2, int paramInt3) {
	}
}
