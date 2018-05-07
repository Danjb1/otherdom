package com.danjb.otherdom.client.scene;

import com.danjb.otherdom.client.MousePicker;
import com.danjb.otherdom.client.Texture;

public class Player {

    private static final Texture TEXTURE = new Texture("player.png", true);
    
    private BillboardModel model;
    
    /**
     * Height, in metres.
     * 
     * This results in a rendered player height of ~1.75m due to empty space in
     * the image.
     */
    private static final float HEIGHT = 2.0f;

    private int sectionX, sectionY;
    
    public Player(int tileX, int tileZ, Camera camera) {
        model = new BillboardModel(
                tileX * TerrainSection.TILE_WIDTH,
                tileZ * TerrainSection.TILE_WIDTH,
                TEXTURE, HEIGHT, camera, MousePicker.CODE_PLAYER);
    }

    public void update() {
        model.update();
    }
    
    public int getSectionX() {
        return sectionX;
    }

    public int getSectionZ() {
        return sectionY;
    }

    public BillboardModel getModel() {
        return model;
    }
    
}
