package com.hotmail.janie177.gametest.engine.entities;

import com.hotmail.janie177.gametest.engine.world.Location;
import org.joml.Vector3f;

public class Light extends AbstractRenderingEntity
{
	private Vector3f colour = new Vector3f(1, 1, 1);

	public Light(Location location)
	{
		super(location);
	}

	public Vector3f getColour() {
		return colour;
	}

	public void setColour(Vector3f colour) {
		this.colour = colour;
	}

	public void setColour(float r, float g, float b) {
		colour.x = r;
		colour.y = g;
		colour.z = b;
	}
}
