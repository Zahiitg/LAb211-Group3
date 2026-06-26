package model;

public class Category extends BaseEntity {
    private String name;

    public Category() {
    }

    public Category(String id, String name) {
        setId(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toCsvLine() {
        return getId() + "," + name;
    }

    @Override
    public void fromCsvLine(String csvRow) {
        String[] parts = csvRow.split(",", -1);
        if (parts.length >= 2) {
            setId(parts[0]);
            this.name = parts[1];
        }
    }
}
