# DistridutedSystem_ass1

# Yaniv Fleischer 203817002
# Yuval Lahav 205689110

TODO: (feel free to change this)

1. sentiment analysis:
  a. Local application runs sentiment analysis on JSON FILE
  b. create HTML file
2. connect to S3
  a. uplaod JRE files and then erase them
  b. uplaod compressed JRE files with passwords, open them and then erase them
  c. run 1 with the addition of uploading JSON FILE to S3
3. connect to SQS
  a. write simple manager (maybe we will run it with a thread)
  b. create SQS for USer and SQS for manager
  c. run 1 with addition of simple manager
4. connect worker
  a. write manager code (maybe we will run it with a thread)
  b. write worker code (maybe we will run it with a thread)
  c. connect SQS to worker
  d. run 1 with worker and manager
5. run everything like it should on aws
6. edit README


free time TODOS:
  1.  delete Message Constructors (with String msg) from all msgs.
  2. try and catch to all functions that have Json functions
  3. with finally erase resources
