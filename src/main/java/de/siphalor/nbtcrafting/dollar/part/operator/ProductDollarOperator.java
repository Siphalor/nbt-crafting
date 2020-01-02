package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class ProductDollarOperator extends BinaryDollarOperator {
	public ProductDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public Tag apply(Tag first, Tag second) {
		if(first instanceof AbstractNumberTag) {
			if(second instanceof AbstractNumberTag)
				return DoubleTag.of(((AbstractNumberTag) first).getDouble() * ((AbstractNumberTag) second).getDouble());
			return StringTag.of(StringUtils.repeat(NbtHelper.asString(second), ((AbstractNumberTag) first).getInt()));
		} else if(second instanceof AbstractNumberTag) {
			return StringTag.of(StringUtils.repeat(NbtHelper.asString(first), ((AbstractNumberTag) second).getInt()));
		}
		return null;
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '*';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			if(lastDollarPart == null) {
				throw new DollarException("Unexpected asterisk!");
			}
			dollarParser.skip();
			return new ProductDollarOperator(lastDollarPart, dollarParser.parse(priority));
		}
	}
}
