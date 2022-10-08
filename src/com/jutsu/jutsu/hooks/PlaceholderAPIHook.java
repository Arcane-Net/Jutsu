package com.projectkorra.projectkorra.hooks;

import static java.util.stream.Collectors.joining;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Ninja;
import com.projectkorra.projectkorra.Style;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion {

	private final ProjectKorra plugin;

	public PlaceholderAPIHook(final ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public String onPlaceholderRequest(final Player player, final String params) {
		final Ninja nPlayer = Ninja.getBendingPlayer(player);
		if (nPlayer == null) {
			return "";
		}

		if (params.startsWith("slot")) {
			final String ability = nPlayer.getAbilities().get(Integer.parseInt(params.substring(params.length() - 1)));
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil == null) {
				return "";
			}
			return coreAbil.getElement().getColor() + coreAbil.getName();
		} else if (params.equals("style") || params.equals("elementcolor")) {
			String e = "Nonbender";
			ChatColor c = ChatColor.WHITE;
			if (player.hasPermission("jutsu.avatar") || (nPlayer.hasElement(Style.AIR) && nPlayer.hasElement(Style.EARTH) && nPlayer.hasElement(Style.FIRE) && nPlayer.hasElement(Style.WATER))) {
				c = Style.AVATAR.getColor();
				e = Style.AVATAR.getName();
			} else if (nPlayer.getElements().size() > 0) {
				c = nPlayer.getElements().get(0).getColor();
				e = nPlayer.getElements().get(0).getName();
			}
			if (params.equals("style")) {
				return e;
			} else {
				return c.toString();
			}
		} else if (params.equals("styles")) {
			return nPlayer.getElements().stream().map(item -> item.getColor() + item.getName()).collect(joining(" "));
		}

		return null;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getAuthor() {
		return this.plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return "ProjectKorra";
	}

	@Override
	public String getVersion() {
		return this.plugin.getDescription().getVersion();
	}
}
