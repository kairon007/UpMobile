package com.special.menu;

import com.special.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResideMenuItem extends LinearLayout {

	public enum Types {TYPE_MENU, TYPE_SETTINGS};
	
	private ImageView iv_icon;
	private TextView tv_title;
	private TextView tv_path;
	private Context context;
	private String title;
	private int icon;
	private Types type; 

	public ResideMenuItem(Context context) {
		super(context);
		this.context = context;
	}

	public ResideMenuItem(Context context, int icon, int title, Types type) {
		super(context);
		this.context = context;
		this.icon = icon;
		this.title = getTitle(title);
		this.type = type;
		initViews(context);
		iv_icon.setImageResource(icon);
		tv_title.setText(title);
	}
	
	public ResideMenuItem(Context context, int icon, int title, String path, Types type) {
		super(context);
		this.context = context;
		this.icon = icon;
		this.title = getTitle(title);
		this.type = type;
		initViews(context);
		iv_icon.setImageResource(icon);
		tv_title.setText(title);
		tv_path.setText(path);
	}

	private void initViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (type == Types.TYPE_MENU) {
			inflater.inflate(R.layout.layout_menu_item, this);
			iv_icon = (ImageView) findViewById(R.id.iv_icon);
			tv_title = (TextView) findViewById(R.id.tv_title);
		} else {
			inflater.inflate(R.layout.layout_settings_item, this);
			iv_icon = (ImageView) findViewById(R.id.iv_icon);
			tv_title = (TextView) findViewById(R.id.tv_title);
			tv_path = (TextView) findViewById(R.id.tv_path);
		}
	}

	/**
	 * set the icon color;
	 * 
	 * @param icon
	 */
	public void setIcon(int icon) {
		iv_icon.setImageResource(icon);
	}

	/**
	 * set the title with resource ;
	 * 
	 * @param title
	 */
	public void setTitle(int title) {
		tv_title.setText(title);
	}

	/**
	 * set the title with string;
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		tv_title.setText(title);
	}
	
	private String getTitle(int title) {
		return context.getResources().getString(title);
	}
	
	public Types getType() {
		return type;
	}
}
