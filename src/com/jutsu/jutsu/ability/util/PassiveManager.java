package com.projectkorra.projectkorra.ability.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.Style.SubElement;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;

public class PassiveManager {

	private static final Map<String, CoreAbility> PASSIVES = new HashMap<>();
	private static final Map<PassiveAbility, Class<? extends CoreAbility>> PASSIVE_CLASSES = new HashMap<>();

	public static void registerPassives(final Player player) {
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer == null) {
			return;
		}
		for (final CoreAbility ability : PASSIVES.values()) {
			if (ability instanceof PassiveAbility) {
				if (!hasPassive(player, ability)) {
					continue;
				} else if (CoreAbility.hasAbility(player, ability.getClass())) {
					continue;
					/*
					 * Passive's such as not taking fall damage are managed in
					 * PKListener, so we do not want to create instances of them
					 * here. This just enables the passive to be displayed in /b
					 * d [style]passive
					 */
				}

				if (!((PassiveAbility) ability).isInstantiable()) {
					continue;
				}

				try {
					final Class<? extends CoreAbility> clazz = PASSIVE_CLASSES.get(ability);
					final Constructor<?> constructor = clazz.getConstructor(Player.class);
					final Object object = constructor.newInstance(player);
					((CoreAbility) object).start();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean hasPassive(final Player player, final CoreAbility passive) {
		if (player == null) {
			return false;
		} else if (passive == null) {
			return false;
		}
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		Style style = passive.getElement();
		if (passive.getElement() instanceof SubElement) {
			style = ((SubElement) passive.getElement()).getParentElement();
		}
		if (nPlayer == null) {
			return false;
		} else if (!(passive instanceof PassiveAbility)) {
			return false;
		} else if (!passive.isEnabled()) {
			return false;
		} else if (!nPlayer.canBendPassive(passive)) {
			return false;
		} else if (!nPlayer.isToggled()) {
			return false;
		} else if (!nPlayer.isElementToggled(style)) {
			return false;
		} else if (!nPlayer.isPassiveToggled(style)) {
			return false;
		} else if (!nPlayer.isToggledPassives()) {
			return false;
		}
		return true;
	}

	public static Set<String> getPassivesForElement(final Style style) {
		final Set<String> passives = new HashSet<>();
		for (final CoreAbility passive : PASSIVES.values()) {
			if (passive.getElement() == style) {
				passives.add(passive.getName());
			} else if (passive.getElement() instanceof SubElement) {
				final Style check = ((SubElement) passive.getElement()).getParentElement();
				if (check == style) {
					passives.add(passive.getName());
				}
			}
		}
		return passives;
	}

	public static Map<String, CoreAbility> getPassives() {
		return PASSIVES;
	}

	public static Map<PassiveAbility, Class<? extends CoreAbility>> getPassiveClasses() {
		return PASSIVE_CLASSES;
	}

}
