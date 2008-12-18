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

import java.io.IOException;

/**
 *
 */
public class MessageLogger implements MessageProcessor {

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
