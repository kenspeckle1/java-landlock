# Java Binding for Landlock API

Welcome to the Java Binding for the Landlock API project! This project provides a Java interface for the Landlock API of the Linux kernel, allowing Java applications to utilize this powerful feature for sandboxing and enhancing security.

## Overview

Landlock is a security feature in the Linux kernel that enables applications to remove specific access rights in a flexible and secure manner. This project wraps the functionality of the Landlock API, making it accessible from Java applications.


## IMPORTANT NOTE
This library will also call [`PR_SET_NO_NEW_PRIVS`](https://www.man7.org/linux/man-pages//man2/PR_SET_NO_NEW_PRIVS.2const.html) which will remove the ability to gain new privileges (e.g. through SUID binaries).

Also this library follows a whitelist/deny-by-default approach, meaning by default everything will be forbidden that is not allowed.

## Requirements

To build and run this project, you will need the following:
- JDK 22 or higher (due to FFM-Api)
- gcc (GNU Compiler Collection)
- make (Build automation tool)


Please ensure that you have these installed on your system before proceeding with the installation.

### JNI
The current code uses the [Foreign Function & Memory API](https://openjdk.org/jeps/454). But there also exists a JNI Version in the `jni` branch.

## Installation
Clone the repository:

```bash

git clone https://github.com/kenspeckle/landlock-java-binding.git
cd landlock-java-binding
```

Compile the project using maven:
```bash

make
```
This will generate the necessary Java bindings for the Landlock API.

## Usage

To use the Java binding in your project, include the generated JAR file in your Java classpath. You can then import the classes provided in this binding to start utilizing the Landlock functionality.
```xml
<dependency>
    <groupId>org.landlock</groupId>
    <artifactId>java-landlock</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
import com.example.landlock.*;
import org.kenspeckle.landlock.Flag;
import org.kenspeckle.landlock.Landlock;
import org.kenspeckle.landlock.RuleSet;

public void startup() {
	Landlock.loadLandlock(); // needs to be called just once for an application
	RuleSet ruleSet = new RuleSet();
	ruleSet.addNetRule(Flag.LANDLOCK_ACCESS_NET_BIND_TCP, 8080);
	ruleSet.addFsRule("/tmp", Flag.LANDLOCK_ACCESS_FS_READ_DIR, Flag.LANDLOCK_ACCESS_FS_READ_FILE);
	ruleSet.addFsRule("/usr/bin/magick", Flag.LANDLOCK_ACCESS_FS_EXECUTE);
	ruleSet.addFsRule("/usr/bin/magick", Flag.LANDLOCK_ACCESS_FS_EXECUTE);
	
	// This will enforce the ruleset
	ruleSet.restrictSelf();
}

```

Refer to the documentation for specific usage examples and API details.


## Running Tests
Note that the tests need to be executed manually due to landlock restricting the process.
