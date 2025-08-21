package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Represents one square unit of the maze grid.
 */
public class Tile {

    /**
     * The color of the maze walls
     */
    public static final Color CARNELLIAN = new Color(179, 27, 27);

    /**
     * The type of wall tile, specified by its shape (0=non-wall, 2=wall cap, 3=wall flat, 4=wall
     * joint) and rotation.
     */
    public record TileType(int shape, double rotation) {

    }

    /**
     * The type of this tile.
     */
    private final TileType type;

    /**
     * The horizontal position of this tile.
     */
    private final int i;

    /**
     * The vertical position of this tile.
     */
    private final int j;

    /**
     * The elevation of this tile.
     */
    private final double elevation;

    /**
     * Construct a new Tile at the given location `(i,j)` with the given `type` and `elevation`.
     */
    public Tile(TileType type, int i, int j, double elevation) {
        this.type = type;
        this.i = i;
        this.j = j;
        this.elevation = elevation;
    }

    /**
     * Draw this tile using the Graphics object `g` at the scaling specified by `tileDim`.
     */
    public void paint(Graphics2D g2) {
        int c = (int) (100 * elevation);
        g2.setColor(new Color(c, c, c));
        g2.fill(new Rectangle(i, j, 1, 1));

        g2.setColor(CARNELLIAN);

        double depth = 1.0 / 3; // half-thickness of wall
        g2.rotate(type.rotation() * Math.PI / 2, i + 0.5, j + 0.5);
        if (type.shape() == 2) {
            g2.fill(new Arc2D.Double(i - depth, j - depth, 2 * depth,
                    2 * depth, 270, 90, Arc2D.PIE));
            g2.setColor(CARNELLIAN.brighter());
            g2.draw(new Arc2D.Double(i - depth, j - depth, 2 * depth,
                    2 * depth, 270, 90, Arc2D.OPEN));
        } else if (type.shape() == 3) {
            g2.fill(new Rectangle2D.Double(i, j, 1, depth));
            g2.setColor(CARNELLIAN.brighter());
            g2.draw(new Line2D.Double(i, j + depth, i + 1, j + depth));
        } else if (type.shape() == 4) {
            Area fillArea = new Area(new Rectangle2D.Double(i, j, 1, 1));
            fillArea.subtract(new Area(
                    new RoundRectangle2D.Double(i - 1, j - 1, 2 - depth, 2 - depth, 2 * depth,
                            2 * depth)));
            g2.fill(fillArea);
            g2.setColor(CARNELLIAN.brighter());
            g2.draw(new Arc2D.Double(i + 1 - 3 * depth, j + 1 - 3 * depth, 2 * depth, 2 * depth,
                    270, 90, Arc2D.OPEN));
            g2.draw(new Line2D.Double(i, j + 1 - depth, i + 1 - 2 * depth, j + 1 - depth));
            g2.draw(new Line2D.Double(i + 1 - depth, j + 1 - 2 * depth, i + 1 - depth, j));
        }
        g2.rotate(type.rotation() * -Math.PI / 2, i + 0.5, j + 0.5);
    }
}
