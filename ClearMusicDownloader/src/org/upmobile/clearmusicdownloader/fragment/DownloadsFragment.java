package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.DownloadsAdapter;

import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.fragments.BaseDownloadsFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class DownloadsFragment extends BaseDownloadsFragment implements OnScrollListener, OnMenuListener {

	private ResideMenu resideMenu;
	private UISwipableList listView;
	private Animation anim;
	private DownloadsAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		resideMenu = ((MainActivity) getActivity()).getResideMenu();
		resideMenu.setMenuListener(this);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void openMenu() {
		for (final MusicData item : adapter.getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				adapter.removeItem(item);
				adapter.cancelTimer();
			}
		}
	}

	@Override
	public void closeMenu() {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		for (final MusicData item : adapter.getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				int wantedPosition = adapter.getPosition(item);
				int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
				int wantedChild = wantedPosition - firstPosition;
				if (wantedChild < 0 || wantedChild >= listView.getChildCount())
					return;
				anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
				anim.setDuration(200);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation paramAnimation) {
					}

					@Override
					public void onAnimationRepeat(Animation paramAnimation) {
					}

					@Override
					public void onAnimationEnd(Animation paramAnimation) {
						adapter.cancelTimer();
						adapter.removeItem(item);
					}
				});
				listView.getChildAt(wantedChild).startAnimation(anim);
			}
		}
	}

	@Override
	protected String getDirectory() {
		return Constants.DIRECTORY_PREFIX;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	protected ArrayAdapter<MusicData> getAdapter() {
		adapter = new DownloadsAdapter(getActivity(), org.upmobile.clearmusicdownloader.R.layout.downloads_item);
		return adapter;
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
}
