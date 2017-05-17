package com.hotmail.janie177.gametest.game.entities;

import com.hotmail.janie177.gametest.engine.world.Location;
import com.hotmail.janie177.gametest.game.entities.types.Monster;

public enum EntityType
{
	MONSTER("Monster", Monster.class);

	private Class<? extends Entity> entityClass;
	private String name;

	EntityType(String name, Class<? extends Entity> entityClass)
	{
		this.name = name;
		this.entityClass = entityClass;
	}

	public String getName()
	{
		return name;
	}

	public Class<? extends Entity> getEntityClass()
	{
		return entityClass;
	}

	public Entity createInstance(Location location)
	{
		Entity entity = null;
		try
		{
			entity = (Entity) Class.forName(this.getEntityClass().getName()).getConstructor(EntityType.class, Location.class).newInstance(this, location);
		}
		catch (Exception e)
		{
			System.out.println("Error while trying to create a new entity instance.");
			e.printStackTrace();
		}
		return entity;
 	}
}
