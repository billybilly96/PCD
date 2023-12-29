package exerciseSeq;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class DirectoryUtility {

	public static List<String> getFilesFromDirectory(File directory, int depth) {
		List<String> result = new LinkedList<>();
		if (directory.isDirectory() && depth > 0) {
			File[] fList = directory.listFiles();
			for (File file : fList) {
				if (file.isDirectory()) {
					result.addAll(getFilesFromDirectory(file, depth - 1));
				} else if (file.isFile() && file.getName().endsWith("txt")) {
					result.add(file.getAbsolutePath());
				}
			}
		}
		return result;
	}

	public static List<String> getFilesFromDirectoryPath(String path, int depth) {
		File directory = new File(path);
		return getFilesFromDirectory(directory, depth);
	}

}
