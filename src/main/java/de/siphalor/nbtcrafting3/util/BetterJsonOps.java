/*
 * Copyright 2020-2022 Siphalor
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

package de.siphalor.nbtcrafting3.util;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.JsonOps;
import com.mojang.datafixers.types.Type;

public class BetterJsonOps extends JsonOps {
	public static final BetterJsonOps INSTANCE = new BetterJsonOps();

	@Override
	public Type<?> getType(JsonElement input) {
		if (input.isJsonPrimitive() && input.getAsJsonPrimitive().isNumber()) {
			Number val = input.getAsNumber();
			int type = NumberUtil.getType(val);
			switch (type) {
				case NumberUtil.CHARACTER:
				case NumberUtil.BYTE:
					return DSL.byteType();
				case NumberUtil.SHORT:
					return DSL.shortType();
				case NumberUtil.INTEGER:
					return DSL.intType();
				case NumberUtil.LONG:
					return DSL.longType();
				case NumberUtil.FLOAT:
					return DSL.floatType();
				case NumberUtil.DOUBlE:
					return DSL.doubleType();
			}
		}
		return super.getType(input);
	}
}
