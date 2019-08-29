package com.mygdx.game.loxodonta.data;

public interface Data {
    public String toJSON();
    public void fromData(Data d);
    public void setFromJSON(String json);
    public void rotate();
}
