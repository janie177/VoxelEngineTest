package com.hotmail.janie177.gametest.game.biomes;

import com.hotmail.janie177.gametest.engine.world.generation.BiomeType;
import com.hotmail.janie177.gametest.game.biomes.biomes.Plains;

public enum Biome
{
	PLAINS(new Plains());

	private BiomeType biome;

	Biome(BiomeType biomeType)
	{
		this.biome = biomeType;
	}
}
