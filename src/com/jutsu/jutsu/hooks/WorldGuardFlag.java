package com.projectkorra.projectkorra.hooks;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.projectkorra.projectkorra.ProjectKorra;

public class WorldGuardFlag {
	public static void registerBendingWorldGuardFlag() {
		final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

		try {
			registry.register(new StateFlag("jutsu", false));
		} catch (final Exception e) {
			ProjectKorra.log.severe("Unable to register jutsu WorldGuard flag: " + e);
		}
	}
}
