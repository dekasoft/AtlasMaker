package com.dekagames.atlasmaker;

import com.dekagames.atlasmaker.algorithm.PictureRect;
import java.awt.image.BufferedImage;

public class GlyphNode {
    public String           glyph;          // строка из одного символа
    public BufferedImage    image;	        // картинка в памяти
    public PictureRect      pictureRect;    // rect для упаковки - инициализируется при передаче в процедуру
                                            // упаковки. Здесь же после упаковки находятся найденные координаты
    public int              width, height;
    public float            lsb;            // смещение символа по X
    public float            originY;        // смещение символа по Y
    public float            advance;        // расстояние от origin до левой границы символа. М.б. отрицательным.

    public GlyphNode(String glyph, BufferedImage img, int w, int h, float lsb, float origY, float advance){
        this.glyph = glyph;
        width = w;
        height = h;
        this.lsb = lsb;
        this.originY = origY;
        this.advance = advance;

        image = img;
    }

    public PictureRect buildPictureRect(){
        pictureRect = new PictureRect(width, height);
        return pictureRect;
    }

}
