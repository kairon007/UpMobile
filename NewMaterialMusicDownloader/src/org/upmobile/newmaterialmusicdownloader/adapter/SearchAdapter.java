package org.upmobile.newmaterialmusicdownloader.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.upmobile.newmaterialmusicdownloader.DownloadListener;
import org.upmobile.newmaterialmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;

public class SearchAdapter extends BaseSearchAdapter {

    public SearchAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    protected BaseSettings getSettings() {
        return new Nulldroid_Settings();
    }

    @Override
    protected Object initRefreshProgress() {
        return LayoutInflater.from(getContext()).inflate(R.layout.progress, null);
    }

    @Override
    protected ViewHolder<Song> createViewHolder(View view) {
        return new SearchViewHolder(view);
    }

    @Override
    protected void download(final RemoteSong song, int position) {
        if (((MainActivity) getContext()).isThisSongDownloaded(song)) {
            UndoBarController.UndoBar undo = new UndoBarController.UndoBar(((MainActivity) getContext()));
            undo.clear();
            undo.message(R.string.has_been_downloaded);
            undo.duration(3000);
            undo.noicon(true);
            undo.style(new UndoBarStyle(-1, R.string.download_anyway));
            undo.listener(new UndoBarController.UndoListener() {

                @Override
                public void onUndo(Parcelable token) {
                    DownloadListener downloadListener = new DownloadListener(getContext(), song, 0, true);
                    downloadListener.setDownloadPath(NewMaterialApp.getDirectory());
                    downloadListener.setUseAlbumCover(true);
                    downloadListener.downloadSong(false);
                }
            });
            undo.show();
        } else {
            DownloadListener downloadListener = new DownloadListener(getContext(), song, 0, true);
            downloadListener.setDownloadPath(NewMaterialApp.getDirectory());
            downloadListener.setUseAlbumCover(true);
            downloadListener.downloadSong(false);
        }
    }

    private class SearchViewHolder extends BaseSearchViewHolder implements OnClickListener {

        public SearchViewHolder(View view) {
            info = (ViewGroup) view.findViewById(R.id.boxInfoItem);
            cover = (ImageView) view.findViewById(R.id.cover);
            title = (TextView) view.findViewById(R.id.titleLine);
            artist = (TextView) view.findViewById(R.id.artistLine);
            duration = (TextView) view.findViewById(R.id.chunkTime);
            threeDot = view.findViewById(R.id.threeDot);
            dowloadLabel = (TextView) view.findViewById(R.id.infoView);
            indicator = info.findViewById(R.id.playingIndicator);
            ((ImageView) indicator).setColorFilter(getContext().getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimary)));
            threeDot.setOnClickListener(this);
        }

        @Override
        protected void hold(Song item, int position) {
            String comment = item.getComment();
            int lableStatus = keeper.checkSongInfo(comment);
            if (lableStatus == StateKeeper.DOWNLOADED) {
                item.setPath(keeper.getSongPath(comment));
            }
            boolean hasPlayingSong = null != service && service.isEnqueueToStream() && (item.equals(service.getPlayingSong()) ||
                    (null != item.getPath() && item.getPath().equals(service.getPlayingSong().getPath())));
            setDownloadLable(lableStatus);
            showPlayingIndicator(hasPlayingSong);
            cover.setImageResource(R.drawable.ic_album_grey);
            super.hold(item, position);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.threeDot:
                    showMenu(view);
                    break;
            }
        }
    }

    @Override
    public void showMessage(Context context, int message) {
        showMessage(context, context.getResources().getString(message));
    }

    @Override
    public void showMessage(final Context context, final String message) {
        ((MainActivity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ((MainActivity) context).showMessage(message);
            }
        });
    }

}
