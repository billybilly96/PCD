package exercise3;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import common.CountUtility;
import common.SimpleDocument;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ReactiveWordsFinder {

	private final Consumer<Map<String, Integer>> consumer;
	private final int frequentWords, minChars;
	private ReactiveResult reactiveResult;
	private PublishSubject<String> fileStream;

	public ReactiveWordsFinder(Consumer<Map<String, Integer>> operation, int words, int mChars) {
		consumer = operation;
		frequentWords = words;
		minChars = mChars;
	}

	public void initializeStream(List<String> inputFiles) {
		AtomicInteger assigner = new AtomicInteger(0);		
		reactiveResult = new ReactiveResult();
		fileStream = PublishSubject.create();		
		fileStream	
			.groupBy(item -> assigner.incrementAndGet() % Runtime.getRuntime().availableProcessors())
			.flatMap(group -> group
				.observeOn(Schedulers.io())
				.map(file -> SimpleDocument.fromFilePath(file))
	    	 	.observeOn(Schedulers.computation())
	    	 	.map(document -> {
	    	 		Map<String, Integer> words = CountUtility.countWords(document.getLines(), minChars);
	    	 		Map<String, Map<String, Integer>> partialResult = reactiveResult.add(document.getFile(), words);
	    	 		return CountUtility.getMostFrequentWords(partialResult, frequentWords);
	    	 	}))
			.subscribe(output -> {
				if (consumer != null) {
					consumer.accept(output);
				}
				if (reactiveResult.completeRequest()) {
					fileStream.onComplete();
				}
			}, res -> {
				if (!(res instanceof StoppedStreamException)) {
					res.printStackTrace();
				}
			}, () -> { if (consumer != null) consumer.accept(null); });
		for (String file : inputFiles) {
			addFile(file);
		}
	}

	public void stopComputation() {	
		fileStream.onError(new StoppedStreamException());
	}

	public void addFile(String file) {
		reactiveResult.submitRequest();
		fileStream.onNext(file);
	}

	public void removeFile(String file) {
		reactiveResult.remove(file);
	}

}