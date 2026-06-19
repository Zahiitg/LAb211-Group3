package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RepositoryJUnitTest.class,
        OrderTest.class,
        OrderDetailTest.class,
        OrderTransactionTest.class
})
public class AllTests {
}