package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.nbt.StringTag;

public class StringDollarPartDeserializer implements DollarPart.UnaryDeserializer {
	@Override
	public boolean matches(int character, DollarParser dollarParser) {
		return character == '"' || character == '\'';
	}

	@Override
	public DollarPart parse(DollarParser dollarParser) {
		int marker = dollarParser.eat();
		return ConstantDollarPart.of(StringTag.of(dollarParser.readTo(marker)));
	}
}
