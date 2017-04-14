In some architectures it is nice to have a Singleton.   But what if we have a distributed network and we want the singleton pattern?     

To learn more about this pattern please visit this link: https://en.wikipedia.org/wiki/Singleton_pattern

Geode enables this pattern using a very simple distributed locking technique in minimal amounts of code.

```
//Create the lock.
DistributedLockService lockService = DistributedLockService.create("GridSingleton_Service", cache.getDistributedSystem());
//Attempt to grab the lock.   
// The -1 parameters mean wait forever for the lock and
// hold the lock forever
lockService.lock("GridSingleton_lock", -1, -1);
// At this point Geode would only allow one thread to progress.
```
Just wrap the above code in a Thread so it releases any application threads and allows the Thread to be waiting on the lock.

Example:
```
Runnable runnable = () -> {
    Cache cache = CacheFactory.getAnyInstance();
    DistributedLockService lockService = DistributedLockService.create("GridSingleton_Service", cache.getDistributedSystem());
    lockService.lock("GridSingleton_lock", -1, -1);
    gridSingleton = true;
};

Thread thread = new Thread(runnable, "GridSingleton_Thread");
thread.setDaemon(true);
thread.start();
```
## Project Breakdown

```
├── LICENSE.txt - Apache
├── README.md - This document
├── build.gradle - I used Gradle for this project
├── data - This is the directory where all of the Geode writes to.
├── gradle - Directory with Gradle wrapper libraries
├── gradlew - Gradle build script
├── gradlew.bat - Gradle build script for windows
├── scripts
│   ├── clearGeodeData - Cleans out the data directory
│   ├── doDemo - Makes it easy to start and stop a server
│   ├── shutdownGeode - Shuts down Geode cleanly
│   └── startGeode - Starts up Geode with 2 locators and 3 cache servers.
├── settings.gradle - Gradle settings
└── src
    ├── main
    │   ├── java
    │   │   └── org
    │   │       └── apache
    │   │           └── geode
    │   │               └── demo
    │   │                   ├── GridSingleton.java - A Really basic singleton.
    │   │                   └── TestWhoIsGridSingleton.java - A function to test who is the grid singleton.
    │   └── resources
    │       └── server.xml - I used the Geode cache XML method for defining Geode deployment.
    └── test
        ├── java
        └── resources
```


## Example Run Through of the demo

