# Coverage Rule Generator
![Travis CI build status](https://travis-ci.com/SERG-Delft/SQLCoRGi.svg?branch=master)
[![codecov](https://codecov.io/gh/SERG-Delft/SQLCoRGi/branch/master/graph/badge.svg)](https://codecov.io/gh/SERG-Delft/SQLCoRGi)

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
import CoverageRuleGenerator;

// ...

String query = "Select Name, Address FROM Customers WHERE balance > 1000";
Set<String> coverageRules = CoverageRuleGenerator.generateRules(query);

// Use the generated coverage rules
```
6. Enjoy the coverage rules generated for the provided query.
