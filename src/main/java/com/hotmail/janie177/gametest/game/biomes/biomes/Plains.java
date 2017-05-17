package com.hotmail.janie177.gametest.game.biomes.biomes;

import com.hotmail.janie177.gametest.engine.world.generation.BiomeType;

public class Plains implements BiomeType {

	@Override
	public float getBaseTerrainHeight() {
		return 0;
	}

	@Override
	public float getTerrainVerticalNoise() {
		return 0;
	}

	@Override
	public float getTerrainCaveSize() {
		return 0;
	}
}
