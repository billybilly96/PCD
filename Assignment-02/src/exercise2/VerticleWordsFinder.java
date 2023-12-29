package exercise2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.CountUtility;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;

public class VerticleWordsFinder extends AbstractVerticle {

	private final DeliveryOptions sendMapOptions;
	private final List<String> initialFiles;
	private final int frequentWords, minChars;
	private Map<String, Future<Buffer>> readFutures;
	private Map<String, Future<Map<String, Integer>>> searchFutures;
	private Map<String, Map<String, Integer>> result;

	public VerticleWordsFinder(List<String> files, int words, int mChars) {
		sendMapOptions = new DeliveryOptions().setCodecName(MapMessageCodec.class.getSimpleName());
		initialFiles = files;
		frequentWords = words;
		minChars = mChars;
		readFutures = new HashMap<>();
		searchFutures = new HashMap<>();
		result = new HashMap<>();
	}

	@Override
	public void start() throws Exception {
		vertx.eventBus().consumer(BusChannels.ADD_FILE, message -> {
			computeFile(message.body().toString());
		});
		vertx.eventBus().consumer(BusChannels.REMOVE_FILE, message -> {
			removeFile(message.body().toString());
		});
		for (String file : initialFiles) {
			computeFile(file);
		}
	}

	private void computeFile(String file) {
		if (!readFutures.containsKey(file) && !searchFutures.containsKey(file)) {
			Future<Buffer> readFut = Future.future();
			Future<Map<String, Integer>> searchFut = readFut.compose(buffer -> {
				Future<Map<String, Integer>> searchFuture = Future.future();
				vertx.executeBlocking(future -> {
					future.complete(CountUtility.countWords(buffer.toString(), minChars));
				}, false, searchFuture);
				return searchFuture;
			});
			searchFut.compose(foundWords -> {
				result.put(file, foundWords);
				updateOutput(new HashMap<>(result));
			}, Future.future().setHandler(res -> {
				if (!(res.cause() instanceof RemovedException)) {
					res.cause().printStackTrace();
				}
			}));
			readFutures.put(file, readFut);
			searchFutures.put(file, searchFut);
			vertx.fileSystem().readFile(file, readFut);
		}
	}

	private void removeFile(String file) {
		if (readFutures.containsKey(file) && searchFutures.containsKey(file)) {
			Future<Buffer> readFut = readFutures.remove(file);
			Future<Map<String, Integer>> searchFut = searchFutures.remove(file);
			if (!readFut.tryFail(new RemovedException())) {
				if (!searchFut.tryFail(new RemovedException())) {
					result.remove(file);
					updateOutput(new HashMap<>(result));
				}
			}
		}
	}

	private void updateOutput(Map<String, Map<String, Integer>> resultCopy) {
		vertx.executeBlocking(future -> {
			future.complete(CountUtility.getMostFrequentWords(resultCopy, frequentWords));
		}, true, (AsyncResult<Map<String, Integer>> res) -> {
			vertx.eventBus().send(BusChannels.SHOW_OUTPUT, res.result(), sendMapOptions);
		});
	}

}