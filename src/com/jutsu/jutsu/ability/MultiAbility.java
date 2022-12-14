package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;

import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;

public interface MultiAbility {

	/**
	 * Returns the sub abilities of a MultiAbility. For example:
	 * <p>
	 * {@code new
	 * MultiAbilitySub("SubAbility", Style.LIGHTNING);}
	 *
	 * @return a list of sub MultiAbilities
	 */
	public abstract ArrayList<MultiAbilityInfoSub> getMultiAbilities();

}
