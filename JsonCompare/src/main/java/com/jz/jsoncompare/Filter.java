package com.jz.jsoncompare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jzfeng
 */
public class Filter {
    List<FailureType> types = new ArrayList<FailureType>();
    List<String> pathsOrg = new ArrayList<String>();
    List<String> pathsDest = new ArrayList<>();
    boolean ignoreCase;


    public Filter(String[] types, String[] pathsOrg, String[] pathsDest) {
        Set<String> set = new HashSet<>();
        for (FailureType type : FailureType.values()) {
            set.add(type.name());
        }

        for (String type : types) {
            if (set.contains(type)) {
                this.types.add(FailureType.valueOf(type));
            }
        }

        for (String path : pathsOrg) {
            if (path.trim().length() > 0) {
                this.pathsOrg.add(path.trim());
            }
        }

        for (String path : pathsDest) {
            if (path.trim().length() > 0) {
                this.pathsDest.add(path.trim());
            }
        }
    }

    Filter(String[] types, String[] pathsOrg, String[] pathsDest, boolean ignoreCase) {
        this(types, pathsOrg, pathsDest);
        this.ignoreCase = ignoreCase;
    }

    Filter(String[] types, String[] pathsOrg) {
        this(types, pathsOrg, new String[]{});
    }


}


