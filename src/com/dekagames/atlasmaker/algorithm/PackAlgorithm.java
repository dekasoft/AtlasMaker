package com.dekagames.atlasmaker.algorithm;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 18.08.13
 * Time: 0:58
 * To change this template use File | Settings | File Templates.
 */

import com.dekagames.atlasmaker.*;

import java.awt.*;
import java.util.ArrayList;

/**Алгоритм упаковки вынесен в отдельный класс
 * Краткое описание работы алгоритма упаковки:
 * На вход подаются размер атласа, и список подготовленных прямоугольников картинок
 * с учетом кропа и границы. Список прямоугольников сортируется по площади от большей к меньшей.
 * Также в алгоритме существует список пустых прямоугольников - изначально он инициализируется
 * всем атласом. Список пустых прямоугольников всегда отсортировывается по площади от меньшей к большей.
 * На каждой итерации цикла упаковки проделываются следующие операции.
 * 1. Из списка прямоугольников картинок выбирается первая (наибольшая) и последовательно пытается поместиться в пустые
 * прямоугольники из списка, начиная с наименьшего до успеха или фейла. В случае фейла, картинка помечается как
 * непомещенная в атлас и выкидывается из дальнейшей обработки.
 * 2. После помещения картинки в пустой прямоугольник, он разбивается на два других двумя вариантами и из двух
 * вариантов разбиения выбирается тот, в котором соотношение большего получившегося прямоугольника к меньшему наибольшее.
 * Два полученных пустых прямоугольника заменяют собой первоначальный в списке пустых прямоугольников и список
 * пересортировывается.
 * 3. Процесс повторяется с п.1 до тех пор пока в списке прямоугольников картинок еще что-то есть.
 */
public class PackAlgorithm {

    private     ArrayList<PictureRect>  pics;	        // список картинок, подлежащих упаковке
    private	    ArrayList<String>	    failedFiles;    // картинки, которые не поместились в атлас
    private	    EmptyRectList		    emptyList;	    // список пустых прямоугольников
    private     SpriteTree              spriteTree;	    // дерево с иерархией картинок
    private     FontTree                fontTree;       // дерево шрифтов


    public PackAlgorithm(SpriteTree spr_tree, FontTree fnt_tree, int w, int h){
        spriteTree = spr_tree;
        fontTree = fnt_tree;
        emptyList = new EmptyRectList(w,h);
        pics = new ArrayList();
    }


    public String[] start(){
        String[] retstr;
        // сформируем список картинок, которые будем упаковывать
        make_pics_list();
        // сортируем полученный список картинок
        sort_pics_list();

        // в цикле будем помещать картинки в атлас, пока картинки не кончатся
        failedFiles = new ArrayList();
        for (int i=0; i<pics.size(); i++){
            if (!place_pic_into_rect(i)){
                failedFiles.add("picture does not placed in atlas!");//pics.get(i).frame.getPath());
            }
        }
        retstr = new String[failedFiles.size()];
        retstr = (String[])failedFiles.toArray(retstr);
        return retstr;
    }

    // формирует список картинок из дерева шрифтов и дерева спрайтов
    private void make_pics_list(){
        SpriteNode sprite;
        FrameNode frame;

        FontNode font;
        GlyphNode glyph;

        pics.clear();
        // пробежимся по дереву спрайтов
        for (int i = 0; i< spriteTree.getCount();i++){
            sprite = spriteTree.getNodeAt(i);
            for (int j=0; j<sprite.getChildCount(); j++){
                frame = sprite.getChildAt(j);
                pics.add(frame.buildPictureRect(sprite.isCropped, sprite.isOutlineWithBorder, sprite.transparentBorder));
            }
        }

        // пробежимся по дереву шрифтов
        for (int i = 0; i< fontTree.getCount(); i++){
            font = fontTree.getNodeAt(i);
            font.recreateGlyphs();
            for (int j=0; j<font.getChildCount(); j++){
                glyph = font.getChildAt(j);
                pics.add(glyph.buildPictureRect());
            }
        }

    }

