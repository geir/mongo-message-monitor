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
