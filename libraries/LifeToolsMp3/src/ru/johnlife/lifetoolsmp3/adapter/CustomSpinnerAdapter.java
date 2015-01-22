package ru.johnlife.lifetoolsmp3.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
	
	private boolean isWhiteTheme;
	
	public CustomSpinnerAdapter(Context context, int resource, List<String> objects, boolean isWhiteTheme) {
		super(context, resource, objects);
		this.isWhiteTheme = isWhiteTheme;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tvItem = new TextView(getContext());
		tvItem.setTextSize(16);
		if (isWhiteTheme) {
			tvItem.setTextColor(getContext().getResources().getColor(android.R.color.black));
		}
		tvItem.setGravity(Gravity.LEFT);
		tvItem.setText(getItem(position).toString());
		tvItem.setEllipsize(TruncateAt.START);
		tvItem.setLines(1);
		tvItem.setSingleLine(true);
		return tvItem;
	}
}
