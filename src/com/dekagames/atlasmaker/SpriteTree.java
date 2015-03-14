package com.dekagames.atlasmaker;

import java.util.ArrayList;

/**
 * Класс - список спрайтов
 */
public class SpriteTree {
    private ArrayList<SpriteNode> sprites;


    public SpriteTree(){
        sprites = new ArrayList<SpriteNode>();
    }

    public void addSprite(SpriteNode node){
        if (node != null)
            sprites.add(node);

    }

    public int getCount(){
        return sprites.size();
    }

    public SpriteNode getNodeAt(int index){
        if (sprites.size() == 0 || index<0 || index>= sprites.size()) return null;
        else return sprites.get(index);
    }

    public void replaceNode(int index, SpriteNode newNode){
        if (sprites.size() == 0 || index<0 || index>= sprites.size()) return;
        sprites.remove(index);
        sprites.add(index, newNode);
    }

    public void remove(int index){
        if (sprites.size() == 0 || index<0 || index>= sprites.size()) return;
        sprites.remove(index);
    }

    public void clear(){
        sprites.clear();
    }

}
