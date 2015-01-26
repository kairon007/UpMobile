package org.upmobile.musix.listadapters;

/**
 * Created by Gustavo on 02/07/2014.
 */
import java.util.ArrayList;

import org.upmobile.musix.R;
import org.upmobile.musix.utils.TypefaceHelper;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Gustavo on 25/06/2014.
 */
public class NavigationDrawerListAdapter extends BaseAdapter {

    TypefaceHelper typefaceHelper;
    ArrayList<String> arrayList;
    private Context mContext;

    public NavigationDrawerListAdapter(Context context, ArrayList<String> arrayList){
        this.mContext = context;
        this.arrayList = arrayList;
        this.typefaceHelper = new TypefaceHelper(context);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public String getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder;
        String menuItem;

        if (row == null) {

            LayoutInflater mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = mLayoutInflater.inflate(R.layout.row_view_navdraweritem, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.txtMenuItem = (TextView) row.findViewById(R.id.NavDrawerItemName);
            viewHolder.imageHolder = (ImageView) row.findViewById(R.id.NavDrawerItemIcon);

            row.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) row.getTag();
        }

        menuItem = arrayList.get(position);

        int intDrawable = 0;
        switch (position){
            case 0: //
                intDrawable = R.drawable.ic_menu_songs_new;
                break;
//            case 1: //
//                intDrawable = R.drawable.ic_menu_artists;
//                break;
//            case 2: //
//                intDrawable = R.drawable.ic_menu_fav;
//                break;
            case 1: //
                intDrawable = R.drawable.ic_menu_exit;
                break;
        }

        Drawable drawable = row.getResources().getDrawable(intDrawable);
        Typeface typeface = typefaceHelper.getRobotoLight();

        viewHolder.txtMenuItem.setText(menuItem);
        viewHolder.txtMenuItem.setTypeface(typeface);
        viewHolder.imageHolder.setImageDrawable(drawable);

        return row;
    }

    private class ViewHolder{
        TextView txtMenuItem;
        ImageView imageHolder;
    }

}
