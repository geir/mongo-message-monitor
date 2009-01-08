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

import org.mongodb.driver.impl.msg.DBMessage;
import org.mongodb.driver.MongoDBException;
import org.mongodb.driver.MongoDBIOException;
import org.mongodb.mmm.processor.MessageProcessor;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *  Handles the connection to the server, sending messages and recieving responses
 *  to forward to client.
 */
public class DBConnection {

    protected SocketChannel _dbSocketChannel;
    protected final SocketChannel _clientSocketChannel;
    protected final MessageProcessor _processor;
    protected ServerReader _sr;
    protected Thread _serverThread;
    protected final MongoProxy _myProxy;

    public DBConnection(MongoProxy proxy, SocketChannel s, MessageProcessor p) throws IOException {
        _myProxy = proxy;
        _clientSocketChannel = s;
        _processor = p;

        _dbSocketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 27020));

        _sr = new ServerReader(_clientSocketChannel, _dbSocketChannel);

        _serverThread = new Thread(_sr, "DBServerReader:" + _clientSocketChannel.socket().getRemoteSocketAddress());
        _serverThread.start();
    }

    public void shutdown() {
        _sr.stopRunning();

        try {
            _dbSocketChannel.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void writeToServer(ByteBuffer buf) throws IOException{
        _dbSocketChannel.write(buf);
    }

    class ServerReader implements Runnable {

        final SocketChannel _client;
        final SocketChannel _db;
        boolean _running = true;

        ServerReader(SocketChannel client, SocketChannel db) {
            _client = client;
            _db = db;
        }

        public void stopRunning() {
            _running = false;
            try {
                _db.close();
            }
            catch(IOException ioe) {
                //
            }
        }
        
        public void run() {

            ByteBuffer buf;

            /*
             * full messages from server can be 1MB+, so and since we don't "nibble" like the java driver does...
             */
            buf = ByteBuffer.allocateDirect(1024 * 1024 * 2);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            try {
                while(_running) {

                    DBMessage msg = DBMessage.readFromChannel(_db, buf);
                    _myProxy.processMessage(MessageProcessor.Direction.FromServer, msg);

                    buf.flip();
                    _client.write(buf);
                }
            }
            catch(MongoDBIOException e){
                _myProxy.log("Caught exception in server read loop", e);
            }
            catch(MongoDBException e){
                _myProxy.log("Caught exception in server read loop", e);
            }
            catch(IOException e){
                System.err.println("Caught exception in server read loop");
            }

            _running = false;

            _myProxy.log("End of server thread");
        }
    }
}
