package com.projectkorra.projectkorra.firebending.passive;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.firebending.Illumination;

public class FirePassive {

	public static void handle(final Player player) {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer != null && nPlayer.canBendPassive(CoreAbility.getAbility(Illumination.class)) && nPlayer.canUsePassive(CoreAbility.getAbility(Illumination.class))) {
			if (!CoreAbility.hasAbility(player, Illumination.class) && !CoreAbility.hasAbility(player, Tremorsense.class) && nPlayer.canBendIgnoreBinds(CoreAbility.getAbility("Illumination")) && ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.Illumination.Passive")) {
				if (nPlayer.isIlluminating()) {
					new Illumination(player);
				}
			}
		}
	}
}
