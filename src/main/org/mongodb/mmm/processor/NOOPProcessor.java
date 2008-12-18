package org.mongodb.mmm.processor;

import org.mongodb.driver.impl.msg.DBMessage;

/**
 *
 */
public class NOOPProcessor implements MessageProcessor {
    public void process(int id, int seqNum, Direction dir, DBMessage message) {
        // do nothing
    }
}
