package com.jz.json.jsoncompare;

/**
 * @author jzfeng
 */
//First Jason file as reference;
public enum FailureType {
    UNEQUAL_VALUE,
    MISSING_FIELD,
    UNEXPECTED_FIELD,
    DIFFERENT_JSONARRY_SIZE,
    MISSING_JSONARRAY_ELEMENT,
    UNEXPECTED_JSONARRAY_ELEMENT,
    DIFFERENT_OCCURENCE_JSONARRAY_ELEMENT;
}

