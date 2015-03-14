package com.dekagames.atlasmaker.algorithm;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 18.08.13
 * Time: 0:52
 * Класс список пустых прямоугольников в которые можно поместить картинку
 */
public class EmptyRectList {
    private ArrayList<Rectangle> rects;

    public EmptyRectList(int picWidth, int picHeight){
        rects = new ArrayList<Rectangle>();
        rects.add(new Rectangle(0, 0, picWidth, picHeight));
    }

    /**
     * сортировка списка прямоугольников в порядке увеличения площади. Первый - самый мелкий
     */
    public void sort(){
        Rectangle   tmpR;
        int	    minIndex;
        int	    minS, S;	    // площади прямоугольников
        // обычная сортировка выбором
        for (int i=0; i<rects.size()-1;i++){
            minIndex = i;
            minS = rects.get(i).width * rects.get(i).height;
            for (int j=i+1; j<rects.size();j++){
                tmpR = rects.get(j);
                S = tmpR.width * tmpR.height;
                if (S < minS){
                    minIndex = j;
                    minS = S;
                }
            }
            // переставим наименьший вверх
            tmpR = rects.get(i);
            rects.set(i, rects.get(minIndex));
            rects.set(minIndex, tmpR);
        }
    }


    public Rectangle getAt(int index){
        return rects.get(index);
    }

    /**
     * заменяет прямоугольник с индексом index на два - rect1 и rect2.
     * первоначальный прямоугольник из списка удаляется, список пересортировывается
     * @param index
     * @param rect1
     * @param rect2
     */
    public void divide(int index, Rectangle rect1, Rectangle rect2){
        rects.remove(index);
        rects.add(rect1);
        rects.add(rect2);
        sort();
    }


    public int getCount(){
        return rects.size();
    }


}
