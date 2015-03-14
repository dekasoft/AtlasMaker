package com.dekagames.atlasmaker;

import java.util.ArrayList;

/**
 * Класс спрайта. Спрайт может состоять из одной картинки, или же содержать последовательность кадров.
 * Спрайты отображаются в общеи дереве
 */
public class SpriteNode {
    public      String                      name;
    public      ArrayList<FrameNode>        elements;               // список элементов - кадров или глифов
    public      boolean                     isOutlineWithBorder;    //спрайт обводится границей, дублирующей крайний пиксель
    public	    boolean		                isCropped;		        // обрезается ли изображение спрайта по прозрачным пикселям
    public	    int                         transparentBorder;		// размер границы из прозрачных пикселей

    public SpriteNode(String name){
        setName(name);
        elements = new ArrayList();
    }

    /**
     * конструктор копирования
     * @param orig
     */
    public SpriteNode(SpriteNode orig){
        this.name = orig.name;
        this.transparentBorder = orig.transparentBorder;
        this.isCropped = orig.isCropped;
        this.isOutlineWithBorder = orig.isOutlineWithBorder;
        elements = new ArrayList(orig.elements);
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

    public void addElement(FrameNode node){
        if (node!=null) {
            elements.add(node);
        }
    }

    public void removeElement(int index){
        if (index<0 || index>=elements.size()) return;
        elements.remove(index);
    }

    // двигает кадр с номером index вверх если это возможно, возвращает новый индекс передвинутой картинки или -1
    public int moveElementUp(int index){
        if (index>=1 && elements.size()>=2 && index<elements.size()){
            FrameNode buf = elements.get(index-1);
            elements.set(index-1,elements.get(index));
            elements.set(index,buf);
            return index-1;
        }
        return -1;
    }

    // двигает кадр с номером index вниз если это возможно, возвращает новый индекс передвинутой картинки или -1
    public int moveElementDown(int index){
        if (index>=0 && index<elements.size()-1){
            FrameNode buf = elements.get(index+1);
            elements.set(index+1,elements.get(index));
            elements.set(index,buf);
            return index+1;
        }
        return -1;
    }

    public FrameNode getChildAt(int childIndex) {
        if (elements.size()!= 0 && childIndex< elements.size())
            return elements.get(childIndex);
        else
            return null;
    }

    public int getChildCount() {
        return elements.size();
    }
}
