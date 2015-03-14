package com.dekagames.atlasmaker;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 12.10.13
 * Time: 21:10
 * To change this template use File | Settings | File Templates.
 */
public class FontTree {
    private ArrayList<FontNode> fonts;


    public FontTree(){
        fonts = new ArrayList<FontNode>();
    }

    public void addFont(FontNode node){
        if (node != null)
            fonts.add(node);

    }

    public int getCount(){
        return fonts.size();
    }

    public FontNode getNodeAt(int index){
        if (fonts.size() == 0 || index<0 || index>= fonts.size()) return null;
        else return fonts.get(index);
    }

    public void replaceNode(int index, FontNode newNode){
        if (fonts.size() == 0 || index<0 || index>= fonts.size()) return;
        fonts.remove(index);
        fonts.add(index, newNode);
    }

    public void remove(int index){
        if (fonts.size() == 0 || index<0 || index>= fonts.size()) return;
        fonts.remove(index);
    }

    public void clear(){
        fonts.clear();
    }

}