1) Validate that we have an environement that works.   By just seeing if GFSH works we can validate that Java has been setup and working.
```
voltron:geode-grid-singleton cblack$ gfsh
    _________________________     __
   / _____/ ______/ ______/ /____/ /
  / /  __/ /___  /_____  / _____  /
 / /__/ / ____/  _____/ / /    / /
/______/_/      /______/_/    /_/    1.1.0

Monitor and Manage Apache Geode
gfsh>exit
Exiting...
```
2) After checking out the code from the repo we need to build the function and Singleton code.
```
voltron:geode-grid-singleton cblack$ ./gradlew clean installDist
:clean
:compileJava
:processResources
:classes
:jar
:startScripts
:installDist

BUILD SUCCESSFUL

Total time: 4.798 secs

This build could be faster, please consider using the Gradle Daemon: http://gradle.org/docs/2.5/userguide/gradle_daemon.html
```
3) Lets run the system.   In this next step we clear out any data that is there then start up a distributed system with 2 locators and 3 servers.   Clearing out the data is optional - I put it there just incase someone wants to know how to clear out all of the Geode data.
```
voltron:geode-grid-singleton cblack$ cd scripts/
voltron:scripts cblack$ ./clearGeodeData
voltron:scripts cblack$ ./startGeode

[...deleted start up output...]

(1) Executing - connect --locator=localhost[10334],localhost[10335]

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.83, port=1099] ..
Successfully connected to: [host=192.168.1.83, port=1099]


(2) Executing - list members

      Name       | Id
---------------- | --------------------------------------------------------
locator4_voltron | 192.168.1.83(locator4_voltron:5422:locator)<ec><v0>:1024
locator5_voltron | 192.168.1.83(locator5_voltron:5427:locator)<ec><v1>:1025
server2_voltron  | 192.168.1.83(server2_voltron:5469)<v2>:1026
server3_voltron  | 192.168.1.83(server3_voltron:5470)<v2>:1027
server1_voltron  | 192.168.1.83(server1_voltron:5471)<v2>:1028


(3) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server2_voltron:5469)<v2>:1026 | false

192.168.1.83(server3_voltron:5470)<v2>:1027 | false

192.168.1.83(server1_voltron:5471)<v2>:1028 | true
```
4) This next step walks through all of the servers and kills them one by one.   We should notice how the Grid Singleton moves from one server to another.  Before we start the demo script notice that the grid singleton is running on server 1.
```
voltron:scripts cblack$ ./doDemo
Killing server 1

(1) Executing - connect --locator=localhost[10334],localhost[10335]

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.83, port=1099] ..
Successfully connected to: [host=192.168.1.83, port=1099]


(2) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server2_voltron:5469)<v2>:1026 | false

192.168.1.83(server3_voltron:5470)<v2>:1027 | false

192.168.1.83(server1_voltron:5471)<v2>:1028 | true



(3) Executing - stop server --name=server1_voltron

...

(4) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server2_voltron:5469)<v2>:1026 | true

192.168.1.83(server3_voltron:5470)<v2>:1027 | false
```
5) Here we can see after killing server 1 the next system to become the grid singleton is server 2.
```
Starting back up server 1
Killing server 2

(1) Executing - connect --locator=localhost[10334],localhost[10335]

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.83, port=1099] ..
Successfully connected to: [host=192.168.1.83, port=1099]


(2) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server2_voltron:5469)<v2>:1026 | true

192.168.1.83(server3_voltron:5470)<v2>:1027 | false

192.168.1.83(server1_voltron:5515)<v4>:1028 | false



(3) Executing - stop server --name=server2_voltron

...

(4) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server3_voltron:5470)<v2>:1027 | true

192.168.1.83(server1_voltron:5515)<v4>:1028 | false
```
6) After killing server 2 server 3 becomes the grid singleton.   At this point we might want to jump to conclusions about fairness policies but don't.
```

Starting back up server 2
Killing server 3

(1) Executing - connect --locator=localhost[10334],localhost[10335]

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.83, port=1099] ..
Successfully connected to: [host=192.168.1.83, port=1099]


(2) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server2_voltron:5541)<v6>:1026 | false

192.168.1.83(server3_voltron:5470)<v2>:1027 | true

192.168.1.83(server1_voltron:5515)<v4>:1028 | false



(3) Executing - stop server --name=server3_voltron

...

(4) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server2_voltron:5541)<v6>:1026 | true

192.168.1.83(server1_voltron:5515)<v4>:1028 | false
```
7) After killing server 3 server 2 took over as the grid singleton.  I was extremely lucky to get a demo that demonstrated that the locking policy.
```
Starting back up server 3

(1) Executing - connect --locator=localhost[10334],localhost[10335]

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.83, port=1099] ..
Successfully connected to: [host=192.168.1.83, port=1099]


(2) Executing - execute function --id=TestWhoIsGridSingleton

Execution summary

              Member ID/Name                | Function Execution Result
------------------------------------------- | -------------------------
192.168.1.83(server2_voltron:5541)<v6>:1026 | true

192.168.1.83(server3_voltron:5567)<v8>:1027 | false

192.168.1.83(server1_voltron:5515)<v4>:1028 | false
```
8) This is the end of the test.   Feel free to modify the demo and play around to with this really simple usecase and see how your architecture can take advanatage of this pattern.

9) To finish up lets shutdown the grid and clean up.
```
voltron:scripts cblack$ ./shutdownGeode
    _________________________     __
   / _____/ ______/ ______/ /____/ /
  / /  __/ /___  /_____  / _____  /
 / /__/ / ____/  _____/ / /    / /  
/______/_/      /______/_/    /_/    1.1.0

Monitor and Manage Apache Geode
gfsh>  connect --locator=localhost[10334]
Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.83, port=1099] ..
Successfully connected to: [host=192.168.1.83, port=1099]

gfsh>  shutdown --include-locators=true --time-out=15
As a lot of data in memory will be lost, including possibly events in queues, do you really want to shutdown the entire distributed system? (Y/n): Y
Shutdown is triggered

gfsh>
Exiting...
voltron:scripts cblack$ ./clearGeodeData
```
