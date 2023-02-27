# Background
When I was working in eBay ViewItem team, My first task in eBay is a task about comparing Experience Service response with the VLS response's listing module. I cannot find an existing tool that supports Json compare with ignoring JsonArray element sequence. Furthermore, I cannot ignore some JsonPaths during the compare.

So I decide to implement it myself.

[UPDATE] There is a 3rd party library called [jsonuit](https://github.com/lukas-krecan/JsonUnit), which is better than my implementation.

# Filter to ignore some differences
In some json comparing, you may want to need ignore some JsonPaths.
You can pass a filter when you compare two JsonObjects.
Here is the definition of a filter:
``` java
    public class Filter {
        // enum of FailureType like UNEQUAL_VALUE, MISSING_PROPERTY, DIFFERENT_JSONARRY_SIZE etc.
        List<FailureType> types = new ArrayList<FailureType>();
        
        //standard JsonPaths that you want to ignore from 1st JsonObject
        List<String> pathsOrg = new ArrayList<String>();
    
        //JsonPath samples, "$.modules.RETURNS.maxView.value[3:5]", "RETURNS.maxView.value[*].label.textSpans[?(@.text =~ \"(.*)\\d{3,}(.*)\"], "RETURNS.maxView.value[-3:-1]"    
        List<String> pathsDest = new ArrayList<>();
        
        // Is JsonPath case sensitive or not?
        boolean ignoreCase; 
    }

    public static Result compareJson(JsonObject o1, JsonObject o2, String mode, Filter filter) {
        // mode is an enum, STRICT | LENIENT
        // Sample Filter
    }

    Filter filter = new Filter(
        new String[]{"UNEXPECTED_PROPERTY"},
        new String[]{"$.listing.listingProperties[2].propertyValues[*]",
        "listing.listingLifecycle.scheduledStartDate.value",
        "listing.termsAndPolicies.logisticsTerms.logisticsPlan[0:]"},
        new String[]{"listing.tradingSummary.lastVisitDate"},
        true
    );

```


# Algorithms
Not too many complex algorithms, BFS is used to traverse JsonNode tree.
The most difficult part is to compare JsonArray with ignoring sequence. To handle that, the algorith is as following:
1 If a JsonArray consists of JsonObjects, trying to find the unique key;
2 To make it as a unique key, the value of this key in those JsonObjects must be unique and the value must be JsonPrimitive;
3 One unique key is found, build up the map. KEY = value of the unique key ; VALUE = JsonObject which has the value;
4 Iterate the map and compare JsonObjects one by one.


``` java
    /**
     * Searches for the unique key of the {@code expected} JSON array.
     *
     * @param array the array to find the unique key of
     * @return the unique key if there's any, otherwise null
     */
    public static String findUniqueKey(JsonArray array) {
        // Find a unique key for the object (id, name, whatever)
        if (array.size() > 0 && (array.get(0) instanceof JsonObject)) {
            JsonObject o = ((JsonObject) array.get(0)).getAsJsonObject();
            Set<String> keys = getKeys(o);
            for (String candidate : keys) {
                if (isUsableAsUniqueKey(candidate, array)) {
                    return candidate;
                }
            }
        }

        return null;
    }
    

    /**
     * @param candidate is usable as a unique key if every element in the
     * @param array     is a JSONObject having that key, and no two values are the same.
     * @return true if the candidate can work as a unique id across array
     */

    public static boolean isUsableAsUniqueKey(String candidate, JsonArray array) {
        Set<JsonElement> seenValues = new HashSet<JsonElement>();
        for (int i = 0; i < array.size(); i++) {
            JsonElement item = array.get(i);
            if (item instanceof JsonObject) {
                JsonObject o = (JsonObject) item;
                if (o.has(candidate)) {
                    JsonElement value = o.get(candidate);
                    if (isSimpleValue(value) && !seenValues.contains(value)) {
                        seenValues.add(value);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }
    
        // build hashmap, KEY is UniqueKey's Value, VALUE is JsonObject;
    public static Map<JsonPrimitive, JsonElementWithPath> arrayOfJsonObjectToMap(
            String parentLevel, JsonArray array, String uniqueKey) {
        Map<JsonPrimitive, JsonElementWithPath> valueMap = new HashMap<JsonPrimitive, JsonElementWithPath>();
        if (uniqueKey != null) {
            for (int i = 0; i < array.size(); ++i) {
                JsonObject jsonObject = (JsonObject) array.get(i);
                JsonPrimitive id = jsonObject.get(uniqueKey).getAsJsonPrimitive();
                valueMap.put(id, new JsonElementWithPath(jsonObject, parentLevel + "[" + String.valueOf(i) + "]"));
            }
        }

        return valueMap;
    }    
    
```
