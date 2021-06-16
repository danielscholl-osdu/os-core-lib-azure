Copyright 2017-2019 Schlumberger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Introduction

This repository houses code that is used across the Microsoft Azure hosted OSDU and OpenDES platforms. The intention of this repository is to minimize code duplication for common scenarios such as dependency configuration for services in Azure (KeyVault, Cosmos, Storage and others).

# Pre-requisites

You need

1. Maven 3.6.x
2. Java 1.8

# Local Usage

```bash
# Compile the codebase
$ mvn compile
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
...

# Unit test the codebase
$ mvn test
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
...
```

# Getting started guide

##Installation

You need to connect to our feed on [Azure DevOps](https://slb-swt.visualstudio.com/data-at-rest/ProdOps%20-%20Production%20Engineer/_packaging?feed=slb-dps&_a=feed) and add the artifact `core-lib-azure` as a dependency to your Maven or Gradle build.

**Example**

```xml
<project>
    <properties>
        <azure.corelib.version>0.0.1</azure.corelib.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.opengroup.osdu</groupId>
            <artifactId>core-lib-azure</artifactId>
            <version>${azure.corelib.version}</version>
        </dependency>
    </dependencies>
</project>
```

## Changes to support newly Added transaction logger and slf4jlogger
The consumer service might run into multiple logger bindings error on start up
which can be fixed by excluding some of the logger dependencies.

Note: Below are reference PRs for exclusion and might change from service to service

Refer this [MR](https://community.opengroup.org/osdu/platform/security-and-compliance/entitlements-azure/-/merge_requests/13) as reference on how to exclude dependencies along with how to enable the
Enabled transaction logger and slf4jlogger

## Environment variables to be added in application.properties to consume the TenantFactoryImpl
| name | value | description |
| ---  | ---   | ---         |
| `tenantInfo.container.name` | `TenantInfo` | cosmos container name |
| `azure.cosmosdb.database` | ex `dev-osdu-r2-db` | cosmos database name |
| `tenantFactoryImpl.required` | ex `true` | Set this property to true in order to consume TenantFactoryImpl class from core-lib-azure |

## Settings to be added in application.properties to consume the BlobStore
| name | value | description |
| ---  | ---   | ---         |
| `azure.blobStore.required` | `true` | - |
| `azure.storage.account-name` | ex `testStorage` | storage account name |

# Default retry and timeout values for service-to-service communication
| name | default value |
| ---  | ---   | 
| `maxRetry` | `3` |
| `connectTimeout` | `60000` |
| `requestTimeout` | `60000` |
| `socketTimeout` | `60000` |
