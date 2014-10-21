package ru.johnlife.lifetoolsmp3.adapter;

import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.R;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter {
	
	private Context context;
	
	public CustomSpinnerAdapter(Context context, int resource, ArrayList<String> objects) {
		super(context, resource, objects);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tvItem = new TextView(context);
		tvItem.setTextSize(16);
		tvItem.setGravity(Gravity.CENTER);
		tvItem.setText(getItem(position).toString());
		return tvItem;
	}

}
