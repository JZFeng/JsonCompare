package com.jz.json.jsoncompare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Filter {
    List<FailureType> types = new ArrayList<FailureType>();
    List<String> fields = new ArrayList<String>();

    Filter() {

    }


    Filter(String[] types, String[] fields) {
        Set<String> set = new HashSet<>();
        for (FailureType type : FailureType.values()) {
            set.add(type.name());
        }
        for(String type : types) {
            if(set.contains(type)) {
                this.types.add(FailureType.valueOf(type));
            }
        }

        for (String field : fields) {
            if(field.length() > 0) {
                this.fields.add(field);
            }
        }
    }


    Filter(FailureType[] types, String[] fields) {
        for (FailureType type : types) {
            this.types.add(type);
        }

        for (String field : fields) {
            this.fields.add(field);
        }

    }

    Filter(List<FailureType> types, List<String> fields) {
        this.types = types;
        this.fields = fields;
    }

}


