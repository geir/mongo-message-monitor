package org.mongodb.mmm;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;

/**
 *
 */
public class Monitor {

    protected ServerSocket _listenSock;
    protected boolean _running=true;

    public Monitor() {
    }

    public void listen(int port) throws IOException {

        try {
            _listenSock = new ServerSocket();

            _listenSock.bind(new InetSocketAddress(port));

            System.out.println("Listening on " + port);

            while(_running) {

                Socket s = _listenSock.accept();

                System.out.println("Received connection : " + s);
                MongoProxy mp = new MongoProxy(s);

                Thread t = new Thread(mp);

                t.start();
            }
        }
        finally {
            _listenSock.close();
        }
    }

    public static void main(String[] args) throws Exception {          

        Monitor m = new Monitor();

        m.listen(27017);
    }
}
