package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.dollars.operator.*;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;

public final class DollarParser {
	private static final Collection<DollarPart.Deserializer> DOLLAR_PART_DESERIALIZERS = new LinkedList<>();
	private Stack<Integer> stopStack = new Stack<>();
	private String string;
	private int currentIndex;

	public DollarParser(String string) {
		this.string = string;
		currentIndex = 0;
	}

	public int eat() {
		return string.codePointAt(currentIndex++);
	}

	public void skip() {
		currentIndex++;
	}

	public int peek() {
		return string.codePointAt(currentIndex + 1);
	}

	public static Dollar[] extractDollars(CompoundTag compoundTag) {
		ArrayList<Dollar> dollars = new ArrayList<>();
		NbtHelper.iterateTags(compoundTag, (path, tag) -> {
			if(NbtHelper.isString(tag) && !tag.asString().isEmpty()) {
				if(tag.asString().charAt(0) == '$') {
					dollars.add(DollarParser.parse(path, tag.asString().substring(1)));
					return true;
				}
			}
			return false;
		});
		return dollars.toArray(new Dollar[0]);
	}

	public static Dollar parse(String key, String value) {
        Dollar dollar = new Dollar(key);
		dollar.expression = new DollarParser(value).parse();
		return dollar;
	}

	public DollarPart parse() {
		try {
			return parse(DOLLAR_PART_DESERIALIZERS.size());
		} catch (IOException | DollarException e) {
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

	public DollarPart parse(int maxPriority) throws IOException, DollarException {
		int peek;

		DollarPart dollarPart = null;
		int priority;

		parse:
		while((peek = peek()) != -1) {
			if(!stopStack.isEmpty() && stopStack.lastElement() == peek) {
				return dollarPart;
			}
			if(peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r') {
				skip();
				continue;
			}

			priority = 0;
			for(DollarPart.Deserializer deserializer : DOLLAR_PART_DESERIALIZERS) {
				if(priority > maxPriority)
					break;
				priority++;
				if(deserializer.matches(peek, this, dollarPart != null)) {
					try {
						dollarPart = deserializer.parse(this, dollarPart, priority);
					} catch (DollarException e) {
						continue;
					}
					continue parse;
				}
			}
			throw new DollarException("Unable to resolve token in dollar expression: \"" + String.valueOf(Character.toChars(peek)) + "\"");
		}

		return dollarPart;
	}

	public DollarPart parseTo(int stop) throws IOException {
		pushStopStack(stop);
		DollarPart dollarPart = parse();
		popStopStack();
		skip();
		return dollarPart;
	}

	public String readTo(int... stops) throws IOException {
		int character = eat();
		StringBuilder stringBuilder = new StringBuilder();
		while(!ArrayUtils.contains(stops, character)) {
			stringBuilder.append(character);
			character = eat();
		}
		return stringBuilder.toString();
	}


	static {
		DOLLAR_PART_DESERIALIZERS.add(new CombinationDollarPartDeserializer());
		DOLLAR_PART_DESERIALIZERS.add(new ChildDollarOperator.BracketDeserializer());
		DOLLAR_PART_DESERIALIZERS.add(new CastDollarOperator.Deserializer());
		DOLLAR_PART_DESERIALIZERS.add(new ChildDollarOperator.DotDeserializer());
		DOLLAR_PART_DESERIALIZERS.add(new DollarPart.Deserializer() {
			@Override
			public boolean matches(int character, DollarParser dollarParser, boolean hasOtherPart) {
				return character == '"' || character == '\'';
			}

			@Override
			public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
				int mark = dollarParser.eat();
				int character;
				StringBuilder stringBuilder = new StringBuilder();
				while (true) {
					character = dollarParser.eat();
					if (character == '\\') {
						stringBuilder.append(Character.toChars(dollarParser.eat()));
					} else if (character == mark) {
						break;
					} else {
						stringBuilder.append(Character.toChars(character));
					}
				}
				return ConstantDollarPart.of(StringTag.of(stringBuilder.toString()));
			}
		});
		DOLLAR_PART_DESERIALIZERS.add(new DollarPart.Deserializer() {
			@Override
			public boolean matches(int character, DollarParser dollarParser, boolean hasOtherPart) {
				return Character.isDigit(character);
			}

			@Override
			public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
				StringBuilder stringBuilder = new StringBuilder(String.valueOf(Character.toChars(dollarParser.eat())));
				boolean dot = false;
				int character;
				while(true) {
					character = dollarParser.peek();
					if(Character.isDigit(character)) {
						dollarParser.skip();
						stringBuilder.append(Character.toChars(character));
					} else if(!dot && character == '.') {
						dollarParser.skip();
						stringBuilder.append('.');
						dot = true;
					} else {
						break;
					}
				}

				try {
					if(dot)
						return ConstantDollarPart.of(DoubleTag.of(Double.parseDouble(stringBuilder.toString())));
					else
						return ConstantDollarPart.of(IntTag.of(Integer.parseInt(stringBuilder.toString())));
				} catch (NumberFormatException e) {
					throw new DollarException(e.getMessage());
				}
			}
		});
		DOLLAR_PART_DESERIALIZERS.add(new ReferenceDollarPart.Deserializer());
		DOLLAR_PART_DESERIALIZERS.add(new QuotientDollarPart.Deserializer());
		DOLLAR_PART_DESERIALIZERS.add(new ProductDollarOperator.Deserializer());
		DOLLAR_PART_DESERIALIZERS.add(new SumDollarOperator.Deserializer());
		DOLLAR_PART_DESERIALIZERS.add(new DifferenceDollarOperator.Deserializer());
	}

	public static void main(String[] args) throws DollarException {
		System.out.println(new DollarParser("(1/2)#a+(0)+':'+(1/2)#i").parse().evaluate(null));
	}
}
