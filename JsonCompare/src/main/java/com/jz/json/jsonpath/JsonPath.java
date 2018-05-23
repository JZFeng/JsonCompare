package com.jz.json.jsonpath;

import com.google.gson.*;
import com.jz.json.jsoncompare.JsonElementWithPath;
import com.jz.json.jsoncompare.WrongJsonPathException;
import com.jz.json.utils.Utils;

import java.io.File;
import java.util.*;

import static com.jz.json.jsonpath.Range.getRange;
import static com.jz.json.jsonpath.Range.mergeRanges;
import static com.jz.json.utils.Utils.getKeys;

/**
 * @author jzfeng
 * <p>
 * get(JsonObject source, String path), support standard JsonPath;
 * Here are some sample JsonPaths
 * String[] paths = new String[]{
 * "$.modules.BINSUMMARY.minView.actions[0]",
 * "modules.SELLERPRESENCE.sellerName.action.URL",
 * "RETURNS.maxView.value.length()",
 * "RETURNS.maxView.value[0:].label",
 * "RETURNS.maxView.value[*].label.textSpans[0]",
 * "RETURNS.maxView.value[1,3,4].label.textSpans[0].text",
 * "RETURNS.maxView.value[1,3,4].label.textSpans[?(@.text == \"Refund\" || @.text == \"Return policy\")].text",
 * "RETURNS.maxView.value[*].label.textSpans[?(@.text =~ \"(.*)\\d{3,}(.*)\" || @.text in {\"Have a nice day\", \"Return policy\"})]",
 * "URL" };
 */

public class JsonPath {

    public static void main(String[] args) throws Exception {

        JsonParser parser = new JsonParser();
        String json = Utils.convertFormattedJson2Raw(new File("/Users/jzfeng/Desktop/au.json"));
        JsonObject source = parser.parse(json).getAsJsonObject();

/*
        String[] paths = new String[]{
                "$.modules.BINSUMMARY.minView.actions[0]",
                "SELLERPRESENCE.sellerName.action.URL",
                 "RETURNS.maxView.value.length()"
                , "RETURNS.maxView.value[0:].label"
                , "RETURNS.maxView.value[*].label.textSpans[0]"
                , "RETURNS.maxView.value[1,3,4].label.textSpans[0].text"
                , "RETURNS.maxView.value[1,3,4].label.textSpans[?(@.text == \"Refund\" || @.text == \"Return policy\")].text"
                , "RETURNS.maxView.value[*].label.textSpans[?(@.text =~ \"(.*)\\d{3,}(.*)\" || @.text in {\"Have a nice day\", \"Return policy\"})]"
                , "URL"
                , "RETURNS.maxView.value[1:3]"
                , "RETURNS.maxView.value[-3:-1]"
                , "RETURNS.maxView.value[-2]"
        };


        String[] ignoredPaths = new String[]{
                "PICTURE.mediaList[0].image.originalImg.URL",  //3
                "RETURNS.maxView.value[3].value[0].textSpans[0].action.URL" // 1
                , "THIRD_PARTY_RESOURCES.js[0].url"  // 1
                , "BINSUMMARY.minView.actions[1].action.URL", // 2
                "$.modules.WATCH.watching.watchAction.action.URL" // 1
                , "modules.WATCH.watch.watchAction.action.URL"  // 1
                , "BINSUMMARY.minView.actions[2].value.cartSigninUrl.URL" // 2
//                total = 11;
        };
*/


        String[] paths = new String[]{"URL"};
        String[] ignoredPaths = new String[]{
                "modules.SELLERPRESENCE.profileLogo.URL",
                "modules.COMMITTOBUY.redirect.url",
                "modules.COMMITTOBUY.fallBackUrl.URL",
                "modules.SELLERPRESENCE.askSeller.action.URL",
                "modules.SELLERPRESENCE.sellerName.action.URL",
                "$.modules.PICTURE",
                "$.modules.ITEMDESC.itemDescription.action.URL",
                "$.modules.EBAYGUARANTEE.embg.infoLink.URL",
                "$.modules.OTHER_ACTIONS.soltAction.action.URL",
                "$.modules.OTHER_ACTIONS.reportItemAction.action.URL",
                "$.modules.OTHER_ACTIONS.surveyAction.action.URL",
                "$.modules.INCENTIVES.incentivesURL.URL",
                "$.modules.BID_LAYER.thumbnail.URL",
                "$.modules.BID_LAYER.reviewBidAction.action.URL",
                "$.modules.BID_LAYER.confirmBidAction.action.URL",
                "$.modules.BIDSUMMARY.bidAction.action.URL",
                "$.modules.TOPRATEDSELLER.topRatedInfo.logo.action.URL",
                "$.modules.RSPSECTION.minView.logo.action.URL",
                "$.modules.THIRD_PARTY_RESOURCES.js[*].url",
                "$.modules.BINSUMMARY.minView.actions[0,1,2].action.URL",
                "$.modules.HANDLINGCONTENT.value[*].textSpans[1].action.URL",
                "$.modules.RETURNS.maxView.value[3:5]",
                "$.modules.BID_LAYER.powerbidButtons[*].action.URL",
                "$.modules.REWARDS.value.textSpans[1].action.URL"
        };

        for (String path : paths) {
            long startTime = System.currentTimeMillis();
            List<JsonElementWithPath> res = get(source, path, true, ignoredPaths);
            System.out.println("****" + res.size() + "****" + (long) (System.currentTimeMillis() - startTime) + "ms" + "," + path);
            for (JsonElementWithPath je : res) {
                System.out.println(je);
            }
        }
    }


