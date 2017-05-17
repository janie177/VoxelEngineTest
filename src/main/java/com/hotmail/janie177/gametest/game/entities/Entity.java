package com.hotmail.janie177.gametest.game.entities;

import com.hotmail.janie177.gametest.engine.entities.RenderingEntity;
import com.hotmail.janie177.gametest.engine.world.Location;

import java.io.Serializable;
import java.util.UUID;

public abstract class Entity
{
	private Location location;
	private RenderingEntity renderingEntity;
	private UUID uuid;
	private EntityType type;
	private long age;

	public Entity(EntityType type, Location location)
	{
		this.location = location;
		this.renderingEntity = setRenderingEntity();
		this.uuid = UUID.randomUUID();
		this.type = type;
		this.age = System.currentTimeMillis();
	}


	/** -- The abstract methods -- **/



	/**
	 * Set the rendering entity for this entity.
	 * @return The rendering entity. Is always called upon creating an instance.
	 */
	public abstract RenderingEntity setRenderingEntity();





	/** -- The default methods -- **/




	/**
	 * Set the location for this entity. Automatically updates the corresponding model.
	 * @param newLocation The new location.
	 */
	public void setLocation(Location newLocation)
	{
		//Cant change entities world. Would complicate saving a lot. Maybe allow it in the future.
		if(location.getWorld() != newLocation.getWorld()) return;

		this.location = newLocation;
		renderingEntity.setLocation(location);
	}

	/**
	 * Add to the location of this entity. Automatically updates the model.
	 * @param x The amount of x to add.
	 * @param y The amount of y to add.
	 * @param z The amount of z to add.
	 */
	public void addLocation(int x, int y, int z)
	{
		location.add(x, y, z);
		renderingEntity.setLocation(location);
	}

	/**
	 * Set the scale for this entity.
	 * @param scale The scale of the entity.
	 */
	public void setScale(float scale)
	{
		renderingEntity.setScale(scale);
	}


	/**
	 * Get the X rotation of this object.
	 * @return The X rotation.
	 */
	public float getRotX()
	{
		return renderingEntity.getRotX();
	}

	/**
	 * Get the Y rotation of this object.
	 * @return The Y rotation.
	 */
	public float getRotY()
	{
		return renderingEntity.getRotX();
	}

	/**
	 * Get the Z rotation of this object.
	 * @return The Z rotation.
	 */
	public float getRotZ()
	{
		return renderingEntity.getRotX();
	}

	public float getScale()
	{
		return renderingEntity.getScale();
	}



	/**
	 * Set the X rotation of this entity.
	 * @param rotation The x rotation.
	 */
	public void setRotX(float rotation)
	{
		renderingEntity.setRotX(rotation);
	}

	/**
	 * Set the Y rotation of this entity.
	 * @param rotation The y rotation.
	 */
	public void setRotY(float rotation)
	{
		renderingEntity.setRotY(rotation);
	}

	/**
	 * Set the Z rotation of this entity.
	 * @param rotation The z rotation.
	 */
	public void setRotZ(float rotation)
	{
		renderingEntity.setRotZ(rotation);
	}

	/**
	 * Get the location of this entity.
	 * @return The location of the entity.
	 */
	public Location getLocation()
	{
		return location;
	}

	/**
	 * Get the rendering entity instance for this entity. Can be used to retrieve raw model and texture data.
	 * @return The rendering entity belonging to this entity.
	 */
	public RenderingEntity getRenderingEntity()
	{
		return renderingEntity;
	}

	public UUID getUuid() {
		return uuid;
	}

	public EntityType getType() {
		return type;
	}

	/**
	 * Get how long this entity has existed in seconds.
	 * @return The age of the entity.
	 */
	public long getAge()
	{
		return (System.currentTimeMillis() - age) / 1000;
	}
}
