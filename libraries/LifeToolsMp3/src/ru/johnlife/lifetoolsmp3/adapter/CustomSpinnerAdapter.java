package ru.johnlife.lifetoolsmp3.adapter;

import java.util.List;

import ru.johnlife.lifetoolsmp3.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
	
	private LayoutInflater inflater;
	
	public CustomSpinnerAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = inflater.inflate(R.layout.item_spinner, parent, false);
		TextView tvItem = (TextView) view.findViewById(R.id.spinnerItem);
//		if (isWhiteTheme) {
//			tvItem.setTextColor(getContext().getResources().getColor(android.R.color.black));
//		}
		tvItem.setText(getItem(position).toString());
		return tvItem;
	}
}
