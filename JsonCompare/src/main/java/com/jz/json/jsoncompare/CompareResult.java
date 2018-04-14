package com.jz.json.jsoncompare;

import java.util.*;

public class CompareResult {
    private CompareMode mode;
    private List<Failure> failures = new ArrayList<Failure>();

    public boolean isPassed() {
        return (failures.size() == 0);
    }

    CompareResult() {

    }

    CompareResult(CompareMode mode) {
        this.mode = mode;
    }

    CompareResult(CompareMode mode, List<Failure> failures) {
        this.mode = mode;
        this.failures.addAll(failures);
    }

    public static void main(String[] args) {
    }


    public void setMode(CompareMode mode) {
        this.mode = mode;
    }

    CompareResult(List<Failure> failures) {
        this.failures.addAll(failures);
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

    public List<Failure> getFailures() {
        return failures;
    }

    public int totalFailures() {
        return failures.size();
    }


    public CompareResult applyFilter(Filter filter) throws WrongFilterException {

        List<Failure> result = new LinkedList<>();

        Map<FailureType, List<Failure>> map = getFieldFailureTypeListMap(failures);

        if (mode != null && isValidFilter(filter)) {

            List<FailureType> typesToIgnore = filter.types;
            List<String> fieldsToIgnore = filter.fields;

            //apply types
            for (FailureType type : typesToIgnore) {
                if (map.containsKey(type)) {
                    map.remove(type);
                }
            }

            //apply fields
            for (Map.Entry<FailureType, List<Failure>> entry : map.entrySet()) {
                result.addAll(entry.getValue());
            }

            for (String field : fieldsToIgnore) {
                String regex = generateRegex(field);

                Iterator<Failure> itr = result.iterator();
                while (itr.hasNext()) {
                    Failure failure = itr.next();
                    if (failure.getField().matches(regex)) {
                        itr.remove();
                    }
                }
            }
        } else {
            System.out.println("Please correct your filter first.");
        }

        return new CompareResult(mode, result);
    }

    private String generateRegex(String field) {
        if (field.startsWith("$")) {
            field = "\\$" + field.substring(1);
        }

        String regex = "(.*)(" + (field.replaceAll("(\\[)(\\d{0,})(\\])", "\\\\" + "$1" + "$2" + "\\\\" + "$3")) + ")(.*)";

        return regex;
    }

    private String getResultInfo(boolean withDetails) {
        StringBuilder sb = new StringBuilder();
        if (failures.size() != 0) {
            sb.append("Total " + failures.size() + " failures : ");
        }

        Map<FailureType, List<Failure>> map = getFieldFailureTypeListMap(failures);

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
                    sb.append(f.getField());
                }
                sb.append("\r\n");
            }
        }

        return sb.toString().trim();
    }

    private boolean isValidFilter(Filter filter) throws WrongFilterException {
        Map<FailureType, List<Failure>> map = getFieldFailureTypeListMap(failures);

        //validate type
        Set<FailureType> set = new HashSet<>();
        for (FailureType type : FailureType.values()) {
            set.add(type);
        }
        for (FailureType type : filter.types) {
            if (!set.contains(type)) {
                throw new WrongFilterException("\"" + type + "\"" + "is not a valid Failure type.");
//                System.out.println("\"" + type + "\"" + "is not a valid Failure type.");
            }
        }

        //validate fields
        for (String field : filter.fields) {
            if (!isValidField(field, mode)) {
                throw new WrongFilterException(field + " is not a valid field filter");
            }
        }

        return true;
    }


    //    private boolean isValidField(String field, CompareMode mode) {
    private static boolean isValidField(String field, CompareMode mode) {
        if (mode == CompareMode.LENIENT) {
            int index = field.indexOf("[");
            while (index != -1) {
                char c = field.charAt(index + 1);
                if (c != ']') {
                    return false;
                }
                field = field.substring(field.indexOf("]") + 1);
                index = field.indexOf("[");
            }
        } else if (mode == CompareMode.STRICT) {
            int index = field.indexOf("[");
            while (index != -1) {
                char c = field.charAt(index + 1);
                if (c == ']') {
                    return false;
                }
                field = field.substring(field.indexOf("]") + 1);
                index = field.indexOf("[");
            }
        }

        return true;
    }


    private Map<FailureType, List<Failure>> getFieldFailureTypeListMap(List<Failure> failures) {
        Map<FailureType, List<Failure>> map = new HashMap<FailureType, List<Failure>>();
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
