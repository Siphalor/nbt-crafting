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

package de.siphalor.nbtcrafting.dollar.part.value;

import java.util.ArrayList;

import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class ListConstructDollarPart implements DollarPart {
	private final DollarPart[] parts;

	private ListConstructDollarPart(DollarPart[] parts) {
		this.parts = parts;
	}

	public static DollarPart of(DollarPart... parts) throws DollarDeserializationException {
		ListConstructDollarPart instance = new ListConstructDollarPart(parts);
		for (DollarPart part : parts) {
			if (!(part instanceof ConstantDollarPart)) {
				return instance;
			}
		}
		try {
			return ValueDollarPart.of(instance.evaluate(null));
		} catch (DollarEvaluationException e) {
			throw new DollarDeserializationException("Failed to short-circuit dollar list construct", e);
		}
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		ArrayList<Object> result = new ArrayList<>(parts.length);
		for (DollarPart part : parts) {
			result.add(part.evaluate(referenceResolver));
		}
		return result;
	}
}
