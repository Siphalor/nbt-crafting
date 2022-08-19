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

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarBinding;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class AssignmentDollarPart implements DollarPart {
	private final DollarBinding binding;
	private final DollarPart value;

	private AssignmentDollarPart(DollarBinding binding, DollarPart value) {
		this.binding = binding;
		this.value = value;
	}

	public static DollarPart of(DollarBinding binding, DollarPart value) {
		return new AssignmentDollarPart(binding, value);
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		Object value = this.value.evaluate(referenceResolver);
		binding.assign(referenceResolver, value);
		return value;
	}
}
