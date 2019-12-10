# Distributed System - Assignment 1

#### Yaniv Fleischer 203817002
#### Yuval Lahav 205689110

<br/><br/>
## Running Instructions:
___Note:___ the folder you are running the jar from should include the following jars:
* ejml-0.23.jar
* stanford-corenlp-3.3.0.jar
* stanford-corenlp-3.3.0-models.jar
* jollyday-0.4.7.jar

__Instructions:__ run a client using the following shell command-
java -jar Assignment1.jar inputFileName1… inputFileNameN outputFileName1… outputFileNameN n (terminate-optional)

TODO

<br/><br/>
## Security
### credentials
* __For a client__ - credentials are stored at the file ~/.aws/credentials (Amazon default).
* __For non-client__ - credentials are provided via predefined roles.
    * Manager - Role is defined with EC2, SQS and S3 permissions.
    * Workers - Role is defined with SQS permissions.

<br/><br/>
## Scalability
### Manager
We implemented a unique almost thread-per-file/worker scheme:

The manager works in 2 aspects:
* Manage Cilents - Manage the different messages that comes from clients.
* Manage Workers - Manage the messages that comes from workers.

In order lo receive scalability we created threads for each aspect. 
* For every file that is received by a client, we created one more Manage-Clients thread.
* For every worker that is initiated, we created one more Manage-Workers thread.

___Limitations on threads:___ we have only one manager, so we limited the above threads to a maximum of 20 threads.


### Workers
For each third file sent to the manager, we initiate an extra Worker.

__Explanation:__
As described in the assignment, each client demends a certain number of Workers, 
so theoretically there are always __n__ Workers, when __n__ is the maximum no. of Workers needed. 
That means the same workers serves the same clients and therefore their amount must grow with the amount of files the Manager needs to process. 

<br/><br/>
## Persistence
TODO: Yaniv!

<br/><br/>
## Run process
Added in another file named: run_procces.png



***********

2. create DeleteTest that erases all Messages, instances, sqs
3. sub Manager tests
4. termination protocol: 
  a. once recieved, accept messages only from buckets that exist in the clientsInfo.
  b. Main manager thread busy waits on clientsInfo. if clientsInfo.isEmpty() && sqs.M2C.isEmpty() -> shutdown all threads, sqs,                       delete messages sqs shutdown all instances. 
5. Add threadpool executer to Main Manager
6. run everything like it should on aws
8. edit README
9. go over instructions



free time TODOS: handling exceptions
  1.  delete Message Constructors (with String msg) from all sqs.
  2. try and catch to all functions that have Json functions
  3. with finally erase resources
  
  new TODO:
  * think about the security of the user-data file