    public static List<JsonElementWithPath> get(
            String source, String path) throws Exception {
        List<JsonElementWithPath> res = new ArrayList<>();
        return get(source, path, false, new String[]{});
    }

    /**
     * @param source
     * @param path
     * @return
     * @throws Exception
     * @author jzfeng
     */
    public static List<JsonElementWithPath> get(
            String source, String path, boolean ignoreCase, String[] ignoredPaths) throws Exception {
        List<JsonElementWithPath> res = new ArrayList<>();
        if (source == null || source.length() == 0 || path == null || path.length() == 0) {
            return res;
        }

        JsonParser parser = new JsonParser();
        JsonObject src = parser.parse(source).getAsJsonObject();
        res = get(src, path, ignoreCase, ignoredPaths);

        return res;
    }


    /**
     * @param source the source of JsonObject
     * @param path   standard json path;
     * @return
     * @throws Exception
     * @author jzfeng
     */
    public static List<JsonElementWithPath> get(JsonObject source, String path) throws Exception {
        return get(source, path, false, new String[]{});
    }

    /**
     * @param source     the source of JsonObject
     * @param path       standard json path;
     * @param ignoreCase if true, it will ignore the case of path; if false, it will strictly match path;
     * @return returns a a list of {@link JsonElementWithPath}
     * @author jzfeng
     */
    public static List<JsonElementWithPath> get(
            JsonObject source, String path, boolean ignoreCase, String[] ignoredPaths) throws Exception {
        List<JsonElementWithPath> result = new ArrayList<>();
        if (path == null || path.length() == 0 || source == null || source.isJsonNull()) {
            return result;
        }

        String regex = generateRegex(path, ignoreCase);
        Map<String, List<Range>> ranges = getRanges(new String[]{path}, ignoreCase); // generate ranges from path;
        Map<String, List<Range>> matchedRanges = new LinkedHashMap<>(); //ranges with absolute path;
        Map<String, List<Range>> ignoredRanges = getRanges(ignoredPaths, ignoreCase);
        Map<String, List<Range>> ignoredMatchedRanges = new LinkedHashMap<>();
        Map<String, JsonArray> cachedJsonArrays = new LinkedHashMap<>(); // save JsonArray to map, in order to reduce time complexibility
        boolean isAbsolutePath = isAbsolutePath(path, source);
        boolean isFinished = false;

        Queue<JsonElementWithPath> queue = new LinkedList<JsonElementWithPath>();
        queue.offer(new JsonElementWithPath(source, "$"));
        while (!queue.isEmpty()) {
            int size = queue.size();
            //Traverse by level
            for (int i = 0; i < size; i++) {
                JsonElementWithPath org = queue.poll();
                String currentLevel = org.getLevel();
                JsonElement je = org.getJsonElement();
//                System.out.println(currentLevel);

                if (je.isJsonArray()) {
                    JsonArray ja = je.getAsJsonArray();
                    cachedJsonArrays.put(ignoreCase ? currentLevel.toLowerCase() : currentLevel, ja);
                    int length = ja.size();
                    for (int j = 0; j < ja.size(); j++) {
                        String level = currentLevel + "[" + j + "]";
                        JsonElementWithPath tmp = new JsonElementWithPath(ja.get(j), level);
                        queue.offer(tmp);
                        if (ignoreCase) {
                            level = level.toLowerCase();
                        }
                        updateMatchedFilters(level, ranges, matchedRanges);
                        updateMatchedFilters(level, ignoredRanges, ignoredMatchedRanges);
                        if (level.matches(regex)) {
                            isFinished = true;
                            if (isMatchingRanges(level, matchedRanges, length) && !isMatchingIgnoredRanges(level, ignoredMatchedRanges, length)) {
                                result.add(tmp);
                            }
                        }
                    }
                } else if (je.isJsonObject()) {
                    JsonObject jo = je.getAsJsonObject();
                    int length = jo.entrySet().size();
                    for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                        String level = currentLevel + "." + entry.getKey();
                        JsonElementWithPath tmp = new JsonElementWithPath(entry.getValue(), level);
                        queue.offer(tmp);
                        if (ignoreCase) {
                            level = level.toLowerCase();
                        }
                        updateMatchedFilters(level, ranges, matchedRanges);
                        updateMatchedFilters(level, ignoredRanges, ignoredMatchedRanges);
                        if (level.matches(regex)) {
                            isFinished = true;
                            if (isMatchingRanges(level, matchedRanges, length) && !isMatchingIgnoredRanges(level, ignoredMatchedRanges, length)) {
                                result.add(tmp);
                            }
                        }
                    }
                }
            }

            // current level is BFS done which means all possible candidates are already captured in the result,
            // end BFS by directly returning result;
            if (isAbsolutePath && isFinished) {
                return result;
            }

        }

