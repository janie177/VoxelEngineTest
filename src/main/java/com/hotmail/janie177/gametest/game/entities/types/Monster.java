package com.hotmail.janie177.gametest.game.entities.types;

import com.hotmail.janie177.gametest.engine.entities.RenderingEntity;
import com.hotmail.janie177.gametest.engine.world.Location;
import com.hotmail.janie177.gametest.game.entities.Entity;
import com.hotmail.janie177.gametest.game.entities.EntityType;
import com.hotmail.janie177.gametest.engine.models.Models;

public class Monster extends Entity
{
	public Monster(EntityType type, Location location) {
		super(type, location);
	}

	@Override
	public RenderingEntity setRenderingEntity()
	{
		return new RenderingEntity(Models.MARKET_STALL.getTexturedModel(), getLocation(), 0, 0, 0, 1);
	}
}
