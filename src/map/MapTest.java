package me.blocksmc.annihilation.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MapTest extends MapRenderer {

    private ArrayList<Player> rendered = new ArrayList<>();

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        if(!rendered.contains(player)) {
            try {
                canvas.drawImage(0, 0, ImageIO.read(new URL("file:///C:/easter-egg.png")));
                rendered.add(player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
