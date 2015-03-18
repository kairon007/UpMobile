package org.upmobile.newmaterialmusicdownloader.ui;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import ru.johnlife.lifetoolsmp3.ui.widget.FloatingActionButton;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.csform.android.uiapptemplate.view.dlg.MaterialDialog;
import com.csform.android.uiapptemplate.view.dlg.MaterialDialog.ButtonCallback;
import com.csform.android.uiapptemplate.view.dlg.Theme;

public class PlaylistView extends BasePlaylistView {

	private ImageView fabIconNew;
	private FloatingActionButton rightLowerButton;
	private MaterialDialog.ButtonCallback buttonCallback;
	private MaterialDialog.Builder builder;
	private MaterialDialog dialog;


	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
		buttonCallback = new ButtonCallback() {
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				EditText input = (EditText) dialog.findViewById(android.R.id.edit);
				String newTitle =  input.getText().toString().trim();
				if (newTitle.isEmpty()) {
					dialog.cancel();
					showMessage(getContext(), R.string.playlist_cannot_be_empty);
					return;
				}
				PlaylistView.this.createPlaylist(getContext().getContentResolver(), input.getText().toString());
				Util.hideKeyboard(getContext(), input);
				dialog.cancel();
			}
			@Override
			public void onNegative(MaterialDialog dialog) {
				super.onNegative(dialog);
				Util.hideKeyboard(getContext(), dialog.getCustomView());
				dialog.cancel();
			}
		};
        fabIconNew = new ImageView(getContext());
        fabIconNew.setColorFilter(getResources().getColor(Util.getResIdFromAttribute((Activity) getContext(), R.attr.colorPrimary)));
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_circle_grey));
        rightLowerButton = new FloatingActionButton.Builder(getContext())
                .setContentView(fabIconNew, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT))
                .setBackgroundDrawable(getStateList())
                .build();
        rightLowerButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});
	}

	@Override
	protected Bitmap getDeafultCover() {
		return ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_album_grey)).getBitmap();
	}

	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectory();
	}

	@Override
	protected int getLayoutId() {
		return 0;
	}

	@Override
	protected void showPlayerFragment(MusicData musicData) {

	}

	@Override
	protected ListView getListView(View view) {
		return null;
	}

	@Override
	protected TextView getMessageView(View view) {
		return null;
	}
	
	@Override
	public void showMessage(Context context, int message) {
		showMessage(context, getResources().getString(message));
	}
	
	@Override
	public void showMessage(Context context, String message) {
		((MainActivity) context).showMessage(message);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void showDialog() {
		builder = new MaterialDialog.Builder(getContext());
		builder.theme(Theme.LIGHT)
		.title(R.string.create_new_playlist)
		.backgroundColor(getResources().getColor(R.color.main_color_grey_100))
		.customView(R.layout.md_input_dialog, false)
		.dividerColorRes(R.color.md_divider_white)
		.titleColorRes(Util.getResIdFromAttribute((Activity) getContext(), R.attr.colorPrimaryApp))
		.neutralColorRes(R.color.material_indigo_500)
		.positiveColorRes(R.color.material_indigo_500)
		.negativeColorRes(R.color.material_red_500)
		.callback(buttonCallback)
		.autoDismiss(false)
		.positiveText(R.string.create)
		.negativeText(android.R.string.cancel);
		dialog = builder.build();
		dialog.show();
	}
	
	@Override
	public void closeDialog() {
		if (null != dialog && dialog.isShowing()) {
			dialog.cancel();
		}
	}
	
	public void onResume() {
		
	}
	
	public void onPause () {
		rightLowerButton.detach();
	}
	
	private StateListDrawable getStateList() {
		int color = getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorAccentApp));
		Drawable mDrawable = getContext().getResources().getDrawable(R.drawable.button_action_touch); 
		mDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, mDrawable);
		stateListDrawable.addState(new int[]{}, getResources().getDrawable(R.drawable.button_action));
		return stateListDrawable;
	}
	
	@Override
	protected Object[] groupItems() {
		int color = getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimaryApp));
		Drawable arrowDown = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_18dp); 
		Drawable arrowUp = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_18dp);
		return new Object[]{arrowDown, arrowUp, color};
	}
	
	@Override
	protected boolean isAnimateExpandCollapse() {
		return true;
	}
}
