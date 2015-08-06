package ru.johnlife.lifetoolsmp3.ui.baseviews;

import android.annotation.SuppressLint;
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
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.BaseLibraryAdapter;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;

@SuppressLint("NewApi")
public abstract class BaseLibraryView extends View implements Handler.Callback {

     public static final int MSG_FILL_ADAPTER = 1;

    private ViewGroup view;
    private BaseAbstractAdapter<MusicData> adapter;
    private ListView listView;
    private TextView emptyMessage;
    private Handler uiHandler;
    private String filterQuery = "";
    private final Object lock = new Object();
    private CheckRemovedFiles checkRemovedFiles;
    private Cursor cursor;
    private PlaybackService service;
    private SDReceiver sdReceiver;
    private IntentFilter intentFilter;

    private ContentObserver observer = new ContentObserver(null) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (isUserDeleted) {
                isUserDeleted = false;
                return;
            }
            fillAdapter(querySong());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (isUserDeleted) {
                isUserDeleted = false;
                return;
            }
            if (uri.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)) {
                fillAdapter(querySong());
            }
        }
    };
    private String comment;

    private void fillAdapter(ArrayList<MusicData> list) {
        if (list.isEmpty() && !adapter.isEmpty()) {
            adapter.clear();
            return;
        }
        Message msg = new Message();
        msg.what = MSG_FILL_ADAPTER;
        msg.obj = list;
        uiHandler.sendMessage(msg);
        StateKeeper.getInstance().setLibraryAdapterItems(list);
    }

    protected boolean isUserDeleted = false;

    protected abstract BaseAbstractAdapter<MusicData> getAdapter();

    protected abstract ListView getListView(View view);

    public abstract TextView getMessageView(View view);

    protected abstract int getLayoutId();

    protected abstract void forceDelete();

    public void onPause() {
        ((BaseLibraryAdapter) adapter).resetListener();
        if (null != checkRemovedFiles) {
            checkRemovedFiles.cancel(true);
            checkRemovedFiles = null;
        }
        StateKeeper.getInstance().setLibraryFirstPosition(listView.getFirstVisiblePosition());
        getContext().unregisterReceiver(sdReceiver);
    }

    public void onResume() {
        ((BaseLibraryAdapter) adapter).setListener();
        checkRemovedFiles = new CheckRemovedFiles(adapter.getAll());
        if (checkRemovedFiles.getStatus() == Status.RUNNING) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            checkRemovedFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            checkRemovedFiles.execute();
        }
        getContext().registerReceiver(sdReceiver, intentFilter);
        updateAdapter();
    }

    private PlaybackService getService() {
        if (PlaybackService.hasInstance()) {
            return PlaybackService.get(getContext());
        }
        return null;
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
    };

    public BaseLibraryView(LayoutInflater inflater) {
        super(inflater.getContext());
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
        init(inflater);
        if (null != listView) {
            showProgress(view);
            listView.setAdapter(adapter);
            animateListView(listView, adapter);
        }
    }

    private void updateAdapter() {
        if (null != StateKeeper.getInstance().getLibraryAdapterItems()) {
            fillAdapter(StateKeeper.getInstance().getLibraryAdapterItems());
        }
        new Thread(new Runnable() {

            private ArrayList<MusicData> querySong;

            @Override
            public void run() {
                querySong = querySong();
                uiHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        fillAdapter(querySong);
                        int firstPosition = StateKeeper.getInstance().getLibraryFirstPosition();
                        if (firstPosition != 0 && firstPosition < adapter.getCount()) {
                            listView.setSelection(firstPosition);
                        }
                        if (querySong.isEmpty()) {
                            hideProgress(view);
                            listView.setEmptyView(emptyMessage);
                        }
                    }

                }, 1000);
            }
        }).start();
    }

    protected void showProgress(View v) {
        v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    protected void hideProgress(View v) {
        v.findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public View getView() {
        return view;
    }

    public void applyFilter(String srcFilter) {
        adapter.getFilter().filter(srcFilter);
        filterQuery = srcFilter;
    }

    public void clearFilter() {
        adapter.clearFilter();
        filterQuery = "";
    }

    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void adapterCancelTimer() {
        adapter.cancelTimer();
    }

    protected void deleteAdapterItem(MusicData item) {
        adapter.remove(item);
    }

    protected void deleteServiceItem(MusicData item) {
        getService().remove(item);
    }

    private void init(LayoutInflater inflater) {
        view = (ViewGroup) inflater.inflate(getLayoutId(), null);
        listView = getListView(view);
        emptyMessage = getMessageView(view);
        adapter = getAdapter();
        if (PlaybackService.hasInstance()) {
            service = PlaybackService.get(view.getContext());
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    service = PlaybackService.get(view.getContext());
                }
            });
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                forceDelete();
                Util.hideKeyboard(getContext(), view);

                if (!service.isCorrectlyState(MusicData.class, adapter.getCount())) {
                    ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(adapter.getAll());
                    service.setArrayPlayback(list);
                }
                if (!(service.isPrepared() && adapter.getItem(i).equals(service.getPlayingSong().getPath()))) {
                    ((BaseMiniPlayerActivity) getContext()).startSong(((MusicData) adapter.getItem(i)));
                }
            }
        });
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                forceDelete();
                AbstractSong data = (AbstractSong) adapter.getItem(position);
                showMenu(view, (MusicData) data);
                return true;
            }
        });
    }

    private void showMenu(final View view, final MusicData musicData) {
        PopupMenu menu = new PopupMenu(getContext(), view);
        menu.getMenuInflater().inflate(R.menu.library_menu, menu.getMenu());
        menu.getMenu().getItem(0).setVisible(false);
        menu.getMenu().getItem(1).setVisible(false);
        menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem paramMenuItem) {
                if (paramMenuItem.getItemId() == R.id.library_menu_delete) {
                    adapter.remove(musicData);
                    musicData.reset(getContext());
                }
                return false;
            }

        });
        menu.show();
    }

    protected ArrayList<MusicData> querySong() {
        ArrayList<MusicData> result = result = new ArrayList<>();
        synchronized (lock) {
            Cursor cursor = buildQuery(getContext().getContentResolver());
            if (null == cursor) return result;
            if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
                cursor.close();
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
        }
        Collections.sort(result, new Comparator<MusicData>() {
            @Override
            public int compare(MusicData lhs, MusicData rhs) {
                return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
            }
        });
        return result;
    }

    private Cursor buildQuery(ContentResolver resolver) {
        synchronized (lock) {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
//            String selection = MediaStore.MediaColumns.DATA + " LIKE '" + "" + "%" + "" + "%'";
            String selection = "(" + MediaStore.Audio.Media.IS_MUSIC + " != 0)";
            cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
        }
        return cursor;
    }

    public void highlightSong(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_FILL_ADAPTER) {
            ArrayList<MusicData> list = ((ArrayList<MusicData>) msg.obj);
            adapter.setDoNotifyData(false);
            adapter.clear();
            adapter.add(list);
            if (!filterQuery.isEmpty()) {
                applyFilter(filterQuery);
            } else {
                adapter.notifyDataSetChanged();
            }
            hideProgress(view);
            if (null != comment) {
                synchronized (lock) {
                    showProgress(view);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (final MusicData data : adapter.getAll()) {
                                if (null != data && null != data.getComment() && data.getComment().equalsIgnoreCase(comment)) {
                                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgress(view);
                                            listView.setSelection(adapter.getPosition(data));
                                            final Animation flash = AnimationUtils.loadAnimation(getContext(), R.anim.flash);
                                            getViewByPosition(adapter.getPosition(data), listView).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    getViewByPosition(adapter.getPosition(data), listView).setAnimation(flash);
                                                    flash.start();
                                                }
                                            }, 100);
                                        }
                                    });
                                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgress(view);
                                        }
                                    });
                                    comment = null;
                                    return;
                                }
                            }
                            comment = null;
                        }
                    }).start();
                }
            }
        }
        return true;
    }

    public View getViewByPosition(int pos, ListView listView) {
        int firstListItemPosition = listView.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    protected void animateListView(ListView listView, BaseAbstractAdapter<MusicData> adapter) {
        //Animate ListView in childs, if need
    }

    private class CheckRemovedFiles extends AsyncTask<Void, Void, ArrayList<MusicData>> {


        private ArrayList<MusicData> srcList;

        public CheckRemovedFiles(ArrayList<MusicData> srcList) {
            this.srcList = srcList;
        }

        @Override
        protected ArrayList<MusicData> doInBackground(Void... params) {
            ArrayList<MusicData> badFiles = new ArrayList<>();
            if (srcList.isEmpty()) return null;
            for (MusicData data : srcList) {
                if (!new File(data.getPath()).exists()) {
                    badFiles.add(data);
                }
            }
            for (MusicData musicData : badFiles) {
                if (isCancelled()) return null;
                srcList.remove(musicData);
                musicData.reset(getContext());
            }
            return srcList;
        }

        @Override
        protected void onPostExecute(ArrayList<MusicData> result) {
            if (null != result) {
                fillAdapter(result);
            }
            super.onPostExecute(result);
        }
    }

    public String getFilterQuery() {
        return filterQuery;
    }
}