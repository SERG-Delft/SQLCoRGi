package nl.tudelft.st01.FunctionalTests;

import java.util.Arrays;

public class ConditionTest extends FunctionTest{

    @Override
    protected void setUpDefaultTests() {
        tests = Arrays.asList(
                new DefaultTest(
                        "testLessThanInteger",
                        "SELECT * FROM table WHERE a < 100",

                        "SELECT * FROM table WHERE a = 99",
                        "SELECT * FROM table WHERE a = 100",
                        "SELECT * FROM table WHERE a = 101",
                        "SELECT * FROM table WHERE a IS NULL"),
                new DefaultTest(
                        "testLessThanEqualsInteger",
                        "SELECT * FROM table WHERE a <= 100",

                        "SELECT * FROM table WHERE a = 99",
                        "SELECT * FROM table WHERE a = 100",
                        "SELECT * FROM table WHERE a = 101",
                        "SELECT * FROM table WHERE a IS NULL"),
                new DefaultTest(
                        "testGreaterThanInteger",
                        "SELECT * FROM Table WHERE x > 28",

                        "SELECT * FROM Table WHERE x = 27",
                        "SELECT * FROM Table WHERE x = 28",
                        "SELECT * FROM Table WHERE x = 29",
                        "SELECT * FROM Table WHERE x IS NULL")
        );
    }
}
