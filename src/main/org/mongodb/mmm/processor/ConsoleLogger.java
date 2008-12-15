package org.mongodb.mmm.processor;

import org.mongodb.driver.impl.msg.DBMessage;

import java.io.IOException;

/**
 *
 */
public class ConsoleLogger implements MessageProcessor {

    protected boolean _logHex = false;
    
    public void process(int id, int seqNum, Direction dir, DBMessage message) {

        log(id, seqNum, dir, message);

        if (_logHex) {
            logHex(message);
        }
    }


    protected void logHex(DBMessage msg) {

        try {
            msg.dumpHex(System.out);
            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void log(int id, int seqNum, Direction dir, DBMessage msg) {

        String dirStr = (dir == Direction.FromClient) ? "->" : "<-";

        System.out.println(id + ":" + seqNum + ":" + dirStr + ":" + msg);
    }
}
