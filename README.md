# Minimal reproduction of gomobile network issue

A repo reproducing the bug whereupon TCP sockets cannot be opened from Go when the android app is bound to a wifi via NetworkRequest.

Only reproducible on Android 10+ (API level 29+).

Short structure description:

- `gotcp` is a minimal module that only tries opening a TCP socket via `net.Dial`.
- `app` holds a minimal Android app that uses that module via gomobile and incorporates the wifi connect logic to showcase the issue.

## Start example

1. Bind the android lib

    ```sh
    cd gotcp
    go mod download
    gomobile bind -target=android -o gotcp.aar
    ```

1. Change the `SSID` and `WIFI_PASS` variables in `MainActivity.java` to a wifi you have access to, and `HOST` and `PORT` to the address of a listening socket in that network.

    ```java
    public final static String SSID = "<Wifi SSID>";
    public final static String WIFI_PASS = "<Wifi Pass>";
    public final static String HOST = "192.168.0.1";
    public final static int PORT = 80;
    ```

1. Start android app

    From Android Studio or install it via gradle, then start it manually.

    ```sh
    ./gradlew installDebug
    ```

## Test cases

There are 5 methods of dialing implemented in this test, each having a button on the UI:
1. *Java dial* - Uses `java.net.Socket` successfully.
1. *C dial* - Uses C Berkeley socket through Android NDK successfully.
1. *Go net dial* - Main test case. Uses Go's `net.DialTimeout()`. Fails when connected through the NetworkRequest API.
1. *Go cgo dial* - Uses C Berkeley socket through cgo successfully.
1. *Go syscall dial* - Uses Berkeley socket through Go system calls using `golang.org/x/sys/unix`. Fails when connected through the NetworkRequest API, at the `unix.Connect()` call.

To perform the main test case do the following steps:
1. Make sure that cellular data is disabled.
1. Press `Go net dial` when connected to wifi via system settings and observe the logs.

    Should be as follows after the 10s timeout

    ```text
    E/GoLog: time="2022-05-04T07:47:19Z" level=info msg="gotcp.GoDial - Called with (192.168.0.1, 80)"
    E/GoLog: time="2022-05-04T07:47:26Z" level=info msg="gotcp.GoDial - Error is dial tcp 192.168.0.1:80: connect: connection timed out"
    ```

1. Now press `Connect to wifi` and bind the app to the wifi programatically
1. Press `Go net dial` again and check the logs.

    Should be as follows, immediately

    ```text
    E/GoLog: time="2022-05-04T07:49:11Z" level=info msg="gotcp.GoDial - Called with (192.168.0.1, 80)"
    E/GoLog: time="2022-05-04T07:49:11Z" level=info msg="gotcp.GoDial - Error is dial tcp 192.168.0.1:80: connect: network is unreachable"
    ```
