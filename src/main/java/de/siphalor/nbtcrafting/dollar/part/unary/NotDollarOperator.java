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

package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;

public class NotDollarOperator extends UnaryDollarOperator {
	private NotDollarOperator(DollarPart dollarPart) {
		super(dollarPart);
	}

	public static DollarPart of(DollarPart dollarPart) throws DollarDeserializationException {
		return shortCircuitConstant(new NotDollarOperator(dollarPart));
	}

	@Override
	public Object apply(Object value) throws DollarEvaluationException {
		return !DollarUtil.asBoolean(value);
	}
}
