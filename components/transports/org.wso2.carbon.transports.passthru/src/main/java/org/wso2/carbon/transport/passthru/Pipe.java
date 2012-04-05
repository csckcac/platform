/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.transport.passthru;

import org.apache.http.nio.IOControl;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a buffer shared by both producers and consumers.
 */
public class Pipe {

    /** IOControl of the reader */
    private IOControl producerIoControl;

    /** IOControl of the consumer */
    private IOControl consumerIoControl;

    /** Fixed size buffer to read and write data */
    private ByteBuffer buffer;

    private boolean producerCompleted = false;

    /** Lock to synchronize the producers and consumers */
    private Lock lock = new ReentrantLock();

    /** Name to identify the buffer */
    private String name = "Buffer";

    private boolean consumerError = false;

    private boolean producerError = false;

    public Pipe(IOControl producerIoControl, 
                    ByteBuffer buffer, String name) {
        this.producerIoControl = producerIoControl;

        this.buffer = buffer;

        this.name += "_" + name;
    }

    /**
     * Set the consumers IOControl
     * @param consumerIoControl IOControl of the consumer
     */
    public void attachConsumer(IOControl consumerIoControl) {
        this.consumerIoControl = consumerIoControl;
    }

    /**
     * Consume the data from the buffer. Before calling this method attachConsumer
     * method must be called with a valid IOControl.
     *
     * @param encoder encoder used to write the data means there will not be any data
     * written in to this buffer
     * @return number of bytes written (consumed)
     * @throws IOException if an error occurred while consuming data
     */
    public int consume(final ContentEncoder encoder) throws IOException {
        if (consumerIoControl == null) {
            throw new IllegalStateException("Consumer cannot be null when calling consume");
        }

        if (producerIoControl == null) {
            throw new IllegalStateException("Producer cannot be null when calling consume");
        }

        lock.lock();
        try {
            // if producer at error we have to stop the encoding and return immediately
            if (producerError) {                
                encoder.complete();
                return -1;
            }

            buffer.flip();
            int bytesWritten = encoder.write(buffer);
            buffer.compact();

            if (buffer.position() == 0) {
                if (producerCompleted) {
                    encoder.complete();
                } else {
                    // buffer is empty. Wait until the producer fills up
                    // the buffer
                    consumerIoControl.suspendOutput();
                }
            }

            if (bytesWritten > 0 && !encoder.isCompleted()) {
                producerIoControl.requestInput();
            }

            return bytesWritten;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Produce data in to the buffer.
     *
     * @param decoder decoder to read bytes from the underlying stream
     * @return bytes read (consumed)
     * @throws IOException if an error occurs while reading data
     */
    public int produce(final ContentDecoder decoder) throws IOException {
        if (producerIoControl == null) {
            throw new IllegalStateException("Producer cannot be null when calling produce");
        }

        lock.lock();
        try {
            int bytesRead = decoder.read(buffer);

            // if consumer is at error we have to let the producer complete
            if (consumerError) {
                buffer.clear();
            }

            if (!buffer.hasRemaining()) {
                // Input buffer is full. Suspend client input
                // until the origin handler frees up some space in the buffer
                producerIoControl.suspendInput();
            }

            // If there is some content in the input buffer make sure consumer output is active
            if (buffer.position() > 0 || decoder.isCompleted()) {
                if (consumerIoControl != null) {
                    consumerIoControl.requestOutput();
                }
            }

            if (decoder.isCompleted()) {
                producerCompleted = true;
            }
            return bytesRead;
        } finally {
            lock.unlock();
        }
    }        

    @Override
    public String toString() {
        return name;
    }

    public void consumerError() {
        lock.lock();
        try {
            this.consumerError = true;
        } finally {
            lock.unlock();
        }
    }

    public void producerError() {
        lock.lock();
        try {
            this.producerError = true;
        } finally {
            lock.unlock();
        }
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}

