package com.jz.json.jsoncompare;

import com.google.gson.JsonElement;

public class Failure {
    private String field; //absolute JsonPath
    private FailureType failureType; //enum
    private JsonElement expected;
    private JsonElement actual;
    private String failureMsg;

    Failure(String field, FailureType failureType, JsonElement expected, JsonElement actual, String failureMsg) {
        this.field = field;
        this.failureType = failureType;
        this.expected = expected;
        this.actual = actual;
        this.failureMsg = failureMsg;
    }

    Failure(String field, FailureType failureType, JsonElement expected, JsonElement actual) {
        this.field = field;
        this.failureType = failureType;
        this.expected = expected;
        this.actual = actual;
    }

    Failure(String field, FailureType failureType) {
        this.field = field;
        this.failureType = failureType;
        this.expected = null;
        this.actual = null;
    }

    Failure() {
        this.field = null;
        this.failureType = null;
        this.expected = null;
        this.actual = null;
    }

    public String getField() {
        return field;
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

    public void setField(String field) {
        this.field = field;
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
        sb.append("FIELD : " + field + "\r\n");
        sb.append("Reason : " + failureMsg + "\r\n");
//        sb.append("EXPECTED : " + getExpected() + "\r\n");
//        sb.append("ACTUAL : " + getActual() + "\r\n");

        return sb.toString().trim();
    }

}
