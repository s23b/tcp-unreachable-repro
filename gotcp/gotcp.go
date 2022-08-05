package gotcp

import (
	"errors"
	"fmt"
	"net"
	"strconv"
	"strings"
	"time"

	"github.com/sirupsen/logrus"
	"golang.org/x/sys/unix"
)

/*
#include <strings.h>
#include <arpa/inet.h>
#include <unistd.h>

int dial(const char* host, int port) {

    int sockfd;
    struct sockaddr_in servaddr;

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd == -1) {
		return -1;
    }

    bzero(&servaddr, sizeof(servaddr));

    // assign IP, PORT
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr(host);
    servaddr.sin_port = htons(port);

    int conn = connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr));
    if (conn != 0) {
		return conn;
    }

    close(sockfd);
	return 0;
}
*/
import "C"

var log = logrus.New()

func NetDial(host string, port int) error {
	log.Infof("gotcp.GoDial - Called with (%s, %d)", host, port)

	conn, err := net.DialTimeout("tcp", fmt.Sprintf("%s:%d", host, port), 10*time.Second)
	log.Infof("gotcp.GoDial - Error is %v", err)

	if err == nil {
		log.Info("gotcp.GoDial - Closing connection")
		err = conn.Close()
		log.Info("gotcp.GoDial - Connection closed")
	}

	return err
}

func CGoDial(host string, port int) error {
	log.Infof("gotcp.CGoDial - Called with (%s, %d)", host, port)
	res, err := C.dial(C.CString(host), C.int(port))
	log.Infof("gotcp.CGoDial - Error: %v Result: %d", err, res)

	if err == nil {
		if res != 0 {
			return errors.New("Connection failed")
		}
	}

	return err
}

func SyscallDial(host string, port int) error {
	log.Infof("gotcp.SyscallDial - Called with (%s, %d)", host, port)
	hostBytes := strings.Split(host, ".")

	if len(hostBytes) != 4 {
		return errors.New("malformed ip")
	}

	var ip [4]byte

	for i, b := range hostBytes {
		val, err := strconv.ParseUint(b, 10, 8)

		if err != nil {
			return errors.New("unsupported ip")
		}

		ip[i] = byte(val)
	}

	sockfd, err := unix.Socket(unix.AF_INET, unix.SOCK_STREAM, 0)
	log.Infof("gotcp.SyscallDial - Socket Error: %v Handle: %d", err, sockfd)

	if err != nil {
		return err
	}

	err = unix.Connect(sockfd, &unix.SockaddrInet4{Addr: ip, Port: port})
	log.Infof("gotcp.SyscallDial - Connect Error: %v", err)

	if err != nil {
		return err
	}

	log.Info("gotcp.SyscallDial - Closing")
	return unix.Close(sockfd)
}
