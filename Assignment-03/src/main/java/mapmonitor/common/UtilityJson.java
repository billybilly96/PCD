package mapmonitor.common;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import assignment.utility.Pair;

public class UtilityJson {

	@SuppressWarnings("unchecked")
	public static Map<Integer, Set<Pair<String, String>>> getGuardiansFromJson(String guardiansConfigFile) throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		FileReader guardiansReader = new FileReader(guardiansConfigFile);
		Object objGuardianConfiguration = jsonParser.parse(guardiansReader);
		JSONArray guardianJsonArray = (JSONArray) objGuardianConfiguration;
		Map<Integer, Set<Pair<String, String>>> guardians = new HashMap<>();
		guardianJsonArray.forEach(remoteObj -> parseGuardianJsonObject((JSONObject) remoteObj, guardians));
		return guardians;
	}

	private static void parseGuardianJsonObject(JSONObject obj, Map<Integer, Set<Pair<String, String>>> guardians) {
		int patch = Integer.parseInt(((String) obj.get("patch")));
		JSONArray stubGuardians = (JSONArray) obj.get("stub_guardians");
		guardians.put(patch, getAllAddressesFromArray(stubGuardians));
	}

	public static Set<Pair<String, String>> getSensorsFromJson(String sensorsConfigFile) throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		FileReader sensorsReader = new FileReader(sensorsConfigFile);
		Object objSensorConfiguration = jsonParser.parse(sensorsReader);
		Set<Pair<String, String>> sensors = getAllAddressesFromArray((JSONArray) objSensorConfiguration);
		return sensors;
	}

	@SuppressWarnings("unchecked")
	private static Set<Pair<String, String>> getAllAddressesFromArray(JSONArray array) {
		Set<Pair<String, String>> pairSet = new HashSet<>();
		array.forEach(obj -> pairSet.add(getHostAndStub((JSONObject) obj)));
		return pairSet;
	}

	private static Pair<String, String> getHostAndStub(JSONObject obj) {
		return new Pair<String, String>((String) obj.get("host"), (String) obj.get("stub"));
	}

}
