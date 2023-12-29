package exerciseSeq;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.CountUtility;
import common.SimpleDocument;

public class Sequential {

	private static final int K = 10; // k = numero di parole più frequenti (da ricercare)
	private static final int N = 3; // n = numero minimo di caratteri delle parole cercate
	private static final int DEPTH = 10;

	public static void main(String[] args) throws IOException {
		Chrono c = new Chrono();
		c.start();

		Map<String, Map<String, Integer>> result = new HashMap<>();
		List<String> files = DirectoryUtility.getFilesFromDirectoryPath(args[0], DEPTH);
		for (String file : files) {
			Map<String, Integer> words = CountUtility.countWords(SimpleDocument.fromFilePath(file).getLines(), N);
			result.put(file, words);
			CountUtility.getMostFrequentWords(result, K);
		}

		c.stop();
		System.out.println(c.getTime());
	}

}
