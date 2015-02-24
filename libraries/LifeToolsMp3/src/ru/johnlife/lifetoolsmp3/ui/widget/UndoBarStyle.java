package ru.johnlife.lifetoolsmp3.ui.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.animation.Animation;

public class UndoBarStyle implements Parcelable {

	public static final int DEFAULT_DURATION = 5000;

	int iconRes;
	int titleRes;
	int bgRes;
	long duration = DEFAULT_DURATION;
	Animation inAnimation;
	Animation outAnimation;

	public UndoBarStyle(@DrawableRes final int icon, @StringRes final int title) {
		iconRes = icon;
		titleRes = title;
	}

	public UndoBarStyle(@DrawableRes final int icon, @StringRes final int title, final long duration) {
		this(icon, title);
		this.duration = duration;
	}

	public UndoBarStyle(@DrawableRes final int icon, @StringRes final int title, @DrawableRes final int bg, final long duration) {
		this(icon, title, duration);
		bgRes = bg;
	}

	public UndoBarStyle setAnim(Animation inAnimation, Animation outAnimation) {
		this.inAnimation = inAnimation;
		this.outAnimation = outAnimation;
		return this;
	}

	@Override
	public String toString() {
		return "UndoBarStyle{" + "iconRes=" + iconRes + ", titleRes=" + titleRes + ", bgRes=" + bgRes + ", duration=" + duration + ", inAnimation=" + inAnimation + ", outAnimation=" + outAnimation + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof UndoBarStyle))
			return false;

		UndoBarStyle that = (UndoBarStyle) o;
		return bgRes == that.bgRes && duration == that.duration && iconRes == that.iconRes && titleRes == that.titleRes;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.iconRes);
		dest.writeInt(this.titleRes);
		dest.writeInt(this.bgRes);
		dest.writeLong(this.duration);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	private UndoBarStyle(Parcel in) {
		this.iconRes = in.readInt();
		this.titleRes = in.readInt();
		this.bgRes = in.readInt();
		this.duration = in.readLong();
	}

	public static final Creator<UndoBarStyle> CREATOR = new Creator<UndoBarStyle>() {
		public UndoBarStyle createFromParcel(Parcel source) {
			return new UndoBarStyle(source);
		}

		public UndoBarStyle[] newArray(int size) {
			return new UndoBarStyle[size];
		}
	};
}
