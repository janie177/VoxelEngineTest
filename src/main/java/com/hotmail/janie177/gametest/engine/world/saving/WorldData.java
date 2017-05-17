package com.hotmail.janie177.gametest.engine.world.saving;

import java.io.Serializable;

public class WorldData implements Serializable
{
	private String name;
	private long seed;
	private long time;

	public WorldData(String name, long seed, long time)
	{
		this.name = name;
		this.seed = seed;
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
