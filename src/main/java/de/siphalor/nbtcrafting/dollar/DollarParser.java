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

package de.siphalor.nbtcrafting.dollar;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.nbt.MergeMode;
import de.siphalor.nbtcrafting.api.nbt.NbtIterator;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.jump.ConditionalJump;
import de.siphalor.nbtcrafting.dollar.jump.UnconditionalJump;
import de.siphalor.nbtcrafting.dollar.operator.*;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;
import de.siphalor.nbtcrafting.dollar.type.CountDollar;
import de.siphalor.nbtcrafting.dollar.type.MergeDollar;
import de.siphalor.nbtcrafting.dollar.type.SimpleDollar;
import de.siphalor.nbtcrafting.util.StringCodepointIterator;

public final class DollarParser {
	private static final int[] OPERATOR_CHARACTERS;
	private static final int[] BRACKET_CHARACTERS = new int[] {'(', ')', '[', ']'};

	private static final Operator BRACKET_CHILD_OPERATOR = new ChildOperator(5, DollarToken.Type.POSTFIX_OPERATOR);
	private static final Operator NOT_OPERATOR = new NotOperator();
	private static final Map<String, Operator> OPERATORS = new TreeMap<>();

	static {
		OPERATORS.put(".", new ChildOperator(0, DollarToken.Type.INFIX_OPERATOR));
		OPERATORS.put("#", new CastOperator());
		OPERATORS.put("*", new MultiplyOperator());
		OPERATORS.put("/", new DivideOperator());
		OPERATORS.put("+", new AddOperator());
		OPERATORS.put("-", new SubtractOperator());
		OPERATORS.put("!", NOT_OPERATOR);

		Set<Integer> operators = OPERATORS.keySet().stream().flatMapToInt(String::chars).boxed()
				.collect(Collectors.toSet());
		operators.add((int) '?');
		operators.add((int) ':');
		OPERATOR_CHARACTERS = operators
				.stream().mapToInt(Integer::intValue)
				.toArray();
	}

	private final DollarToken[] tokens;
	private int currentIndex;

	private DollarParser(DollarToken[] tokens) {
		this.tokens = tokens;
	}

	private static String codepointToString(int codepoint) {
		return new String(new int[]{codepoint}, 0, 1);
	}

	public static Dollar[] extractDollars(CompoundTag compoundTag, boolean remove) {
		LinkedList<Dollar> dollars = new LinkedList<>();
		NbtIterator.iterateTags(compoundTag, (path, key, tag) -> {
			if (key.equals("$")) {
				if (NbtUtil.isList(tag)) {
					AbstractListTag<Tag> list = NbtUtil.asListTag(tag);
					for (Tag t : list) {
						try {
							dollars.addFirst(parseMerge(t, path));
						} catch (DollarDeserializationException e) {
							NbtCrafting.logError("Failed to parse dollar at " + path + ": ");
							e.printStackTrace();
						}
					}
				} else {
					try {
						dollars.addFirst(parseMerge(tag, path));
					} catch (DollarDeserializationException e) {
						NbtCrafting.logError("Failed to parse dollar at " + path + ": ");
						e.printStackTrace();
					}
				}
				return remove;
			}
			if (NbtUtil.isString(tag) && !tag.asString().isEmpty()) {
				if (tag.asString().charAt(0) == '$') {
					try {
						if (key.equals(NbtCrafting.MOD_ID + ":count")) {
							dollars.addFirst(new CountDollar(tokenize(tag.asString().substring(1)).parse()));
						} else {
							dollars.addFirst(new SimpleDollar(tokenize(tag.asString().substring(1)).parse(), path));
						}
					} catch (DollarDeserializationException e) {
						NbtCrafting.logError("Error parsing dollar: " + tag.asString());
						e.printStackTrace();
					}
					return remove;
				}
			}
			return false;
		});

		dollars.sort((a, b) -> {
			if (a instanceof MergeDollar) return b instanceof MergeDollar ? 0 : -1;
			return 1;
		});
		return dollars.toArray(new Dollar[0]);
	}

