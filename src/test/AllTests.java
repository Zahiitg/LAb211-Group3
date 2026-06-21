package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CsvParseJUnitTest.class,
        RepositoryJUnitTest.class,
        ProductRepositoryJUnitTest.class,
        FlashSaleRepositoryJUnitTest.class,
        FlashSaleEngineJUnitTest.class,
        FlashSaleControllerJUnitTest.class,
        AdminControllerJUnitTest.class,
        CustomerControllerJUnitTest.class,
        OrderControllerJUnitTest.class,
        OrderTest.class,
        OrderDetailTest.class,
        OrderTransactionTest.class
})
public class AllTests {
}
