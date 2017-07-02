package ru.lsv.admissionparser.common;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

public class TempStorage {

	private static TempStorage tempStorage = null;

	public static synchronized TempStorage getTempStorage() throws IOException {
		if (tempStorage == null) {
			tempStorage = new TempStorage();
		}
		return tempStorage;
	}

	private File tempDir;

	private TempStorage() throws IOException {
		tempDir = new File(".tmp");
		if (tempDir.exists()) {
			if (tempDir.isDirectory()) {
				FileUtils.cleanDirectory(tempDir);
			} else {
				throw new IOException("Cannot create temp dicrectory");
			}
		} else {
			tempDir.mkdirs();
		}
	}

	public File getTemp() {
		return new File(tempDir, UUID.randomUUID().toString());
	}

	public File getTemp(String ext) {
		return new File(tempDir, UUID.randomUUID().toString() + "." + ext);
	}

	public void clean() throws IOException {
		FileUtils.deleteDirectory(getTempStorage().tempDir);
	}

}
