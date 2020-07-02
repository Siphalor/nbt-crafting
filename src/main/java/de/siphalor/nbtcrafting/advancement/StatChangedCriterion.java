package de.siphalor.nbtcrafting.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.siphalor.nbtcrafting.NbtCrafting;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class StatChangedCriterion extends AbstractCriterion<StatChangedCriterion.Conditions<?>> {
	private static final Identifier ID = new Identifier(NbtCrafting.MOD_ID, "stat_changed");

	@Override
	public Identifier getId() {
		return ID;
	}

	public <T> void trigger(ServerPlayerEntity player, Stat<T> stat, int value) {
		StatType<T> statType = stat.getType();
		T object = stat.getValue();

		test(player.getAdvancementTracker(), conditions -> conditions.match(statType, object, value));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Conditions conditionsFromJson(JsonObject obj, JsonDeserializationContext context) {
		Identifier statId = new Identifier(JsonHelper.getString(obj, "stat"));
		StatType<?> statType = Registry.STAT_TYPE.getOrEmpty(statId).orElseThrow(() -> new JsonSyntaxException("Unknown stat: " + statId));

		Identifier id = new Identifier(JsonHelper.getString(obj, "id"));
		Object object = statType.getRegistry().get(id);

		return new Conditions(statType, object, NumberRange.IntRange.fromJson(obj.get("range")));
	}

	static class Conditions<T> extends AbstractCriterionConditions {
		private final StatType<T> statType;
		private final T object;
		private final NumberRange.IntRange intRange;

		public Conditions(StatType<T> statType, T object, NumberRange.IntRange intRange) {
			super(ID);
			this.statType = statType;
			this.object = object;
			this.intRange = intRange;
		}

		public boolean match(StatType<?> statType, Object object, int value) {
			if (statType == this.statType && object == this.object) {
				return intRange.test(value);
			}
			return false;
		}
	}
}
