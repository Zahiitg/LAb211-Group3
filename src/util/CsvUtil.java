package util;
public class CsvUtil {
    public static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }
    public static String unescapeCsv(String value) {
        if (value == null) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) return value.substring(1, value.length() - 1).replace("\"\"", "\"");
        return value;
    }
    public static String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}
