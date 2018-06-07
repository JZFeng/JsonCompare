package com.jz.json.jsoncompare;

import com.google.gson.JsonElement;

/**
 * @author jzfeng
 */
public class Failure {
    private String path; //absolute JsonPath
    private FailureType failureType; //enum
    private JsonElement expected;
    private JsonElement actual;
    private String failureMsg;

    Failure(String path, FailureType failureType, JsonElement expected, JsonElement actual, String failureMsg) {
        this.path = path;
        this.failureType = failureType;
        this.expected = expected;
        this.actual = actual;
        this.failureMsg = failureMsg;
    }

    Failure(String path, FailureType failureType, JsonElement expected, JsonElement actual) {
        this.path = path;
        this.failureType = failureType;
        this.expected = expected;
        this.actual = actual;
    }

    Failure(String jsonPath, FailureType failureType) {
        this.path = jsonPath;
        this.failureType = failureType;
        this.expected = null;
        this.actual = null;
    }

    Failure() {
        this.path = null;
        this.failureType = null;
        this.expected = null;
        this.actual = null;
    }

    public String getPath() {
        return path.trim();
    }

    public FailureType getFailureType() {
        return failureType;
    }

    public JsonElement getExpected() {
        return expected;
    }

    public JsonElement getActual() {
        return actual;
    }

    public String getFailureMsg() {
        return failureMsg;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFailureType(FailureType failureType) {
        this.failureType = failureType;
    }

    public void setExpected(JsonElement expected) {
        this.expected = expected;
    }

    public void setActual(JsonElement actual) {
        this.actual = actual;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JsonPath : " + path + "\r\n");
        sb.append("Reason : " + failureMsg + "\r\n");
//        sb.append("EXPECTED : " + getExpected() + "\r\n");
//        sb.append("ACTUAL : " + getActual() + "\r\n");

        return sb.toString().trim();
    }

}
