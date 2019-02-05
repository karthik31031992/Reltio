
#Data Load Utility

## Description
The Data Loader is a tool for bulk loading entities, relations, and interactions into a reltio tenant in JSON form. The tool allows for the process tracking with email alerts and output redirection to a csv file when records fail to load. The tool is commonly used in sequence following the JSON Generator tool to prepare data for loading.

##Change Log


```

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

