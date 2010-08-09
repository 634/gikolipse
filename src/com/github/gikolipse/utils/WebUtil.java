package com.github.gikolipse.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import static com.github.gikolipse.utils.Const.RETURN_STRING;

import com.github.gikolipse.exceptions.GikolipseException;

public class WebUtil {
	public String readContents(String url, String webSiteEncode) {
		StringBuffer html = new StringBuffer();
		BufferedReader br = null;
		try {
			URL urlObject = new URL(url);

			URLConnection uc = urlObject.openConnection();
			BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
			br = new BufferedReader(new InputStreamReader(bis, webSiteEncode));
			String line;
			while ((line = br.readLine()) != null) {
				html.append(line + RETURN_STRING);
			}
		} catch (MalformedURLException e) {
			throw new GikolipseException(e);
		} catch (UnknownHostException e) {
			throw new GikolipseException(e);
		} catch (IOException e) {
			throw new GikolipseException(e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				throw new GikolipseException(e);
			}
		}
		return html.toString();
	}
}
