package com.jz.json.jsoncompare;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import com.jz.json.utils.Utils;

import java.io.File;
import java.util.Set;
import java.util.Map;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.List;
import java.util.HashSet;

import static com.jz.json.utils.Utils.*;

/**
 * @author jzfeng
 */

public class JsonCompare {

    public static void main(String[] args) throws Exception {

        JsonParser parser = new JsonParser();

        String json = convertFormattedJson2Raw(new File("/Users/jzfeng/Desktop/JsonArray1.json"));
        JsonObject o1 = parser.parse(json).getAsJsonObject();

        json = convertFormattedJson2Raw(new File("/Users/jzfeng/Desktop/JsonArray2.json"));
        JsonObject o2 = parser.parse(json).getAsJsonObject();


        Filter filter = new Filter(
                new String[]{"UNEXPECTED_PROPERTY"},

                new String[]{"$.listing.listingProperties[2].propertyValues[*]",
                        "listing.listingLifecycle.scheduledStartDate.value",
                        "listing.termsAndPolicies.logisticsTerms.logisticsPlan[0:]"},

                new String[]{"listing.tradingSummary.lastVisitDate"}
        );

        Result result = compareJson(o1, o2, "LENIENT");

        System.out.println(result.getResultDetails());

    }

    public static Result compareJson(JsonObject o1, JsonObject o2) {
        Mode mode = Mode.STRICT;
        Result r = new Result(mode);
        compareJson("", (JsonElement) o1, (JsonElement) o2, r, mode);
        return r;
    }

    public static Result compareJson(JsonObject o1, JsonObject o2, String m) {
        Mode mode = Mode.valueOf(m.trim().toUpperCase());
        Result r = new Result(mode);
        compareJson("", (JsonElement) o1, (JsonElement) o2, r, mode);
        return r;
    }

    public static Result compareJson(JsonObject o1, JsonObject o2, String m, Filter filter) {
        Result result = compareJson(o1, o2, m);
        return result.applyFilter(filter, o1, o2);
    }


    private static Result compareJson(String parentPath, JsonObject o1, JsonObject o2, Mode mode) {
        Result r = new Result(mode);
        compareJson(parentPath, (JsonElement) o1, (JsonElement) o2, r, mode);
        return r;
    }


