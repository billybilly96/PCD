package exercise2;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

@SuppressWarnings("rawtypes")
public class MapMessageCodec implements MessageCodec<Map, Map> {

	private static final String MAP_KEY = "map";

	@Override
	public void encodeToWire(Buffer buffer, Map map) {
		String jsonToString = new JsonObject().put(MAP_KEY, map).encode();
		int length = jsonToString.getBytes().length;
		buffer.appendInt(length);
		buffer.appendString(jsonToString);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map decodeFromWire(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
		String jsonString = buffer.getString(pos, pos + length);
		JsonObject jsonContent = new JsonObject(jsonString);
		return new HashMap<>((Map) jsonContent.getValue(MAP_KEY));
	}

	@Override
	public Map transform(Map map) {
		return map;
	}

	@Override
	public String name() {
		return getClass().getSimpleName();
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

}
