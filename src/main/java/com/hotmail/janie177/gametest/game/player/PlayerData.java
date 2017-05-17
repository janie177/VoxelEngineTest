package com.hotmail.janie177.gametest.game.player;

import com.hotmail.janie177.gametest.engine.world.Location;

import java.io.Serializable;

public class PlayerData implements Serializable
{
	private Location location;

	public PlayerData(Location location)
	{
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
