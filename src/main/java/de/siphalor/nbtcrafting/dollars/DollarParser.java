package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.dollars.operator.AsteriskDollarOperator;
import de.siphalor.nbtcrafting.dollars.operator.MinusDollarOperator;
import de.siphalor.nbtcrafting.dollars.operator.PlusDollarOperator;
import de.siphalor.nbtcrafting.dollars.value.NumberDollarValue;
import de.siphalor.nbtcrafting.dollars.value.StringDollarValue;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public final class DollarParser {
	private static final Collection<DollarPart.Factory> DOLLAR_PART_FACTORIES = new LinkedList<>();
	private Stack<Integer> stopStack = new Stack<>();
	private Reader reader;

	public DollarParser(Reader reader) {
		this.reader = reader;
	}

	public int eat() throws IOException {
		return reader.read();
	}

	public int peek() throws IOException {
		reader.mark(1);
		int character = reader.read();
		reader.reset();
		return character;
	}

	public static void registerDollarPart(DollarPart.Factory dollarPartFactory) {
		DOLLAR_PART_FACTORIES.add(dollarPartFactory);
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
		dollar.expression = new DollarParser(new StringReader(value)).parse();
		return dollar;
	}

	public DollarPart parse() {
		try {
			return parse(DOLLAR_PART_FACTORIES.size());
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
		int peek = peek();

		DollarPart dollarPart = null;
		int priority;

		while(peek != -1) {
			if(!stopStack.isEmpty() && stopStack.lastElement() == peek) {
				return dollarPart;
			}
			if(peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r') {
				eat();
				peek = peek();
				continue;
			}

			priority = 0;
			for(DollarPart.Factory factory : DOLLAR_PART_FACTORIES) {
				if(factory.matches(peek)) {
					dollarPart = factory.parse(this, dollarPart, priority);
					break;
				}
				if(++priority > maxPriority) {
					break;
				}
			}
			peek = peek();
		}

		return dollarPart;
	}

	public DollarPart parseTo(int stop) throws IOException {
		pushStopStack(stop);
		DollarPart dollarPart = parse();
		popStopStack();
		eat();
		return dollarPart;
	}

	public String readTo(int... stops) throws IOException {
		int character = reader.read();
		StringBuilder stringBuilder = new StringBuilder();
		while(!ArrayUtils.contains(stops, character)) {
			stringBuilder.append(character);
			character = reader.read();
		}
		return stringBuilder.toString();
	}


	static {
		DOLLAR_PART_FACTORIES.add(new CombinationDollarPartFactory());
		DOLLAR_PART_FACTORIES.add(new StringDollarValue.Factory());
		DOLLAR_PART_FACTORIES.add(new NumberDollarValue.Factory());
		DOLLAR_PART_FACTORIES.add(new AsteriskDollarOperator.Factory());
		DOLLAR_PART_FACTORIES.add(new MinusDollarOperator.Factory());
		DOLLAR_PART_FACTORIES.add(new PlusDollarOperator.Factory());
	}

	public static void main(String[] args) throws DollarException {
		System.out.println(new DollarParser(new StringReader(" 5+( 8 - 1 )")).parse().apply(null));
	}
}
