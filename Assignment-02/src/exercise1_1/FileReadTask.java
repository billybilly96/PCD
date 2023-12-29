package exercise1_1;

import java.util.concurrent.Callable;

import common.SectionDocument;

public class FileReadTask implements Callable<SectionDocument> {

	private final String inputFile;
	private final long taskChars;

	public FileReadTask(String file, long tChars) {
		inputFile = file;
		taskChars = tChars;
	}

	@Override
	public SectionDocument call() throws Exception {
		return SectionDocument.fromFilePath(inputFile, taskChars);
	}

}