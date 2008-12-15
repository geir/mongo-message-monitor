package org.mongodb.mmm.processor;

import org.mongodb.driver.impl.msg.DBMessage;

/**
 *
 */
public interface MessageProcessor {

    enum Direction { FromClient, FromServer }

    public void process(int id, int seqNum, Direction dir, DBMessage message);

}
