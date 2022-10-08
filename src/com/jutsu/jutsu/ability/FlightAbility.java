package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Style;

public abstract class FlightAbility extends AirAbility implements SubAbility {

	public FlightAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return AirAbility.class;
	}

	@Override
	public Style getElement() {
		return Style.FLIGHT;
	}

}
