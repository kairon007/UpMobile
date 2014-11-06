package ru.johnlife.lifetoolsmp3.adapter;

import java.util.List;

import ru.johnlife.lifetoolsmp3.Util;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
	
	public CustomSpinnerAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tvItem = new TextView(getContext());
		tvItem.setTextSize(16);
		if (null != Util.getThemeName(getContext()) && Util.getThemeName(getContext()).equals(Util.WHITE_THEME)) {
			tvItem.setTextColor(getContext().getResources().getColor(android.R.color.black));
		}
		tvItem.setGravity(Gravity.LEFT);
		tvItem.setText(getItem(position).toString());
		return tvItem;
	}
}
