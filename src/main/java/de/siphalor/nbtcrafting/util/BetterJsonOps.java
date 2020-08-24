package de.siphalor.nbtcrafting.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

public class BetterJsonOps extends JsonOps {
	public static final BetterJsonOps INSTANCE = new BetterJsonOps(false);

	protected BetterJsonOps(boolean compressed) {
		super(compressed);
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, JsonElement input) {
		if (input.isJsonPrimitive() && input.getAsJsonPrimitive().isNumber()) {
			Number val = input.getAsNumber();
			int type = NumberUtil.getType(val);
			switch (type) {
				case NumberUtil.CHARACTER:
				case NumberUtil.BYTE:
					return outOps.createByte(val.byteValue());
				case NumberUtil.SHORT:
					return outOps.createShort(val.shortValue());
				case NumberUtil.INTEGER:
					return outOps.createInt(val.intValue());
				case NumberUtil.LONG:
					return outOps.createLong(val.longValue());
				case NumberUtil.FLOAT:
					return outOps.createFloat(val.floatValue());
				case NumberUtil.DOUBlE:
					return outOps.createDouble(val.doubleValue());
			}
		}
		return super.convertTo(outOps, input);
	}
}
