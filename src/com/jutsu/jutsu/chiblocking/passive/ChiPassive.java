package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.QuickStrike;
import com.projectkorra.projectkorra.chiblocking.SwiftKick;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ActionBar;

public class ChiPassive {
	public static boolean willChiBlock(final Player attacker, final Player player) {
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer == null) {
			return false;
		}

		final ChiAbility stance = nPlayer.getStance();
		final QuickStrike quickStrike = CoreAbility.getAbility(player, QuickStrike.class);
		final SwiftKick swiftKick = CoreAbility.getAbility(player, SwiftKick.class);
		double newChance = getChance();

		if (stance != null && stance instanceof AcrobatStance) {
			newChance += ((AcrobatStance) stance).getChiBlockBoost();
		}

		if (quickStrike != null) {
			newChance += quickStrike.getBlockChance();
		} else if (swiftKick != null) {
			newChance += swiftKick.getBlockChance();
		}

		if (Math.random() > newChance / 100.0) {
			return false;
		} else if (nPlayer.isChiBlocked()) {
			return false;
		}

		return true;
	}

	public static void blockChi(final Player player) {
		if (Suffocate.isChannelingSphere(player)) {
			Suffocate.remove(player);
		}

		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer == null) {
			return;
		}

		nPlayer.blockChi();
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 2, 0);

		final long start = System.currentTimeMillis();
		new BukkitRunnable() {
			@Override
			public void run() {
				ActionBar.sendActionBar(Style.CHI.getColor() + "* Chiblocked *", player);
				if (System.currentTimeMillis() >= start + getDuration()) {
					nPlayer.unblockChi();
					this.cancel();
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	public static double getChance() {
		return ConfigManager.getConfig().getDouble("Abilities.Chi.Passive.BlockChi.Chance");
	}

	public static int getDuration() {
		return ConfigManager.getConfig().getInt("Abilities.Chi.Passive.BlockChi.Duration");
	}

	public static long getTicks() {
		return (getDuration() / 1000) * 20;
	}
}
