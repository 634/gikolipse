package com.github.gikolipse.services;

import static com.github.gikolipse.utils.Const.RETURN_STRING;
import static com.github.gikolipse.utils.Const.ENCODING_2CH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.gikolipse.utils.A;
import com.github.gikolipse.utils.WebUtil;

public class BBSListService {

	public List<A> createThreadList(String bbsUrl) {
		String subbackHtmlUrl = bbsUrl + "subback.html";

		WebUtil webUtil = new WebUtil();
		String html = webUtil.readContents(subbackHtmlUrl, ENCODING_2CH);
		String[] htmlLines = html.split(RETURN_STRING);

		List<A> threadList = new ArrayList<A>();

		Pattern linkPattern = Pattern.compile("^<a href=\".*");
		for (String htmlLine : htmlLines) {

			Matcher categoryPatternMatcher = linkPattern.matcher(htmlLine);
			if (categoryPatternMatcher.matches()) {
				Pattern pattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(htmlLine);
				String linkText = matcher.replaceAll("");

				String linkUrl = htmlLine.substring(htmlLine.indexOf("href=\"") + 6, htmlLine.indexOf(">"));

				threadList.add(new A(linkText, linkUrl));
			}
		}

		return threadList;
	}

	public Map<String, List<A>> createBBSList() {
		WebUtil webUtil = new WebUtil();
		String html = webUtil.readContents("http://menu.2ch.net/bbsmenu.html", ENCODING_2CH);
		String[] htmlLines = html.split(RETURN_STRING);

		Map<String, List<A>> categoryMap = new HashMap<String, List<A>>();
		boolean categoryFlag = false;
		String category = "";

		Pattern categoryPattern = Pattern.compile("^<BR><BR><B>.*</B><BR>$");
		Pattern linkPattern = Pattern.compile("^<A HREF=.*");

		for (String htmlLine : htmlLines) {

			String linkText = "";
			String linkUrl = "";

			if (htmlLine.equals("")) {
				categoryFlag = false;
			}

			Matcher categoryPatternMatcher = categoryPattern.matcher(htmlLine);
			if (categoryPatternMatcher.matches()) {
				categoryFlag = true;

				Pattern pattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(htmlLine);
				category = matcher.replaceAll("");
				category = category.trim();

				if (!categoryMap.containsKey(category)) {
					categoryMap.put(category, new ArrayList<A>());
				}
			}

			Matcher linkPatternMatcher = linkPattern.matcher(htmlLine);
			if (categoryFlag && linkPatternMatcher.matches()) {
				Pattern pattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(htmlLine);
				linkText = matcher.replaceAll("");

				linkUrl = htmlLine.substring(htmlLine.indexOf("http"), htmlLine.indexOf(">"));
				linkUrl = linkUrl.replaceAll(" TARGET=_blank", "");

				List<A> aList = categoryMap.get(category);
				aList.add(new A(linkText, linkUrl));
				categoryMap.put(category, aList);
			}
		}

		return categoryMap;
	}
}
