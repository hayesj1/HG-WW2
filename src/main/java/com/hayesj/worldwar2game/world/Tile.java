package com.hayesj.worldwar2game.world;

/**
 * Created by Jacob Hayes on 6/12/2017.
 *
 * @author Jacob Hayes
 */
public interface Tile {
	float TILE_SIZE = 0.5f;


	float getArea();
	Tile[] getAdjacentTiles();

	boolean isAdjacentTo(Tile other);
	boolean isDiagonalTo(Tile other);
}
