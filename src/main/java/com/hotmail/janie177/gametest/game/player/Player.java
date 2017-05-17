package com.hotmail.janie177.gametest.game.player;

import com.hotmail.janie177.gametest.engine.entities.Camera;
import com.hotmail.janie177.gametest.engine.world.Location;
import com.hotmail.janie177.gametest.engine.world.World;
import com.hotmail.janie177.gametest.game.entities.Entity;
import org.joml.Vector3f;

public class Player extends Camera
{
	public Player(Location location)
	{
		super(location);
	}

	/**
	 * All player attributes.
	 */

	/**
	 * Get the world the player is in.
	 * @return The world the player is in.
	 */
	public World getWorld()
	{
		return getLocation().getWorld();
	}


	/**
	 * Set a players location.
	 * WARNING: Only call this for teleportation. For walking, use the updateLocationNaturally function.
	 * @param location The location to set the player to.
	 */
	@Override
	public void setLocation(Location location)
	{
		//Set the players location to the new location.
		getLocation().changeToLocation(location);
	}


	/**
	 * Get a vector pointing in the direction the player is looking in.
	 * @return a new vector pointing at what the player is looking at.
	 */
	public Vector3f getViewDirection()
	{
		float yawR = (float) Math.toRadians(getYaw() - 90), pitchR = (float) Math.toRadians(getPitch());
		float x = (float)(Math.cos(yawR) * Math.cos(pitchR));
		float z = (float)(Math.sin(yawR) * Math.cos(pitchR));
		//float y = -(float) (Math.sin(pitchR));
		return new Vector3f(x, 0, z).normalize();
	}


	public PlayerData getPlayerData()
	{
		return new PlayerData(getLocation());
	}

	public void setPlayerData(PlayerData data)
	{
		setLocation(data.getLocation());
	}
}
