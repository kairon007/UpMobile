package org.upmobile.musicpro.modelmanager;

import org.upmobile.musicpro.config.WebserviceConfig;
import org.upmobile.musicpro.util.ParseXmlUtil;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

/**
 * 
 * @Description : Class to manager all model object
 * 
 */
public class ModelManager {
	private AQuery aquery;

	public ModelManager(Context context) {
		aquery = new AQuery(context);
	}

	public void getBaseUrl(final ModelManagerListener listener) {
		aquery.ajax(WebserviceConfig.URL_BASE, XmlDom.class, new AjaxCallback<XmlDom>() {
			@Override
			public void callback(String url, XmlDom object, AjaxStatus status) {
				if (object == null) {
					listener.onError();
					return;
				}
				ParseXmlUtil.getBaseUrl(object);
				listener.onSuccess(null);
			}
		});
	}

	public void getListMusicTypes(final ModelManagerListener listener) {
		aquery.ajax(WebserviceConfig.URL_MUSIC_TYPES, XmlDom.class, new AjaxCallback<XmlDom>() {
			@Override
			public void callback(String url, XmlDom object, AjaxStatus status) {
				if (object == null) {
					listener.onError();
					return;
				}
				listener.onSuccess(ParseXmlUtil.getListMusicTypes(object));
			}
		});
	}

	public void getListSongs(String url, final ModelManagerListener listener) {
		aquery.ajax(url, XmlDom.class, new AjaxCallback<XmlDom>() {
			@Override
			public void callback(String url, XmlDom object, AjaxStatus status) {
				if (object == null) {
					listener.onError();
					return;
				}
				Object[] objects = new Object[2];
				objects[0] = ParseXmlUtil.getNextPage(object);
				objects[1] = ParseXmlUtil.getListSongs(object);
				listener.onSuccess(objects);
			}
		});
	}
}