    // сортируем список изображений от большего к меньшему
    private void sort_pics_list(){
        PictureRect tmpR;
        int	    maxIndex;
        int	    maxS, S;	    // площади картинок
        // обычная сортировка выбором
        for (int i=0; i<pics.size()-1;i++){
            maxIndex = i;
            maxS = pics.get(i).w * pics.get(i).h;
            for (int j=i+1; j<pics.size();j++){
                tmpR = pics.get(j);
                S = tmpR.w * tmpR.h;
                if (S > maxS){
                    maxIndex = j;
                    maxS = S;
                }
            }
            // переставим наименьший вверх
            tmpR = pics.get(i);
            pics.set(i, pics.get(maxIndex));
            pics.set(maxIndex, tmpR);
        }
    }

    // рассчитываем соотношение большего прямоугольника к меньшему
    private float get_rects_ratio(Rectangle r1, Rectangle r2){
        // если один из прямоугольников выродился - вернем заведомо большое число - этот вариант оптимальный
        if (r1.width == 0 || r1.height == 0 || r2.width == 0 || r2.height==0) return 99999.9f;
        float ratio = (float)(r1.width*r1.height)/(float)(r2.width*r2.height);
        if (ratio<1) ratio = 1.0f/ratio;
        return ratio;
    }

    // поместим картинку c номером index в первый подходящий пустой прямоугольник и вернем номер этого прямоугольника.
    // а если картинка не влезает ни в один из пустых прямоугольников - вернем -1.
    private boolean place_pic_into_rect(int index){
        int rectIndex = -1;  // индекс пустого прямоугольника в который помещается картинка
        // размеры картинки
        PictureRect picRect = pics.get(index);
        Rectangle   emptyRect;
        Rectangle   rectA1, rectA2;	// разбиение по варианту A
        Rectangle   rectB1, rectB2;	// разбиение по варианту B
        float	    ratioA, ratioB;	// соотношения прямоугольников

        int pw = picRect.w;
        int ph = picRect.h;

        for (int i=0; i<emptyList.getCount(); i++){
            emptyRect = emptyList.getAt(i);
            if (pw <= emptyRect.width && ph <= emptyRect.height){
                rectIndex = i;
                break;
            }
        }

        // если картинка помещается в один из прямоугольников запишем ее координаты на атласе
        // разделим пустой прямоугольник и т.д.

        if (rectIndex >= 0) {
            emptyRect = emptyList.getAt(rectIndex);

            picRect.x = emptyRect.x;
            picRect.y = emptyRect.y;

            // первый вариант разбиения - нижний прямоугольник имеет одну с картинкой ширину - разбиение по 
            // вертикальной линии
            rectA1 = new Rectangle(emptyRect.x, emptyRect.y+ph, pw, emptyRect.height-ph);   // нижний прямоугольник
            rectA2 = new Rectangle(emptyRect.x+pw,emptyRect.y,emptyRect.width-pw,emptyRect.height); // правый прямоугольник

            // второй вариант разбиения - разбиение по нижней грани картинки 
            rectB1 = new Rectangle(emptyRect.x, emptyRect.y+ph,emptyRect.width,emptyRect.height-ph); // нижний прямоугольник
            rectB2 = new Rectangle(emptyRect.x+pw, emptyRect.y,emptyRect.width-pw,ph);		    // правый прямоугольник

            // выберем вариант разбиения и разделим прямоугольник
            ratioA = get_rects_ratio(rectA1, rectA2);
            ratioB = get_rects_ratio(rectB1, rectB2);
            if (ratioA>ratioB)  // выберем вариант разбиения A
                emptyList.divide(rectIndex, rectA1, rectA2);
            else		// выберем вариант B
                emptyList.divide(rectIndex, rectB1, rectB2);

            picRect.isInAtlas = true;
            return true;
        }
        else {
            picRect.isInAtlas = false;
            return false;
        }
    }

}
