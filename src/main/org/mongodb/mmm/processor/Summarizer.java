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

package org.mongodb.mmm.processor;

import org.mongodb.driver.impl.msg.DBMessage;
import org.mongodb.driver.impl.msg.MessageType;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Summarizer implements MessageProcessor {

    protected static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected static Data _data = new Data();

    static {
        Runnable r = new Runnable() {
            public void run() {
                System.out.println(_data.renderAndReset());
            }
        };
        scheduler.scheduleAtFixedRate(r, 1, 1, TimeUnit.SECONDS);
    }

    public Summarizer() {
    }
    
    public void process(int id, int seqNum, Direction dir, DBMessage message) {

        _data.tally(message);
    }

    static class Data {

        Map<MessageType, AtomicInteger> _ops = new HashMap<MessageType, AtomicInteger>();

        Data() {
            for(MessageType type : EnumSet.allOf(MessageType.class)) {
                _ops.put(type, new AtomicInteger());
            }
        }            

        void tally(DBMessage message) {
            _ops.get(message.getMessageType()).incrementAndGet();
        }

        String renderAndReset() {

            StringBuffer sb = new StringBuffer();
            Formatter f = new Formatter(sb);

            f.format("%tT Z", new Date());
            sb.append(":");
            f.format(" %5d", _ops.get(MessageType.OP_INSERT).intValue());
            f.format(" %5d", _ops.get(MessageType.OP_QUERY).intValue());
            f.format(" %5d", _ops.get(MessageType.OP_GET_MORE).intValue());
            f.format(" %5d", _ops.get(MessageType.OP_DELETE).intValue());
            f.format(" %5d", _ops.get(MessageType.OP_KILL_CURSORS).intValue());

            _data = new Data();
            
            return sb.toString();
        }
    }
}
