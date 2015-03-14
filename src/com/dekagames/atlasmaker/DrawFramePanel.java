package com.dekagames.atlasmaker;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 18.08.13
 * Time: 23:56
 * To change this template use File | Settings | File Templates.
 */

/**
 * Панель для рисования кадра в диалоге редактирования спрайта
 */
public class DrawFramePanel extends JPanel {
    public      BufferedImage   bufImage;
    public      int			    xPivot, yPivot;
    private     BufferedImage	imgPivot;

    public DrawFramePanel(){
        super();
        this.setPreferredSize(new Dimension(1024,1024));
        // сделаем изображение горячей точки
        imgPivot = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
        Graphics g = imgPivot.getGraphics();
        g.drawRect(8, 8, 16, 16);
        g.drawLine(16, 0, 16, 32);
        g.drawLine(0, 16, 32, 16);
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        if (bufImage!=null){
            this.setPreferredSize(new Dimension(bufImage.getWidth(), bufImage.getHeight()));
            g.drawImage(bufImage, 0, 0, null);
            g.drawImage(imgPivot, xPivot-16, yPivot-16, null);
        }
    }

}
