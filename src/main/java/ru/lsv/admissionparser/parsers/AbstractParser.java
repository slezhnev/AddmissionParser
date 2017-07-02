package ru.lsv.admissionparser.parsers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import ru.lsv.admissionparser.common.TempStorage;

@Slf4j
public abstract class AbstractParser {

	public abstract String getDescription(String enrollee);

	protected File downloadFileFromUrl(URL url) throws IOException {
		File tmp = TempStorage.getTempStorage().getTemp();
		try (InputStream urlConn = url.openConnection().getInputStream();
				FileOutputStream fOuf = new FileOutputStream(tmp)) {
			IOUtils.copy(urlConn, fOuf);
		}
		log.debug("Content downloaded from {} to {}", url, tmp.getName());
		return tmp;
	}

	protected String getDownloadLink(String content, String firstSearch,
			String downloadStartString, String downloadEndString) {
		int filPos = content.indexOf(firstSearch);
		if (filPos > -1) {
			int downloadPos = content.indexOf(downloadStartString, filPos);
			if (downloadPos > -1) {
				int downloadEndPos = content.indexOf(downloadEndString,
						downloadPos);
				if (downloadEndPos > -1) {
					return content.substring(
							downloadPos + downloadStartString.length(),
							downloadEndPos);
				}
			}
		}
		return null;
	}

}
