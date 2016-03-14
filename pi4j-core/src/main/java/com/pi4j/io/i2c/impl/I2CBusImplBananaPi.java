package com.pi4j.io.i2c.impl;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Library (Core)
 * FILENAME      :  I2CBusImplBananaPi.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2016 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.jni.I2C;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is implementation of i2c bus. This class keeps underlying linux file descriptor of
 * particular bus. As all reads and writes from/to i2c bus are blocked I/Os current implementation uses only
 * one file per bus for all devices. Device implementations use this class file handle.
 *
 * @author Daniel Sendula, Robert Savage
 *
 */
public class I2CBusImplBananaPi extends I2CBusImpl {

    /** Singleton instance of bus 0 */
    private static I2CBus bus0 = null;

    /** Singleton instance of bus 1 */
    private static I2CBus bus1 = null;

    /** Singleton instance of bus 2 */
    private static I2CBus bus2 = null;

    /** Singleton instance of bus 3 */
    private static I2CBus bus3 = null;

    /**
     * to lock the creation/destruction of the bus singletons
     */
    private final static Lock lock = new ReentrantLock(true);

    /**
     * Factory method that returns bus implementation.
     *
     * @param busNumber bus number
     * @return appropriate bus implementation
     * @throws IOException thrown in case there is a problem opening bus file or bus number is not 0 or 1.
     */
    public static I2CBus getBus(int busNumber) throws IOException {
        I2CBus bus;
        lock.lock();
        if (busNumber == 0) {
            bus = bus0;
            if (bus == null) {
                bus = new I2CBusImplBananaPi("/dev/i2c-0");
                bus0 = bus;
            }
        } else if (busNumber == 1) {
            bus = bus1;
            if (bus == null) {
                bus = new I2CBusImplBananaPi("/dev/i2c-1");
                bus1 = bus;
            }
        } else if (busNumber == 2) {
            bus = bus2;
            if (bus == null) {
                bus = new I2CBusImplBananaPi("/dev/i2c-2");
                bus2 = bus;
            }
        } else if (busNumber == 3) {
            bus = bus3;
            if (bus == null) {
                bus = new I2CBusImplBananaPi("/dev/i2c-3");
                bus3 = bus;
            }
        } else {
            throw new IOException("Unknown bus number " + busNumber);
        }
        lock.unlock();
        return bus;
    }

    /**
     * Constructor of i2c bus implementation.
     *
     * @param filename file name of device to be opened.
     *
     * @throws IOException thrown in case that file cannot be opened
     */
    public I2CBusImplBananaPi(String filename) throws IOException {
        super(filename);
    }

    /**
     * Closes this i2c bus
     *
     * @throws IOException never in this implementation
     */
    @Override
    public void close() throws IOException {
        lock.lock();
        I2C.i2cClose(fd);
        /* after closing the fd, we must "forget" the singleton bus instance, otherwise further request to this bus will
         * always fail
         */
        if (this == bus0) {
            bus0 = null;
        } else if (this == bus1) {
            bus1 = null;
        } else if (this == bus2) {
            bus2 = null;
        } else if (this == bus3) {
            bus3 = null;
        }
        lock.unlock();
    }
}
