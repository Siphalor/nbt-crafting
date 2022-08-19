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

package de.siphalor.nbtcrafting.dollar.reference;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.exception.UnresolvedDollarReferenceException;

public class MapBackedReferenceResolver implements ReferenceResolver {
	private final Map<String, Object> references;

	public MapBackedReferenceResolver(Map<String, Object> references) {
		this.references = references;
	}

	@Nullable
	@Override
	public Object resolve(String reference) throws UnresolvedDollarReferenceException {
		Object value = references.get(reference);
		if (value == null && !references.containsKey(reference)) {
			throw new UnresolvedDollarReferenceException(reference);
		}
		return value;
	}
}
