package org.upmobile.musicpro.util;

import java.util.ArrayList;
import java.util.List;

import org.upmobile.musicpro.config.WebserviceConfig;
import org.upmobile.musicpro.object.CategoryMusic;
import org.upmobile.musicpro.object.Song;

import com.androidquery.util.XmlDom;

public class ParseXmlUtil {
	public static void getBaseUrl(XmlDom xmlDom) {
		WebserviceConfig.URL_SONG = xmlDom.text("song");
		WebserviceConfig.URL_IMAGE = xmlDom.text("image");
		WebserviceConfig.URL_NEXT_PAGE = xmlDom.text("next_page");
	}

	public static List<CategoryMusic> getListMusicTypes(XmlDom xmlDom) {
		List<CategoryMusic> imageinfo = new ArrayList<CategoryMusic>();
		List<XmlDom> entries = xmlDom.tags("item");
		for (XmlDom entry : entries) {
			try {
				CategoryMusic item = new CategoryMusic();
				item.setId(entry.text("id"));
				item.setTitle(entry.text("title"));
				item.setImage(entry.text("image"));
				imageinfo.add(item);
			} catch (Exception e) {
			}
		}
		return imageinfo;
	}

	public static List<Song> getListSongs(XmlDom xmlDom) {
		List<Song> listSongs = new ArrayList<Song>();
		List<XmlDom> entries = xmlDom.tags("item");
		for (XmlDom entry : entries) {
			try {
				Song item = new Song();
				item.setId(entry.text("id"));
				item.setIdType(entry.text("id_type"));
				item.setPosition(Integer.parseInt(entry.text("position")));
				item.setName(entry.text("name"));
				item.setArtist(entry.text("artist"));
				item.setUrl(entry.text("url"));
				item.setImage(entry.text("image"));
				listSongs.add(item);
			} catch (Exception e) {
			}
		}
		return listSongs;
	}

	public static String getNextPage(XmlDom xmlDom) {
		String nextPage = xmlDom.text("next_page");
		if (nextPage == null) {
			return "";
		}
		return nextPage;
	}
}
