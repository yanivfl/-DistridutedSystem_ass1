# Distributed System - Assignment 1

#### Yaniv Fleischer 203817002
#### Yuval Lahav 205689110

<br/><br/>
## Running Instructions:
1. Create a pair key (AWS) with the name: "YuvalKeyPair.pem" 
2. Open a bucket in your S3
3. Open the user_data.sh file of the asignment and change to bucket name to your bucket's name. 
(line: 10)
4. Create a zip file with the name: "jarsAss1.zip", protected by a password "YanivYuval", containing the following:
 * Assignment1.jar
 * ejml-0.23.jar
 * jollyday-0.4.7.jar
 * stanford-corenlp-3.3.0-models.jar
 * stanford-corenlp-3.3.0.jar
 * user_data.sh
5. Upload the zip to S3. 
6. Run the java command from the assignment:
"java -jar Assignment1.jar inputFileName1… inputFileNameN outputFileName1… outputFileNameN n (terminate)"
 

<br/><br/>
## Security
### credentials
* __For a client__ - credentials are stored at the file ~/.aws/credentials (Amazon default).
* __For non-client__ - credentials are provided via predefined roles.
    * Manager - Role is defined with EC2, SQS and S3 permissions.
    * Workers - Role is defined with SQS permissions.
    
### Assignment jars & user data
Contained in a zip, protected by password.

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
* We always have more workers than exactly needed in case one of the workers die.
* We delete messages from the SQS queue only after the worker has fully done the task. 
* We used visibillity timeout. That means that if 1 node stall, another can take the message (after a certain amout of time) and perform the task.
* Exceptions (in LocalApp, Manager, Worker) - we handle it. If it's an interrupt exception then we leave gracefully, if not we continue to work.

<br/><br/>
## Run process image
Added in another file named: run_procces.png

<br/><br/>
## Statistics
__Test:__ 
* Client 1 - contains 3 input files, n=100.
* Client 2 - contains 2 input files, n=100, terminate.
(total - all 5 input files from the assignment)

__AMI:__ ami-b66ed3de

__Manager instance type:__ T2Small

__Workers instance type:__ T2Large

__Time for the program to finish:__ 16 minutes





