package com.jz.json.jsoncompare;

import com.google.gson.*;

public class JsonElementWithLevel {
    private JsonElement jsonElement;
    private String level;

    public JsonElementWithLevel(JsonElement jsonElement, String level) {
        this.jsonElement = jsonElement;
        this.level = level;
    }

    public JsonElement getJsonElement() {
        return this.jsonElement;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return level + " : " + jsonElement;
    }


}
