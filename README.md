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

You can clone our repo and then run the application.
Here is an example of how you would use this tool:

```java
import nl.tudelft.st01.CoverageRuleGenerator;

// ...

String query = "Select Name, Address FROM Customers WHERE balance > 1000";
List<String> coverageTargets = CoverageRulesGenerator.generateRules(query);

// Do with coverageTargets what you want
```

## Useful Links / Resources

[SQLFpc Web Tool](https://in2test.lsi.uniovi.es/sqlfpc/SQLFpcWeb.aspx)  
[Spreadsheet with example queries and corresponding results](https://docs.google.com/spreadsheets/d/1MvCkE1jT9OSuqwx1zA13ZczDIyj6YJizuWKqK0SITdY/edit#gid=0)  

