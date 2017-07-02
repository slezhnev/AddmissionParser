package ru.lsv.admissionparser.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

@Slf4j
public class LUNNParser extends AbstractParser {

	@Override
	public String getDescription(String enrollee) {
		String content;
		try {
			content = FileUtils.readFileToString(downloadFileFromUrl(new URL(
					"http://www.lunn.ru/page/informaciya-o-hode-priema")),
					"UTF-8");
		} catch (Exception e) {
			log.error("Cannot download content of list description");
			return null;
		}
		String downloadLink = getDownloadLink(
				content,
				"Прикреплённые документы",
				"http://www.lunn.ru/sites/default/files/media/abiturientu/priem_2017/priem/spisok_abiturientov_bakalavriat_specialitet",
				"\" type=\"");
		if (downloadLink == null) {
			return buildBadResponse("Не могу найти ссылку на скачивание DOC-файла!");
		}
		downloadLink = "http://www.lunn.ru/sites/default/files/media/abiturientu/priem_2017/priem/spisok_abiturientov_bakalavriat_specialitet"
				+ downloadLink;
		File doc;
		try {
			doc = downloadFileFromUrl(new URL(downloadLink));
		} catch (Exception e) {
			log.error("Cannot download {}", downloadLink, e);
			return null;
		}
		try (FileInputStream fis = new FileInputStream(doc)) {
			HWPFDocument document = new HWPFDocument(fis);
			Boolean isInTable = false;
			Range range = document.getRange();
			String currentSpeciality = null;
			int currCol = 0;
			String numPP = "";
			StringBuilder res = new StringBuilder(header)
					.append("<a href=\"")
					.append(downloadLink)
					.append("\">Документ с сайта</a><br/>")
					.append("<table><tbody><thead>")
					.append("<tr><th>Специальность</th><th>Место</th></thead><tbody>");
			for (int numPar = 0; numPar < range.numParagraphs(); numPar++) {
				Paragraph par = range.getParagraph(numPar);
				if (!isInTable && par.isInTable()) {
					isInTable = true;
					currentSpeciality = par.text().replace((char) 11, ' ')
							.replace((char) 7, ' ').trim();
					currCol = 0;
					numPP = "";
				} else if (!par.isInTable() && isInTable
						&& par.text().equals("\r")) {
					isInTable = false;
				}
				if (isInTable) {
					if (par.isTableRowEnd()) {
						// This is LAST paragraph in row
						currCol = 0;
					} else {
						if (currCol == 0) {
							numPP = par.text().replace((char) 11, ' ')
									.replace((char) 7, ' ').replace('.', ' ')
									.trim();
						} else if (currCol == 1
								&& !StringUtils.isEmpty(par.text())) {
							if (enrollee.equals(par.text().trim())) {
								res.append("<tr><td>")
										.append(currentSpeciality)
										.append("</td><td>").append(numPP)
										.append("</td></tr>");
							}
						}
						currCol++;
					}
				}
			}
			res.append("</tbody></table>");
			//
			document.close();
			//
			return res.toString();
		} catch (IOException e) {
			log.error("Cannot parse downloaded document!");
			return null;
		}
	}

	private String header = "<h1>Лингвистический</h1>";

	private String buildBadResponse(String string) {
		return header + "<table><tr><td>" + string + "</td></tr></table>";
	}

}
