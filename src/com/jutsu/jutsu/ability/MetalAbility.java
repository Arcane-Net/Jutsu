package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Style;

public abstract class MetalAbility extends EarthAbility implements SubAbility {

	public MetalAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}

	@Override
	public Style getElement() {
		return Style.METAL;
	}

}
