package util;

/**
 * Represents a map to be used by a game of PacMann.  A map is a rectangular 2D array of tiles
 * (whose types are specified by `types`), each with an elevation (given by `elevations`).
 */
public record GameMap(MazeGenerator.TileType[][] types, double[][] elevations) {

}
