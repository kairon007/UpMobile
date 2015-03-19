package org.upmobile.newmaterialmusicdownloader.ui;

import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.views.BasePlaylistView;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.csform.android.uiapptemplate.view.dlg.MaterialDialog;
import com.csform.android.uiapptemplate.view.dlg.MaterialDialog.ButtonCallback;
import com.nineoldandroids.view.ViewHelper;

public class PlaylistView extends BasePlaylistView {

	private ImageView floatingButton;
	private MaterialDialog.ButtonCallback buttonCallback;
	private MaterialDialog.Builder builder;
	private MaterialDialog dialog;
	float showPosition;
	float hidePosition;

	public PlaylistView(LayoutInflater inflater) {
		super(inflater);
		floatingButton = (ImageView) getView().findViewById(R.id.floatingButton);
		floatingButton.setImageDrawable(getStateList());
		buttonCallback = new ButtonCallback() {
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				EditText input = (EditText) dialog.findViewById(android.R.id.edit);
				String newTitle = input.getText().toString().trim();
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
		showPosition = ViewHelper.getY(floatingButton) - Util.dpToPx(getContext(), 16);
		hidePosition = ViewHelper.getY(floatingButton) - floatingButton.getHeight() * 3;
		ViewHelper.setY(floatingButton, hidePosition);
		showPosition = 0 - showPosition;
		show();
		floatingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog();
			}
		});

	}

	private Bitmap createBitMap(int width, int height) {
		return createBitMap(width, height, getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimaryApp)),
				getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorAccentApp)));
	}

	private Bitmap createBitMap(int width, int height, int colorPrimary, int colorAccent) {
		float widthPx = Util.dpToPx(getContext(), width);
		float heightPx = Util.dpToPx(getContext(), height);
		float mainCircleWidthPx = Util.dpToPx(getContext(), width - 2); // 50
		float mainCircleHeightPx = Util.dpToPx(getContext(), height - 2); // 50
		Bitmap bitMap = Bitmap.createBitmap((int) widthPx, (int) heightPx, Bitmap.Config.ARGB_8888);
		bitMap = bitMap.copy(bitMap.getConfig(), true);
		Canvas canvas = new Canvas(bitMap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		// draw shadow
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		int[] colors = new int[] { Color.DKGRAY, Color.DKGRAY, Color.DKGRAY, Color.LTGRAY, Color.WHITE, Color.TRANSPARENT };
		float[] positions = new float[] { 0.4f, 0.7f, 0.7f, 0.8f, 0.9f, 1f };
		RadialGradient radialGradient = new RadialGradient((int) widthPx / 2, (int) heightPx / 2, Util.dpToPx(getContext(), 23), colors, positions, Shader.TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawCircle(((int) widthPx) / 2, ((int) heightPx) / 2, Util.dpToPx(getContext(), 20), paint);
		// draw main circle
		paint.setShader(null);
		paint.setColor(colorPrimary);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas.drawCircle(mainCircleWidthPx / 2, mainCircleHeightPx / 2, Util.dpToPx(getContext(), 18), paint);
		// draw plus
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(mainCircleHeightPx / 20);
		paint.setColor(getResources().getColor(android.R.color.white));
		canvas.drawLine(mainCircleWidthPx / 10 * 3, mainCircleWidthPx / 10 * 4 + mainCircleWidthPx / 10, mainCircleWidthPx / 10 * 7, mainCircleWidthPx / 10 * 4 + mainCircleWidthPx / 10, paint);
		canvas.drawLine(mainCircleWidthPx / 10 * 4 + mainCircleWidthPx / 10, mainCircleWidthPx / 10 * 3, mainCircleWidthPx / 10 * 4 + mainCircleWidthPx / 10, mainCircleWidthPx / 10 * 7, paint);
		return bitMap;
	}

	public void show() {
		ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "y", showPosition);
		animator.setInterpolator(new BounceInterpolator());
		animator.setDuration(1500);
		animator.start();
		floatingButton.setVisibility(View.VISIBLE);
	}

	public void hide() {
		ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "y", hidePosition);
		animator.setInterpolator(new BounceInterpolator());
		animator.setDuration(1500);
		animator.start();
	}

	@Override
	protected Bitmap getDeafultCover() {
		return ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_album_grey)).getBitmap();
	}

	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectoryPrefix();
	}

	@Override
	protected int getLayoutId() {
		return 0;
	}

	@Override
	protected void showPlayerFragment(MusicData musicData) {
		((MainActivity) getContext()).showPlayerElement(true);
		((MainActivity) getContext()).startSong(musicData);		
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

	@Override
	protected void showDialog() {
		 builder = new MaterialDialog.Builder(getContext())
		.title(R.string.create_new_playlist)
		.customView(R.layout.md_input_dialog, false)
		.titleColorAttr(R.attr.colorPrimaryApp)
		.positiveColorAttr(R.attr.colorPrimaryApp).callback(buttonCallback)
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

	private StateListDrawable getStateList() {
		int color = getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimaryDarkApp));
		Drawable pressed = new BitmapDrawable(getResources(), createBitMap(48, 48, color, 0));
		Drawable idle = new BitmapDrawable(getResources(), createBitMap(48, 48));
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[] { android.R.attr.state_pressed }, pressed);
		stateListDrawable.addState(new int[] {}, idle);
		return stateListDrawable;
	}

	@Override
	protected Object[] groupItems() {
		int color = getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimaryApp));
		Drawable arrowDown = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_18dp);
		Drawable arrowUp = getContext().getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_18dp);
		return new Object[] { arrowDown, arrowUp, color };
	}

	@Override
	protected boolean isAnimateExpandCollapse() {
		return false;
	}
}
