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

package de.siphalor.nbtcrafting.dollar.part.ternary;

import java.util.Map;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ConstantDollarPart;

public class ConditionDollarOperator implements DollarPart {
	private final DollarPart condition;
	private final DollarPart thenPart;
	private final DollarPart elsePart;

	private ConditionDollarOperator(DollarPart condition, DollarPart thenPart, DollarPart elsePart) {
		this.condition = condition;
		this.thenPart = thenPart;
		this.elsePart = elsePart;
	}

	public static DollarPart of(DollarPart condition, DollarPart thenPart, DollarPart elsePart) {
		if (condition instanceof ConstantDollarPart) {
			if (DollarUtil.asBoolean(((ConstantDollarPart) condition).getConstantValue())) {
				return thenPart;
			} else {
				return elsePart;
			}
		}
		return new ConditionDollarOperator(condition, thenPart, elsePart);
	}

	@Override
	public Object evaluate(Map<String, Object> reference) throws DollarEvaluationException {
		if (DollarUtil.asBoolean(condition.evaluate(reference))) {
			return thenPart.evaluate(reference);
		}
		return elsePart.evaluate(reference);
	}
}