	private static MergeDollar parseMerge(Tag tag, String path) throws DollarDeserializationException {
		if (NbtUtil.isString(tag)) {
			String val = tag.asString();
			if (val.charAt(0) == '$') {
				val = val.substring(1);
			}
			return new MergeDollar(tokenize(val).parse(), path, Collections.emptyList());
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
				return new MergeDollar(tokenize(compound.getString("value")).parse(), path, mergeModes);
			} else {
				NbtCrafting.logError("The value field is required on dollar merge objects. Errored on " + tag.asString());
			}
		} else {
			NbtCrafting.logError("Found invalid dollar merge tag: " + tag.asString());
		}
		return null;
	}

	public @Nullable DollarToken eat() {
		if (currentIndex >= tokens.length) {
			return null;
		}
		return tokens[currentIndex++];
	}

	public void skip() {
		currentIndex++;
	}

	public @Nullable DollarToken peek() {
		if (currentIndex >= tokens.length) {
			return null;
		}
		return tokens[currentIndex];
	}

	public static DollarParser tokenize(String text) throws DollarDeserializationException {
		ArrayList<DollarToken> tokens = new ArrayList<>();
		StringCodepointIterator codepoints = new StringCodepointIterator(text);
		int token = codepoints.next();
		while (token != -1) {
			if (token >= '0' && token <= '9') {
				StringBuilder numberText = new StringBuilder();
				numberText.appendCodePoint(token);
				token = codepoints.next();
				boolean floating = false;
				boolean exponentBegin = false;
				while (true) {
					if (token == '.') {
						if (floating) {
							throw new DollarDeserializationException("Invalid number (multiple points) " + numberText + " at position " + codepoints.getIndex());
						}
						floating = true;
					} else if (token == 'e' || token == 'E') {
						if (exponentBegin) {
							throw new DollarDeserializationException("Invalid number (multiple exponents) " + numberText + " at position " + codepoints.getIndex());
						}
						exponentBegin = true;
					} else if (exponentBegin && token == '-') {
						exponentBegin = false;
					} else if (token < '0' || token > '9') {
						break;
					}
					numberText.appendCodePoint(token);
					token = codepoints.next();
				}

				try {
					switch (token) {
						case 'b':
						case 'B':
							tokens.add(new DollarToken(DollarToken.Type.NUMBER, Byte.parseByte(numberText.toString()), codepoints.getIndex()));
							break;
						case 's':
						case 'S':
							tokens.add(new DollarToken(DollarToken.Type.NUMBER, Short.parseShort(numberText.toString()), codepoints.getIndex()));
							break;
						case 'l':
						case 'L':
							tokens.add(new DollarToken(DollarToken.Type.NUMBER, Long.parseLong(numberText.toString()), codepoints.getIndex()));
							break;
						case 'f':
						case 'F':
							tokens.add(new DollarToken(DollarToken.Type.NUMBER, Float.parseFloat(numberText.toString()), codepoints.getIndex()));
							break;
						case 'd':
						case 'D':
							tokens.add(new DollarToken(DollarToken.Type.NUMBER, Double.parseDouble(numberText.toString()), codepoints.getIndex()));
							break;
						default:
							if (ArrayUtils.contains(OPERATOR_CHARACTERS, token) || token == ' ' || ArrayUtils.contains(BRACKET_CHARACTERS, token) || token == -1) {
								if (floating) {
									tokens.add(new DollarToken(DollarToken.Type.NUMBER, Double.parseDouble(numberText.toString()), codepoints.getIndex()));
								} else {
									tokens.add(new DollarToken(DollarToken.Type.NUMBER, Integer.parseInt(numberText.toString()), codepoints.getIndex()));
								}
							} else {
								throw new DollarDeserializationException("Unexpected character " + codepointToString(token) + " at position " + codepoints.getIndex());
							}
					}
				} catch (NumberFormatException e) {
					throw new DollarDeserializationException("Unable to parse number " + numberText + " at position " + codepoints.getIndex());
				}
				continue;
			} else if (Character.isAlphabetic(token)) {
				StringBuilder stringBuilder = new StringBuilder();
				while (token != -1 && (Character.isAlphabetic(token) || Character.isDigit(token) || token == '_')) {
					stringBuilder.appendCodePoint(token);
					token = codepoints.next();
				}
				tokens.add(new DollarToken(DollarToken.Type.LITERAL, stringBuilder.toString(), codepoints.getIndex()));
				continue;
			} else if (token == '"' || token == '\'') {
				int quoteToken = token;
				StringBuilder stringBuilder = new StringBuilder();
				while (true) {
					token = codepoints.next();
					if (token == -1) {
						throw new DollarDeserializationException("Unterminated string literal");
					} else if (token == '\\') {
						token = codepoints.next();
						if (token == -1) {
							throw new DollarDeserializationException("Unterminated string literal");
						} else if (token == '\\') {
							stringBuilder.append('\\');
						} else {
							throw new DollarDeserializationException("Invalid escape sequence in string literal at position " + codepoints.getIndex());
						}
					} else if (token == quoteToken) {
						break;
					} else {
						stringBuilder.appendCodePoint(token);
					}
				}
				tokens.add(new DollarToken(DollarToken.Type.STRING, stringBuilder.toString(), codepoints.getIndex()));
			} else if (token == '(') {
				tokens.add(new DollarToken(DollarToken.Type.PARENTHESIS_OPEN, null, codepoints.getIndex()));
			} else if (token == ')') {
				tokens.add(new DollarToken(DollarToken.Type.PARENTHESIS_CLOSE, token, codepoints.getIndex()));
			} else if (token == '[') {
				tokens.add(new DollarToken(DollarToken.Type.BRACKET_OPEN, null, codepoints.getIndex()));
			} else if (token == ']') {
				tokens.add(new DollarToken(DollarToken.Type.BRACKET_CLOSE, null, codepoints.getIndex()));
			} else if (ArrayUtils.contains(OPERATOR_CHARACTERS, token)) {
				int[] operatorTokens = new int[]{token, 0, 0};
				int begin = codepoints.getIndex();
				int tokenCount = 1;
				String operatorText = null;
				Operator operator = null;
				while (tokenCount < 3) {
					token = codepoints.next();
					if (!ArrayUtils.contains(OPERATOR_CHARACTERS, token)) {
						break;
					}
					operatorTokens[tokenCount++] = token;
					operatorText = new String(operatorTokens, 0, tokenCount);
					Operator o = OPERATORS.get(operatorText);
					if (o == null) {
						break;
					}
					operator = o;
				}
				if (tokenCount == 3) {
					token = codepoints.next();
				}

				if (operator == null) {
					switch (operatorTokens[0]) {
						case '?':
							tokens.add(new DollarToken(DollarToken.Type.CONDITION_THEN, null, begin));
							continue;
						case ':':
							tokens.add(new DollarToken(DollarToken.Type.CONDITION_ELSE, null, begin));
							continue;
						default:
							operatorText = new String(operatorTokens, 0, 1);
							operator = OPERATORS.get(operatorText);
					}
				}
				if (operator == null) {
					throw new DollarDeserializationException("Unknown operator " + operatorText + " at position " + begin);
				}

				tokens.add(new DollarToken(operator.getTokenType(), operator, begin));
				continue;
			} else if (token != ' ') {
				throw new DollarDeserializationException("Unexpected character " + codepointToString(token) + " at position " + codepoints.getIndex());
			}
			token = codepoints.next();
		}
		return new DollarParser(tokens.toArray(new DollarToken[0]));
	}

	public Object[] parse() throws DollarDeserializationException {
		Collection<Object> instructions = new ArrayList<>();
		DollarToken endToken = parseGroup(instructions);
		if (endToken != null) {
			throw new DollarDeserializationException("Unexpected token " + endToken);
		}
		return instructions.toArray();
	}

	private DollarToken parseGroup(Collection<Object> instructions) throws DollarDeserializationException {
		Stack<Operator> operators = new Stack<>();
		DollarToken token = eat();
		while (token != null) {
			switch (token.type) {
				case LITERAL:
				case NUMBER:
				case STRING:
				case PARENTHESIS_OPEN:
					if (token.type == DollarToken.Type.PARENTHESIS_OPEN) {
						DollarToken closeToken = parseGroup(instructions);
						if (closeToken == null || closeToken.type != DollarToken.Type.PARENTHESIS_CLOSE || (int) closeToken.value != ')') {
							throw new DollarDeserializationException("Unmatched parenthesis, got " + closeToken);
						}
					} else {
						if (token.type == DollarToken.Type.LITERAL) {
							instructions.add(new Literal((String) token.value));
						} else {
							instructions.add(token.value);
						}
					}

					token = eat();
					while (token != null) {
						Operator operator;
						if (token.type == DollarToken.Type.POSTFIX_OPERATOR) {
							operator = (Operator) token.value;
						} else if (token.type == DollarToken.Type.BRACKET_OPEN) {
							DollarToken closeToken = parseGroup(instructions);
							if (closeToken == null || closeToken.type != DollarToken.Type.BRACKET_CLOSE) {
								throw new DollarDeserializationException("Unmatched parenthesis");
							}
							operator = BRACKET_CHILD_OPERATOR;
						} else {
							break;
						}
						int precedence = operator.getPrecedence();
						while (!operators.isEmpty() && operators.peek().getPrecedence() < precedence) {
							instructions.add(operators.pop());
						}
						instructions.add(operator);
						token = eat();
					}

					if (token == null) {
						while (!operators.isEmpty()) {
							instructions.add(operators.pop());
						}
						return null;
					}
					if (token.type == DollarToken.Type.INFIX_OPERATOR) {
						Operator operator = (Operator) token.value;
						token = eat();
						int precedence = operator.getPrecedence();
						while (!operators.isEmpty() && operators.peek().getPrecedence() < precedence) {
							instructions.add(operators.pop());
						}
						operators.push(operator);
					//} else if (isLiteral && token.type == DollarToken.Type.PARENTHESIS_OPEN) {
					// TODO: Function calls
					} else if (token.type == DollarToken.Type.PARENTHESIS_CLOSE
							|| token.type == DollarToken.Type.BRACKET_CLOSE
							|| token.type == DollarToken.Type.CONDITION_ELSE) {
						while (!operators.isEmpty()) {
							instructions.add(operators.pop());
						}
						return token;
					} else if (token.type == DollarToken.Type.CONDITION_THEN) {
						while (!operators.isEmpty()) {
							instructions.add(operators.pop());
						}
						instructions.add(NOT_OPERATOR);
						ConditionalJump jumpToElse = new ConditionalJump(-instructions.size());
						instructions.add(jumpToElse);
						DollarToken endToken = parseGroup(instructions);
						if (endToken == null || endToken.type != DollarToken.Type.CONDITION_ELSE) {
							throw new DollarDeserializationException("Unexpected token " + endToken + ", expected :");
						}
						UnconditionalJump jumpToEnd = new UnconditionalJump(-instructions.size());
						instructions.add(jumpToEnd);
						jumpToElse.offset += instructions.size();
						endToken = parseGroup(instructions);
						jumpToEnd.offset += instructions.size();
						return endToken;
					} else {
						throw new DollarDeserializationException("Unexpected token " + token);
					}
					break;
				case PREFIX_OPERATOR:
					Operator operator = ((Operator) token.value);
					operators.push(operator);
					token = eat();
					break;
				default:
					throw new DollarDeserializationException("Unexpected token " + token);
			}
		}
		throw new DollarDeserializationException("Unexpected end of string");
	}

	// Testing only
	public static void main(String[] args) {
		try {
			ListTag listTag = new ListTag();
			listTag.add(IntTag.of(5));
			listTag.add(IntTag.of(3));
			listTag.add(IntTag.of(1));
			DollarParser parser = tokenize("(1 + 2 * 3 / (4 - 2.5)) + list[1+2-1/1]");
			Object[] instructions = parser.parse();
			System.out.println(DollarUtil.evaluate(instructions, ImmutableMap.of("list", listTag)::get));
		} catch (DollarDeserializationException | DollarEvaluationException e) {
			throw new RuntimeException(e);
		}
	}
}
