package com.hayesj.worldwar2game.util;

import com.jme3.input.InputManager;
import com.jme3.math.FastMath;

/**
 * Created by Jacob Hayes on 5/16/2017.
 *
 * @author Jacob Hayes
 */
public interface Directions {
	enum Direction { LEFT, RIGHT, UP, DOWN; }
	enum CardinalDirection { WEST, EAST, NORTH, SOUTH; }
	enum DisplayEdge { LEFT, RIGHT, TOP, BOTTOM; }

	static Direction toDirection(CardinalDirection dir) { return Direction.values()[dir.ordinal()]; }
	static Direction toDirection(DisplayEdge edge) { return Direction.values()[edge.ordinal()]; }

	static DisplayEdge toDisplayEdge(Direction dir) { return DisplayEdge.values()[dir.ordinal()]; }
	static DisplayEdge toDisplayEdge(CardinalDirection dir) { return DisplayEdge.values()[dir.ordinal()]; }

	static CardinalDirection toCardinal(Direction dir) { return CardinalDirection.values()[dir.ordinal()]; }
	static CardinalDirection toCardinal(DisplayEdge edge) { return CardinalDirection.values()[edge.ordinal()]; }

	static boolean equal(CardinalDirection cardDir, DisplayEdge edge) { return toDirection(cardDir).equals(toDirection(edge)); }
	static boolean equal(Direction dir, DisplayEdge edge) { return dir.equals(toDirection(edge)); }
	static boolean equal(Direction dir, CardinalDirection cardDir) { return dir.equals(toDirection(cardDir)); }

	static Directions.DisplayEdge CursorOnEdge(InputManager manager, float width, float height) { return CursorOnEdge(manager, width, height, 1.0f); }
	static Directions.DisplayEdge CursorOnEdge(InputManager manager, float width, float height, float fuzzySize) {
		if (fuzzySize < 0) {
			fuzzySize = FastMath.abs(fuzzySize);
		}

		if (manager.getCursorPosition().getX() <= fuzzySize) {
			return Directions.DisplayEdge.LEFT;
		} else if (manager.getCursorPosition().getX() >= (width - fuzzySize)) {
			return Directions.DisplayEdge.RIGHT;
		} else if (manager.getCursorPosition().getY() >= (height - fuzzySize)) {
			return Directions.DisplayEdge.TOP;
		} else if (manager.getCursorPosition().getY() <= fuzzySize) {
			return Directions.DisplayEdge.BOTTOM;
		} else {
			return null;
		}
	}

}
