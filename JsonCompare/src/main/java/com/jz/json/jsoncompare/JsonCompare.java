package com.jz.json.jsoncompare;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Map;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.List;
import java.util.HashSet;

import static com.jz.json.jsoncompare.Utils.*;

/**
 * @author jzfeng
 */

public class JsonCompare {

    public static void main(String[] args) throws IOException, WrongFilterException {

        JsonParser parser = new JsonParser();
        File f = new File(".");
        System.out.println(f.getAbsolutePath());

        String json = convertFormattedJson2Raw(new File("src/main/java/com/jz/json/jsoncompare/testdata/O.json"));
        JsonObject o1 = parser.parse(json).getAsJsonObject();

        json = convertFormattedJson2Raw(new File("src/main/java/com/jz/json/jsoncompare/testdata/D.json"));
        JsonObject o2 = parser.parse(json).getAsJsonObject();


        Filter filter = new Filter(
                new String[]{},
                new String[]{});

        CompareResult result = compareJson(o1, o2, "LENIENT");
        result = result.applyFilter(filter);
        System.out.println(result.getResultDetails());
    }

    public static CompareResult compareJson(JsonObject o1, JsonObject o2) {
        CompareMode mode = CompareMode.LENIENT;
        CompareResult r = new CompareResult(mode);
        compareJson("", (JsonElement) o1, (JsonElement) o2, r, mode);
        return r;
    }

    public static CompareResult compareJson(JsonObject o1, JsonObject o2, String m) {
        CompareMode mode = CompareMode.valueOf(m.trim().toUpperCase());
        CompareResult r = new CompareResult(mode);
        compareJson("", (JsonElement) o1, (JsonElement) o2, r, mode);
        return r;
    }

    private static CompareResult compareJson(String parentLevel, JsonObject o1, JsonObject o2, CompareMode mode) {
        CompareResult r = new CompareResult(mode);
        compareJson(parentLevel, (JsonElement) o1, (JsonElement) o2, r, mode);
        return r;
    }

