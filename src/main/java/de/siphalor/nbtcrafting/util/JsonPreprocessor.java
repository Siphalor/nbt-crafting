package de.siphalor.nbtcrafting.util;

import com.google.gson.JsonElement;

public class JsonPreprocessor {
	public static JsonElement process(JsonElement jsonElement) {
		boolean marked = false;
		/*if(jsonElement instanceof JsonObject) {
			for(Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
				if(!marked && entry.getKey().equals("$stringify") && entry.getValue().getAsBoolean()) {
					((JsonObject) jsonElement).remove(entry.getKey());
					marked = true;
				} else if(entry.getValue().isJsonArray() || entry.getValue().isJsonObject()) {
					((JsonObject) jsonElement).add(entry.getKey(), process(entry.getValue()));
				}
			}
		} else if(jsonElement instanceof JsonArray) {
			for(int i = 0; i < ((JsonArray) jsonElement).size(); i++) {
				JsonElement jsonElement1 = ((JsonArray) jsonElement).get(i);
				if(!marked && JsonHelper.isString(jsonElement1) && jsonElement1.getAsString().equals("$stringify")) {
					((JsonArray) jsonElement).remove(i);
					i--;
					marked = true;
				} else if(jsonElement1.isJsonArray() || jsonElement1.isJsonObject()) {
					((JsonArray) jsonElement).set(i, process(jsonElement1));
				}
			}
		}
		if(marked) {
			return new JsonPrimitive(jsonElement.toString());
		}*/
		return jsonElement;
	}
}
