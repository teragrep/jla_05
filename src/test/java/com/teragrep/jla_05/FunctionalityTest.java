/*
   Log4j RELP Plugin
   Copyright (C) 2021  Suomen Kanuuna Oy

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.teragrep.jla_05;

import org.apache.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;


public class FunctionalityTest {

    static Logger logger = Logger.getLogger(FunctionalityTest.class.getName());

    //@Test
    @DisplayName("Main Test")
    public void TestFunctionality() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = new Date();
        System.out.println("Start: " + sdf.format(start));
        int event_count = 3;
        Assertions.assertAll(() -> {
            for(int i=1; i<=event_count; i++) {
                logger.info("Info Message #" + i);
                logger.warn("Warning message #" + i);
                logger.trace("Trace message #" + i);
            }
            // Required so RELP session gets closed gracefully
            LogManager.shutdown();
        });
        Date end = new Date();
        System.out.println("End: " + sdf.format(end));
        long duration = (end.getTime() - start.getTime())/1000;
        long eps = event_count/(duration==0?1:duration);
        System.out.println("Elapsed: " + duration + "s (" + eps + " eps)");
    }
}
