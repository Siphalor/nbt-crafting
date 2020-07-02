package de.siphalor.nbtcrafting.dollar.type;

import com.mojang.datafixers.util.Pair;
import de.siphalor.nbtcrafting.api.nbt.MergeMode;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class MergeDollar extends Dollar {
	protected final String path;
	protected final Collection<Pair<Pattern, MergeMode>> mergeModes;

	public MergeDollar(DollarPart expression, String path, Collection<Pair<Pattern, MergeMode>> mergeModes) {
		super(expression);
		this.path = path;
		this.mergeModes = mergeModes;
	}

	@Override
	public void apply(ItemStack stack, Map<String, Object> references) throws DollarException {
		Tag value = NbtUtil.asTag(evaluate(references));
		if (!(value instanceof CompoundTag)) {
			throw new DollarEvaluationException("Couldn't set stacks main tag as given dollar expression evaluates to non-object value.");
		} else {
			NbtUtil.mergeInto(stack.getOrCreateTag(), (CompoundTag) value, mergeModes, "");
		}
	}
}
