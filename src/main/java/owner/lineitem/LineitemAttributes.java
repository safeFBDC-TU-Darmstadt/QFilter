package owner.lineitem;

import java.util.HashMap;
import java.util.Map;

/**
 * A class listing important attributes of the tpc-h benchmark table to use across this application.
 */
public class LineitemAttributes {

    public static final String filePath = "src/main/resources/lineitem.tbl";
    public static final String[] attributeNames = new String[]{"orderkey", "partkey", "suppkey", "linenumber", "quantity", "extendedprice", "discount", "tax"};
    public static Map<String, Class<?>> attributeTypes = new HashMap<>();
    public static String tableName = "lineitem";
    public static int maxStringLength = 8;
    public static int decimalPlaces = 2;

    static {
        attributeTypes.put("orderkey", Integer.class);
        attributeTypes.put("partkey", Integer.class);
        attributeTypes.put("suppkey", Integer.class);
        attributeTypes.put("linenumber", Integer.class);
        attributeTypes.put("quantity", Integer.class);
        attributeTypes.put("extendedprice", Float.class);
        attributeTypes.put("discount", Float.class);
        attributeTypes.put("tax", Float.class);
    }

    /**
     * A method to determine the attribute names of the resulting table of using different parameters for policy attachment.
     *
     * @param attachPolicy {@code true}  if a policy should be attached.
     * @param columnPolicy {@code true} if a column level policy should be attached.
     *                     {@code false} if a tuple level policy should be attached.
     * @param columnLimit  The maximum number of attributes of the tpc-h table that should be used.
     * @return An array of attribute names for the resulting table of using the above parameters. Policy attribute names
     * will be given by an appended "p" to the original name using a column level policy. For a tuple level policy, the
     * attribute name "tuplep" will be added.
     */
    public static String[] getAttributes(boolean attachPolicy, boolean columnPolicy, int columnLimit) {
        String[] attributes;
        if (!attachPolicy) {
            attributes = new String[columnLimit];
            System.arraycopy(attributeNames, 0, attributes, 0, columnLimit);
            return attributes;
        }

        if (columnPolicy) {
            attributes = new String[columnLimit * 2];
            for (int i = 0; i < columnLimit; i++) {
                attributes[i] = attributeNames[i];
                attributes[columnLimit + i] = attributeNames[i] + "p";
            }
        } else {
            attributes = new String[columnLimit + 1];
            System.arraycopy(attributeNames, 0, attributes, 0, columnLimit);
            attributes[columnLimit] = "tuplep";
        }
        return attributes;
    }

}
