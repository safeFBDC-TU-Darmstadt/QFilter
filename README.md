# QFilter: Integration of Access Control with Aggregation Query Processing over Outsourced Data #

## Configuration &amp; System Requirements ##
The memory usage of this prototype is mainly determined by the number of servers and the upload batch size.
For a manual start of the prototype, these variables - as well as multiple variables concerning the simulation setting - can be configured in the manual.Configuration file.
Execution with the default values (numServers=200 and batchSize=100) does not require more than 1GB of main memory.
To resolve the project dependencies, you will need to have gradle installed.
Overall, you can configure the following variables:

> - **numServers**: the number of servers to use (there is always a minimum threshold of servers to use for correctness; we need at least 2*c*l+2 for queries with at most c conditions, and a maximum of l digits for any number in the uploaded table [l=8 for the first eight attributes of the *LINTEITEM* table])
> - **attachPolicy**: true if you want to attach a policy to the data, false otherwise
> - **batchSize**: the batchSize to use when uploading the secret share tables to the servers (= number of rows to use for each batch upload)
> - **rowLimit**: the maximum number of rows to upload to the servers
> - **columnPolicy**: whether to attach an attribute based policy (=true) or a tuple based policy (=false)
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
For a correct startup, wait for a confirming console output of after calling <code>server.Server.main()</code> ("${numServers} servers are online!") and <code>owner.DataOwner.main()</code> ("finished uploading tables!").
After calling <code>user.DataUser.main()</code>, you can input simple queries for the *LINEITEM* table (this prototype only supports count, sum and avg aggregation, query conditions with the same conditional type (AND/OR), and only simple query predicates like "attr=const").
We only use the following attributes of the *LINEITEM* table: "orderkey", "partkey", "suppkey", "linenumber", "quantity", "extendedprice", "discount" and "tax".
