package com.projectkorra.projectkorra.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.Style;

/**
 * Called when a player's jutsu style is modified
 */
public class PlayerChangeElementEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final CommandSender sender;
	private final Player target;
	private final Style style;
	private final Result result;

	/**
	 *
	 * @param sender the {@link CommandSender} who changed the player's jutsu
	 * @param target the {@link Player player} who's jutsu was changed
	 * @param style the {@link Style style} that was affected
	 * @param result whether the style was chosen, added, removed, or
	 *            permaremoved
	 */
	public PlayerChangeElementEvent(final CommandSender sender, final Player target, final Style style, final Result result) {
		this.sender = sender;
		this.target = target;
		this.style = style;
		this.result = result;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 *
	 * @return the {@link CommandSender} who changed the player's jutsu
	 */
	public CommandSender getSender() {
		return this.sender;
	}

	/**
	 *
	 * @return the {@link Player player} who's jutsu was changed
	 */
	public Player getTarget() {
		return this.target;
	}

	/**
	 *
	 * @return the {@link Style style} that was affected
	 */
	public Style getElement() {
		return this.style;
	}

	/**
	 *
	 * @return whether the style was chosen, added, removed, or permaremoved
	 */
	public Result getResult() {
		return this.result;
	}

	public static enum Result {
		CHOOSE, REMOVE, ADD, PERMAREMOVE;
		private Result() {}
	}

}
