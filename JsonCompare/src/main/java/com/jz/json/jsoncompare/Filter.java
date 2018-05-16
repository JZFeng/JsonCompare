package com.jz.json.jsoncompare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jzfeng
 */
public class Filter {
    List<FailureType> ignoredTypes = new ArrayList<FailureType>();
    List<String> ignoredPaths = new ArrayList<String>();

    Filter(String[] ignoredTypes, String[] ignoredPaths) {
        Set<String> set = new HashSet<>();
        for (FailureType type : FailureType.values()) {
            set.add(type.name());
        }
        for(String type : ignoredTypes) {
            if(set.contains(type)) {
                this.ignoredTypes.add(FailureType.valueOf(type));
            }
        }

        for (String field : ignoredPaths) {
            if(field.length() > 0) {
                this.ignoredPaths.add(field);
            }
        }
    }


    Filter(FailureType[] ignoredTypes, String[] ignoredPaths) {
        for (FailureType type : ignoredTypes) {
            this.ignoredTypes.add(type);
        }

        for (String field : ignoredPaths) {
            this.ignoredPaths.add(field);
        }

    }

    Filter(List<FailureType> ignoredTypes, List<String> ignoredPaths) {
        this.ignoredTypes = ignoredTypes;
        this.ignoredPaths = ignoredPaths;
    }

}