        return applyIgnoredPathsWithoutArray(result, ignoredPaths, source);
    }


    /**
     * Very expensive if you do not enter full JsonPath, N square time complexibility;
     *
     * @param result
     * @param ignoredPaths
     * @return
     */
    private static List<JsonElementWithPath> applyIgnoredPathsWithoutArray(
            List<JsonElementWithPath> result, String[] ignoredPaths, JsonObject source) {
        if (ignoredPaths == null || ignoredPaths.length == 0) {
            return result;
        }

        //get all absolute paths.
        Set<String> absolutePaths = getAbsolutePaths(ignoredPaths, source);

        //get regex for each ignoredPath
        List<String> regexs = new ArrayList<>();
        for (String ignoredPath : ignoredPaths) {
            if (ignoredPath.indexOf('[') == -1) {
                regexs.add(generateRegex(ignoredPath.trim(), false) + ".*");
            }
        }

        Iterator<JsonElementWithPath> itr = result.iterator();
        while (itr.hasNext()) {
            JsonElementWithPath je = itr.next();
            String level = je.getLevel();
            if (absolutePaths.contains(level)) {
                itr.remove();
                continue;
            }

            for (String regex : regexs) {
                if (level.matches(regex)) {
                    itr.remove();
                    break;
                }
            }
        }

        return result;
    }


    /**
     * @param currentLevel  $.courses[i].grade
     * @param matchedRanges
     * @return true if i in matchedRange();
     */
    private static boolean isMatchingRanges(
            String currentLevel,
            Map<String, List<Range>> matchedRanges,
            int length

                                           ) throws Exception {

        if (matchedRanges == null || matchedRanges.size() == 0) {
            return true;
        }
        if (currentLevel.indexOf('[') == -1) {
            return true;
        }

        StringBuilder prefix = new StringBuilder();
        StringBuilder prepath = new StringBuilder();
        int index = 0;
        while ((index = currentLevel.indexOf('[')) != -1) {
            prepath.append(currentLevel.substring(0, currentLevel.indexOf(']') + 1));
            prefix.append(currentLevel.substring(0, index) + "[]");
            int i = Integer.parseInt(currentLevel.substring(index + 1, currentLevel.indexOf(']')));
            List<Range> filters = null;

            filters = matchedRanges.get(prefix.toString().trim());

            if (filters == null || filters.size() == 0) {
                return true;
            }

            boolean isMatched = false;
            if (filters.get(0) instanceof Range) {
                isMatched = isMatchingRange(filters, i, length);
            }

            if (isMatched) {
                currentLevel = currentLevel.substring(currentLevel.indexOf(']') + 1);
            } else {
                return false;
            }

        }

        return true;
    }


    private static boolean isMatchingIgnoredRanges(
            String currentLevel,
            Map<String, List<Range>> matchedRanges,
            int length) throws Exception {

        if (matchedRanges == null || matchedRanges.size() == 0) {
            return false;

        }
        if (currentLevel.indexOf('[') == -1) {
            return false;
        }

        StringBuilder prefix = new StringBuilder();
        StringBuilder prepath = new StringBuilder();
        int index = 0;
        while ((index = currentLevel.indexOf('[')) != -1) {
            prepath.append(currentLevel.substring(0, currentLevel.indexOf(']') + 1));
            prefix.append(currentLevel.substring(0, index) + "[]");
            int i = Integer.parseInt(currentLevel.substring(index + 1, currentLevel.indexOf(']')));
            List<Range> filters = null;

            filters = matchedRanges.get(prefix.toString().trim());

            if (filters == null || filters.size() == 0) {

                return false;

            }

            boolean isMatched = false;
            if (filters.get(0) instanceof Range) {
                isMatched = isMatchingRange(filters, i, length);
            }

            if (isMatched) {
                return true;
            } else {
                currentLevel = currentLevel.substring(currentLevel.indexOf(']') + 1);
            }

        }

        return false;
    }


    /**
     * @param currentLevel
     * @param ranges
     * @param matchedRanges
     */
    private static void updateMatchedFilters(
            String currentLevel, Map<String, List<Range>> ranges, Map<String, List<Range>> matchedRanges) {

        StringBuilder prefix = new StringBuilder();
        int index = 0;
        while ((index = currentLevel.indexOf('[')) != -1) {
            //update matchedRanges
            prefix.append(currentLevel.substring(0, index) + "[]");
            if (!matchedRanges.containsKey(prefix)) {
                for (Map.Entry<String, List<Range>> entry : ranges.entrySet()) {
                    String key = entry.getKey();
                    List<Range> value = entry.getValue();
                    int idx = prefix.indexOf(key);
                    if (idx != -1 && prefix.toString().substring(idx).equals(key)) {
                        matchedRanges.put(prefix.toString(), value);
                    }
                }
            }

            currentLevel = currentLevel.substring(currentLevel.indexOf(']') + 1);

        }
    }


    /**
     * to check whether $.modules.RETURNS.maxView.value[4] is matching the range in matchedRanges;
     *
     * @param i index of a JsonArray
     * @return return true if  i in any of the range [(0, 0), (3,3)], otherwise return false;
     */
    private static boolean isMatchingRange(
            List<Range> rangelist, int i, int length) throws Exception {
        if (rangelist != null && rangelist.size() > 0) {
            for (Range range : rangelist) {
                if (range.getStart() < 0 && range.getEnd() < 0) {
                    if ((i - length) >= range.getStart() && (i - length) <= range.getEnd()) {
                        return true;
                    }
                } else {
                    if (i >= range.getStart() && i <= range.getEnd()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private static String generateRegex(String path, boolean ignoreCase) {
        if (ignoreCase) {
            path = path.toLowerCase();
        }

        if (path.startsWith("$")) {
            path = path.substring(2);
        }

        StringBuilder prefix = new StringBuilder();
        int index = 0;
        while ((index = path.indexOf('[')) != -1) {
            prefix.append(path.substring(0, index) + "[]");
            path = path.substring(path.indexOf(']') + 1);
        }
        prefix.append(path);
        String regex = "(.*)\\." + prefix.toString().trim().replaceAll("\\[\\]", "\\\\[\\\\d+\\\\]");

        return regex;
    }


    private static int length(JsonObject jsonObject, String path, boolean ignoreCase) throws Exception {
        if (path == null || path.length() == 0) {
            return 0;
        }
        if (jsonObject == null || jsonObject.isJsonNull() || !jsonObject.isJsonObject()) {
            return 0;
        }

        int length = 0;
        List<JsonElementWithPath> result = get(jsonObject, path, ignoreCase, new String[]{});

        if (result == null || result.size() == 0) {
            length = 0;
        } else if (result.size() > 1) {
            throw new Exception("Please correct your json path to match a single JsonElement.");
        } else {
            JsonElement jsonElement = result.get(0).getJsonElement();
            if (jsonElement.isJsonObject()) {
                length = jsonElement.getAsJsonObject().entrySet().size();
            } else if (jsonElement.isJsonArray()) {
                length = jsonElement.getAsJsonArray().size();
            } else if (jsonElement.isJsonPrimitive()) {
                length = jsonElement.getAsJsonPrimitive().getAsString().length();
            }

        }

        return length;
    }


    /**
     * In most cases, you will not need ignore multiple JsonPaths. However in case you have one scenario,
     * it does support.
     * But keep in mind:
     * 1 For same JsonPath, both has List<Condition> and List<Range>, it chooses List<Condition>;
     * 2 For same JsonPath, if it has multiple List<Condition>, it chooses the last one;
     *
     * @param paths
     * @param ignoreCase
     * @return
     * @throws Exception
     * @Author jzfeng
     */

    private static Map<String, List<Range>> getRanges(String[] paths, boolean ignoreCase) throws Exception {
        if (paths == null || paths.length == 0) {
            return new LinkedHashMap<>();
        }

        Map<String, Set<Range>> rangeMap = new LinkedHashMap<>();
        for (String path : paths) {
            if (path == null || path.trim().length() == 0) {
                continue;
            }

            StringBuilder prefix = new StringBuilder();
            int index = 0;
            while ((index = path.indexOf('[')) != -1) {
                prefix.append(path.substring(0, index) + "[]");
                String r = path.substring(index + 1, path.indexOf(']')).trim();
                String key = prefix.toString().trim();
                if (r.contains("@")) { //conditions, "?(@.text =~ "(.*)\d{3,}(.*)" || @.text in {"Have a nice day", "Return policy"})"
                    //to-do
                } else if (r.matches("(.*)([,:])(.*)") || r.contains("*") || r.matches("\\s{0,}(\\d+)\\s{0,}") ) {
                    List<Range> ranges = getRange(r);
                    if (ranges != null && ranges.size() > 0) {
                        if (ignoreCase) {
                            key = key.toLowerCase();
                        }

                        if (rangeMap.containsKey(key)) {
                            rangeMap.get(key).addAll(ranges);
                        } else {
                            rangeMap.put(key, new HashSet<>(ranges));
                        }
                    }
                } else {
                    throw new WrongJsonPathException("Invalid JsonPath : " + path);
                }

                path = path.substring(path.indexOf(']') + 1);
            }
        }

        Map<String, List<Range>> rm = processRangeMap(rangeMap);

        return rm;
    }


    private static Map<String, List<Range>> processRangeMap(Map<String, Set<Range>> rangeMap) {
        Map<String, List<Range>> res = new LinkedHashMap<>();
        if (rangeMap == null || rangeMap.size() == 0) {
            return res;
        }

        for (Map.Entry<String, Set<Range>> entry : rangeMap.entrySet()) {
            String key = entry.getKey();
            Set<Range> value = entry.getValue();
            if (value != null && value.size() > 0) {
                List<Range> ranges = mergeRanges(new ArrayList<>(value));
                res.put(key, ranges);
            }
        }

        return res;
    }

    private static Set<String> getAbsolutePaths(String[] paths, JsonObject source) {
        Set<String> res = new LinkedHashSet<>();
        if (paths == null || paths.length == 0) {
            return res;
        }

        Set<String> keys = getKeys(source);
        for (String path : paths) {
            path = path.trim();
            if (path.indexOf('[') != -1) {
                continue;
            }
            if (path.startsWith("$")) {
                res.add(path);
                continue;
            }

            int index = path.indexOf(".");
            if ((index == -1 && keys.contains(path)) || (index != -1 && keys.contains(path.substring(0, index)))) {
                res.add("$." + path);
            }

        }

        return res;
    }

    private static boolean isAbsolutePath(String[] paths, JsonObject source) {
        if (paths == null || paths.length == 0) {
            return true;
        }

        Set<String> keys = getKeys(source);
        for (String path : paths) {
            path = path.trim();
            if (path.startsWith("$")) {
                continue;
            }

            int index = path.indexOf(".");
            if (index == -1) {
                if (!keys.contains(path)) {
                    return false;
                }
            } else {
                if (!keys.contains(path.substring(0, index))) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isAbsolutePath(String path, JsonObject source) {
        return isAbsolutePath(new String[]{path}, source);
    }

}
