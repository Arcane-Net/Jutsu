package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Style;

public abstract class BlueFireAbility extends FireAbility implements SubAbility {

	public BlueFireAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Style getElement() {
		return Style.BLUE_FIRE;
	}

	public static double getDamageFactor() {
		return getConfig().getDouble("Properties.Fire.BlueFire.DamageFactor");
	}

	public static double getCooldownFactor() {
		return getConfig().getDouble("Properties.Fire.BlueFire.CooldownFactor");
	}

	public static double getRangeFactor() {
		return getConfig().getDouble("Properties.Fire.BlueFire.RangeFactor");
	}

}
