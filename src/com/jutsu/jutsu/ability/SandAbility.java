package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Style;

public abstract class SandAbility extends EarthAbility implements SubAbility {

	public SandAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}

	@Override
	public Style getElement() {
		return Style.SAND;
	}

}
