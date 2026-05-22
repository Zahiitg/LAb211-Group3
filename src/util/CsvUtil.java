package util;

public class CsvUtil {
    /**
     * Splits a CSV line by comma, ignoring commas that are enclosed in double quotes.
     */
    public static String[] splitCsvLine(String line) {
        if (line == null || line.isEmpty()) {
            return new String[0];
        }
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    /**
     * Escapes a string for CSV, enclosing in quotes if it contains commas or quotes.
     */
    public static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    /**
     * Unescapes a CSV string, removing bounding quotes and un-doubling inner quotes.
     */
    public static String unescapeCsv(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\"\"", "\"");
        }
        return s;
    }
}
