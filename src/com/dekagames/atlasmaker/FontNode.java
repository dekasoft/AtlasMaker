package com.dekagames.atlasmaker;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class FontNode {
    public static final float OUTLINE_FACTOR = 8.0f;       // соответствие реальной толщины обводки к значению в спине

    public      String                      name;
    public      String                      familyName;     // название шрифта
    public      String                      glyphSet;
    public      ArrayList<GlyphNode>        elements;       // список элементов - кадров или глифов
    public	    int			                border;		    // размер границы из прозрачных пикселей
    public      int                         outline;        // размер обводки
    public      int                         size;           // размер шрифта
    public      boolean                     isBold,isItalic;


    public FontNode(String name){
        setName(name);
        elements = new ArrayList();
        border = 0;
        outline = 2;
        size = 20;
        isBold = true;
        isItalic = false;
    }

    /**
     * конструктор копирования
     * @param orig
     */
    public FontNode(FontNode orig){
        this.name = orig.name;
        elements = new ArrayList(orig.elements);
        border = orig.border;
        outline = orig.outline;
        size = orig.size;

        isBold = orig.isBold;
        isItalic = orig.isItalic;
        familyName = orig.familyName;
        glyphSet = orig.glyphSet;
    }

    /**
     * Задаем новое имя для спрайта
     * @param name
     */
    public final void setName(String name){
        if (name != null) this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }


    public GlyphNode getChildAt(int childIndex) {
        if (elements.size()!= 0 && childIndex< elements.size())
            return elements.get(childIndex);
        else
            return null;
    }

    public int getChildCount() {
        return elements.size();
    }

    // создаем (пересоздаем) все глифы - поддерживаем шрифт в актуальном состоянии
    public void recreateGlyphs(){
        GlyphVector glyphVector;
        Shape   shapeFilled, shapeOutline;
        Stroke stroke;
        GlyphMetrics glyphMetrics;
        char[] c = new char[1];
        int glyphHeight, glyphWidth, glyphOriginY;
        float   glyphLSB;

        int font_style = Font.PLAIN;
        if (isBold)
            font_style |= Font.BOLD;
        if (isItalic)
            font_style |= Font.ITALIC;
        // создадим шрифт
        Font font = new Font(familyName, font_style, size);

        elements = new ArrayList<GlyphNode>();
        for (int i=0; i<glyphSet.length(); i++){
            // создадим изображение в памяти, достаточно большого размера
            BufferedImage img = new BufferedImage((size+border)*2, (size+border)*2, BufferedImage.TYPE_INT_ARGB);
            // получим всякие вспомогательные хрени
            Graphics2D gr = img.createGraphics();
            // включим антиалиасинг
            gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gr.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            gr.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

            gr.setFont(font);
            FontRenderContext frc = gr.getFontRenderContext();

            c[0] = glyphSet.charAt(i);
            glyphVector = font.createGlyphVector(frc,c);

            glyphMetrics = glyphVector.getGlyphMetrics(0);
            glyphHeight = glyphMetrics.getBounds2D().getBounds().height;//rectangle.height;
            glyphWidth = glyphMetrics.getBounds2D().getBounds().width;//rectangle.width;

            // Пробелы не упаковываем в атлас!!!!!
            if (glyphHeight == 0 || glyphWidth == 0)
                continue;

            glyphOriginY = glyphMetrics.getBounds2D().getBounds().y;//rectangle.width;
            glyphLSB =  glyphMetrics.getLSB();   // расстояние от origin до левой границы символа. М.б. отрицательным.


            // рисуем сам символ белым цветом
            shapeFilled = glyphVector.getOutline();
            gr.translate(-glyphLSB + outline/OUTLINE_FACTOR+border, -glyphOriginY + outline/OUTLINE_FACTOR+border);
            gr.setPaint(Color.white);
            gr.fill(shapeFilled);
            gr.draw(shapeFilled);


            // если есть обводка, то создаем ее и рисуем черным цветом
            if (outline > 0){
                stroke = new BasicStroke(outline/OUTLINE_FACTOR);
                shapeOutline = stroke.createStrokedShape(shapeFilled);
                gr.setPaint(Color.black);
                gr.fill(shapeOutline);
                gr.draw(shapeOutline);
            }


            int w =  glyphWidth + 2 * (int)Math.ceil(outline/OUTLINE_FACTOR) + 2*border;
            int h =  glyphHeight + 2 * (int)Math.ceil(outline/OUTLINE_FACTOR) + 2*border;
            GlyphNode glyphNode = new GlyphNode(new String(c),img.getSubimage(0,0,w,h), w, h,
                                        glyphLSB+outline/OUTLINE_FACTOR,
                                        -glyphOriginY+outline/OUTLINE_FACTOR, glyphMetrics.getAdvance());
            elements.add(glyphNode);
        }
    }
}
