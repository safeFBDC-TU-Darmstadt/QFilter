
# QFilter: Integration of Access Control with Aggregation Query Processing over Outsourced Data #

QFilter is a fine-grained access control enforcement mechanism tightly integrated with query processing to evaluate aggregation SQL queries (i.e., "count", "sum", and "avg" having single-dimensional, conjunctive, or disjunctive equality query conditions) over secret-shared data without revealing any information about data, associated access policies, user queries and query results. It employs an Attribute-Based Access Control (ABAC) model to specify the set of fine-grained access control policies at the data owner side by adding new attributes to the relation to be outsourced in such a way that each value of the new attribute represents a specific ABAC policy. It obliviously rewrites the submitted aggregation SQL query by adding some query conditions in the where clause of the query to check access authorizations and filter out unauthorized data items during query processing at the server side. It does not require any communication between any two servers before/during/after query rewriting and query execution at the server side. QFilter has a set of string matching based operators to obliviously compute query results. It is a privacy-preserving communication-efficient access control enforcement mechanism which can support two levels of policy attachments: Tuple Level Policy Attachment and Attribute Level Policy Attachment.

__Please note:__ QFilter consists of two branches "master" and "performance-evaluation". 

## Configuration &amp; System Requirements ##
QFilter requires an installation of **Java JDK version 17**. To resolve the project dependencies, you will need to have **gradle version 7.3 or higher** installed, or you can use the gradle wrapper.
The memory usage of QFilter is mainly determined by the number of servers and the upload batch size.
For a manual start, these variables - as well as multiple variables concerning the simulation setting - can be configured in the manual.Configuration file.
Overall, you can configure the following variables:

> - **numServers**: the number of servers to use (there is always a minimum threshold of servers to use for correctness; we need at least 2*c*l+2 for queries with at most c query conditions, and a maximum of l digits for any number in the uploaded table [l=8 for the first eight attributes of the *LINEITEM* table in TPC-H benchmark])
> - **attachPolicy**: true if you want to attach a policy to the data, false otherwise
> - **batchSize**: the batchSize to use when uploading the secret share tables to the servers (= number of rows to use for each batch upload)
> - **rowLimit**: the maximum number of rows to upload to the servers
> - **columnPolicy**: whether to attach an attribute level policy (=true) or a tuple level policy (=false)
> - **NUM_GROUPS**: the number of user groups to use
> - **accessibilityPercentage**: the percentage of tuples / column values accessible the first user group (data users are in the first user group by default)

Execution with the default values (numServers=200 and batchSize=100) does not require more than 1GB of main memory.

## Manual Start ##
First, you will need to resolve the project dependencies and build the project by running the command <code>gradle build</code>.

This branch uses policy attachment, but not defined by any predicates (only randomly attached by the number of user groups and accessibility rate of data items). For a manual start on this branch, you can use the <code>manual.Starter.main()</code> or manually follow this order:
```
manual.RegistryHost.main()
server.Server.main()
owner.DataOwner.main()
user.DataUser.main()
```

We have created a gradle run task for the Starter and each actor. You can run the Starter by running the command <code>gradle runStarter -q --console=plain</code> in the project directory. Alternatively, you can manually execute the following commands:
```
gradle runRegistry
gradle runServers
gradle runDataOwner
gradle runDataUser -q --console=plain
```
Each run task requires a new terminal.

For a correct startup with the latter, wait for a confirming console output of after calling <code>server.Server.main()</code> ("${numServers} servers are online!") and <code>owner.DataOwner.main()</code> ("finished uploading tables!").
After calling <code>user.DataUser.main()</code>, you can input simple queries for the *LINEITEM* table (this prototype only supports count, sum and avg aggregation, query conditions with the same conditional type (AND/OR), and only simple query predicates like "attr=const").
We only use the following attributes of the *LINEITEM* table: "orderkey", "partkey", "suppkey", "linenumber", "quantity", "extendedprice", "discount" and "tax".

## Publication ##

Meghdad Mirabi and Carsten Binnig. (2023). **Integration of Access Control with Aggregation Query Processing over Outsourced Data**. In: 49th International Conference on Very Large Data Bases, Vancouver, Canada - August 28 to September 1, 2023 **(Under Review)** <p dir="auto"><a href="/safeFBDC-TU-Darmstadt/QFilter/blob/master/Final%20Paper-VLDB2023.pdf">Link</a></p>

<p dir="auto"><a href="/safeFBDC-TU-Darmstadt/sgx4ml-cpp/blob/main/BTW2023_Paper.pdf">The paper is available here.</a></p>
<p dir="auto"><a href="/safeFBDC-TU-Darmstadt/sgx4ml-cpp/blob/main/BTW2023_Paper.pdf">The paper is available here.</a></p>
