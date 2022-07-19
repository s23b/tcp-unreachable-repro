package gotcp

import (
	"fmt"
	"net"
	"time"

	"github.com/sirupsen/logrus"
)

var log = logrus.New()

func DoDial(host string, port int) error {
	log.Infof("gotcp.DoDial - Called with (%s, %d)", host, port)

	conn, err := net.DialTimeout("tcp", fmt.Sprintf("%s:%d", host, port), 10*time.Second)
	log.Infof("gotcp.DoDial - Error is %v", err)

	if err == nil {
		log.Info("gotcp.DoDial - Closing connection")
		err = conn.Close()
		log.Info("gotcp.DoDial - Connection closed")
	}

	return err
}
