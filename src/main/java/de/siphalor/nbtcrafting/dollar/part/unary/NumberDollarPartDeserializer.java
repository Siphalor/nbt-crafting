package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;

public class NumberDollarPartDeserializer implements DollarPart.UnaryDeserializer {
	@Override
	public boolean matches(int character, DollarParser dollarParser) {
		return Character.isDigit(character);
	}

	@Override
	public DollarPart parse(DollarParser dollarParser) throws DollarException {
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
}
