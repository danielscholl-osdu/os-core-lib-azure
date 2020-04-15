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
