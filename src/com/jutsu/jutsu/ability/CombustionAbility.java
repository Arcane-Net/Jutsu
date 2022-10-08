package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Style;

public abstract class CombustionAbility extends FireAbility implements SubAbility {

	public CombustionAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Style getElement() {
		return Style.COMBUSTION;
	}

	@Override
	public boolean isExplosiveAbility() {
		return true;
	}

	//Overriding these methods to make sure Combustion abilities don't get buffed by blue fire
	@Override
	public double applyModifiersDamage(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public double applyModifiersRange(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public long applyModifiersCooldown(long value) {
		return (long) GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0));
	}

}
