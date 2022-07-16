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

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.nbt.MergeMode;
import de.siphalor.nbtcrafting.api.nbt.NbtIterator;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.function.DollarFunction;
import de.siphalor.nbtcrafting.dollar.function.DollarFunctions;
import de.siphalor.nbtcrafting.dollar.instruction.*;
import de.siphalor.nbtcrafting.dollar.type.CountDollar;
import de.siphalor.nbtcrafting.dollar.type.MergeDollar;
import de.siphalor.nbtcrafting.dollar.type.SimpleDollar;
import de.siphalor.nbtcrafting.util.StringCodepointIterator;

public final class DollarParser {
	private static final int[] OPERATOR_CHARACTERS;
	private static final int[] BRACKET_CHARACTERS = new int[] {'(', ')', '[', ']'};

	private static final Instruction DOT_CHILD_INSTRUCTION = new ChildInstruction(0);
	private static final Instruction BRACKET_CHILD_INSTRUCTION = new ChildInstruction(5);
	private static final Instruction NEGATE_INSTRUCTION = new NegateInstruction(); // 10
	private static final Instruction CAST_INSTRUCTION = new CastInstruction(); // 15
	private static final Instruction NOT_INSTRUCTION = new NotInstruction(); // 20
	private static final Instruction MULTIPLY_INSTRUCTION = new MultiplyInstruction(); // 30
	private static final Instruction DIVIDE_INSTRUCTION = new DivideInstruction(); // 30
	private static final Instruction ADD_INSTRUCTION = new AddInstruction(); // 40
	private static final Instruction SUBTRACT_INSTRUCTION = new SubtractInstruction(); // 40
	private static final Instruction GREATER_THAN_INSTRUCTION = new ComparisonInstruction(value -> value > 0); // 50
	private static final Instruction GREATER_THAN_EQUAL_INSTRUCTION = new ComparisonInstruction(value -> value >= 0); // 50
	private static final Instruction LESS_THAN_INSTRUCTION = new ComparisonInstruction(value -> value < 0); // 50
	private static final Instruction LESS_THAN_EQUAL_INSTRUCTION = new ComparisonInstruction(value -> value <= 0); // 50