    private static void compareJson(
            String parentPath, JsonElement o1, JsonElement o2, Result result, Mode mode) {
        if (o1 == null && o2 == null) {
            return;
        }
        if (o1.isJsonNull() && o2.isJsonNull()) {
            return;
        }

        Queue<JsonElementWithPath> q1 = new LinkedList<JsonElementWithPath>();
        Queue<JsonElementWithPath> q2 = new LinkedList<JsonElementWithPath>();
        q1.offer(new JsonElementWithPath(o1, "$"));
        q2.offer(new JsonElementWithPath(o2, "$"));
        Failure failure = new Failure();
        String failureMsg = null;
        //iterate all nodes;
        while (!q1.isEmpty()) {
            int size = q1.size();
            for (int i = 0; i < size; i++) {
                JsonElementWithPath org = q1.poll();
                JsonElementWithPath dest = q2.poll();
                String currentLevelOfOrg = org.getPath();
                String currentLevelOfDest = dest.getPath();
                JsonElement je1 = org.getJsonElement();
                JsonElement je2 = dest.getJsonElement();

                if (je1.isJsonPrimitive()) {
                    Result r = compareJsonPrimitive(currentLevelOfOrg, je1.getAsJsonPrimitive(), je2.getAsJsonPrimitive(), mode);
                    result.getFailures().addAll(r.getFailures());
                } else if (je1.isJsonArray()) {
                    JsonArray ja1 = je1.getAsJsonArray();
                    JsonArray ja2 = je2.getAsJsonArray();
                    if (ja1.size() != ja2.size()) {
//                        System.out.println("JsonArrays size are different : " + " " + currentLevelOfOrg + ", size: " + ja1.size() + ", size: " + ja2.size());
                        failureMsg = "Different JsonArray size: " + ja1.size() + " VS " + ja2.size() + "\n\r" + ja1 + ";\n\r" + ja2;
                        failure = new Failure(currentLevelOfOrg, FailureType.DIFFERENT_JSONARRY_SIZE, ja1, ja2, failureMsg);
                        result.addFailure(failure);
                    } else {
                        compareJsonArray(currentLevelOfOrg, ja1, ja2, result, mode);
                    }
                } else if (je1.isJsonObject()) {
                    //Compare two JsonObject;
                    JsonObject jo1 = je1.getAsJsonObject();
                    JsonObject jo2 = je2.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : jo1.entrySet()) {
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();
                        String level = currentLevelOfOrg + "." + key;
                        if (parentPath.startsWith("$") && !level.contains(parentPath)) {
                            level = parentPath + level.substring(1);
                        }

                        if (!jo2.has(key)) {
//                            System.out.println("Destination Json does not have key : " + level);
                            failureMsg = "Missing field \"" + level + "\" " + "from actual result.";
                            failure = new Failure(level, FailureType.MISSING_PROPERTY, jo1, null, failureMsg);
                            result.addFailure(failure);
                        } else {
                            //only store JsonElements that have same "key";
                            q1.offer(new JsonElementWithPath(value, level));
                            q2.offer(new JsonElementWithPath(jo2.get(key), level));
                        }
                    }

                    // Need iterate all nodes in Destination JsonObject.
                    for (Map.Entry<String, JsonElement> entry : jo2.entrySet()) {
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();

                        if (!jo1.has(key)) {
//                            System.out.println("Origin Json does not have key " + currentLevelOfDest + "." + key);
                            failureMsg = "Unexpected field \"" + currentLevelOfDest + "." + key + "\"" + " from actual result.";
                            failure = new Failure(currentLevelOfDest + "." + key, FailureType.UNEXPECTED_PROPERTY, null, jo2, failureMsg);
                            result.addFailure(failure);
                        }
                    }
                }
            }
        }
    }

    private static Result compareJsonArray(
            String parentPath, JsonArray expected, JsonArray actual, Mode mode) {
        Result r = new Result(mode);
        compareJsonArray(parentPath, expected, actual, r, mode);
        return r;
    }

    private static void compareJsonArray(
            String parentPath, JsonArray expected, JsonArray actual, Result result, Mode mode) {
        if (expected.size() != actual.size()) {
            String failureMsg = "Different JsonArray size: " + expected.size() + " VS " + actual.size() + "\n\r" + expected + ";\n\r" + actual;
            Failure failure = new Failure(parentPath, FailureType.DIFFERENT_JSONARRY_SIZE, expected, actual, failureMsg);
            result.addFailure(failure);
            return;
        } else if (expected.size() == 0) {
            return; // Nothing to compare
        }

        if (mode == Mode.STRICT) {
            compareJsonArrayWithStrictOrder(parentPath, expected, actual, result, mode);
        } else if (allSimpleValues(expected)) {
            List<Failure> r = compareJsonArrayOfJsonPrimitives(parentPath, expected, actual);
            result.getFailures().addAll(r);
        } else if (allJsonObjects(expected)) {
            compareJsonArrayOfJsonObjects(parentPath, expected, actual, result, mode);
        } else if (allJsonArrayss(expected)) {
            //to-do
        }

    }

    private static Result compareJsonPrimitive(
            String parentPath, JsonPrimitive o1, JsonPrimitive o2, Mode mode) {

        Result result = new Result(mode, new ArrayList<>());
        String s1 = o1.getAsString().trim();
        String s2 = o2.getAsString().trim();
        if (!s1.equalsIgnoreCase(s2)) {
            String failureMsg = "Unequal Value : " + parentPath + ", " + s1 + " , " + s2;
            Failure failure = new Failure(parentPath, FailureType.UNEQUAL_VALUE, o1, o2, failureMsg);
            result.addFailure(failure);
        }

        return result;
    }


    private static void compareJsonArrayWithStrictOrder(
            String parentPath, JsonArray expected, JsonArray actual, Result result, Mode mode) {
        for (int j = 0; j < expected.size(); j++) {
            JsonElement expectedValue = expected.get(j);
            JsonElement actualValue = actual.get(j);
            compareJson(parentPath + "[" + j + "]", expectedValue, actualValue, result, mode);
        }
    }

    private static void compareJsonArrayOfJsonObjects(
            String parentPath, JsonArray expected, JsonArray actual, Result result, Mode mode) {
        String uniqueKey = findUniqueKey(expected);
        Failure failure = new Failure();
        String failureMsg = null;

        if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual)) {
            List<Failure> jsonArrayCompareResult = recursivelyCompareJsonArray(parentPath, expected, actual, mode);
            result.getFailures().addAll(jsonArrayCompareResult);
            return;
        }

