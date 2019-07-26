# Coverage Rule Generator
[![Build Status](https://travis-ci.com/SERG-Delft/SQLCoRGi.svg?branch=master)](https://travis-ci.com/SERG-Delft/SQLCoRGi)
[![codecov](https://codecov.io/gh/SERG-Delft/SQLCoRGi/branch/master/graph/badge.svg)](https://codecov.io/gh/SERG-Delft/SQLCoRGi)

## Features

This tool takes in an SQL query (as a string) and returns MC/DC coverage rules for that query.
If the query contains an error, this tool will throw an appropriate exception, which clearly indicates what went wrong.

The implementation is based on the following paper: _Tuya, Javier, María José Suárez‐Cabal, and Claudio De La Riva. "Full predicate coverage for testing SQL database queries." Software Testing, Verification and Reliability 20, no. 3 (2010): 237-288._

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

## EvoSQL

This tool is part of the EvoSQL project, a search-based algorithm that generates test data for SQL queries. See our ICSE 2018 paper: _Castelein, J, Aniche, M, Soltani, M, Panichella, A & van Deursen, A 2018, Search-Based Test Data Generation for SQL Queries. in Proceedings of the 40th International Conference on Software Engineering. pp. 1220-1230, ICSE 2018, Gothenburg, Sweden, 27/05/18. https://doi.org/10.1145/3180155.3180202_

EvoSQL source code is available at https://github.com/serg-delft/evosql.

## License

This project is licensed under Apache License 2.0.
