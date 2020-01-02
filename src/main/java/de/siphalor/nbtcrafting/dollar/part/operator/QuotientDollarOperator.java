package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class QuotientDollarOperator extends BinaryDollarOperator {
	public QuotientDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public Tag apply(Tag first, Tag second) {
		if(first instanceof AbstractNumberTag && second instanceof AbstractNumberTag) {
			return DoubleTag.of(((AbstractNumberTag) first).getDouble() / ((AbstractNumberTag) second).getDouble());
		} else {
			return IntTag.of(StringUtils.countMatches(NbtHelper.asString(first), NbtHelper.asString(second)));
		}
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '/';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			dollarParser.skip();
			return new QuotientDollarOperator(lastDollarPart, dollarParser.parse(priority));
		}
	}
}
