package com.dekagames.atlasmaker;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 15.08.13
 * Time: 0:26
 * To change this template use File | Settings | File Templates.
 */
public class AtlasPanel extends JPanel {
    private final static int CELL_W = 20;	// размер ячейки сетки

    public      BufferedImage   atlasImage;
    private     BufferedImage   grid;

    public AtlasPanel(){
        super();
        this.setPreferredSize(new Dimension(1024,1024));
    }

    // рисуем шашечки прозрачного фона
    private void draw_grid(Graphics g){
        Graphics gr;
        if (atlasImage != null){
            grid = new BufferedImage(atlasImage.getWidth(),atlasImage.getHeight(),BufferedImage.TYPE_INT_ARGB);
            gr = grid.getGraphics();

            gr.setColor(Color.WHITE);
            gr.fillRect(0, 0, atlasImage.getWidth(), atlasImage.getHeight());
            gr.setColor(new Color(220, 220, 220));
            for (int i=0; i<atlasImage.getWidth(); i+=2*CELL_W){
                // рисовать будем по две вертикальные линии за проход
                for (int j=0; j<atlasImage.getHeight(); j+=(2*CELL_W))
                    gr.fillRect(i, j, CELL_W, CELL_W);
                for (int j=CELL_W; j<atlasImage.getHeight(); j+=(2*CELL_W))
                    gr.fillRect(i+CELL_W, j, CELL_W, CELL_W);
            }
            g.drawImage(grid, 0, 0, null);
        }
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        if (atlasImage != null)
            draw_grid(g);
        g.drawImage(atlasImage, 0, 0, null);
    }
}
