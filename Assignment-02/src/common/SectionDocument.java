package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SectionDocument {

	private final String file;
	private final List<List<String>> lines;

	public SectionDocument(String filePath, List<List<String>> totLines) {
		file = filePath;
		lines = totLines;
	}

	public String getFile() {
		return file;
	}

	public List<List<String>> getLines() {
		return lines;
	}

	public static SectionDocument fromFilePath(String inputFile, long taskChars) throws IOException {
		long charsCount = 0;
		List<String> readLines = new LinkedList<>();
		List<List<String>> totLines = new LinkedList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line = reader.readLine();
			while (line != null) {
				charsCount += line.length();
				readLines.add(line);
				if (charsCount >= taskChars) {
					totLines.add(readLines);
					charsCount = 0;
					readLines = new LinkedList<>();
				}
				line = reader.readLine();
			}
			if (!readLines.isEmpty()) {
				totLines.add(readLines);
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new SectionDocument(inputFile, totLines);
	}
}