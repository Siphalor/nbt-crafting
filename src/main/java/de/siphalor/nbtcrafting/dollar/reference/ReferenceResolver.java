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

import javax.annotation.Nullable;

import de.siphalor.nbtcrafting.dollar.exception.UnresolvedDollarReferenceException;

@FunctionalInterface
public interface ReferenceResolver {
	/**
	 * Resolves a dollar reference to a value.
	 * @param reference the dollar reference to resolve
	 * @return the associated value (may be {@code null})
	 * @throws UnresolvedDollarReferenceException if the reference could not be resolved
	 */
	@Nullable
	Object resolve(String reference) throws UnresolvedDollarReferenceException;
}
