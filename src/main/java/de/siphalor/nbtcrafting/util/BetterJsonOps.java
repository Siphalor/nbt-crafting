/*
 * Copyright 2020 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

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
