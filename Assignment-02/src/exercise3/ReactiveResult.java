package exercise3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReactiveResult {

	private Map<String, Map<String, Integer>> result;
	private Set<String> fileToRemove;
	private int pendingRequests;

	public ReactiveResult() {
		result = new HashMap<>();
		fileToRemove = new HashSet<>();
		pendingRequests = 0;
	}

	public synchronized Map<String, Map<String, Integer>> add(String file, Map<String, Integer> words) {
		if (fileToRemove.contains(file)) {
			fileToRemove.remove(file);
		} else {
			result.put(file, words);
		}
		return new HashMap<>(result);
	}

	public synchronized void remove(String file) {
		if (result.containsKey(file)) {
			result.remove(file);
		} else {
			fileToRemove.add(file);
		}
	}

	public synchronized void submitRequest() {
		pendingRequests++;
	}

	public synchronized boolean completeRequest() {
		pendingRequests--;
		return pendingRequests == 0;
	}
}
