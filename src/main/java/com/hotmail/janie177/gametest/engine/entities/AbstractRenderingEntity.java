package com.hotmail.janie177.gametest.engine.entities;
import com.hotmail.janie177.gametest.engine.world.Location;

import java.util.UUID;

public abstract class AbstractRenderingEntity
{
	/**
	 * This is the basic class that all Entities have in common.
	 */

	//Every entity has a position.
	private Location location;
	private UUID uuid;

	public AbstractRenderingEntity(Location location)
	{
		this.location = location;
		uuid = UUID.randomUUID();
	}

	public Location getLocation()
	{
		return location;
	}

	public void setLocation(Location location)
	{
		this.location = location;
	}

	public UUID getUuid() {
		return uuid;
	}
}
