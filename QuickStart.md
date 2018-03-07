# Quick Start 
The purpose of the quick start guide it to get you up and running with the tool as quickly as possible. For a more detailed guide on using the utility please visit our [help portal](https://help.reltio.com/index.html#integrations/dataload.html)
##Building
Use this section to detail how to build your tool and the location of the main method.

The main method of the application is at the following path:
**dataload-processor/src/main/java/com/reltio/cst/dataload/impl/LoadJsonToTentant.java **

##Dependencies 

1. gson-2.2.4
2. reltio-cst-core-1.3


##Parameters File Example
The file take in the path to a parameter file as an argument. Below is an example with the options available. 
```
RECORDS_PER_POST=<Total Number of Records Per POST. Recommended value: 50>
THREAD_COUNT=<Number of threads to run the dataload. Recommended value: 10>
JSON_FILE_PATH=<Full path of the input JSON File>
FAILED_RECORD_FILE_PATH=<Failed Records Output File Path>
JSON_FILE_TYPE=<PIPE_ARRAY/ARRAY/OBJECT> (PIPE_ARRAY=Current File format, ARRAY=Without pipe only JSON in array format,OBJECT=Without pipe only JSON in object format)
DATALOAD_SERVER_HOST= <Host name of the dataload server. Ex: dev-dataload.reltio.com>
TENANT_ID=<Id of the tenant.. Ex: BdfZ3Fx9Hbn8Pcg>
DATALOAD_TYPE=<Type of dataload. Possible Values: Entities/Relations>
TYPE_OF_DATA=<Entity/Relationship Type of the data. Ex: HCO>
USERNAME=<Reltio Username>
PASSWORD=<Reltio Account password>
MAX_QUEUE_SIZE=<Maximum Queue Size. Recommended Value: 300000>
AUTH_URL=<Complete Auth Path URL: Ex: https://auth.reltio.com/oauth/token>
USER_COMMENTS=<Details of the dataload. Can be used to help anyone to understand the purpose of the dataload>
IS_PARTIAL_OVERRIDE=TRUE/FALSE

```
##Executing
Command to start the utility.
```
#!plaintext

java -jar reltio-dataload-processor-2.0.2.jar propertiesFile.txt > $logfilepath$


```