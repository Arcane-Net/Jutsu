package com.projectkorra.projectkorra.ability.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;

public class MultiAbilityManager {

	public static Map<Player, HashMap<Integer, String>> playerAbilities = new ConcurrentHashMap<>();
	public static Map<Player, Integer> playerSlot = new ConcurrentHashMap<>();
	public static Map<Player, String> playerBoundAbility = new ConcurrentHashMap<>();
	public static ArrayList<MultiAbilityInfo> multiAbilityList = new ArrayList<MultiAbilityInfo>();

	public MultiAbilityManager() {
		final ArrayList<MultiAbilityInfoSub> waterArms = new ArrayList<MultiAbilityInfoSub>();
		waterArms.add(new MultiAbilityInfoSub("Pull", Style.WATER));
		waterArms.add(new MultiAbilityInfoSub("Punch", Style.WATER));
		waterArms.add(new MultiAbilityInfoSub("Grapple", Style.WATER));
		waterArms.add(new MultiAbilityInfoSub("Grab", Style.WATER));
		waterArms.add(new MultiAbilityInfoSub("Freeze", Style.ICE));
		waterArms.add(new MultiAbilityInfoSub("Spear", Style.ICE));
		multiAbilityList.add(new MultiAbilityInfo("WaterArms", waterArms));
	}

	/**
	 * Sets up a player's binds for a MultiAbility.
	 *
	 * @param player Player having the multiability bound
	 * @param multiAbility MultiAbility being bound
	 */
	public static void bindMultiAbility(final Player player, final String multiAbility) {
		if (!player.isOnline()) {
			return;
		}
		
		final PlayerBindChangeEvent event = new PlayerBindChangeEvent(player, multiAbility, 0, true, true);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		if (playerAbilities.containsKey(player)) {
			unbindMultiAbility(player);
		}
		
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		
		playerSlot.put(player, player.getInventory().getHeldItemSlot());
		playerBoundAbility.put(player, multiAbility);
		playerAbilities.put(player, new HashMap<Integer, String>(nPlayer.getAbilities()));

		final List<MultiAbilityInfoSub> modes = getMultiAbility(multiAbility).getAbilities();

		nPlayer.getAbilities().clear();
		for (int i = 0; i < modes.size(); i++) {
			if (!player.hasPermission("jutsu.ability." + multiAbility + "." + modes.get(i).getName())) {
				nPlayer.getAbilities().put(i + 1, new StringBuilder().append(modes.get(i).getAbilityColor()).append(ChatColor.STRIKETHROUGH).append(modes.get(i).getName()).toString());
			} else {
				nPlayer.getAbilities().put(i + 1, modes.get(i).getAbilityColor() + modes.get(i).getName());
			}
		}
		
		player.getInventory().setHeldItemSlot(0);
	}

	/**
	 * Returns the MultiAbility the player has bound. Returns null if no
	 * multiability is bound and active.
	 *
	 * @param player The player to use
	 * @return name of multi ability bounded
	 */
	public static String getBoundMultiAbility(final Player player) {
		if (playerBoundAbility.containsKey(player)) {
			return playerBoundAbility.get(player);
		}
		return null;
	}

	/**
	 * Returns a MultiAbility based on name.
	 *
	 * @param multiAbility Name of the multiability
	 * @return the multiability object or null
	 */
	public static MultiAbilityInfo getMultiAbility(final String multiAbility) {
		for (final MultiAbilityInfo ma : multiAbilityList) {
			if (ma.getName().equalsIgnoreCase(multiAbility)) {
				return ma;
			}
		}
		return null;
	}

	/**
	 * Returns a boolean based on whether a player has a MultiAbility active.
	 *
	 * @param player The player to check
	 * @return true If player has a multiability active
	 */
	public static boolean hasMultiAbilityBound(final Player player) {
		if (playerAbilities.containsKey(player)) {
			return true;
		}
		return false;
	}

	/**
	 * MultiAbility equivalent of
	 * {@link Ninja#getBoundAbility()}. Returns a boolean based
	 * on whether a player has a specific MultiAbility active.
	 *
	 * @param player The player to check
	 * @param multiAbility The multiability name
	 * @return true If player has the specified multiability active
	 */
	public static boolean hasMultiAbilityBound(final Player player, final String multiAbility) {
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer == null) {
			return false;
		}

		return playerAbilities.containsKey(player) && playerBoundAbility.get(player).equals(multiAbility);
	}

	/**
	 * Clears all MultiAbility data for a player. Called on player quit event.
	 *
	 * @param player
	 */
	public static void remove(final Player player) {
		playerAbilities.remove(player);
		playerBoundAbility.remove(player);
		playerSlot.remove(player);
	}

	/**
	 * Cleans up all MultiAbilities.
	 */
	public static void removeAll() {
		playerAbilities.clear();
		playerSlot.clear();
		playerBoundAbility.clear();
	}

	/**
	 * Keeps track of the player's selected slot while a MultiAbility is active.
	 */
	public static boolean canChangeSlot(final Player player, int slot) {
		if (playerAbilities.isEmpty()) {
			return true;
		}
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer != null) {
			if (nPlayer.getBoundAbility() == null && multiAbilityList.contains(getMultiAbility(playerBoundAbility.getOrDefault(player, "")))) {
				return slot < getMultiAbility(playerBoundAbility.get(player)).getAbilities().size();
			}
		}
		return true;
	}

	/**
	 * Reverts a player's binds to a previous state before use of a
	 * MultiAbility.
	 *
	 * @param player
	 */
	public static void unbindMultiAbility(final Player player) {
		if (!player.isOnline()) {
			return;
		}
		
		playerAbilities.compute(player, MultiAbilityManager::resetBinds);
		playerBoundAbility.remove(player);
		playerSlot.remove(player);
	}
	
	private static HashMap<Integer, String> resetBinds(Player player, HashMap<Integer, String> prevBinds) {
		if (prevBinds == null) {
			return null;
		}
		
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer == null) {
			return null;
		}
		
		player.getInventory().setHeldItemSlot(playerSlot.getOrDefault(player, 0));
		ProjectKorra.plugin.getServer().getPluginManager().callEvent(new PlayerBindChangeEvent(player, playerBoundAbility.get(player), false, true));

		for (int i = 1; i < 10; i++) {
			nPlayer.getAbilities().put(i, prevBinds.get(i));
		}
		
		return null;
	}

	/**
	 * MultiAbility class. Manages each MultiAbility's sub abilities.
	 *
	 */
	public static class MultiAbilityInfo {
		private String name;
		private ArrayList<MultiAbilityInfoSub> abilities;

		public MultiAbilityInfo(final String name, final ArrayList<MultiAbilityInfoSub> abilities) {
			this.name = name;
			this.abilities = abilities;
		}

		public ArrayList<MultiAbilityInfoSub> getAbilities() {
			return this.abilities;
		}

		public String getName() {
			return this.name;
		}

		public void setAbilities(final ArrayList<MultiAbilityInfoSub> abilities) {
			this.abilities = abilities;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}

	public static class MultiAbilityInfoSub {
		private String name;
		private Style style;

		public MultiAbilityInfoSub(final String name, final Style style) {
			this.name = name;
			this.style = style;
		}

		public Style getElement() {
			return this.style;
		}

		public String getName() {
			return this.name;
		}

		public void setElement(final Style style) {
			this.style = style;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public ChatColor getAbilityColor() {
			return this.style != null ? this.style.getColor() : null;
		}
	}

}
