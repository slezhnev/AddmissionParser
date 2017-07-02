package ru.lsv.admissionparser;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import lombok.extern.slf4j.Slf4j;
import ru.lsv.admissionparser.common.TempStorage;
import ru.lsv.admissionparser.parsers.HSEParser;
import ru.lsv.admissionparser.parsers.LUNNParser;
import ru.lsv.admissionparser.parsers.UNNParser;

@Slf4j
public class MainClass {

	public static void main(String[] args) throws IOException {
		try {
			String enrollee = "Лежнева Маргарита  Сергеевна";
			log.debug("Starting...");
			log.debug("UNN processing");
			String unnRes = new UNNParser().getDescription(enrollee);
			log.debug("HSE processing");
			String hseRes = new HSEParser().getDescription(enrollee);
			log.debug("LUNN processing");
			String lunnRes = new LUNNParser().getDescription(enrollee);
			try (Writer fout = new OutputStreamWriter(new FileOutputStream(
					"./result.html"), "UTF-8")) {
				StringBuilder res = new StringBuilder("<html><body>")
						.append("<title>Результаты анализа</title>")
						.append("<meta charset=\"utf-8\">").append(unnRes)
						.append("<br/><br/><br/>").append(hseRes)
						.append("<br/><br/><br/>").append(lunnRes)
						.append("<br/><br/><br/>");
				fout.write(res.toString());
			}
			log.debug("Finished! Result stored to \"result.html\"");
		} finally {
			TempStorage.getTempStorage().clean();
		}
	}
}
