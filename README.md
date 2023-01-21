# QFilter: Integration of Access Control with Aggregation Query Processing over Outsourced Data #

QFilter is a fine-grained access control enforcement mechanism tightly integrated with query processing to evaluate aggregation SQL queries (i.e., "count", "sum", and "avg" having single-dimensional, conjunctive, or disjunctive equality query conditions) over secret-shared data without revealing any information about data, associated access policies, user queries and query results. It employs an Attribute-Based Access Control (ABAC) model to specify the set of fine-grained access control policies at the data owner by adding new attributes to relation to be outsourced in such a way that each value of the new attribute represents a specific ABAC policy. It obliviously rewrites the submitted aggregation SQL query by adding some query conditions in the where clause of the query to check access authorizations and filter out unauthorized data items during query processing at the server side. It does not require any communication between any two servers before/during/after query rewriting and query execuation at the server side. QFilter has a set of string matching based operators to obliviously compute query results. It is a privacy-preserving communication-efficient access control enforcement mechanism which can supprot two levels of policy attachemts: Tuple Level Policy Attachemnt and Attribute Level Policy Attachment.

## Configuration &amp; System Requirements ##
The memory usage of this prototype is mainly determined by the number of servers and the upload batch size.
For a manual start of the prototype, these variables - as well as multiple variables concerning the simulation setting - can be configured in the manual.Configuration file.
Execution with the default values (numServers=200 and batchSize=100) does not require more than 1GB of main memory.
To resolve the project dependencies, you will need to have gradle installed.
Overall, you can configure the following variables:

> - **numServers**: the number of servers to use (there is always a minimum threshold of servers to use for correctness; we need at least 2*c*l+2 for queries with at most c query conditions, and a maximum of l digits for any number in the uploaded table [l=8 for the first eight attributes of the *LINEITEM* table in TPC-H benchmark])
> - **attachPolicy**: true if you want to attach a policy to the data, false otherwise
> - **batchSize**: the batchSize to use when uploading the secret share tables to the servers (= number of rows to use for each batch upload)
> - **rowLimit**: the maximum number of rows to upload to the servers
> - **columnPolicy**: whether to attach an attribute level policy (=true) or a tuple level policy (=false)
> - **NUM_GROUPS**: the number of user groups to use
> - **accessibilityPercentage**: the percentage of tuples / column values accessible the first user group (data users are in the first user group by default)

## Manual Start ##
For a manual start of the prototype, follow this order:
```
manual.RegistryHost.main()
server.Server.main()
owner.DataOwner.main()
user.DataUser.main()
```
For a correct startup, wait for a confirming console output of after calling <code>server.Server.main()</code> ("${numServers} servers are online!") and <code>owner.DataOwner.main()</code> ("Enter a boolean expression to express your policy logic:").
After calling <code>user.DataUser.main()</code>, you can input attributes one by one or simple queries for the *LINEITEM* table (this prototype only supports count, sum and avg aggregation, query conditions with the same conditional type (AND/OR), and only simple query predicates like "attr=const").
We only use the following attributes of the *LINEITEM* table: "orderkey", "partkey", "suppkey", "linenumber", "quantity", "extendedprice", "discount" and "tax".

## ABAC Implementation ##
The Attribute Based Access Control (ABAC) model is mainly enforced by means of the classes included in the packages and classes owner.authorizations, owner.DataOwner, user.DataUser, user.CredentialExchanger.

It can be completely controlled via the CLIs with:
* Input of attributes via Data User 
* Input of access control policy logic (including defined attributes from Data User) via Data Owner (supported policies: multiple AND, OR Operations + Structure (A AND B) OR C, (A OR B) AND C).

Important to know is, that first, users' attributes must be defined by data users because the policy logic can be only applied by the data owner to the existing users' attributes. The attributes coming from the Data User are in the background stored in HashMaps. This means that you have to use the HashMap keys for defining the boolean expression at the data owner side. These keys are numbers and therefore, all boolean expressions are checked for numbers. The logical connectors need to be defined in lower case letters. Brackets are allowed and applied in the expression logic. 
```
__Please note:__ When first starting a Data User, an error message is displayed since no credentials are 
registered for this user. The error message is sufficiently caught - the application will continue to run.
Credentials are created once attributes are defined for the user (since without them, no credentials are required).
