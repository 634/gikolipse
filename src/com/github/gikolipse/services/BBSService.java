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

// http://pc11.2ch.net/hack/
// http://pc11.2ch.net/hack/subback.html
// 1275593668/l50
// http://pc11.2ch.net/test/read.cgi/hack/1275593668/l50

public class BBSService {

	public String getHtmlString(A threadUrl) {
		WebUtil webUtil = new WebUtil();
		String html = webUtil.readContents(threadUrl.url, ENCODING_2CH);

		String[] htmlLines = html.split(RETURN_STRING);
		List<String> bodyLineList = new ArrayList<String>();
		boolean start = false;
		for (String htmlLine : htmlLines) {
			if (htmlLine.indexOf("<h1 style") > -1) {
				start = true;
			}
			if (start) {
				bodyLineList.add(htmlLine);
			}
			if (start && htmlLine.indexOf("</dl>") > -1) {
				break;
			}
		}

		String viewHtml = "";
		for (String htmlLine : bodyLineList) {
			if (htmlLine.indexOf("<h1 style") > -1) {
				String title = htmlLine;
				title = title.replaceAll("<.+?>", "");
				viewHtml += title + RETURN_STRING;
			} else if (htmlLine.indexOf("<dl") > -1) {
				continue;
			} else if (htmlLine.indexOf("</dl>") > -1) {
				continue;
			} else {
				// <dt>1 ：<a href="mailto:sage"><b>仕様書無しさん</b></a>：2010/06/29(火)
				// 06:27:00
				// <dd> ３５歳以上のプログラマー　ラッキー７ <br> <br> １げっとーーー <br> <br> 前スレ <br>
				// ６　ttp://pc11.2ch.net/test/read.cgi/prog/1272786744/ <br>
				// ５　ttp://pc11.2ch.net/test/read.cgi/prog/1260988722/ <br>
				// ４　ttp://pc11.2ch.net/test/read.cgi/prog/1244718013/l50 <br>
				// ３　ttp://pc11.2ch.net/test/read.cgi/prog/1228027625/ <br>
				// ２　ttp://pc11.2ch.net/test/read.cgi/prog/1214471906/ <br>
				// １　ttp://pc11.2ch.net/test/read.cgi/prog/1180452814/ <br><br>
				htmlLine = htmlLine.replaceAll("<br>", RETURN_STRING);
				htmlLine = htmlLine.replaceAll("<dd>", "：");
				String[] titleAndBody = htmlLine.split("：");

				String sequence = titleAndBody[0];
				sequence = sequence.replaceAll("<.+?>", "");

				String name = titleAndBody[1];
				name = name.replaceAll("<.+?>", "");
				
				String date = titleAndBody[2];
				date = date.replaceAll("<.+?>", "");
				
				String contents = titleAndBody[3];
				contents = contents.replaceAll("<.+?>", "");
				
				viewHtml += String.format("%s - %s - %s%s%s", sequence, name, date, RETURN_STRING, contents);
			}

			viewHtml += "";
		}

		return viewHtml;
	}

	public List<A> createThreadList(String bbsUrl) {
		String subbackHtmlUrl = bbsUrl + "subback.html";

		String subDir = bbsUrl.replaceAll("http://", "");
		subDir = subDir.substring(subDir.indexOf("/"));

		String rootUrl = bbsUrl.replaceAll(subDir, "");

		subDir = subDir.replaceAll("/", "");

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

				String linkUrl = rootUrl + "/test/read.cgi/" + subDir + "/" + htmlLine.substring(htmlLine.indexOf("href=\"") + 6, htmlLine.indexOf(">") - 1);
				linkUrl = linkUrl.replaceAll("/l50", "");

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
