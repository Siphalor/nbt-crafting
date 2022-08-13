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

package de.siphalor.nbtcrafting.dollar.part.binary;

import java.util.function.BiFunction;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class NumericBinaryDollarOperator extends BinaryDollarOperator {
	private final BiFunction<Number, Number, Number> function;

	private NumericBinaryDollarOperator(BiFunction<Number, Number, Number> function, DollarPart first, DollarPart second) {
		super(first, second);
		this.function = function;
	}

	public static DollarPart of(BiFunction<Number, Number, Number> function, DollarPart first, DollarPart second) throws DollarDeserializationException {
		return shortCircuitConstant(new NumericBinaryDollarOperator(function, first, second));
	}

	public static DollarPart differenceOf(DollarPart first, DollarPart second) throws DollarDeserializationException {
		return of(NumberUtil::difference, first, second);
	}

	public static DollarPart quotientOf(DollarPart first, DollarPart second) throws DollarDeserializationException {
		return of(NumberUtil::quotient, first, second);
	}

	@Override
	public Object apply(Object first, Object second) throws DollarEvaluationException {
		if (first == null) {
			first = 0;
		}
		if (second == null) {
			second = 0;
		}
		if (!(first instanceof Number)) {
			throw new DollarEvaluationException(DollarUtil.asString(first) + " is not a number");
		}
		if (!(second instanceof Number)) {
			throw new DollarEvaluationException(DollarUtil.asString(second) + " is not a number");
		}
		return function.apply((Number) first, (Number) second);
	}
}
