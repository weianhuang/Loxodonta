package com.mygdx.game.loxodonta;

import com.badlogic.gdx.graphics.Texture;

public class Button {
    private Texture no_hover;
    private Texture hover;
    enum state{WAIT, HOVER, DOWN, UP};
    private state btn_state;
    private Texture btn_texture;


    public Button(Texture no_hover, Texture hover){
        this.hover = hover;
        this.no_hover = no_hover;
        this.btn_texture = no_hover;
        this.btn_state = state.WAIT;
    }

    public Texture get_hover(){ return hover;}
    public Texture get_no_hover(){ return no_hover;}
    public Texture get_texture(){ return (btn_state == state.HOVER ? hover:no_hover);}

    public void reset_texture(){ btn_texture = (btn_state == state.HOVER ? hover:no_hover);}

    public void set_btn_texture(Texture t){ btn_texture = t;}

    public void set_hover(Texture h){ hover = h;}
    public void set_no_hover(Texture h){no_hover = h;}

    public void set_state(state s){btn_state = s; reset_texture();}
    public state get_state(){return btn_state;}

    public void dispose(){
        hover.dispose();
        no_hover.dispose();
    }

    public void setFilter(Texture.TextureFilter a, Texture.TextureFilter b){
        hover.setFilter(a,b);
        no_hover.setFilter(a,b);
    }
}
