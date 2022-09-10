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

package de.siphalor.nbtcrafting3.dollar;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.api.nbt.MergeContext;
import de.siphalor.nbtcrafting3.api.nbt.NbtIterator;
import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting3.dollar.antlr.DollarScriptLexer;
import de.siphalor.nbtcrafting3.dollar.antlr.DollarScriptParser;
import de.siphalor.nbtcrafting3.dollar.exception.UnresolvedDollarReferenceException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.dollar.type.CountDollar;
import de.siphalor.nbtcrafting3.dollar.type.MergeDollar;
import de.siphalor.nbtcrafting3.dollar.type.SimpleDollar;

public final class DollarExtractor {
	private DollarExtractor() {}

	public static Dollar[] extractDollars(CompoundTag compoundTag, boolean remove) {
		LinkedList<Dollar> dollars = new LinkedList<>();
		NbtIterator.iterateTags(compoundTag, (path, key, tag) -> {
			if (key.equals("$")) {
				if (NbtUtil.isList(tag)) {
					AbstractListTag<Tag> list = NbtUtil.asListTag(tag);
					for (Tag t : list) {
						parseMerge(t, path).ifPresent(dollars::addLast);
					}
				} else {
					parseMerge(tag, path).ifPresent(dollars::addLast);
				}
				return remove ? NbtIterator.Action.REMOVE : NbtIterator.Action.SKIP;
			}
			if (NbtUtil.isString(tag) && !tag.asString().isEmpty()) {
				if (tag.asString().charAt(0) == '$') {
					DollarPart expression = parse(tag.asString().substring(1));
					if (expression != null) {
						if (key.equals(NbtCrafting.MOD_ID + ":count")) {
							dollars.addFirst(new CountDollar(expression));
						} else {
							dollars.addFirst(new SimpleDollar(expression, path + key));
						}
					}
					return remove ? NbtIterator.Action.REMOVE : NbtIterator.Action.SKIP;
				}
			}
			return NbtIterator.Action.RECURSE;
		});

		return dollars.toArray(new Dollar[0]);
	}

	private static Optional<MergeDollar> parseMerge(Tag tag, String path) {
		if (NbtUtil.isString(tag)) {
			String val = tag.asString();
			if (val.charAt(0) == '$') {
				val = val.substring(1);
			}
			DollarPart expression = parse(val);
			if (expression == null) {
				return Optional.empty();
			}
			return Optional.of(new MergeDollar(expression, path, MergeContext.EMPTY));
		} else if (NbtUtil.isCompound(tag)) {
			CompoundTag compound = NbtUtil.asCompoundTag(tag);
			if (compound.contains("value", 8)) {
				MergeContext mergeContext = MergeContext.parse(path, compound);
				DollarPart expression = parse(compound.getString("value"));
				if (expression == null) {
					return Optional.empty();
				}
				return Optional.of(new MergeDollar(expression, path, mergeContext));
			} else {
				NbtCrafting.logError("The value field is required on dollar merge objects. Errored on " + tag.asString());
			}
		} else {
			NbtCrafting.logError("Found invalid dollar merge tag: " + tag.asString());
		}
		return Optional.empty();
	}

	public static DollarPart parse(String string) {
		return parse(string, true);
	}

	public static DollarPart parse(String string, boolean reportErrors) {
		try {
			DollarScriptLexer lexer = new DollarScriptLexer(CharStreams.fromString(string));
			DollarScriptParser parser = new DollarScriptParser(new CommonTokenStream(lexer));
			lexer.removeErrorListeners();
			parser.removeErrorListeners();
			DollarScriptParserErrorListener errorListener = new DollarScriptParserErrorListener(Collections.singletonList(string));
			lexer.addErrorListener(errorListener);
			parser.addErrorListener(errorListener);

			DollarScriptParser.ScriptContext parseTree = parser.script();
			DollarPart expression = parseTree.accept(new DollarScriptVisitor());

			List<String> errors = errorListener.getErrors();
			if (!errors.isEmpty()) {
				if (reportErrors) {
					NbtCrafting.logError("Errors while parsing dollar expression: " + string);
					for (String error : errors) {
						NbtCrafting.logError(error);
					}
				}
				return null;
			}

			return expression;
		} catch (Exception e) {
			if (reportErrors) {
				NbtCrafting.logError("Unable to parse dollar expression: " + string + ": " + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
	}

	// Testing only
	public static void main(String[] args) {
		DollarPart dollarPart = parse("map([{a:1,b:2},{a:3,b:4}],v->{v.c=v.a*v.b; v})");
		try {
			System.out.println(dollarPart.evaluate(ref -> {
				switch (ref) {
					case "a":
						return 2;
					case "b":
						return "b";
					default:
						throw new UnresolvedDollarReferenceException(ref);
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
