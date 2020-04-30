package de.siphalor.nbtcrafting.advancement;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.siphalor.nbtcrafting.NbtCrafting;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.class_5257;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
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

		test(player, conditions -> conditions.match(statType, object, value));
	}

	@Override
	protected Conditions<?> method_27854(JsonObject jsonObject, EntityPredicate.class_5258 arg, class_5257 arg2) {
		Identifier statId = new Identifier(JsonHelper.getString(jsonObject, "stat"));
		StatType<?> statType = Registry.STAT_TYPE.getOrEmpty(statId).orElseThrow(() -> new JsonSyntaxException("Unknown stat: " + statId));

		Identifier id = new Identifier(JsonHelper.getString(jsonObject, "id"));
		Object object = statType.getRegistry().get(id);

		return new Conditions(statType, object, NumberRange.IntRange.fromJson(jsonObject.get("range")), arg);
	}

	static class Conditions<T> extends AbstractCriterionConditions {
		private StatType<T> statType;
		private T object;
		private NumberRange.IntRange intRange;

		public Conditions(StatType<T> statType, T object, NumberRange.IntRange intRange, EntityPredicate.class_5258 playerPredicate) {
			super(ID, playerPredicate);
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
