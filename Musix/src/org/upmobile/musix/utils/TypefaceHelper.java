package org.upmobile.musix.utils;
import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Gustavo on 02/07/2014.
 */
public class TypefaceHelper {
    Context mContext;

    public TypefaceHelper(Context mContext) {
        this.mContext = mContext;
    }

    public Typeface getRobotoLight() {
        return Typeface.createFromAsset(this.mContext.getAssets(), "fonts/Roboto-Light.ttf");
    }

    public Typeface getRobotoMedium() {
        return Typeface.createFromAsset(this.mContext.getAssets(), "fonts/Roboto-Medium.ttf");
    }

}
