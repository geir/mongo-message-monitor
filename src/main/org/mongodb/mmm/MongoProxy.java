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
import org.mongodb.mmm.processor.NOOPProcessor;

import java.net.Socket;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  Handles a client connection
 */
public class MongoProxy implements Runnable {

    protected final static AtomicInteger id = new AtomicInteger();

    protected final AtomicInteger count = new AtomicInteger();

    protected final Socket _clientSocket;
    protected Socket _dbSocket;
    protected boolean _running = true;
    protected final int _myID = id.getAndIncrement();

    protected DBConnection _dbConnection;
    protected Thread _serverThread;

    protected MessageProcessor _processor = new NOOPProcessor();

    public MongoProxy(Socket client, MessageProcessor mp) {
        _clientSocket = client;
        _processor = mp;
    }

    public void run() {

        log("Connection from [" + _clientSocket.getRemoteSocketAddress() + "]");

        try {
            createServerConnection();
        } catch (IOException e) {
            log("Error creating connection to server.  Shutting down connection to client", e);

            shutdownClient();
            return;
        }

        try {
            processMessages();
        }
        catch(MongoDBException me ) {
            log("Error processing messages  Shutting down.", me);
        }
        catch(MongoDBIOException me ) {
            log("Error processing messages  Shutting down.", me);
        }

        shutdownServer();
        shutdownClient();

        log("MessageProxy thread ending.");
    }

    protected void shutdownServer() {
        _dbConnection.shutdown();
    }

    protected void shutdownClient() {
        try {
            _clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void processMessages() throws MongoDBException {

        try {
            BufferedInputStream is = new BufferedInputStream(_clientSocket.getInputStream());

            while(_running) {

                DBMessage msg = DBMessage.readFromStream(is);

                processMessage(MessageProcessor.Direction.FromClient, msg);

                _dbConnection.writeToServer(msg);
            }
        }
        catch(IOException e) {
            throw new MongoDBIOException("Error processing messages", e);
        }
    }

    public void processMessage(MessageProcessor.Direction dir, DBMessage msg) {
        _processor.process(_myID, count.getAndIncrement(), dir, msg);
    }

    private void createServerConnection() throws IOException {
        _dbConnection = new DBConnection(this, _clientSocket, _processor);
    }

    private void log(String s, Throwable e) {
        StringBuffer sb = new StringBuffer();
        sb.append(_myID);
        sb.append(":").append(s);

        if (e != null) {
            sb.append(" : ");
            sb.append(e);
        }

        System.out.println(sb);
    }

    private void log(String msg) {
        log(msg, null);
    }

}
