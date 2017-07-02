package ru.lsv.admissionparser.parsers;

import java.net.URL;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

@Slf4j
public class UNNParser extends AbstractParser {

	@Override
	public String getDescription(String enrollee) {
		String content;
		try {
			content = FileUtils
					.readFileToString(
							downloadFileFromUrl(new URL(
									"https://enter.unn.ru/preport/stat/abit.php?id=281474976757599")),
							"UTF-8");
		} catch (Exception e) {
			log.error("Exception while parsing URL", e);
			return null;
		}
		String start = "<table id=\"showtable\" class=\"tablesorter\">";
		String end = "</table>";
		int startResp = content.indexOf(start);
		if (startResp > -1) {
			int endResp = content.indexOf(end, startResp);
			if (endResp > -1) {
				return buildResponse(content.substring(startResp,
						endResp + end.length()));
			} else {
				return buildBadResponse("Не могу найти финишную последовательность для получения информации!");
			}
		} else {
			return buildBadResponse("Не могу найти стартовую последовательность для получения информации!");
		}
	}

	private String header = "<h1>Университет Лобачевского</h1>";

	private String buildBadResponse(String string) {
		return header + "<table><td><tr>" + string + "</tr></td></table>";
	}

	private String buildResponse(String string) {
		return header
				+ string.replaceAll("href=\"index.php",
						"href=\"https://enter.unn.ru/preport/stat/index.php");
	}
}
