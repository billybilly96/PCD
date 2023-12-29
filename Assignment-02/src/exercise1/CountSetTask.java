package exercise1;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import common.CountUtility;

public class CountSetTask extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private final Consumer<Map<String, Integer>> consumer;
	private final int frequentWords, minChars;
	private final long taskChars;
	private Map<String, Map<String, Integer>> result;
	private Map<String, CountFileTask> workerTasks;
	private AtomicBoolean computing;

	public CountSetTask(Consumer<Map<String, Integer>> operation, List<String> initialFiles, int words, int mChars, long tChars) {
		super();
		consumer = operation;
		frequentWords = words;
		minChars = mChars;
		taskChars = tChars;
		computing = new AtomicBoolean(true);
		result = new ConcurrentHashMap<>();
		workerTasks = new ConcurrentHashMap<>();
		for (String file : initialFiles) {
			workerTasks.put(file, new CountFileTask(file, minChars, taskChars));
		}
	}

	@Override
	protected void compute() {
		for (CountFileTask fileTask : workerTasks.values()) {
			fileTask.fork();
		}
		try {
			String file = workerTasks.keySet().iterator().next();
			while (computing.get()) {
				CountFileTask fileTask = workerTasks.get(file);
				if (fileTask != null) {
					Map<String, Integer> fileResult = fileTask.join();
					result.put(file, fileResult);
					Map<String, Integer> output = CountUtility.getMostFrequentWords(result, frequentWords);
					if (consumer != null) {
						consumer.accept(output);
					}
				}
				workerTasks.remove(file);
				file = workerTasks.keySet().iterator().next();
			}
		} catch (NoSuchElementException e) {
			computing.set(false);
			if (consumer != null) {
				consumer.accept(null);
			}
		}
	}

	public void addFile(String file) {
		if (!isDone() && !(workerTasks.containsKey(file) || result.containsKey(file))) {
			CountFileTask fileTask = new CountFileTask(file, minChars, taskChars);
			workerTasks.put(file, fileTask);
			fileTask.fork();
		}
	}

	public void removeFile(String file) {
		if (!isDone() && (workerTasks.containsKey(file) || result.containsKey(file))) {
			CountFileTask task = workerTasks.remove(file);
			result.remove(file);
			if (task != null) {
				task.cancel(true);
			}
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		computing.set(false);
		for (CountFileTask fileTask : workerTasks.values()) {
			fileTask.cancel(mayInterruptIfRunning);
		}
		return super.cancel(mayInterruptIfRunning);
	}

}