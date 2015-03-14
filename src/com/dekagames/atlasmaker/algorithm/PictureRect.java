package com.dekagames.atlasmaker.algorithm;

/** Класс описывающий картинку как элемент атласа.
 * Именно с экземплярами этого класса проводятся все действия при составлении атласа.
 */
public class PictureRect {
    public      int		    w,h;	// реальная высота и ширина кадра в атласе (с учетом crop и border).
                                    // При выгрузке эти ширина и высота будут уменьшены на размер border
    public      int		    x,y;	// положение картинки на атласе от левого верхнего угла.
                                    // При выгрузке к этим координатам будет прибавлена величина border
    public  boolean	        isInAtlas;

    public PictureRect(int width, int height){
        w = width;
        h = height;
    }

    /**
     * Вычисляет площадь картинки
     */
    public int getSquare(){
        return w*h;
    }

    /**
     * Устанавливает координаты картинки на текстуре
     * @param x
     * @param y
     */
    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }
}
