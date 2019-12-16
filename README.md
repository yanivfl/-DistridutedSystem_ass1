# Distributed System - Assignment 1

#### Yaniv Fleischer 203817002
#### Yuval Lahav 205689110

<br/><br/>
## Running Instructions:


__Instructions:__ run a client using the following shell command-

$> java -jar Assignment1.jar inputFileName1… inputFileNameN outputFileName1… outputFileNameN n (terminate-optional)

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
* Manage Cilents - Manage the different messages that come from clients and send messages to workers.
* Manage Workers - Manage the messages that come from workers and send messages to clients.

In order lo receive scalability we created threads for each aspect. 
* For every file that is received by a client, we created one more Manage-Clients thread.
* For every worker that is initiated, we created one more Manage-Workers thread.

We initialize the manager with 3 threads for each aspect.
when a file is done we re-evaluate the threads and terminate threads to reach the thread per file we want.


___Limitations on threads:___ we have only one manager, so we limited the above threads to a maximum of 20 threads.


### Workers
For each third file sent to the manager, we initiate an extra Worker.

__Explanation:__
As described in the assignment, each client demends a certain number of Workers, 
so theoretically there are always __n__ Workers, when __n__ is the maximum number of Workers needed. 
This means the same workers serve the same clients and therefore, their amount must grow with the amount of files the Manager needs to process. 

<br/><br/>
## Persistence
we always have more workers than exactly needed in case one of the workers die. in addition we delete messages only after we have fully done the task the message was designated for.
What if a node stalls for a while? we have more than one worker/thread for every task so if 1 node stalls, another can take the message (visibility timeout) and perform the task.
if there is an exception in any of the nodes (Local APp, Manager, Worker) we handle it, if it's an interrupt exception then we leave gracefully. if not we continue to work.

an addition failure due to broken communication or failed nodes is missed information. 
we handled missed information by deleting messages after task has been done.

<br/><br/>
## Run process
Added in another file named: run_procces.png




