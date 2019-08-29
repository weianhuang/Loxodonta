package com.mygdx.game.loxodonta.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    public static final String JSONFilePrefix = "json/";

    private static ObjectMapper mapper;

    public static ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    public static String autoWriteJSON(Object o) {
        String s = "";
        try {
            s = getObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println("Error in autoWriteJSON: "+e);
        }
        return s;
    }

    public static <T>T autoReadJSON(String s, Class<T> c) {
        T out = null;
        try {
            out = getObjectMapper().readValue(s, c);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in autoWriteJSON: "+e);
        }
        return out;
    }

    public static boolean writeStringToFile(String json, String fileName) {
        fileName = JSONFilePrefix + fileName;
        boolean success = false;
        try {
            FileHandle fh = Gdx.files.local(fileName);
            fh.writeString(json, false);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in writeStringToFile: "+e);
        }
        return success;
    }

    public static String readStringFromFile(String fileName) {
        fileName = JSONFilePrefix + fileName;
        String out = "";
        try {
            FileHandle fh = Gdx.files.local(fileName);
            out = fh.readString();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in readStringFromFile: "+e);
        }
        return out;
    }
}
