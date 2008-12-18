package org.mongodb.mmm;

import org.mongodb.driver.impl.msg.DBMessage;
import org.mongodb.driver.MongoDBException;
import org.mongodb.mmm.processor.MessageProcessor;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 *  Handles the connection to the server, sending messages and recieving responses
 *  to forward to client.
 */
public class DBConnection {

    protected Socket _dbSocket;
    protected final Socket _clientSocket;
    protected final MessageProcessor _processor;
    protected ServerReader _sr;
    protected Thread _serverThread;
    protected final MongoProxy _myProxy;
    protected OutputStream _serverOutputStream;

    public DBConnection(MongoProxy proxy, Socket s, MessageProcessor p) throws IOException {
        _myProxy = proxy;
        _clientSocket = s;
        _processor = p;
        init();
    }

    protected void init() throws IOException {
        _dbSocket = new Socket();
        _dbSocket.connect(new InetSocketAddress("127.0.0.1", 27020));

        _serverOutputStream = new BufferedOutputStream(_dbSocket.getOutputStream());
        
        _sr = new ServerReader(_clientSocket, _dbSocket);

        _serverThread = new Thread(_sr);
        _serverThread.start();
    }

    public void shutdown() {
        _sr.stopRunning();

        // TODO close the socket
    }

    public void writeToServer(DBMessage msg) throws IOException{

        _serverOutputStream.write(msg.toByteArray());
        _serverOutputStream.flush();
    }

    class ServerReader implements Runnable {

        final Socket _client;
        final Socket _db;
        boolean _running = true;

        ServerReader(Socket client, Socket db) {
            _client = client;
            _db = db;
        }

        public void stopRunning() {
            _running = false;
        }
        
        public void run() {

            try {
                OutputStream clientOutStream = new BufferedOutputStream(_client.getOutputStream());
                InputStream dbInStream = new BufferedInputStream(_db.getInputStream());

                while(_running) {
                    DBMessage msg = DBMessage.readFromStream(dbInStream);
                    _myProxy.processMessage(MessageProcessor.Direction.FromServer, msg);
                    clientOutStream.write(msg.toByteArray());
                    clientOutStream.flush();
                }
            }
            catch(MongoDBException e){
                e.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }

            _running = false;
        }
    }
}