	static {
		OPERATOR_CHARACTERS = DollarToken.SEQUENCES.keySet().stream()
				.flatMapToInt(String::chars)
				.filter(ch -> !Character.isAlphabetic(ch))
				.distinct().toArray();
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
				String literal = stringBuilder.toString();
				DollarToken.Type tokenType = DollarToken.SEQUENCES.get(literal);
				if (tokenType != null) {
					tokens.add(new DollarToken(tokenType, literal, codepoints.getIndex()));
				} else {
					tokens.add(new DollarToken(DollarToken.Type.LITERAL, literal, codepoints.getIndex()));
				}
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
			} else if (token != ' ') {
				IntList tokenList = new IntArrayList(10);
				tokenList.add(token);
				DollarToken.Type lastExactMatch = null;

				SortedMap<String, DollarToken.Type> matchingOperators;

				while (true) {
					matchingOperators = DollarToken.SEQUENCES.subMap(
							new String(new int[]{token}, 0, 1),
							new String(new int[]{token + 1}, 0, 1)
					);
					token = codepoints.next();
					if (matchingOperators.isEmpty()) {
						break;
					}
					String firstKey = matchingOperators.firstKey();
					if (firstKey.length() == tokenList.size() && firstKey.equals(new String(tokenList.toIntArray(), 0, tokenList.size()))) {
						lastExactMatch = matchingOperators.get(firstKey);
						if (matchingOperators.size() == 1) {
							break;
						}
					}
					if (token == -1) {
						break;
					}
					tokenList.add(token);
				}

				if (lastExactMatch != null) {
					tokens.add(new DollarToken(lastExactMatch, new String(tokenList.toIntArray(), 0, tokenList.size()), codepoints.getIndex()));
					continue;
				} else {
					throw new DollarDeserializationException("Unrecognized character sequence \"" + new String(tokenList.toIntArray(), 0, tokenList.size()) + "\" at position " + codepoints.getIndex());
				}
			}
			token = codepoints.next();
		}
		return new DollarParser(tokens.toArray(new DollarToken[0]));
	}

	public Instruction[] parse() throws DollarDeserializationException {
		Collection<Instruction> instructions = new ArrayList<>();
		DollarToken endToken = parseGroup(instructions);
		if (endToken != null) {
			throw new DollarDeserializationException("Unexpected token " + endToken);
		}
		return instructions.toArray(new Instruction[0]);
	}

	private DollarToken parseGroup(Collection<Instruction> instructions) throws DollarDeserializationException {
		Stack<Instruction> operators = new Stack<>();
		List<JumpInstruction> jumpsToEnd = new ArrayList<>();
		DollarToken token = eat();
		while (token != null) {
			if (token.type.isValue() || token.type == DollarToken.Type.PARENTHESIS_OPEN) {
				if (token.type == DollarToken.Type.PARENTHESIS_OPEN) {
					DollarToken closeToken = parseGroup(instructions);
					if (closeToken == null || closeToken.type != DollarToken.Type.PARENTHESIS_CLOSE) {
						throw new DollarDeserializationException("Unmatched parenthesis, got " + closeToken);
					}
				} else {
					if (token.type == DollarToken.Type.LITERAL) {
						String literalValue = ((String) token.value);
						token = peek();
						if (token != null && token.type == DollarToken.Type.PARENTHESIS_OPEN) {
							skip();
							int parameterCount = 0;
							while (true) {
								DollarToken stopToken = parseGroup(instructions);
								parameterCount++;
								if (stopToken == null) {
									throw new DollarDeserializationException("Unexpected end of input, expected end of parameter list");
								}
								if (stopToken.type == DollarToken.Type.PARENTHESIS_CLOSE) {
									break;
								}
								if (stopToken.type != DollarToken.Type.COMMA) {
									throw new DollarDeserializationException("Unexpected token " + stopToken + ", expected end of parameter list");
								}
							}

							DollarFunction function = DollarFunctions.get(literalValue);
							if (function == null) {
								throw new DollarDeserializationException("Could not resolve function \"" + literalValue + "\"");
							}
							if (!function.isParameterCountCorrect(parameterCount)) {
								throw new DollarDeserializationException("Invalid parameter count " + parameterCount + " to function \"" + literalValue + "\"");
							}
							instructions.add(new StaticCallInstruction(parameterCount, function));
						} else {
							instructions.add(new PushInstruction(new Literal(literalValue)));
						}
					} else if (token.type == DollarToken.Type.NULL) {
						instructions.add(new PushInstruction(null));
					} else {
						instructions.add(new PushInstruction(token.value));
					}
				}

				token = eat();
				while (token != null && token.type.isPostfixOperator()) {
					Instruction operator;
					if (token.type == DollarToken.Type.BRACKET_OPEN) {
						DollarToken endToken = parseGroup(instructions);
						if (endToken == null || endToken.type == DollarToken.Type.BRACKET_CLOSE) {
							throw new DollarDeserializationException("Unmatched bracket, got " + endToken);
						}
						operator = BRACKET_CHILD_INSTRUCTION;
					} else {
						throw new DollarDeserializationException("Unsupported postfix operator: " + token);
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
					int instructionCount = instructions.size();
					for (JumpInstruction jump : jumpsToEnd) {
						jump.offset += instructionCount;
					}
					return null;
				}
				if (token.type.isInfixOperator()) {
					Instruction operator;
					switch (token.type) {
						case DOT:
							operator = DOT_CHILD_INSTRUCTION;
							break;
						case OCTOTHORPE:
							operator = CAST_INSTRUCTION;
							break;
						case ASTERISK:
							operator = MULTIPLY_INSTRUCTION;
							break;
						case SLASH:
							operator = DIVIDE_INSTRUCTION;
							break;
						case PLUS:
							operator = ADD_INSTRUCTION;
							break;
						case MINUS:
							operator = SUBTRACT_INSTRUCTION;
							break;
						case LESS_THAN:
							operator = LESS_THAN_INSTRUCTION;
							break;
						case GREATER_THAN:
							operator = GREATER_THAN_INSTRUCTION;
							break;
						case LESS_THAN_EQUAL:
							operator = LESS_THAN_EQUAL_INSTRUCTION;
							break;
						case GREATER_THAN_EQUAL:
							operator = GREATER_THAN_EQUAL_INSTRUCTION;
							break;
						case AND: {
							while (!operators.isEmpty()) {
								instructions.add(operators.pop());
							}
							AndInstruction andInstruction = new AndInstruction(-instructions.size());
							jumpsToEnd.add(andInstruction);
							instructions.add(andInstruction);
							token = eat();
							continue;
						}
						case OR: {
							while (!operators.isEmpty()) {
								instructions.add(operators.pop());
							}
							OrInstruction orInstruction = new OrInstruction(-instructions.size());
							jumpsToEnd.add(orInstruction);
							instructions.add(orInstruction);
							token = eat();
							continue;
						}
						case QUESTION:
							while (!operators.isEmpty()) {
								instructions.add(operators.pop());
							}
							instructions.add(NOT_INSTRUCTION);
							ConditionalJumpInstruction jumpToElse = new ConditionalJumpInstruction(-instructions.size());
							instructions.add(jumpToElse);
							DollarToken endToken = parseGroup(instructions);
							if (endToken == null || endToken.type != DollarToken.Type.COLON) {
								throw new DollarDeserializationException("Unexpected token " + endToken + ", expected :");
							}
							JumpInstruction jumpToEnd = new JumpInstruction(-instructions.size());
							instructions.add(jumpToEnd);
							jumpsToEnd.add(jumpToEnd);
							jumpToElse.offset += instructions.size();
							endToken = parseGroup(instructions);

							int instructionCount = instructions.size();
							for (JumpInstruction jump : jumpsToEnd) {
								jump.offset += instructionCount;
							}
							return endToken;
						case EQUAL:
						case NOT_EQUAL:
						default:
							throw new DollarDeserializationException("Unsupported infix operator: " + token);
					}
					token = eat();
					int precedence = operator.getPrecedence();
					while (!operators.isEmpty() && operators.peek().getPrecedence() < precedence) {
						instructions.add(operators.pop());
					}
					operators.push(operator);
				} else if (token.type.isStop()) {
					while (!operators.isEmpty()) {
						instructions.add(operators.pop());
					}
					int instructionCount = instructions.size();
					for (JumpInstruction jump : jumpsToEnd) {
						jump.offset += instructionCount;
					}
					return token;
				} else {
					throw new DollarDeserializationException("Unexpected token " + token);
				}
			} else if (token.type.isPrefixOperator()) {
				switch (token.type) {
					case MINUS:
						operators.push(NEGATE_INSTRUCTION);
						break;
					case EXCLAMATION:
						operators.push(NOT_INSTRUCTION);
						break;
					default:
						throw new DollarDeserializationException("Unsupported prefix operator: " + token);
				}
				token = eat();
			} else {
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
			//DollarParser parser = tokenize("(1-2.0 ? -4.5#B : (5.0 / 2.0)#i) ? test + '' : 'cd'");
			//DollarParser parser = tokenize("1 || 1/0");
			DollarParser parser = tokenize("min(ifNull(null, 45),78) + min(9,n)");
			Instruction[] instructions = parser.parse();

			DollarRuntime dollarRuntime = new DollarRuntime(ImmutableMap.of("test", "Hello World", "n", 2, "list", listTag)::get);
			System.out.println(dollarRuntime.run(instructions));
		} catch (DollarDeserializationException | DollarEvaluationException e) {
			throw new RuntimeException(e);
		}
	}
}
