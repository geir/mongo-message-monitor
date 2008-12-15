package org.mongodb.mmm;

import org.mongodb.driver.impl.msg.DBMessage;
import org.mongodb.driver.MongoDBException;
import org.mongodb.driver.MongoDBIOException;
import org.mongodb.mmm.processor.MessageProcessor;
import org.mongodb.mmm.processor.ConsoleLogger;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class MongoProxy implements Runnable {

    protected final static AtomicInteger id = new AtomicInteger();

    protected final AtomicInteger count = new AtomicInteger();

    protected final Socket _clientSocket;
    protected Socket _dbSocket;
    protected boolean _running = true;
    protected final int _myID = id.getAndIncrement();

    protected ServerReader _sr;
    protected Thread _serverThread;

    protected MessageProcessor _processor = new ConsoleLogger();

    public MongoProxy(Socket client) {
        _clientSocket = client;
    }

    public void run() {

        System.out.println(_myID + ": connection from [" + _clientSocket.getRemoteSocketAddress() + "]");

        try {
            if (!createDBConnection()) {
                return;
            }

            processMessages();
        }
        catch(MongoDBException me ) {
            me.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean createDBConnection() throws IOException {

        _dbSocket = new Socket();

        _dbSocket.connect(new InetSocketAddress("127.0.0.1", 27020));

        _sr = new ServerReader(_clientSocket, _dbSocket);

        _serverThread = new Thread(_sr);

        _serverThread.start();
        

        return true;
    }

    protected void processMessages() throws MongoDBException {

        while(_running) {

            try {

                DBMessage msg = DBMessage.readFromStream(_clientSocket.getInputStream());

                _processor.process(_myID, count.getAndIncrement(), MessageProcessor.Direction.FromClient, msg);

                writeToServer(msg);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void writeToServer(DBMessage msg) {

        try {
            OutputStream os = _dbSocket.getOutputStream();
            os.write(msg.toByteArray());
        } catch (IOException e) {
            throw new MongoDBIOException("IO Error : ", e);
        }
    }

    class ServerReader implements Runnable {

        final Socket _client;
        final Socket _db;
        boolean _running = true;

        ServerReader(Socket client, Socket db) {

            _client = client;
            _db = db;
        }

        public void run() {

            try {
                OutputStream os = _client.getOutputStream();

                while(_running) {
                    DBMessage msg = DBMessage.readFromStream(_db.getInputStream());
                    _processor.process(_myID, count.getAndIncrement(), MessageProcessor.Direction.FromServer, msg);
                    os.write(msg.toByteArray());
                }
            }
            catch(MongoDBException e){
                e.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
