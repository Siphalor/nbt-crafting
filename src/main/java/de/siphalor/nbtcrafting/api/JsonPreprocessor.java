package de.siphalor.nbtcrafting.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.util.NumberUtil;
import net.minecraft.util.JsonHelper;

import java.util.Map;

public class JsonPreprocessor {
	private static final String STRINGIFY_KEY = NbtCrafting.MOD_ID + ":stringify";
	private static final String ARRAY_TYPE_KEY = NbtCrafting.MOD_ID + ":array_type=";

	public static JsonElement process(JsonElement jsonElement) {
		boolean marked = false;
		if (jsonElement instanceof JsonObject) {
			for (Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
				if (entry.getValue().isJsonArray() || entry.getValue().isJsonObject()) {
					((JsonObject) jsonElement).add(entry.getKey(), process(entry.getValue()));
				}
			}
			marked = JsonHelper.getBoolean((JsonObject) jsonElement, "$stringify", false);
			if (marked) {
				NbtCrafting.logWarn("The use of $stringify is deprecated, please use nbtcrafting:stringify instead");
			} else {
				marked = JsonHelper.getBoolean((JsonObject) jsonElement, STRINGIFY_KEY, false);
			}
		} else if (jsonElement instanceof JsonArray) {
			int typeCast = -1;
			for (int i = 0; i < ((JsonArray) jsonElement).size(); i++) {
				JsonElement jsonArrayElement = ((JsonArray) jsonElement).get(i);
				if (!marked && jsonArrayElement instanceof JsonPrimitive && ((JsonPrimitive) jsonArrayElement).isString()) {
					String key = jsonArrayElement.getAsString();
					if (key.equals("$stringify")) {
						NbtCrafting.logWarn("The use of $stringify is deprecated, please use nbtcrafting:stringify instead");
						((JsonArray) jsonElement).remove(i);
						i--;
						marked = true;
					} else if (key.equals(STRINGIFY_KEY)) {
						((JsonArray) jsonElement).remove(i);
						i--;
						marked = true;
					} else if (key.startsWith(ARRAY_TYPE_KEY)) {
						((JsonArray) jsonElement).remove(i);
						i--;
						typeCast = NumberUtil.getType(key.substring(ARRAY_TYPE_KEY.length()));
					}
				} else if (jsonArrayElement instanceof JsonArray || jsonArrayElement instanceof JsonObject) {
					((JsonArray) jsonElement).set(i, process(jsonArrayElement));
				}
			}

			if (typeCast != -1) {
				for (int i = 0; i < ((JsonArray) jsonElement).size(); i++) {
					if (JsonHelper.isNumber(((JsonArray) jsonElement).get(i))) {
						((JsonArray) jsonElement).set(i, new JsonPrimitive(NumberUtil.cast(((JsonArray) jsonElement).get(i).getAsNumber(), typeCast)));
					}
				}
			}
		}
		if (marked) {
			return new JsonPrimitive(jsonElement.toString());
		}
		return jsonElement;
	}
}
