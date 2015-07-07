/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.accountcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.dc.client.utils.DcLogger;

/**
 * PCSライブラリのロガー.
 */
public class PcsLogger implements DcLogger {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(AccountCreator.class);

    @Override
    public void debug(String msg) {
        // Loggerを利用する場合は、ここでLoggerにはき出す
        // コンソールに出したい場合は、System.out.println(msg); とする
        log.debug(msg);
    }
}
