package com.hotmail.janie177.gametest.engine.world.generation;

public interface BiomeType
{
	/**
	 * Get the averate height of the terrain in this biome./
	 * @return
	 */
	float getBaseTerrainHeight();

	float getTerrainVerticalNoise();

	float getTerrainCaveSize();
}
