package org.upmobile.clearmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.LibraryAdapter;

import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseLibraryView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.special.menu.ResideMenu;
import com.special.menu.ResideMenu.OnMenuListener;
import com.special.utils.UISwipableList;

public class LibraryView extends BaseLibraryView implements OnScrollListener, OnMenuListener, Constants, Handler.Callback {
	
	private ResideMenu resideMenu;
	private UISwipableList listView;
	private Animation anim;
	private LibraryAdapter adapter;

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
		resideMenu = ((MainActivity) getContext()).getResideMenu();
		((MainActivity) getContext()).setResideMenuListener(this);
	}

	@Override
	protected ArrayAdapter<MusicData> getAdapter() {
		return adapter = new LibraryAdapter(getContext(), R.layout.library_item);
	}

	@Override
	protected ListView getListView(View view) {
		listView = (UISwipableList) view.findViewById(R.id.listView);
		listView.setActionLayout(R.id.hidden_view);
		listView.setItemLayout(R.id.front_layout);
		listView.setIgnoredViewHandler(resideMenu);
		listView.setOnScrollListener(this);
		return listView;
	}

	@Override
	protected String getFolderPath() {
		return Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	public void openMenu() {
		for (final MusicData item : adapter.getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				item.reset(getContext());
				adapter.remove(item);
				deleteItem(item);
			}
		}
	}

	@Override
	public void closeMenu() {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		for (final MusicData item : adapter.getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				int wantedPosition = adapter.getPosition(item);
				int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
				int wantedChild = wantedPosition - firstPosition;
				if (wantedChild < 0 || wantedChild >= listView.getChildCount()) return;
				anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
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
						item.reset(getContext());
						adapter.remove(item);
						deleteItem(item);
					}
				});
				listView.getChildAt(wantedChild).startAnimation(anim);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_FILL_ADAPTER) {
			ArrayList<MusicData> array = (ArrayList<MusicData>) msg.obj;
			if (adapter.isEmpty()) {
				adapter = new LibraryAdapter(getContext(), R.layout.library_item, array);
				listView.setAdapter(adapter);
				listView.setOnScrollListener(this);
			} else {
				adapter.changeArray((ArrayList<MusicData>) msg.obj);
			}
		}
		return true;
	}

}
