/**
 *  See the NOTICE.txt file distributed with this work for
 *  information regarding copyright ownership.
 *
 *  The authors license this file to you under the
 *  Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License.  You may
 *  obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mongodb.mmm;

import org.mongodb.mmm.processor.MessageLogger;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class Monitor {

    protected boolean _running=true;

    public Monitor() {
    }

    public void listen(int port) throws IOException {

        ServerSocketChannel listenSock = ServerSocketChannel.open();

        try {
            listenSock.socket().bind(new InetSocketAddress(port));

            System.out.println("MMM : Listening on " + port);

            while(_running) {

                SocketChannel s = listenSock.accept();

                s.configureBlocking(true);
                
                System.out.println("MMM: Received connection : " + s);
                MongoProxy mp = new MongoProxy(s, new MessageLogger(false, System.out));

                Thread t = new Thread(mp, "Proxy:" + s.socket().getRemoteSocketAddress());

                t.start();
            }
        }
        finally {
            listenSock.close();
        }
    }

    public static void main(String[] args) throws Exception {          

        Monitor m = new Monitor();

        m.listen(27017);
    }
}
