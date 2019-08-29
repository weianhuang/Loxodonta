package com.mygdx.game.loxodonta;

import com.badlogic.gdx.graphics.Texture;

public class LevelButton extends Button{

    private int level;
    private Texture comp_no_hover;
    private Texture comp_hover;
    private boolean locked;
    private boolean completed;


    public LevelButton(Texture no_hover, Texture hover, Texture comp_no_hover, Texture comp_hover, int level,
                       boolean locked, boolean completed){
        super(no_hover, hover);
        this.level = level;
        this.locked = locked;
        this.comp_hover = comp_hover;
        this.comp_no_hover = comp_no_hover;
        this.completed = completed;
    }

    public void set_level(int l){level = l;}
    public int get_level(){return level;}
    public void set_completed(boolean b){completed = b;}
    public boolean get_completed(){return completed;}

    @Override
    public Texture get_texture(){
        if (completed){
            return (get_state() == state.HOVER? comp_hover : comp_no_hover);
        } else{
            return super.get_texture();
        }
    }

    @Override
    public void reset_texture(){
        if (completed){
            set_btn_texture((get_state() == state.HOVER? comp_hover : comp_no_hover));
        } else{
            super.reset_texture();
        }
    }



    @Override
    public void setFilter(Texture.TextureFilter a, Texture.TextureFilter b){
        super.setFilter(a,b);
        comp_hover.setFilter(a,b);
        comp_no_hover.setFilter(a,b);
    }

}
