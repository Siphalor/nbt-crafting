package de.siphalor.nbtcrafting.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.JsonHelper;

import java.util.Map;

public class JsonPreprocessor {
	public static JsonElement process(JsonElement jsonElement) {
		boolean marked = false;
		if(jsonElement instanceof JsonObject) {
			for(Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
				if(entry.getValue().isJsonArray() || entry.getValue().isJsonObject()) {
					((JsonObject) jsonElement).add(entry.getKey(), process(entry.getValue()));
				}
			}
			marked = JsonHelper.getBoolean((JsonObject) jsonElement, "$stringify", false);
		} else if(jsonElement instanceof JsonArray) {
			for(int i = 0; i < ((JsonArray) jsonElement).size(); i++) {
				JsonElement jsonElement1 = ((JsonArray) jsonElement).get(i);
				if(!marked && jsonElement1 instanceof JsonPrimitive && ((JsonPrimitive) jsonElement1).isString() && jsonElement1.getAsString().equals("$stringify")) {
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
		}
		return jsonElement;
	}
}
