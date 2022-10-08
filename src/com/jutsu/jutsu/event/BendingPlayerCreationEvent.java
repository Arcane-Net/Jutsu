package com.projectkorra.projectkorra.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.Ninja;

/**
 * Called when a new Ninja is created
 */

public class BendingPlayerCreationEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Ninja nPlayer;

	public BendingPlayerCreationEvent(final Ninja nPlayer) {
		this.nPlayer = nPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * @return Ninja created
	 */
	public Ninja getBendingPlayer() {
		return this.nPlayer;
	}
}
