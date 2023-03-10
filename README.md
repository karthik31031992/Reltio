
#Data Load Utility

## Description
The Data Loader is a tool for bulk loading entities, relations, and interactions into a reltio tenant in JSON form. The tool allows for the process tracking with email alerts and output redirection to a csv file when records fail to load. The tool is commonly used in sequence following the JSON Generator tool to prepare data for loading.

##Change Log


```
v2.7.0
Last Updated Date  : 20th Feb 2022
LUB                : Aditi Verma
Version            : 2.7.0
Description        : https://reltio.jira.com/browse/ROCS-125
                     Update Reltio CST Core Jar to version 1.5.1.

v2.6.9
Last Updated Date  : 5th Jan 2022
LUB                : John Sargunam
Version            : 2.6.9
Description        : Updated log4J version to 2.17.1

v2.6.8
Last Updated Date  : 21st Dec 2021
LUB                : John Sargunam
Version            : 2.6.8
Description        : Updated log4J version to 2.17.0

v2.6.7
Last Updated Date  : 15th Dec 2021
LUB                : Christopher Jezorek
Version            : 2.6.7
Description        : Updated log4J version to 2.16.0	

v2.6.4
Last Updated Date  : 2nd Dec 2021
LUB                : Alexey Sidelnikov
Version            : 2.6.4
Description        : https://reltio.jira.com/browse/RP-94776
                     Support 429 error in data-loader utility.			 

v2.6.4
Last Updated Date  : 19th Nov 2019
LUB                : Vignesh Chandran
Version            : 2.6.4
Description        : https://reltio.jira.com/browse/CUST-3115
                     Bug Fix in Process Tracker Json.


v2.6.3
Last Updated Date  : 23nd September 2019
LUB                : shivaputrappa Patil
Version            : 2.6.3
Description        : https://reltio.jira.com/browse/ROCS-77, (Client credentials implementation)


v2.6.2
Last Updated Date  : 26nd August 2019
LUB                : Sanjay
Version            : 2.6.2
Description        :  https://reltio.jira.com/browse/CUST-3095
					  https://reltio.jira.com/browse/ROCS-70
					  

v2.6
Last Updated Date  : 26nd June 2019
LUB                : Shivaputrappa Patil
Version            : 2.6
Description        :  https://reltio.jira.com/browse/ROCS-31
					  https://reltio.jira.com/browse/ROCS-18
                      https://reltio.jira.com/browse/ROCS-56

v2.5.9
Last Updated Date  : 2nd Apr 2019
LUB                : Vignesh Chandran
Version            : 2.5.9
Description        : https://reltio.jira.com/browse/ROCS-27 MAIL_SMTP_HOST is now made as a property instead of hard-coding and removed MAIL_TRANSPORT_PROTOCOL removed from property file.

v2.5.8
Last Updated Date  : 29th Mar 2019
LUB                : Vignesh Chandran
Version            : 2.5.8
Description        : ROCS-3 now the password gets encrypted after the inital run.

v2.5.7
Last Updated Date  : 28th Feb 2019
LUB                : Vignesh Chandran
Version            : 2.5.7
Description        : Fix as per CUST-3065, Change in behaviour of checking the status instead of queue size it would be status.

v2.5.6
Last Updated Date  : 11th Jan 2019
LUB                : Vignesh Chandran
Version            : 2.5.6
Description        : Update reltio-core-cst to latest version 1.4.4 with the bug fix for CUST-3049


v2.5.5
Last Updated Date  : 21st Nov 2018
LUB                : Vignesh Chandran, Sanjay
Version            : 2.5.5
Description        : Update on the reltio-core-cst v1.4.3
                     Removed all System.out.println statements and introduced log4j framework and details errors are now moved to debug logs. Done changes to make MAX_OBJECTS_TO_UPDATE property as optional
                     Removed unnessary jar references.

v2.5.4
Last Updated Date  : 28/08/2018
LUB                : Vignesh Chandran
Version            : 2.5.4
Description        : Update on the reltio-core-cst v1.4.2
                     Changes in the properties file for ROCS standarization DATALOAD_SERVER_HOST renamed to ENVIRONMENT_URL

v2.5.3
Last Update Date: 24/07/2018
Version: 2.5.3
Description: Update on the reltio-core-cst v1.4.1

 
v2.5.2
Last Update Date: 06/06/2018
Version: 2.5.2
Description: Update on the reltio-core-cst v1.4.0

Last Update Date: 06/30/2017
Version: 1.0.0
Description: Initial version
```
##Contributing 
Please visit our [Contributor Covenant Code of Conduct](https://bitbucket.org/reltio-ondemand/common/src/a8e997d2547bf4df9f69bf3e7f2fcefe28d7e551/CodeOfConduct.md?at=master&fileviewer=file-view-default) to learn more about our contribution guidlines

## Licensing
```
Copyright (c) 2017 Reltio

 

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

 

    http://www.apache.org/licenses/LICENSE-2.0

 

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and limitations under the License.
```

## Quick Start 
To learn about dependencies, building and executing the tool view our [quick start](https://bitbucket.org/reltio-ondemand/util-dataload-processor/src/master/QuickStart.md).