    private static void compareJson(
            String parentLevel, JsonElement o1, JsonElement o2, CompareResult result, CompareMode mode) {
        if (o1 == null && o2 == null) {
            return;
        }
        if (o1.isJsonNull() && o2.isJsonNull()) {
            return;
        }

        Queue<JsonElementWithLevel> q1 = new LinkedList<JsonElementWithLevel>();
        Queue<JsonElementWithLevel> q2 = new LinkedList<JsonElementWithLevel>();
        q1.offer(new JsonElementWithLevel(o1, "$"));
        q2.offer(new JsonElementWithLevel(o2, "$"));
        Failure failure = new Failure();
        String failureMsg = null;
        //iterate all nodes;
        while (!q1.isEmpty()) {
            int size = q1.size();
            for (int i = 0; i < size; i++) {
                JsonElementWithLevel org = q1.poll();
                JsonElementWithLevel dest = q2.poll();
                String currentLevelOfOrg = org.getLevel();
                String currentLevelOfDest = dest.getLevel();
                JsonElement je1 = org.getJsonElement();
                JsonElement je2 = dest.getJsonElement();

                if (je1.isJsonPrimitive()) {
                    compareJsonPrimitive(currentLevelOfOrg, je1.getAsJsonPrimitive(), je2.getAsJsonPrimitive(), result);
                } else if (je1.isJsonArray()) {
                    if (mode == CompareMode.LENIENT) {
                        currentLevelOfOrg = currentLevelOfOrg + "[]";
                        currentLevelOfDest = currentLevelOfDest + "[]";
                    }
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
                        if (parentLevel.startsWith("$") && !level.contains(parentLevel)) {
                            level = parentLevel + level.substring(1);
                        }

                        if (!jo2.has(key)) {
//                            System.out.println("Destination Json does not have key : " + level);
                            failureMsg = "Missing field \"" + level + "\" " + "from actual result.";
                            failure = new Failure(level, FailureType.MISSING_FIELD, jo1, null, failureMsg);
                            result.addFailure(failure);
                        } else {
                            //only store JsonElements that have same "key";
                            q1.offer(new JsonElementWithLevel(value, level));
                            q2.offer(new JsonElementWithLevel(jo2.get(key), level));
                        }
                    }

                    // Need iterate all nodes in Destination JsonObject.
                    for (Map.Entry<String, JsonElement> entry : jo2.entrySet()) {
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();

                        if (!jo1.has(key)) {
//                            System.out.println("Origin Json does not have key " + currentLevelOfDest + "." + key);
                            failureMsg = "Unexpected field \"" + currentLevelOfDest + "." + key + "\"" + " from actual result.";
                            failure = new Failure(currentLevelOfDest + "." + key, FailureType.UNEXPECTED_FIELD, null, jo2, failureMsg);
                            result.addFailure(failure);
                        }
                    }
                }
            }
        }
    }

    private static CompareResult compareJsonArray(
            String parentLevel, JsonArray expected, JsonArray actual, CompareMode mode) {
        CompareResult r = new CompareResult(mode);
        compareJsonArray(parentLevel, expected, actual, r, mode);
        return r;
    }

    private static void compareJsonArray(
            String parentLevel, JsonArray expected, JsonArray actual, CompareResult result, CompareMode mode) {
        if (expected.size() != actual.size()) {
            String failureMsg = "Different JsonArray size: " + expected.size() + " VS " + actual.size() + "\n\r" + expected + ";\n\r" + actual;
            Failure failure = new Failure(parentLevel, FailureType.DIFFERENT_JSONARRY_SIZE, expected, actual, failureMsg);
            result.addFailure(failure);
        } else if (expected.size() == 0) {
            return; // Nothing to compare
        }

        if (mode == CompareMode.STRICT) {
            compareJsonArrayWithStrictOrder(parentLevel, expected, actual, result, mode);
        } else if (allSimpleValues(expected)) {
            List<Failure> jsonArrayCompareResult = compareJsonArrayOfJsonPrimitives(parentLevel, expected, actual);
            result.getFailures().addAll(jsonArrayCompareResult);
        } else if (allJsonObjects(expected)) {
            compareJsonArrayOfJsonObjects(parentLevel, expected, actual, result, mode);
        }

    }

    private static void compareJsonPrimitive(
            String parentLevel, JsonPrimitive o1, JsonPrimitive o2, CompareResult result) {

        String s1 = o1.getAsString().trim();
        String s2 = o2.getAsString().trim();
        if (!s1.equalsIgnoreCase(s2)) {
//            System.out.println("Two primitive elements are not equal : " + parentLevel + ", " + s1 + " , " + s2);
            String failureMsg = "Unequal Value : " + parentLevel + ", " + s1 + " , " + s2;
            Failure failure = new Failure(parentLevel, FailureType.UNEQUAL_VALUE, o1, o2, failureMsg);
            result.addFailure(failure);
        }
    }


    private static void compareJsonArrayWithStrictOrder(
            String parentLevel, JsonArray expected, JsonArray actual, CompareResult result, CompareMode mode) {
        for (int j = 0; j < expected.size(); ++j) {
            JsonElement expectedValue = expected.get(j);
            JsonElement actualValue = actual.get(j);
            compareJson(parentLevel + "[" + j + "]", expectedValue, actualValue, result, mode);
//            compareJson(parentLevel + "[]", expectedValue, actualValue, result);
        }
    }

    private static void compareJsonArrayOfJsonObjects(
            String parentLevel, JsonArray expected, JsonArray actual, CompareResult result, CompareMode mode) {
        String uniqueKey = findUniqueKey(expected);
        Failure failure = new Failure();
        String failureMsg = null;

        if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual)) {
            List<Failure> jsonArrayCompareResult = recursivelyCompareJsonArray(parentLevel, expected, actual, mode);
            result.getFailures().addAll(jsonArrayCompareResult);
            return;
        }

