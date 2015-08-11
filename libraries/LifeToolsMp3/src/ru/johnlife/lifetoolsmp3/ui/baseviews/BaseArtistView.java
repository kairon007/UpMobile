package ru.johnlife.lifetoolsmp3.ui.baseviews;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.ArtistData;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;

/**
 * Created by Aleksandr on 04.08.2015.
 */
public abstract class BaseArtistView extends View implements Handler.Callback, View.OnClickListener, TextWatcher {

    public static final int MSG_FILL_ADAPTER = 1;

    private ViewGroup view;
    private BaseAbstractAdapter<AbstractSong> adapter;
    private ListView listView;
    private TextView emptyMessage;
    private EditText liveSearch;
    private View clearLiveSearch;
    private Handler uiHandler;
    private final Object lock = new Object();
    private Cursor cursor;
    private PlaybackService service;
    private SDReceiver sdReceiver;
    private IntentFilter intentFilter;
    private View liveSearchScroll;

    protected void showProgress(View v) {
        v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    protected void hideProgress(View v) {
        v.findViewById(R.id.progress).setVisibility(View.GONE);
    }

    protected abstract BaseAbstractAdapter<AbstractSong> getAdapter();

    protected abstract ListView getListView(View view);

    public abstract TextView getMessageView(View view);

    protected abstract int getLayoutId();

    protected abstract void forceDelete();

    protected abstract void showPlayerFragment(MusicData musicData);

    protected abstract void animateListView(ListView listView, BaseAbstractAdapter<AbstractSong> adapter);

    protected abstract void showShadow(boolean show);

    public View getView() {
        return view;
    }

    private ContentObserver observer = new ContentObserver(null) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (lock) {
                fillAdapter(querySong());
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
                synchronized (lock) {
                    fillAdapter(querySong());
                }
            }
        }
    };

    public BaseArtistView(LayoutInflater inflater) {
        super(inflater.getContext());
        init(inflater);
        uiHandler = new Handler(this);
        sdReceiver = new SDReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        getContext().registerReceiver(sdReceiver, intentFilter);
        getContext().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
        if (null != listView) {
            showProgress(view);
            listView.setAdapter(adapter);
            animateListView(listView, adapter);
        }
        if (null != StateKeeper.getInstance().getArtistAdapterItems()) {
            updateAdapter(StateKeeper.getInstance().getArtistAdapterItems());
            hideProgress(view);
        }
        updateAdapter();
    }

    public void onPause() {
        StateKeeper.getInstance().setArtistAdapterItems(adapter.getOriginalItems());
        getContext().unregisterReceiver(sdReceiver);
    }

    public void onResume() {
        liveSearchScroll.scrollTo(0, 0);
        liveSearchScroll.scrollBy(0, 0);
        getContext().registerReceiver(sdReceiver, intentFilter);
    }

    public class SDReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)
                    || intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)
                    || intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
                    || intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
                    || intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                    || intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                updateAdapter();
            }
        }
    }

    private void fillAdapter(final ArrayList<AbstractSong> list) {
        if (list.isEmpty() && !adapter.isEmpty()) {
            adapter.clear();
            return;
        }
        Message msg = new Message();
        msg.what = MSG_FILL_ADAPTER;
        msg.obj = list;
        uiHandler.sendMessage(msg);
    }

    private void init(LayoutInflater inflater) {
        view = (ViewGroup) inflater.inflate(getLayoutId(), null);
        listView = getListView(view);
        emptyMessage = getMessageView(view);
        adapter = getAdapter();
        liveSearchScroll = view.findViewById(R.id.liveSearchScroll);
        liveSearchScroll.setVisibility(VISIBLE);
        liveSearch = (EditText) view.findViewById(R.id.liveSearch);
        clearLiveSearch = view.findViewById(R.id.clearLiveSearch);
        liveSearch.addTextChangedListener(this);
        clearLiveSearch.setOnClickListener(this);
        View v = inflate(getContext(), R.layout.empty_scroll, null);
        listView.addHeaderView(v);
        if (PlaybackService.hasInstance()) {
            service = PlaybackService.get(view.getContext());
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    service = PlaybackService.get(view.getContext());
                }
            }).start();
        }
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                if (i == 0 && listView.getHeaderViewsCount() > 0) return false; //header click;
                i = i - listView.getHeaderViewsCount();
                forceDelete();
                AbstractSong data = (AbstractSong) adapter.getItem(i);
                showMenu(view, data);
                return true;
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            int lastScroll = getScrollListView();
            int maxScroll = liveSearchScroll.getLayoutParams().height;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int scrollBy = getScrollListView() - lastScroll;
                lastScroll = getScrollListView();
                int resultScroll = liveSearchScroll.getScrollY() + scrollBy;
                if (resultScroll < 0 && adapter.getCount() != 0) {
                    liveSearchScroll.scrollTo(0, 0);
                    showShadow(false);
                } else if (resultScroll > maxScroll && adapter.getCount() != 0) {
                    liveSearchScroll.scrollTo(0, maxScroll);
                    showShadow(true);
                } else {
                    if (adapter.getCount() != 0) liveSearchScroll.scrollBy(0, scrollBy);
                    if (0 != scrollBy) {
                        Util.hideKeyboard(getContext(), view);
                        showShadow(false);
                    }
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0 && listView.getHeaderViewsCount() > 0) return; //header click;
                i = i - listView.getHeaderViewsCount();
                forceDelete();
                AbstractSong abstractSong = (AbstractSong) adapter.getItem(i);
                if (abstractSong.getClass() == ArtistData.class) {
                    if (((ArtistData) abstractSong).isExpanded()) {
                        adapter.remove(((ArtistData) abstractSong).getArtistSongs());
                        ((ArtistData) abstractSong).setExpanded(false);
                    } else {
                        adapter.addByData(((ArtistData) abstractSong).getArtistSongs(), abstractSong);
                        ((ArtistData) abstractSong).setExpanded(true);
                    }
                } else {
                    Util.hideKeyboard(getContext(), view);
                    if (null != service) {
                        service.setArrayPlayback(new ArrayList<>(getArtistBySong((MusicData) adapter.getItem(i)).getArtistSongs()));
                        showPlayerFragment((MusicData) adapter.getItem(i));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(final CharSequence s, int start, int before, int count) {
        if (s.toString().trim().isEmpty()) return;
        if (null == adapter.getOriginalItems() || adapter.getOriginalItems().isEmpty()) {
            liveSearch.setText("");
            liveSearch.clearFocus();
            return;
        }
        adapter.getFilter().filter(s.toString(), new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                String message = count == 0 ? getContext().getResources().getString(R.string.search_no_results_for) + " - " + s : getResources().getString(R.string.library_empty);
                emptyMessage.setText(message);
                emptyMessage.setVisibility(count == 0 ? VISIBLE : INVISIBLE);
            }
        });
    }

    @Override
    public void afterTextChanged(Editable s) {}

    public int getScrollListView() {
        View c = listView.getChildAt(1);
        if (c == null) return 0;
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePosition * c.getHeight();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.clearLiveSearch) {
            if (liveSearch.getText().toString().isEmpty()) return;
            adapter.clearFilter();
            liveSearch.clearFocus();
            liveSearch.setText("");
            emptyMessage.setVisibility(INVISIBLE);
        }
    }

    private void showMenu(final View view, final AbstractSong musicData) {
        PopupMenu menu = new PopupMenu(getContext(), view);
        menu.getMenuInflater().inflate(R.menu.library_menu, menu.getMenu());
        menu.getMenu().getItem(0).setVisible(false);
        menu.getMenu().getItem(1).setVisible(false);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem paramMenuItem) {
                if (paramMenuItem.getItemId() == R.id.library_menu_delete) {
                    removeData(musicData);
                }
                return false;
            }

        });
        menu.show();
    }

    private void updateAdapter(final ArrayList<AbstractSong> newAdapter) {
        ((Activity) getContext()).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                adapter.setDoNotifyData(false);
                adapter.clear();
                adapter.add(newAdapter);
                adapter.notifyDataSetChanged();
                if (newAdapter.isEmpty()) {
                    String message = adapter.getCount() == 0 && !liveSearch.getText().toString().isEmpty() ? getContext().getResources().getString(R.string.search_no_results_for) + " - " + liveSearch.getText() : getResources().getString(R.string.artist_empty);
                    emptyMessage.setText(message);
                }
            }
        });
    }

    private void updateAdapter() {
        new Thread(new Runnable() {

            private ArrayList<AbstractSong> querySong;

            @Override
            public void run() {
                querySong = querySong();
                uiHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        fillAdapter(querySong);
                        if (querySong.isEmpty()) {
                            hideProgress(view);
                            listView.setEmptyView(emptyMessage);
                        }
                    }

                }, 1000);
            }
        }).start();
    }

    protected ArrayList<AbstractSong> querySong() {
        synchronized (lock) {
            ArrayList<String> expanded = new ArrayList<>();
            for (AbstractSong abstractSong : adapter.getAll()) {
                if (abstractSong.getClass() == ArtistData.class && ((ArtistData) abstractSong).isExpanded()) {
                    expanded.add(abstractSong.getTitle().trim());
                }
            }
            ArrayList<ArtistData> result = new ArrayList<>();
            Cursor cursor = buildQuery(getContext().getContentResolver());
            if (null == cursor) return new ArrayList<AbstractSong>(result);
            if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
                cursor.close();
                return new ArrayList<AbstractSong>(result);
            }
            ArtistData d = new ArtistData();
            d.populate(cursor);
            result.add(d);
            while (cursor.moveToNext()) {
                ArtistData data = new ArtistData();
                data.populate(cursor);
                result.add(data);
            }
            cursor.close();
            ArrayList<AbstractSong> abstractSongArrayList = new ArrayList<AbstractSong>(result);
            for (AbstractSong data : result) {
                ((ArtistData) data).getSongsByArtist(getContext());
                if (expanded.contains(data.getTitle().trim())) {
                    ((ArtistData) data).setExpanded(true);
                    abstractSongArrayList.addAll(abstractSongArrayList.indexOf(data) + 1, ((ArtistData) data).getArtistSongs());
                }
            }
            return abstractSongArrayList;
        }
    }

    private Cursor buildQuery(ContentResolver resolver) {
        synchronized (lock) {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
            cursor = resolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    new String[] {
                    /* 0 */ BaseColumns._ID,
                    /* 1 */ MediaStore.Audio.ArtistColumns.ARTIST,
                    /* 2 */ MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                    }, null, null, MediaStore.Audio.Artists.ARTIST);

        }
        return cursor;
    }

    public void removeData(AbstractSong data) {
        getContext().getContentResolver().unregisterContentObserver(observer);
        if (MusicData.class != data.getClass()) {
            if (((ArtistData) data).getArtistSongs().equals(service.getArrayPlayback())) {
                service.stopPressed();
            }
            if (((ArtistData) data).isExpanded()) {
                adapter.remove(((ArtistData) data).getArtistSongs());
            }
            for (AbstractSong musicData : ((ArtistData) data).getArtistSongs()) {
                StateKeeper.getInstance().removeSongInfo(musicData.getComment());
                ((MusicData) musicData).reset(getContext());
            }
            adapter.remove(data);
        } else {
            ArtistData a = getArtistBySong((MusicData) data);
            if (null != a) {
                a.getArtistSongs().remove(data);
                if (a.getNumberOfTracks() == 1) {
                    adapter.remove(a);
                } else {
                    a.setNumberOfTracks(a.getNumberOfTracks() - 1);
                }
            }
            StateKeeper.getInstance().removeSongInfo(data.getComment());
            ((MusicData) data).reset(getContext());
            adapter.remove(data);
        }
        String message = adapter.getCount() == 0 && !liveSearch.getText().toString().isEmpty() ? getContext().getResources().getString(R.string.search_no_results_for) + " - " + liveSearch.getText() : getResources().getString(R.string.artist_empty);
        emptyMessage.setText(message);
        getContext().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
    }

    public ArtistData getArtistBySong (MusicData data) {
        for (int i = adapter.getPosition(data) - 1; i > -1; i--) {
            if (adapter.getItem(i).getClass() == ArtistData.class) {
                return (ArtistData) adapter.getItem(i);
            }
        }
        return null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_FILL_ADAPTER) {
            ArrayList<AbstractSong> list = ((ArrayList<AbstractSong>) msg.obj);
            adapter.setDoNotifyData(false);
            adapter.clear();
            adapter.add(list);
            liveSearch.setText(liveSearch.getText());
            if (liveSearch.getText().toString().isEmpty()) {
                adapter.notifyDataSetChanged();
            } else {
                liveSearch.setSelection(liveSearch.getText().length());
            }
            hideProgress(view);
        }
        return true;
    }

    public void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showMessage(Context context, int message) {
        showMessage(context, context.getString(message));
    }
}
