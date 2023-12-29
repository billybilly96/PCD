package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SimpleDocument {

	private final String file;
	private final List<String> lines;

	public SimpleDocument(String filePath, List<String> totLines) {
		file = filePath;
		lines = totLines;
	}

	public String getFile() {
		return file;
	}

	public List<String> getLines() {
		return lines;
	}

	public static SimpleDocument fromFilePath(String inputFile) throws IOException {
		List<String> lines = new LinkedList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
			String line = reader.readLine();
			while (line != null) {
				lines.add(line);
				line = reader.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new SimpleDocument(inputFile, lines);
	}

}