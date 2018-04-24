package com.jz.json.jsoncompare;

/**
 * @author jzfeng
 * In STRICT mode, when comparing JsonObjects, it stills ignore the KEY sequence in JsonObject.
 * However, it follows the strict orders in a JsonArray.
 * In LENIENT mode, it ignores both KEY orders of a JsonObject and element orders in a JsonArray.
 */
public enum CompareMode {
    STRICT, LENIENT;
}
