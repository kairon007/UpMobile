package org.upmobile.clearmusicdownloader.ui;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.DownloadsAdapter;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseDownloadsView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.special.menu.ResideMenu.OnMenuListener;
import com.special.utils.UISwipableList;

public class DownloadsView extends BaseDownloadsView implements OnScrollListener, OnMenuListener{
	
	private UISwipableList listView;
	private Animation anim;
	private DownloadsAdapter adapter;
	
	public DownloadsView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	public View getView() {
		View v = super.getView();
		ViewGroup scrollBox = (ViewGroup) v.findViewById(R.id.flt_scroll);
		scrollBox.setVisibility(View.GONE);
		listView.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);
		return v;
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
				anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
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
		return ClearMusicDownloaderApp.getDirectoryPrefix();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}

	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		adapter = new DownloadsAdapter(getContext(), org.upmobile.clearmusicdownloader.R.layout.downloads_item);
		return adapter;
	}

	@Override
	protected ListView getListView(View view) {
		listView = (UISwipableList) view.findViewById(R.id.listView);
		listView.setActionLayout(R.id.hidden_view);
		listView.setItemLayout(R.id.front_layout);
		listView.setIgnoredViewHandler(((MainActivity) getContext()).getResideMenu());
		listView.setOnScrollListener(this);
		return listView;
	}

	@Override
	protected TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.message_listview);
	}

}
