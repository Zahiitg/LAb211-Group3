package repository;

import model.Category;

public class CategoryRepository extends CsvRepository<Category> {

    public CategoryRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected Category createEntity() {
        return new Category();
    }

    @Override
    protected String getHeader() {
        return "id,name";
    }
}
