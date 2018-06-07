package com.jz.json.jsoncompare;

import com.google.gson.*;

/**
 * @author jzfeng
 */

public class JsonElementWithPath {
    private JsonElement jsonElement;
    private String path;

    public JsonElementWithPath(JsonElement jsonElement, String path) {
        this.jsonElement = jsonElement;
        this.path = path;
    }

    public JsonElement getJsonElement() {
        return this.jsonElement;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path + " : " + jsonElement;
    }


}
