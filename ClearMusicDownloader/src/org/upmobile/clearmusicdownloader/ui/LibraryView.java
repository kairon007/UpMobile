package org.upmobile.clearmusicdownloader.ui;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.adapters.LibraryAdapter;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BaseLibraryView;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.special.menu.ResideMenu.OnMenuListener;
import com.special.utils.UISwipableList;

public class LibraryView extends BaseLibraryView implements OnScrollListener, OnMenuListener, Constants {
	
	private CustomTextWatcher textWatcher; 
	private UISwipableList listView;
	private View emptyView;
	private EditText etFilter;
	private ImageButton ibClearFilter;
	private ViewGroup scrollBox;
	private Animation anim;

	public LibraryView(LayoutInflater inflater) {
		super(inflater);
		((MainActivity) getContext()).setResideMenuListener(this);
	}

	@Override
	protected void specialInit(View view) {
		scrollBox = (ViewGroup) view.findViewById(R.id.flt_scroll);
		textWatcher = new CustomTextWatcher();
		etFilter = (EditText) view.findViewById(R.id.flt_filter_text);
		etFilter.addTextChangedListener(textWatcher);
		ibClearFilter = (ImageButton) view.findViewById(R.id.flt_clear_filter);
		ibClearFilter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				etFilter.setText("");
				clearFilter();
			}
		});
	}
	
	@Override
	protected BaseAbstractAdapter<MusicData> getAdapter() {
		return new LibraryAdapter(getContext(), R.layout.library_item);
	}

	@Override
	protected ListView getListView(View view) {
		listView = (UISwipableList) view.findViewById(R.id.listView);
		listView.setActionLayout(R.id.hidden_view);
		listView.setItemLayout(R.id.front_layout);
		listView.setIgnoredViewHandler(((MainActivity) getContext()).getResideMenu());
		listView.setOnScrollListener(this);
		emptyView = inflate(getContext(), R.layout.empty_header_library, null);
		listView.addHeaderView(emptyView);
		return listView;
	}
	
	@Override
	protected String getFolderPath() {
		return ClearMusicDownloaderApp.getDirectory();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.fragment_list_transition;
	}
	
	@Override
	protected TextView getMessageView(View view) {
		return (TextView) view.findViewById(R.id.message_listview);
	}

	@Override
	public void openMenu() {
		for (final MusicData item : getAdapter().getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				item.reset(getContext());
				deleteAdapterItem(item);
				deleteServiceItem(item);
			}
		}
	}

	@Override
	public void closeMenu() { }
	
	public int getScrollListView() {
	    View c = listView.getChildAt(1);
	    if (c == null) return 0;
	    int firstVisiblePosition = listView.getFirstVisiblePosition();
	    int top = c.getTop();
	    return -top + firstVisiblePosition * c.getHeight();
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		for (final MusicData item : getAdapter().getAll()) {
			if (item.check(MusicData.MODE_VISIBLITY)) {
				int wantedPosition = getAdapter().getPosition(item);
				int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
				int wantedChild = wantedPosition - firstPosition;
				if (wantedChild < 0 || wantedChild >= listView.getChildCount()) return;
				anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
				anim.setDuration(200);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation paramAnimation) {
						adapterCancelTimer();
					}

					@Override
					public void onAnimationRepeat(Animation paramAnimation) {
					}

					@Override
					public void onAnimationEnd(Animation paramAnimation) {
						item.reset(getContext());
						deleteAdapterItem(item);
						deleteServiceItem(item);
					}
				});
				listView.getChildAt(wantedChild).startAnimation(anim);
			}
		}
	}

	private int lastScroll = getScrollListView();
	private int maxScroll = scrollBox.getLayoutParams().height;
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		int scrollBy = getScrollListView() - lastScroll;
		if (null == scrollBox || visibleItemCount < 5) return;
		lastScroll = getScrollListView();
		int resultScroll = scrollBox.getScrollY() + scrollBy;
		if (resultScroll < 0) {
			scrollBox.scrollTo(0, 0);
		} else if (resultScroll > maxScroll) {
			scrollBox.scrollTo(0, maxScroll);
		} else {
			scrollBox.scrollBy(0, scrollBy);
		}
	}
	
	public class CustomTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String query =  s.toString().toLowerCase();
			applyFilter(query);
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	}

}
