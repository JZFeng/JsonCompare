package com.jz.json.jsoncompare;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jz.json.jsonpath.IFilter;

import java.util.*;

import static com.jz.json.jsonpath.JsonPath.*;
import static com.jz.json.utils.Utils.getJsonArrayMap;

/**
 * @author jzfeng
 */

public class Result {
    private Mode mode;
    private List<Failure> failures = new ArrayList<Failure>();

    public boolean passed() {
        return (failures.size() == 0);
    }

    public Result(Mode mode) {
        this.mode = mode;
    }

    public Result(List<Failure> failures) {
        this.failures.addAll(failures);
    }

    public Result(Mode mode, List<Failure> failures) {
        this.mode = mode;
        this.failures.addAll(failures);
    }


    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return this.mode;
    }

    public List<Failure> getFailures() {
        return failures;
    }

    public boolean addFailure(Failure failure) {
        return this.failures.add(failure);
    }

    @Override
    public String toString() {
        return getResultInfo(false);
    }

    public String getResultDetails() {
        return getResultInfo(true);
    }

    public int totalFailures() {
        return failures.size();
    }

    public Result applyFilter(Filter filter, JsonObject source, JsonObject dest) {
        if (mode != null) {
            //apply ignoredTypes
            boolean ignoreCase = filter.ignoreCase;
            Set<FailureType> typesToIgnore = new HashSet<>();
            for (FailureType type : filter.types) {
                typesToIgnore.add(type);
            }
            Iterator<Failure> itr = failures.iterator();
            while (itr.hasNext()) {
                Failure failure = itr.next();
                if (typesToIgnore.contains(failure.getFailureType())) {
                    itr.remove();
                }
            }

            //apply ignoredPaths
            Map<String, JsonArray> cachedJsonArrays = getJsonArrayMap(source, ignoreCase);
            String[] paths = filter.pathsOrg.toArray(new String[filter.pathsOrg.size()]);
            failures = applyIgnoredPaths(paths, ignoreCase, source, cachedJsonArrays);

            cachedJsonArrays = getJsonArrayMap(dest, ignoreCase);
            paths = filter.pathsDest.toArray(new String[filter.pathsDest.size()]);
            failures = applyIgnoredPaths(paths, ignoreCase, dest, cachedJsonArrays);
        } else {
            System.out.println("Please correct your filter first.");
        }

        return this;
    }


    public List<Failure> applyIgnoredPaths(
            String[] ignoredPaths,
            boolean ignoreCase,
            JsonObject source,
            Map<String, JsonArray> cachedJsonArrays) {
        if (ignoredPaths == null || ignoredPaths.length == 0) {
            return failures;
        }

        ignoredPaths = updatePaths2Full(ignoredPaths, source);
        Set<String> absolutePaths = getFullPathsWithoutArray(ignoredPaths, source); // for performance purpose;
        Map<String, List<IFilter>> ignoredFilters = getFilters(ignoredPaths, ignoreCase);
        Map<String, List<IFilter>> ignoredMatchedFilters = updateFilters2Full(cachedJsonArrays, ignoredFilters);

        //get regex for each ignoredPath
        List<String> regexs = new ArrayList<>();
        for (String ignoredPath : ignoredPaths) {
            if (ignoredPath.indexOf('[') == -1) {
                regexs.add(generateRegex(ignoredPath.trim(), ignoreCase) + ".*");
            }
        }

        Iterator<Failure> itr = failures.iterator();
        while (itr.hasNext()) {
            String level = itr.next().getPath();
            if (isPathMatchingAbsolutePaths(level, absolutePaths) || isPathMatchingRegxs(ignoreCase ? level.toLowerCase() : level, regexs) || isPathMatchingIgnoredFilters(cachedJsonArrays, ignoreCase ? level.toLowerCase() : level, ignoredMatchedFilters)) {
                itr.remove();
            }
        }

        return failures;
    }


    private String getResultInfo(boolean withDetails) {
        StringBuilder sb = new StringBuilder();
        if (failures.size() != 0) {
            sb.append("Total " + failures.size() + " failures : ");
        }

        Map<FailureType, List<Failure>> map = getFailureTypeListMap(failures);

        for (Map.Entry<FailureType, List<Failure>> entry : map.entrySet()) {
            FailureType type = entry.getKey();
            List<Failure> failures = entry.getValue();
            int numOfFailures = failures.size();
            sb.append("\n\r" + "****************************************************\n\r");
            sb.append("[Failure Reason = " + type + "; " + "Number of Failures:" + numOfFailures + "]\r\n");
            for (Failure f : failures) {
                if (withDetails) {
                    sb.append(f.toString());
                } else {
                    sb.append(f.getPath());
                }
                sb.append("\r\n");
            }
        }

        return sb.toString().trim();
    }

    private Map<FailureType, List<Failure>> getFailureTypeListMap(List<Failure> failures) {
        Map<FailureType, List<Failure>> map = new HashMap<>();
        for (Failure f : failures) {
            FailureType type = f.getFailureType();
            if (!map.containsKey(type)) {
                map.put(type, new ArrayList<Failure>());
            }
            map.get(type).add(f);
        }

        return map;
    }

}
