# Background
When I was working in eBay ViewItem team, My first task in eBay is a task about comparing Experience Service response with the VLS response's listing module. I cannot find an existing tool that supports Json compare with ignoring JsonArray element sequence. Furthermore, I cannot ignore some JsonPaths during the compare.

So I decide to implement it myself.

[UPDATE] there is a 3rd party library called [jsonuit](https://github.com/lukas-krecan/JsonUnit)

# Filter to ignore some differences
In some json comparing, you may want to need ignore some JsonPaths.
You can pass a filter when you compare two JsonObjects.
Here is the definition of a filter:
``` java
public class Filter {
List<FailureType> types = new ArrayList<FailureType>(); // enum of FailureType like UNEQUAL_VALUE,
MISSING_PROPERTY, DIFFERENT_JSONARRY_SIZE etc.
List<String> pathsOrg = new ArrayList<String>(); //standard JsonPaths that you want to ignore from 1st
JsonObject
//JsonPath samples, "$.modules.RETURNS.maxView.value[3:5]", "RETURNS.maxView.value[*].label.textSpans[?(@.
text =~ \"(.*)\\d{3,}(.*)\"], "RETURNS.maxView.value[-3:-1]"
List<String> pathsDest = new ArrayList<>(); //standard JsonPaths that you want to ignore from 2nd JsonObject
boolean ignoreCase; // Is JsonPath case sensitive or not?
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
