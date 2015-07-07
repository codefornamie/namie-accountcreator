/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.accountcreator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.dc.client.Accessor;
import com.fujitsu.dc.client.Account;
import com.fujitsu.dc.client.Cell;
import com.fujitsu.dc.client.DaoException;
import com.fujitsu.dc.client.DcContext;
import com.fujitsu.dc.client.Role;
import com.fujitsu.dc.client.utils.DcLoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * アカウント一括作成コマンド。
 */
public class AccountCreator {
    private static Logger log = LoggerFactory.getLogger(AccountCreator.class);

    private static final int PASSWORD_LENGTH = 32;
    private String baseUrl = Conf.getValue("pio.base");
    private String cellName = Conf.getValue("pio.cell");
    private String schema = "";
    private String boxName = Conf.getValue("pio.box");
    // personium.ioへの接続アカウント、パスワードは、-Dpio.user=admin -Dpio.password=xxxx のように指定する
    private String adminUsername = System.getProperty("pio.user");
    private String adminPassword = System.getProperty("pio.password");

    private MessageDigest md = null;

    /**
     * メインルーチン。
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        PcsLoggerFactory pcsLoggerFactory = new PcsLoggerFactory();
        DcLoggerFactory.setDefaultFactory(pcsLoggerFactory);

        String filename = args[0];
        AccountCreator app = new AccountCreator();
        app.exec(filename);
    }

    /**
     * アカウント一括作成処理。
     * @param filename アカウント定義ファイル(CSV)
     */
    public void exec(String filename) {
        boolean deleteMode = false;

        DcContext dc = new DcContext(baseUrl, cellName, schema, boxName);
        dc.setHttpClient(new HttpClientForProxy());
        Cell cell = null;
        try {
            Accessor ac = dc.getAccessorWithAccount(cellName, adminUsername, adminPassword);
            cell = ac.cell(cellName);
        } catch (DaoException e) {
            log.error(e.getMessage());
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length != 2) {
                    System.err.println("invalid csv format.");
                    return;
                }
                String username = line[0];
                String password = line[1];
                String hashedPassword = toHashedString(password);

                try {
                    if (deleteMode) {
                        deleteAccount(cell, username);
                    } else {
                        createAccount(cell, username, hashedPassword);
                    }
                    log.info("\"" + username + "\" create SUCCESS.");
                } catch (DaoException e) {
                    log.error("\"" + username + "\" create FAILED. " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * アカウントの作成。
     * @param cell 対象Cell
     * @param username 登録するユーザー名
     * @param password 登録するパスワード
     * @throws DaoException Personium例外
     */
    private void createAccount(Cell cell, String username, String password) throws DaoException {
        HashMap<String, Object> accountMap = new HashMap<String, Object>();
        accountMap.put("Name", username);
        Account account = cell.account.create(accountMap, password);
        //Role role = cell.role.retrieve("role_kizuna_service");
        Role role = cell.role.retrieve("role_kizuna_service", this.boxName);
        account.role.link(role);
    }

    /**
     * アカウントの削除。
     * @param cell 対象Cell
     * @param username 削除対象のユーザー名
     * @throws DaoException Personium例外
     */
    private void deleteAccount(Cell cell, String username) throws DaoException {
        Account account = cell.account.retrieve(username);
        Role role = cell.role.retrieve("role_kizuna_service");
        account.role.unLink(role);
        cell.account.del(username);
    }

    /**
     * 入力文字列をSHA-256でハッシュ値に変換し、先頭から32文字を返す。
     * @param src 変換元文字列
     * @return 変換後の文字列
     */
    private String toHashedString(String src) {
        byte[] bytes = toHashValue(src);
        String result = toEncryptedString(bytes);
        return result.substring(0, PASSWORD_LENGTH);
    }

    /*
     * 文字列をハッシュ値（バイト配列）へ変換する。
     * @param source 変換元文字列
     * @return 変換後の文字列
     */
    private byte[] toHashValue(String source) {
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(source.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        return md.digest();
    }

    /*
     * バイト配列を16進数の文字列に変換して返す。
     * @param bytes 変換元のバイト配列
     * @return 変換後の文字列
     */
    private String toEncryptedString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = String.format("%02x", b);
            sb.append(hex);
        }
        return sb.toString();
    }
}
