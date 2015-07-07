/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.accountcreator;

import com.fujitsu.dc.client.utils.DcLogger;
import com.fujitsu.dc.client.utils.DcLoggerFactory;

/**
 * PCSライブラリのログファクトリ.
 */
public class PcsLoggerFactory extends DcLoggerFactory {

    /**
     * インスタンス生成.
     * @param clazz loggerを利用する場合の対象クラス.
     * @return PcsLogger ロガークラス.
     */
    @Override
    @SuppressWarnings("rawtypes")
    protected DcLogger newInstance(Class clazz) {
        return new PcsLogger();
    }
}
