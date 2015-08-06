package ru.johnlife.lifetoolsmp3.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.utils.TestApp;
import ru.johnlife.lifetoolsmp3.utils.Util;

/**
 * Created by Aleksandr on 04.08.2015.
 */
public abstract class BaseArtistAdapter extends BaseAbstractAdapter<AbstractSong> {

    private LayoutInflater inflater;

    protected abstract Bitmap getDefaultCover();

    private final int CACHE_CAPACITY = 50;

    @SuppressWarnings("serial")
    private final HashMap<Integer, Bitmap> hardBitmapCache =
            new LinkedHashMap<Integer, Bitmap>(CACHE_CAPACITY, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(LinkedHashMap.Entry<Integer, Bitmap> eldest) {
                    return size() > CACHE_CAPACITY;
                }
            };

    public void putToCache(Integer url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (hardBitmapCache) {
                hardBitmapCache.put(url, bitmap);
            }
        }
    }

    public Bitmap getFromCache(Integer id) {
        Bitmap bitmap = null;
        synchronized (hardBitmapCache) {
            bitmap = hardBitmapCache.get(id);
            if (bitmap != null) {
                hardBitmapCache.remove(id);
                hardBitmapCache.put(id, bitmap);
                return bitmap;
            }
        }
        return bitmap;
    }

    public void clearCache() {
        for (Map.Entry<Integer, Bitmap> entry : hardBitmapCache.entrySet()) {
            Bitmap b = entry.getValue();
            if (b != null) {
                b.recycle();
            }
            b = null;
        }
        hardBitmapCache.clear();
        System.gc();
    }

    public BaseArtistAdapter(Context context, int resource) {
        super(context, resource);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup p) {
        View v = super.getView(position, convertView, p);
        AbstractSong item = (AbstractSong) getItem(position);
        boolean isMusicData = item.getClass() == MusicData.class;
        if (isMusicData) {
            v.findViewById(R.id.musicRowView).setVisibility(View.VISIBLE);
            v.findViewById(R.id.artisRowView).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.musicRowView).setVisibility(View.GONE);
            v.findViewById(R.id.artisRowView).setVisibility(View.VISIBLE);
        }
        return v;
    }

    protected abstract class BaseArtistViewHolder extends ViewHolder<AbstractSong> {

        protected ImageView cover;
        protected TextView title;
        protected TextView artist;
        protected TextView duration;
        protected View threeDot;
        protected TextView artistRowName;
        protected TextView numberOfSongs;
        protected TextView textSongs;

        @Override
        protected void hold(final AbstractSong data, int position) {
            if (data.getClass() == MusicData.class) {
                cover.setTag(data);
                cover.setImageBitmap(getDefaultCover());
                Bitmap bmp = getFromCache(data.hashCode());
                TestApp.start();
                if (null != bmp) {
                    cover.setImageBitmap(bmp);
                } else {
                    cover.setImageBitmap(getDefaultCover());
                    ((MusicData) data).getCover(new RemoteSong.OnBitmapReadyListener() {
                        int tag = data.hashCode();

                        @Override
                        public void onBitmapReady(Bitmap bmp) {
                            if (bmp != null) {
                                bmp = Util.resizeBitmap(bmp, Util.dpToPx(getContext(), 72), Util.dpToPx(getContext(), 72));
                            }
                            setCover(bmp, tag);
                            putToCache(data.hashCode(), bmp);
                        }

                        private void setCover(final Bitmap bmp, final int tag) {
                            ((BaseMiniPlayerActivity) getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (tag == cover.getTag().hashCode()) {
                                        cover.setImageBitmap(bmp == null ? getDefaultCover() : bmp);
                                    }
                                }
                            });
                        }
                    });
                }
                TestApp.stop();
                title.setText(data.getTitle());
                artist.setText(data.getArtist());
                duration.setText(Util.getFormatedStrDuration(data.getDuration()));
            } else {
                artistRowName.setText(data.getArtist());
                numberOfSongs.setText(data.getDuration() + "");
                textSongs.setText(getContext().getResources().getQuantityText(R.plurals.songs, (int) data.getDuration()));
            }
        }
    }
}
