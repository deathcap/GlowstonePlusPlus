package net.glowstone.map;

import java.awt.Image;
import net.glowstone.net.message.play.game.MapDataMessage.Section;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 * Represents a canvas for drawing to a map. Each canvas is associated with a specific {@link org.bukkit.map.MapRenderer} and represents that renderer's layer on the map.
 */
public final class GlowMapCanvas implements MapCanvas {

    public static final int MAP_SIZE = 128;
    private final byte[] buffer = new byte[MAP_SIZE * MAP_SIZE];
    private final MapView mapView;
    private MapCursorCollection cursors = new MapCursorCollection();
    private byte[] base;

    /**
     * Creates a new GlowMapCanvas for the given {@link MapView} and renders the contents seen by
     * the given player according to all the MapView's renderers.
     *
     * @param mapView The {@link MapView} to associate with this canvas and render
     * @param player The player to pass to {@link MapRenderer#render(MapView, MapCanvas, Player)}
     * @return a new, rendered {@link GlowMapCanvas}
     */
    public static GlowMapCanvas createAndRender(MapView mapView, Player player) {
        GlowMapCanvas out = new GlowMapCanvas(mapView);
        for (MapRenderer renderer : mapView.getRenderers()) {
            renderer.initialize(mapView);
            renderer.render(mapView, out, player);
        }
        return out;
    }

    protected GlowMapCanvas(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public MapView getMapView() {
        return mapView;
    }

    @Override
    public MapCursorCollection getCursors() {
        return cursors;
    }

    @Override
    public void setCursors(MapCursorCollection cursors) {
        this.cursors = cursors;
    }

    @Override
    public void setPixel(int x, int y, byte color) {
        if (x < 0 || y < 0 || x >= MAP_SIZE || y >= MAP_SIZE) {
            return;
        }
        if (buffer[y * MAP_SIZE + x] != color) {
            buffer[y * MAP_SIZE + x] = color;
            // todo: mark dirty
        }
    }

    @Override
    public byte getPixel(int x, int y) {
        if (x < 0 || y < 0 || x >= MAP_SIZE || y >= MAP_SIZE) {
            return 0;
        }
        return buffer[y * MAP_SIZE + x];
    }

    @Override
    public byte getBasePixel(int x, int y) {
        if (x < 0 || y < 0 || x >= MAP_SIZE || y >= MAP_SIZE) {
            return 0;
        }
        return base[y * MAP_SIZE + x];
    }

    protected void setBase(byte... base) {
        this.base = base;
    }

    protected byte[] getBuffer() {
        return buffer;
    }

    @Override
    public void drawImage(int x, int y, Image image) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void drawText(int x, int y, MapFont font, String text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Converts a snapshot of this canvas to a {@link Section} for transmission to the client.
     *
     * @return a {@link Section} holding a copy of this canvas's contents
     */
    public Section toSection() {
        return new Section(MAP_SIZE, MAP_SIZE, mapView.getCenterX(), mapView.getCenterZ(), buffer.clone());
    }
}
