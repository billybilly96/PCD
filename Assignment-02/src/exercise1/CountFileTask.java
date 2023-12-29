package exercise1;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

import common.SectionDocument;

public class CountFileTask extends RecursiveTask<Map<String, Integer>> {

	private static final long serialVersionUID = 1L;
	private final int minChars;
	private final long taskChars;
	private final String file;
	private List<CountLinesTask> workerTasks;

	public CountFileTask(String inputFile, int mChars, long tChars) {
		super();
		minChars = mChars;
		taskChars = tChars;
		file = inputFile;
		workerTasks = new LinkedList<>();
	}

	@Override
	protected Map<String, Integer> compute() {
		Map<String, Integer> result = new HashMap<>();
		try {
			SectionDocument document = SectionDocument.fromFilePath(file, taskChars);
			for (List<String> lines : document.getLines()) {
				CountLinesTask lineTask = new CountLinesTask(lines, minChars);
				workerTasks.add(lineTask);
				lineTask.fork();
			}
			for (CountLinesTask lineTask : workerTasks) {
				Map<String, Integer> partialResult = lineTask.join();
				partialResult.forEach((word, occurrences) -> {
					result.merge(word, occurrences, (val1, val2) -> val1 + val2);
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		for (CountLinesTask lineTask : workerTasks) {
			lineTask.cancel(mayInterruptIfRunning);
		}
		return super.cancel(mayInterruptIfRunning);
	}

}
