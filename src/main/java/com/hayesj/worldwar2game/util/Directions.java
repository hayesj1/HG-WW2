package com.hayesj.worldwar2game.util;

import com.jme3.input.InputManager;

import java.util.EnumSet;

/**
 * Created by Jacob Hayes on 5/16/2017.
 *
 * @author Jacob Hayes
 */
public interface Directions {
	enum Direction { LEFT, RIGHT, UP, DOWN, CENTER}
	enum CardinalDirection { WEST, EAST, NORTH, SOUTH, NONE}

	static Direction toDirection(CardinalDirection dir) { return Direction.values()[dir.ordinal()]; }
	static Direction toDirection(ScreenRegion.Region edge) { return Direction.values()[edge.ordinal()]; }

	static ScreenRegion.Region toDisplayEdge(Direction dir) { return ScreenRegion.Region.values()[dir.ordinal()]; }
	static ScreenRegion.Region toDisplayEdge(CardinalDirection dir) { return ScreenRegion.Region.values()[dir.ordinal()]; }

	static CardinalDirection toCardinal(Direction dir) { return CardinalDirection.values()[dir.ordinal()]; }
	static CardinalDirection toCardinal(ScreenRegion.Region edge) { return CardinalDirection.values()[edge.ordinal()]; }

	static boolean equal(CardinalDirection cardDir, ScreenRegion.Region edge) { return toDirection(cardDir).equals(toDirection(edge)); }
	static boolean equal(Direction dir, ScreenRegion.Region edge) { return dir.equals(toDirection(edge)); }
	static boolean equal(Direction dir, CardinalDirection cardDir) { return dir.equals(toDirection(cardDir)); }

	static EnumSet<ScreenRegion.Region> isCursorOnEdge(InputManager manager, ScreenRegion screen) {
		EnumSet<ScreenRegion.Region> ret = screen.regionsContaining(manager.getCursorPosition());
		return ret;
	}

}
