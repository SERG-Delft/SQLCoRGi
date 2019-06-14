# Coverage Rule Generator

<!---
e.g. https://gitlab.ewi.tudelft.nl/TI2806/2018-2019/CS/CP19-CS-01/cool-project/badges/master/pipeline.svg
--->

[![build status](https://gitlab.ewi.tudelft.nl/TI2806/2018-2019/ST/cp19-st-01/st-01/badges/master/pipeline.svg)](https://gitlab.ewi.tudelft.nl/TI2806/2018-2019/ST/cp19-st-01/st-01/commits/master)
[![coverage report](https://gitlab.ewi.tudelft.nl/TI2806/2018-2019/ST/cp19-st-01/st-01/badges/master/coverage.svg)](https://gitlab.ewi.tudelft.nl/TI2806/2018-2019/ST/cp19-st-01/st-01/commits/master)


Our time estimates are cumulative, e.g.: If we estimate 2 people will each spend 3 hours on an issue, the estimate is 6 hours.

## Features

This tool takes in an SQL query (as a string) and returns MC/DC coverage rules for that query.
If the query contains an error, this tool will throw an appropriate exception, which clearly indicates what went wrong.

## Usage

Please do the following to use our tool:
1. Make sure `Java 8+` is installed on your machine
2. Make sure `Maven 3.3.9+` is installed on your machine
3. Clone our repository
4. Open our tool in your favourite IDE
5. Go to the `CoverageRuleGenerator` class and change the input query string.
Here is an example of how you would use this:

```java
import nl.tudelft.st01.CoverageRuleGenerator;

// ...

String query = "Select Name, Address FROM Customers WHERE balance > 1000";
List<String> coverageTargets = CoverageRulesGenerator.generateRules(query);

// Do with coverageTargets what you want
```
6. Enjoy the coverage targets related to the provided query.
7. (Optionally, add the Checkstyle, PMD and SpotBug plugins to your IDE to simplify your life. We used IntelliJ to develop this tool.)

## Useful Links / Resources

[SQLFpc Web Tool](https://in2test.lsi.uniovi.es/sqlfpc/SQLFpcWeb.aspx)  
[Spreadsheet with example queries and corresponding results](https://docs.google.com/spreadsheets/d/1MvCkE1jT9OSuqwx1zA13ZczDIyj6YJizuWKqK0SITdY/edit#gid=0)  

