# DistridutedSystem_ass1

# Yaniv Fleischer 203817002
# Yuval Lahav 205689110

TODO: (feel free to change this)


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
  
