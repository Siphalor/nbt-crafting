package de.siphalor.nbtcrafting.dollar;

import com.google.common.collect.ImmutableList;
import de.siphalor.nbtcrafting.api.nbt.NbtIterator;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.operator.*;
import de.siphalor.nbtcrafting.dollar.part.unary.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

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
		if(currentIndex++ >= stringLength)
			return -1;
		return string.codePointAt(currentIndex);
	}

	public void skip() {
		currentIndex++;
	}

	public int peek() {
		if(currentIndex + 1 >= stringLength)
			return -1;
		return string.codePointAt(currentIndex + 1);
	}

	public static Dollar[] extractDollars(CompoundTag compoundTag, boolean remove) {
		LinkedList<Dollar> dollars = new LinkedList<>();
		NbtIterator.iterateTags(compoundTag, (path, key, tag) -> {
			if(tag instanceof StringTag && !tag.asString().isEmpty()) {
				if(tag.asString().charAt(0) == '$') {
					if(path.isEmpty() && key.equals("$") && tag.asString().charAt(0) == '$') {
						DollarParser.parse("", tag.asString().substring(1)).ifPresent(dollars::addFirst);
					} else {
						DollarParser.parse(path + key, tag.asString().substring(1)).ifPresent(dollars::add);
					}
					return remove;
				}
			}
			return false;
		});

		return dollars.toArray(new Dollar[0]);
	}

	public static Optional<Dollar> parse(String path, String value) {
        Dollar dollar = new Dollar(path);
		return parse(value).map(dollarPart -> {
			dollar.expression = dollarPart;
			return dollar;
		});
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
		while(true) {
			while(Character.isWhitespace(peek = peek())) {
				skip();
			}
			if(peek == -1)
				return dollarPart;
			if(!stopStack.isEmpty() && stopStack.lastElement() == peek) {
				return dollarPart;
			}

			priority = 0;
			for(Collection<DollarPart.Deserializer> deserializers : DESERIALIZERS) {
				if(priority > maxPriority)
					break;
				priority++;
				for(DollarPart.Deserializer deserializer : deserializers) {
					if(deserializer.matches(peek, this)) {
						dollarPart = deserializer.parse(this, dollarPart, priority);
						continue parse;
					}
				}
			}
			throw new DollarDeserializationException("Unable to resolve token in dollar expression: \"" + String.valueOf(Character.toChars(peek)) + "\"");
		}
	}

	public DollarPart parseUnary() throws DollarDeserializationException {
		int peek;

		while(Character.isWhitespace(peek = peek())) {
			skip();
		}
		if(peek == -1)
			return null;

		for(DollarPart.UnaryDeserializer deserializer : UNARY_DESERIALIZERS) {
			if(deserializer.matches(peek, this)) {
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
		while(!ArrayUtils.contains(stops, character = eat())) {
			if(character == -1)
				return null;
			if(escaped) {
				stringBuilder.append(Character.toChars(character));
				escaped = false;
			} else if(character == '\\') {
				escaped = true;
			} else {
				stringBuilder.append(Character.toChars(character));
			}
		}
		return stringBuilder.toString();
	}

	public static void main(String[] args) {
		parse("(1./2)#a+(0)+':'+(1/2)#i").flatMap(dollarPart -> {
			try {
				return Optional.of(dollarPart.evaluate(null));
			} catch (DollarException e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}).ifPresent(System.out::println);
	}
}
