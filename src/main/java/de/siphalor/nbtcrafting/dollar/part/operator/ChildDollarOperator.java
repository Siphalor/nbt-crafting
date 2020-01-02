package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.unary.ConstantDollarPart;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.*;

import java.io.IOException;

public class ChildDollarOperator extends BinaryDollarOperator {
	public ChildDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public Tag apply(Tag first, Tag second) {
		if(first instanceof CompoundTag) {
			String key = NbtHelper.asString(second);
			if(((CompoundTag) first).contains(key)) {
				return ((CompoundTag) first).get(key);
			}
		} else if(first instanceof ListTag && second instanceof AbstractNumberTag) {
			int index = ((AbstractNumberTag) second).getInt();
			if(index < ((ListTag) first).size()) {
				return ((ListTag) first).get(index);
			}
		}
		return null;
	}

	public static class DotDeserializer implements Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '.';
		}

		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) {
			dollarParser.skip();
			StringBuilder stringBuilder = new StringBuilder();
			int character = dollarParser.eat();
			if(Character.isJavaIdentifierStart(character)) {
				stringBuilder.append(Character.toChars(character));
				while(true) {
					character = dollarParser.peek();
					if(Character.isJavaIdentifierPart(character)) {
						dollarParser.skip();
						stringBuilder.append(Character.toChars(character));
					} else {
						break;
					}
				}
				return new ChildDollarOperator(lastDollarPart, ConstantDollarPart.of(StringTag.of(stringBuilder.toString())));
			}
			return null;
		}
	}

	public static class BracketDeserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '[';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			dollarParser.skip();
			return new ChildDollarOperator(lastDollarPart, dollarParser.parseTo(']'));
		}
	}
}