//        System.out.println("Unique key is " + uniqueKey);
        Map<JsonPrimitive, JsonElementWithPath> expectedValueMap = arrayOfJsonObjectToMap(parentPath, expected, uniqueKey);
        Map<JsonPrimitive, JsonElementWithPath> actualValueMap = arrayOfJsonObjectToMap(parentPath, actual, uniqueKey);
        for (JsonPrimitive id : expectedValueMap.keySet()) {
            if (!actualValueMap.containsKey(id)) {
                JsonElementWithPath expectedValue = expectedValueMap.get(id);
                failureMsg = "\"" + expectedValue.getJsonElement() + "\"" + " is missing from actual JsonArray.";
                failure = new Failure(expectedValue.getPath(), FailureType.MISSING_JSONARRAY_ELEMENT, expectedValue.getJsonElement(), null, failureMsg);
                result.getFailures().add(failure);
                continue;
            }
            JsonElementWithPath expectedValue = expectedValueMap.get(id);
            JsonElementWithPath actualValue = actualValueMap.get(id);
            //更新parent level
            compareJson(expectedValue.getPath(), expectedValue.getJsonElement(), actualValue.getJsonElement(), result, mode); //或者是currentLevelOfOrg[*] ???
        }
        for (JsonPrimitive id : actualValueMap.keySet()) {
            if (!expectedValueMap.containsKey(id)) {
                JsonElementWithPath actualValue = actualValueMap.get(id);
                failureMsg = "\"" + actualValue.getJsonElement() + "\"" + " is unexpected from actual JsonArray.";
                failure = new Failure(actualValue.getPath(), FailureType.UNEXPECTED_JSONARRAY_ELEMENT, null, actualValue.getJsonElement(), failureMsg);
                result.getFailures().add(failure);
            }
        }

    }

    private static List<Failure> compareJsonArrayOfJsonPrimitives(
            String parentPath, JsonArray expected, JsonArray actual) {
        List<Failure> result = new ArrayList<>();
        Map<JsonElement, Integer> expectedCount = getCardinalityMap(jsonArrayToList(expected));
        Map<JsonElement, Integer> actualCount = getCardinalityMap(jsonArrayToList(actual));
        Map<JsonElement, String> expectedMap = jsonArrayToMap(parentPath, expected);
        Map<JsonElement, String> actualMap = jsonArrayToMap(parentPath, actual);
        Failure failure = new Failure();
        String failureMsg = null;

        for (JsonElement o : expectedCount.keySet()) {
            if (!actualCount.containsKey(o)) {
                failureMsg = "Missing JsonArray Element " + o + " in actual JsonArray";
                failure = new Failure(expectedMap.get(o), FailureType.MISSING_JSONARRAY_ELEMENT, o, null, failureMsg);
                failure.setExpected(o);
                result.add(failure);
            } else if (!actualCount.get(o).equals(expectedCount.get(o))) {
                failureMsg = "Occurrence of " + o + " : " + expectedCount.get(o) + " VS " + actualCount.get(o);
                failure = new Failure(expectedMap.get(o), FailureType.DIFFERENT_OCCURENCE_JSONARRAY_ELEMENT, o, o, failureMsg);
                result.add(failure);
            }
        }

        for (JsonElement o : actualCount.keySet()) {
            if (!expectedCount.containsKey(o)) {
                failureMsg = "Unexpected JsonArray Element " + o + " in actual JsonArray";
                failure = new Failure(actualMap.get(o), FailureType.UNEXPECTED_JSONARRAY_ELEMENT, null, o, failureMsg);
                failure.setActual(o);
                result.add(failure);
            }
        }

        return result;
    }


    // This is expensive (O(n^2)), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.

    private static List<Failure> recursivelyCompareJsonArray(
            String parentPath, JsonArray expected, JsonArray actual, Mode mode) {
        List<Failure> result = new ArrayList<>();
        Set<Integer> matched = new HashSet<Integer>(); //Save the matched element from actual
        for (int i = 0; i < expected.size(); ++i) {
            JsonElement expectedElement = expected.get(i);
            boolean matchFound = false;
            for (int j = 0; j < actual.size(); ++j) {
                JsonElement actualElement = actual.get(j);
                if (matched.contains(j) || !actualElement.getClass().equals(expectedElement.getClass())) {
                    continue;
                }

                Result r = null;
                if (expectedElement instanceof JsonObject) {
                    r = compareJson(parentPath + "[" + i + "]", (JsonObject) expectedElement, (JsonObject) actualElement, mode);
                } else if (expectedElement instanceof JsonArray) {
                    r = compareJsonArray(parentPath + "[" + i + "]", (JsonArray) expectedElement, (JsonArray) actualElement, mode);
                } else if (expectedElement instanceof JsonPrimitive) {
                    r = compareJsonPrimitive(parentPath + "[" + i + "]", (JsonPrimitive) expectedElement, (JsonPrimitive) actualElement, mode);
                }

                if (expectedElement.equals(actualElement) || r.isPassed()) {
                    matched.add(j);
                    matchFound = true;
                    break;
                }

            }
            if (!matchFound) {
                String failureMsg = "\"" + expectedElement + "\"" + " is missing from actual JsonArray.";
                Failure failure = new Failure(parentPath + "[" + i + "]", FailureType.MISSING_JSONARRAY_ELEMENT, expectedElement, null, failureMsg);
                result.add(failure);
            }
        }

        for (int j = 0; j < actual.size(); j++) {
            if (matched.contains(j)) {
                continue;
            }

            JsonElement actualElement = actual.get(j);
            String failureMsg = "\"" + actualElement + "\"" + " is unexpected from actual JsonArray.";
            Failure failure = new Failure(parentPath + "[" + j + "]", FailureType.UNEXPECTED_JSONARRAY_ELEMENT, null, actualElement, failureMsg);
            result.add(failure);
        }


        return result;
    }


}
