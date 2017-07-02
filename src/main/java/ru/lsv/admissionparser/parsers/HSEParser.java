package ru.lsv.admissionparser.parsers;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Slf4j
public class HSEParser extends AbstractParser {

	@Override
	public String getDescription(String enrollee) {
		String content;
		try {
			content = FileUtils.readFileToString(downloadFileFromUrl(new URL(
					"https://nnov.hse.ru/bacnn/bazaabitur2017")), "UTF-8");
		} catch (Exception e) {
			log.error("Cannot download content of list description");
			return null;
		}
		String downloadLink = getDownloadLink(content, "Филология",
				"<a class=\"link fileRef\" href=\"", "\">Фил.xlsx");
		if (downloadLink == null) {
			return buildBadResponse("Не могу определить линк для скачивания Excel файла!");
		}
		downloadLink = "https://nnov.hse.ru" + downloadLink;
		log.debug("Download link = {}", downloadLink);
		try (FileInputStream fis = new FileInputStream(
				downloadFileFromUrl(new URL(downloadLink)))) {
			Workbook wb = new XSSFWorkbook(fis);
			Sheet sheet = wb.getSheet("НН Фил");
			int rows = sheet.getLastRowNum();
			List<DataStorage> budget = new LinkedList<>();
			List<DataStorage> commercial = new LinkedList<>();
			for (int i = sheet.getFirstRowNum() + 5; i < rows; i++) {
				Row row = sheet.getRow(i);
				String name = row.getCell(2).getStringCellValue();
				double score = row.getCell(14).getNumericCellValue();
				String place = row.getCell(15).getStringCellValue();
				if (place.indexOf("Б") > -1) {
					budget.add(new DataStorage(name, Math.round(score)));
				}
				if (place.indexOf("К") > -1) {
					commercial.add(new DataStorage(name, Math.round(score)));
				}
			}
			Comparator<DataStorage> comparator = new Comparator<DataStorage>() {

				@Override
				public int compare(DataStorage o1, DataStorage o2) {
					return Long.compare(o1.score, o2.score);
				}

			};
			Collections.sort(budget, comparator.reversed());
			Collections.sort(commercial, comparator.reversed());
			int budgetPos = 0;
			int commercialPos = 0;
			for (DataStorage val : budget) {
				budgetPos++;
				if (enrollee.equals(val.name)) {
					break;
				}
			}
			for (DataStorage val : commercial) {
				commercialPos++;
				if (enrollee.equals(val.name)) {
					break;
				}
			}
			wb.close();
			return buildResponse(downloadLink, budgetPos, commercialPos);
		} catch (Exception e) {
			log.error("Cannot download entrants list from {}", downloadLink, e);
			return null;
		}
	}

	private String buildResponse(String downloadLink, int budgetPos,
			int commercialPos) {
		StringBuilder res = new StringBuilder(header).append("<a href=\"")
				.append(downloadLink).append("\">Документ с сайта</a><br/>")
				.append("<table><tbody><thead>")
				.append("<tr><th>Бюджет</th><th>Комм.</th></thead>")
				.append("<tr><td>").append(budgetPos).append("</td><td>")
				.append(commercialPos).append("</td></tr></tbody></table>");
		return res.toString();
	}

	private String header = "<h1>НИУ ВШЭ</h1>";

	private String buildBadResponse(String string) {
		return header + "<table><tr><td>" + string + "</td></tr></table>";
	}

	@Data
	private static class DataStorage {
		private final String name;
		private final long score;
	}

}
