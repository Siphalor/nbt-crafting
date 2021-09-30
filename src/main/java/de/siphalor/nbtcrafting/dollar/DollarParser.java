/*
 * Copyright 2020-2021 Siphalor
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

package de.siphalor.nbtcrafting.dollar;

import java.util.*;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.ArrayUtils;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.nbt.MergeMode;
import de.siphalor.nbtcrafting.api.nbt.NbtIterator;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.operator.*;
import de.siphalor.nbtcrafting.dollar.part.unary.*;
import de.siphalor.nbtcrafting.dollar.type.CountDollar;
import de.siphalor.nbtcrafting.dollar.type.MergeDollar;
import de.siphalor.nbtcrafting.dollar.type.SimpleDollar;

public final class DollarParser {
	private static final Collection<DollarPart.UnaryDeserializer> UNARY_DESERIALIZERS = ImmutableList.of(
			new CombinationDollarPartDeserializer(),
			new InverseDollarOperator.Deserializer(),
			new NumberDollarPartDeserializer(),
			new ReferenceDollarPart.Deserializer(),
			new StringDollarPartDeserializer()
	);
	private static final List<Collection<DollarPart.Deserializer>> DESERIALIZERS = ImmutableList.of(
			ImmutableList.of(
					new CastDollarOperator.Deserializer(),
					new ChildDollarOperator.DotDeserializer(),
					new ChildDollarOperator.BracketDeserializer()
			),
			ImmutableList.of(
					new ProductDollarOperator.Deserializer(),
					new QuotientDollarOperator.Deserializer()
			),
			ImmutableList.of(
					new SumDollarOperator.Deserializer(),
					new DifferenceDollarOperator.Deserializer()
			),
			ImmutableList.of(
					new ConditionDollarOperator.Deserializer()
			)
	);
	private final Stack<Integer> stopStack = new Stack<>();
	private final String string;
	private final int stringLength;
	private int currentIndex;

	public DollarParser(String string) {
		this.string = string;
		this.stringLength = string.length();
		this.currentIndex = -1;
	}

	public int eat() {
		if (currentIndex++ >= stringLength)
			return -1;
		return string.codePointAt(currentIndex);
	}

	public void skip() {
		currentIndex++;
	}

	public int peek() {
		if (currentIndex + 1 >= stringLength)
			return -1;
		return string.codePointAt(currentIndex + 1);
	}

	public static Dollar[] extractDollars(CompoundTag compoundTag, boolean remove) {
		LinkedList<Dollar> dollars = new LinkedList<>();
		NbtIterator.iterateTags(compoundTag, (path, key, tag) -> {
			if (key.equals("$")) {
				if (NbtUtil.isList(tag)) {
					AbstractListTag<Tag> list = NbtUtil.asListTag(tag);
					for (Tag t : list) {
						parseMerge(t, path).ifPresent(dollars::addFirst);
					}
				} else {
					parseMerge(tag, path).ifPresent(dollars::addFirst);
				}
				return remove;
			}
			if (NbtUtil.isString(tag) && !tag.asString().isEmpty()) {
				if (tag.asString().charAt(0) == '$') {
					if (key.equals(NbtCrafting.MOD_ID + ":count")) {
						parse(tag.asString().substring(1)).ifPresent(exp -> dollars.addFirst(new CountDollar(exp)));
					} else {
						parse(tag.asString().substring(1)).ifPresent(exp -> dollars.addFirst(new SimpleDollar(exp, path + key)));
					}
					return remove;
				}
			}
			return false;
		});

		dollars.sort((a, b) -> {
			if (a instanceof MergeDollar)
				return b instanceof MergeDollar ? 0 : -1;
			return 1;
		});
		return dollars.toArray(new Dollar[0]);
	}

	private static Optional<MergeDollar> parseMerge(Tag tag, String path) {
		if (NbtUtil.isString(tag)) {
			String val = tag.asString();
			if (val.charAt(0) == '$') {
				val = val.substring(1);
			}
			return parse(val).map(exp -> new MergeDollar(exp, path, Collections.emptyList()));
		} else if (NbtUtil.isCompound(tag)) {
			CompoundTag compound = NbtUtil.asCompoundTag(tag);
			if (compound.contains("value", 8)) {
				Collection<Pair<Pattern, MergeMode>> mergeModes = new LinkedList<>();
				if (compound.contains("paths", 10)) {
					CompoundTag paths = compound.getCompound("paths");
					for (String p : paths.getKeys()) {
						try {
							//noinspection ConstantConditions
							MergeMode mergeMode = MergeMode.valueOf(paths.get(p).asString().toUpperCase(Locale.ENGLISH));
							if (p.startsWith("/") && p.endsWith("/")) {
								mergeModes.add(Pair.of(Pattern.compile(Pattern.quote(path) + "\\.?" + p.substring(1, p.length() - 1)), mergeMode));
							} else {
								mergeModes.add(Pair.of(Pattern.compile(Pattern.quote(path) + "\\.?" + Pattern.quote(p)), mergeMode));
							}
						} catch (Exception e) {
							NbtCrafting.logError("Unable to deduce dollar merge mode from tag: " + paths.get(p));
						}
					}
				}
				return parse(compound.getString("value")).map(exp -> new MergeDollar(exp, path, mergeModes));
			} else {
				NbtCrafting.logError("The value field is required on dollar merge objects. Errored on " + tag.asString());
			}
		} else {
			NbtCrafting.logError("Found invalid dollar merge tag: " + tag.asString());
		}
		return Optional.empty();
	}

	public static Optional<DollarPart> parse(String string) {
		return Optional.ofNullable(new DollarParser(string).parse());
	}

	public DollarPart parse() {
		try {
			return parse(DESERIALIZERS.size());
		} catch (DollarException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void pushStopStack(int stop) {
		stopStack.push(stop);
	}

	public void popStopStack() {
		stopStack.pop();
	}

	public DollarPart parse(int maxPriority) throws DollarDeserializationException {
		int peek;

		DollarPart dollarPart = parseUnary();
		int priority;

		parse:
		while (true) {
			while (Character.isWhitespace(peek = peek())) {
				skip();
			}
			if (peek == -1)
				return dollarPart;
			if (!stopStack.isEmpty() && stopStack.lastElement() == peek) {
				return dollarPart;
			}

			priority = 0;
			for (Collection<DollarPart.Deserializer> deserializers : DESERIALIZERS) {
				if (++priority > maxPriority)
					break;
				for (DollarPart.Deserializer deserializer : deserializers) {
					if (deserializer.matches(peek, this)) {
						dollarPart = deserializer.parse(this, dollarPart, priority);
						continue parse;
					}
				}
			}
			if (maxPriority < DESERIALIZERS.size())
				return dollarPart;
			throw new DollarDeserializationException("Unable to resolve token in dollar expression: \"" + String.valueOf(Character.toChars(peek)) + "\"");
		}
	}

	public DollarPart parseUnary() throws DollarDeserializationException {
		int peek;

		while (Character.isWhitespace(peek = peek())) {
			skip();
		}
		if (peek == -1)
			return null;

		for (DollarPart.UnaryDeserializer deserializer : UNARY_DESERIALIZERS) {
			if (deserializer.matches(peek, this)) {
				return deserializer.parse(this);
			}
		}
		return null;
	}

	public DollarPart parseTo(int stop) {
		pushStopStack(stop);
		DollarPart dollarPart = parse();
		popStopStack();
		skip();
		return dollarPart;
	}

	public String readTo(int... stops) {
		int character;
		boolean escaped = false;
		StringBuilder stringBuilder = new StringBuilder();
		while (!ArrayUtils.contains(stops, character = eat())) {
			if (character == -1)
				return null;
			if (escaped) {
				stringBuilder.append(Character.toChars(character));
				escaped = false;
			} else if (character == '\\') {
				escaped = true;
			} else {
				stringBuilder.append(Character.toChars(character));
			}
		}
		return stringBuilder.toString();
	}

	// Testing only
	public static void main(String[] args) {
		parse("a + b").flatMap(dollarPart -> {
			try {
				ListTag a = new ListTag();
				a.add(new ListTag());
				ListTag b = new ListTag();
				b.add(new ListTag());
				return Optional.of(dollarPart.evaluate(ImmutableMap.of(
						"a", a,
						"b", b
				)));
			} catch (DollarException e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}).ifPresent(System.out::println);
	}
}