//        System.out.println("Unique key is " + uniqueKey);
        Map<JsonPrimitive, JsonObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
        Map<JsonPrimitive, JsonObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);
        for (JsonPrimitive id : expectedValueMap.keySet()) {
            if (!actualValueMap.containsKey(id)) {
                failureMsg = "\"" + expectedValueMap.get(id) + "\"" + " is missing from actual JsonArray.";
                failure = new Failure(parentLevel, FailureType.MISSING_JSONARRAY_ELEMENT, expectedValueMap.get(id), null, failureMsg);
                result.getFailures().add(failure);
                continue;
            }
            JsonObject expectedValue = expectedValueMap.get(id);
            JsonObject actualValue = actualValueMap.get(id);
            compareJson(parentLevel, (JsonElement) expectedValue, (JsonElement) actualValue, result, mode); //或者是currentLevelOfOrg[*] ???
        }
        for (JsonPrimitive id : actualValueMap.keySet()) {
            if (!expectedValueMap.containsKey(id)) {
                failureMsg = "\"" + actualValueMap.get(id) + "\"" + " is unexpected from actual JsonArray.";
                failure = new Failure(parentLevel, FailureType.UNEXPECTED_JSONARRAY_ELEMENT, null, actualValueMap.get(id), failureMsg);
                result.getFailures().add(failure);
            }
        }

    }

    private static List<Failure> compareJsonArrayOfJsonPrimitives(
            String parentLevel, JsonArray expected, JsonArray actual) {
        List<Failure> result = new ArrayList<>();
        Map<JsonElement, Integer> expectedCount = Utils.getCardinalityMap(jsonArrayToList(expected));
        Map<JsonElement, Integer> actualCount = Utils.getCardinalityMap(jsonArrayToList(actual));
        Failure failure = new Failure();
        String failureMsg = null;

        for (JsonElement o : expectedCount.keySet()) {
            if (!actualCount.containsKey(o)) {
                failureMsg = "Missing JsonArray Element " + o + " in actual JsonArray";
                failure = new Failure(parentLevel, FailureType.MISSING_JSONARRAY_ELEMENT, o, null, failureMsg);
                failure.setExpected(o);
                result.add(failure);
            } else if (!actualCount.get(o).equals(expectedCount.get(o))) {
                failureMsg = "Occurrence of " + o + " : " + expectedCount.get(o) + " VS " + actualCount.get(o);
                failure = new Failure(parentLevel, FailureType.DIFFERENT_OCCURENCE_JSONARRAY_ELEMENT, o, o, failureMsg);
                result.add(failure);
            }
        }

        for (JsonElement o : actualCount.keySet()) {
            if (!expectedCount.containsKey(o)) {
                failureMsg = "Unexpected JsonArray Element " + o + " in actual JsonArray";
                failure = new Failure(parentLevel, FailureType.UNEXPECTED_JSONARRAY_ELEMENT, null, o, failureMsg);
                failure.setActual(o);
                result.add(failure);
            }
        }

        return result;
    }


    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.

    private static List<Failure> recursivelyCompareJsonArray(
            String parentLevel, JsonArray expected, JsonArray actual, CompareMode mode) {
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
                if (expectedElement instanceof JsonObject) {
                    CompareResult r = compareJson(parentLevel, expectedElement.getAsJsonObject(), actualElement.getAsJsonObject(), mode);
                    //save all the results, can de-dupe if necessary.
                    result.addAll(r.getFailures());

                    if (r.isPassed() == true) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (expectedElement instanceof JsonArray) {
                    if (compareJsonArray(parentLevel, (JsonArray) expectedElement, (JsonArray) actualElement, mode).isPassed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (expectedElement.equals(actualElement)) {
                    matched.add(j);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                String failureMsg = "\"" + expectedElement + "\"" + " is missing from actual JsonArray.";
                Failure failure = new Failure(parentLevel, FailureType.UNEXPECTED_JSONARRAY_ELEMENT, expectedElement, null, failureMsg);
                result.add(failure);
            }
        }

        for (int j = 0; j < actual.size(); j++) {
            if (matched.contains(j)) {
                continue;
            }

            JsonElement actualElement = actual.get(j);
            String failureMsg = "\"" + actualElement + "\"" + " is unexpected from actual JsonArray.";
            Failure failure = new Failure(parentLevel, FailureType.MISSING_JSONARRAY_ELEMENT, null, actualElement, failureMsg);
            result.add(failure);
        }

//        result = dedupleJsonArrayCompareResult(result);

        return result;
    }

    /*
    Not sure whether we need this kind of filter.
     */
    private static List<Failure> dedupleJsonArrayCompareResult(List<Failure> result) {
        Set<String> set = new HashSet<>();
  /*      Iterator<Failure> itr = result.iterator();
        while(itr.hasNext()) {
            Failure failure = itr.next();
            String field = failure.getField();
            for(String s : set) {
                if(s.contains(field)) {
                    itr.remove();
                    break;
                }
            }
            set.add(field);
        }*/

        return result;
    }

}
