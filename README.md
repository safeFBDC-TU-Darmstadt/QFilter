# QFilter: Integration of Access Control with Aggregation Query Processing over Outsourced Data #

## System Requirements ##
This branch requires an installation of **Java JDK version 17**. To resolve the project dependencies, you will need to have **gradle version 7.3 or higher** installed, or you can use the gradle wrapper.

## Executing Experiments ##
This branch still uses policy attachment, but not defined by any predicates (only randomly attached by the number of user groups and accessibility rate of data items). The relevant (varied) variable of each experiment (#columns, #rows, accessibility ratio, #userGroups) is always the first variable declared (typically as an array) in the experiment class.
Executing the tests of an experiment will print out the relevant metric (storage size in Byte or time in nanoseconds) for each of the values in the defined array (sometimes also for different query configurations).

For example, you can run experiment 1 by running the command <code>gradle test --tests 'Experiment1'</code> in the project directory. Overall there are 14 experiments (Experiment1, ..., Experiment14).
