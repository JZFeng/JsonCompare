import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jz.json.jsoncompare.CompareResult;
import org.junit.Assert;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;
import static com.jz.json.jsoncompare.JsonCompare.compareJson;
import static com.jz.json.jsoncompare.Utils.convertFormattedJson2Raw;


/**
 * @author jzfeng
 */

public class TestJsonCompare {


    @Test
    public void testJsonCompare_compare_two_null() throws Exception {

        CompareResult result = compareJson(null, null, "LENIENT");
        System.out.println(result.getResultDetails());
        Assert.assertTrue("Two JsonObjects are equal", result.getFailures().size() == 0);
    }

    @Test
    public void testJsonCompare_compare_two_empty() throws Exception {

        CompareResult result = compareJson(new JsonObject(), new JsonObject(), "LENIENT");
        System.out.println(result.getResultDetails());
        Assert.assertTrue("Two JsonObjects are equal", result.getFailures().size() == 0);
    }

    @Test
    public void testJsonCompare_equal_JsonArray_lenient() throws Exception {
        JsonParser parser = new JsonParser();
        String json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/JA1.json"));
        JsonObject o1 = parser.parse(json).getAsJsonObject();

        json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/JA2.json"));
        JsonObject o2 = parser.parse(json).getAsJsonObject();

        CompareResult result = compareJson(o1, o2, "LENIENT");
        System.out.println(result.getResultDetails());
        Assert.assertTrue("Two JsonObjects are equal", result.getFailures().size() == 0);
    }

    @Test
    public void testJsonCompare_jsonObjects_lenient() throws IOException {
        JsonParser parser = new JsonParser();
        String json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/O.json"));
        JsonObject o1 = parser.parse(json).getAsJsonObject();

        json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/D.json"));
        JsonObject o2 = parser.parse(json).getAsJsonObject();

        CompareResult result = compareJson(o1, o2, "LENIENT");
        System.out.println(result.getResultDetails());
        Assert.assertTrue("Two JsonObjects are equal", result.getFailures().size() == 13);
    }

    @Test
    public void testJsonCompare_jsonObjects_strict() throws Exception {
        JsonParser parser = new JsonParser();
        String json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/O.json"));
        JsonObject o1 = parser.parse(json).getAsJsonObject();

        json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/D.json"));
        JsonObject o2 = parser.parse(json).getAsJsonObject();

        CompareResult result = compareJson(o1, o2, "STRICT");
        System.out.println(result.getResultDetails());
        Assert.assertTrue("Two JsonObjects are equal", result.getFailures().size() == 12);
    }

    @Test
    public void testJsonCompare_jsonObjects_lenient_with_filter() throws Exception {
        JsonParser parser = new JsonParser();
        String json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/O.json"));
        JsonObject o1 = parser.parse(json).getAsJsonObject();

        json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/D.json"));
        JsonObject o2 = parser.parse(json).getAsJsonObject();

        CompareResult result = compareJson(o1, o2, "STRICT");
        System.out.println(result.getResultDetails());
    }

    @Test
    public void testJsonCompare_jsonObjects_strict_with_filter() throws Exception {
        JsonParser parser = new JsonParser();
        String json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/O.json"));
        JsonObject o1 = parser.parse(json).getAsJsonObject();

        json = convertFormattedJson2Raw(new File("/Users/jzfeng/git/JsonCompare/JsonCompare/src/test/java/D.json"));
        JsonObject o2 = parser.parse(json).getAsJsonObject();

        CompareResult result = compareJson(o1, o2, "STRICT");
        System.out.println(result.getResultDetails());
    }


}
