//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.reltio.cst.dataload.util;

import com.reltio.cst.service.ReltioAPIService;
import com.reltio.cst.service.TokenGeneratorService;
import com.reltio.cst.service.impl.SimpleReltioAPIServiceImpl;
//import com.reltio.cst.service.impl.TokenGeneratorServiceImpl;
import com.reltio.cst.dataload.TokenGeneratorServiceImpl;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import com.reltio.cst.util.PasswordEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Util {
    private static Logger logger = LoggerFactory.getLogger(Util.class.getName());

    public Util() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isEmpty(String data) {
        return data == null || data.trim().length() == 0;
    }

    public static void close(AutoCloseable... resources) {
        if (resources != null) {
            AutoCloseable[] var1 = resources;
            int var2 = resources.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                AutoCloseable resource = var1[var3];

                try {
                    if (resource != null) {
                        resource.close();
                    }
                } catch (Exception var6) {
                    logger.error("failed to close resource " + var6.getMessage());
                }
            }
        }

    }

    public static Properties getProperties(String propertyFilePath) throws Exception {
        FileReader in = new FileReader(propertyFilePath);
        Properties config = new Properties();

        try {
            config.load(in);
        } catch (Exception var7) {
            logger.error("failed to load property file" + propertyFilePath);
            logger.error(var7.getMessage());
            throw var7;
        } finally {
            close(in);
        }

        return config;
    }

    public static void write(Properties config, String propertyFilePath) throws Exception {
        FileOutputStream output = new FileOutputStream(propertyFilePath);
        config.store(output, (String)null);
        close(output);
    }

    public static void write(Properties config, String propertyFilePath, String comments) throws Exception {
        FileOutputStream output = new FileOutputStream(propertyFilePath);
        config.store(output, comments);
        close(output);
    }

    public static ReltioAPIService getReltioService(Properties config,String refresh_token) throws Exception {
        TokenGeneratorService service = null;
        String authUrl = config.getProperty("AUTH_URL");
        String username = config.getProperty("USERNAME");
        String password = config.getProperty("PASSWORD");
        String clientCredentials = config.getProperty("CLIENT_CREDENTIALS");
        if (!isEmpty(username) && !isEmpty(password)) {
            service = new TokenGeneratorServiceImpl(username, password, authUrl);
        } else if (!isEmpty(clientCredentials)) {
           // String clientCredentials = config.getProperty("CLIENT_CREDENTIALS");
            service = new TokenGeneratorServiceImpl(clientCredentials, authUrl);
        }
        else {
            service = new TokenGeneratorServiceImpl(refresh_token, authUrl);
        }

        ReltioAPIService restApi = new SimpleReltioAPIServiceImpl(service);
        return restApi;
    }

    public static ReltioAPIService getReltioService(String clientCredentials, String authUrl) throws Exception {
        TokenGeneratorService service = new TokenGeneratorServiceImpl(clientCredentials, authUrl);
        ReltioAPIService restApi = new SimpleReltioAPIServiceImpl(service);
        return restApi;
    }

    public static ReltioAPIService getReltioService(String username, String password, String authUrl) throws Exception {
        TokenGeneratorService service = new TokenGeneratorServiceImpl(username, password, authUrl);
        ReltioAPIService restApi = new SimpleReltioAPIServiceImpl(service);
        return restApi;
    }

    public static BufferedReader getBufferedReader(String file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return reader;
    }

    public static BufferedWriter getBufferedWriter(String file) throws Exception {
        BufferedWriter reader = new BufferedWriter(new FileWriter(file));
        return reader;
    }

    public static List<String> listMissingProperties(Properties properties, Collection<String> requiredProps) {
        List<String> missingKeys = new ArrayList();
        Iterator var3 = requiredProps.iterator();

        while(var3.hasNext()) {
            String key = (String)var3.next();
            if (isEmpty(properties.getProperty(key))) {
                missingKeys.add(key);
            }
        }

        return missingKeys;
    }

    public static List<String> listMissingProperties(Properties properties, Collection<String> requiredProps, Map<List<String>, List<String>> mutualExclusiveProps) {
        List<String> missingKeys = new ArrayList();
        missingKeys.addAll(getMissingProperties(properties, requiredProps));
        if (mutualExclusiveProps != null && mutualExclusiveProps.size() > 0) {
            Iterator var4 = mutualExclusiveProps.entrySet().iterator();

            while(var4.hasNext()) {
                Entry<List<String>, List<String>> entry = (Entry)var4.next();
                List<String> propKeys = getMissingProperties(properties, (Collection)entry.getKey());
                List<String> propValues = getMissingProperties(properties, (Collection)entry.getValue());
                if (!isEmpty((Collection)propKeys) && !isEmpty((Collection)propValues)) {
                    missingKeys.addAll(propKeys);
                }
            }
        }

        return missingKeys;
    }

    private static List<String> getMissingProperties(Properties properties, Collection<String> props) {
        List<String> missingKeys = new ArrayList();
        if (!isEmpty(props)) {
            Iterator var3 = props.iterator();

            while(var3.hasNext()) {
                String key = (String)var3.next();
                if (isEmpty(properties.getProperty(key))) {
                    missingKeys.add(key);
                }
            }
        }

        return missingKeys;
    }

    public static Properties getProperties(String propertyFilePath, String... encryptableKey) throws Exception {
        FileReader in1 = new FileReader(propertyFilePath);
        FileReader in2 = new FileReader(propertyFilePath);
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("Reltio");
        Properties config = new Properties();
        Properties configEncrypt = new EncryptableProperties(encryptor);
        config.load(in1);
        configEncrypt.load(in2);
        boolean flag = false;
        String[] var8 = encryptableKey;
        int var9 = encryptableKey.length;

        for(int var10 = 0; var10 < var9; ++var10) {
            String singleKey = var8[var10];
            String propertyValue = config.getProperty(singleKey);
            if (isEmpty(propertyValue)) {
                logger.warn("Invalid/Missing Property or Property value is empty.. Skipping this property : " + singleKey);
            } else if (!propertyValue.startsWith("ENC(")) {
                logger.info("Encrypting " + singleKey + " ...");
                config.setProperty(singleKey, "ENC(" + PasswordEncryptor.encryptPassword(config.getProperty(singleKey)) + ")");
                flag = true;
                System.out.println("Finished encrypting " + singleKey + "...");
            }
        }

        if (flag) {
            write(config, propertyFilePath, "Password Encrypted");
        }

        close(in1, in2);
        return configEncrypt;
    }

    public static void waitForTasksReady(Collection<Future<Long>> futures, int maxNumberInList) throws Exception {
        while(futures.size() > maxNumberInList) {
            Thread.sleep(20L);
            futures.removeIf(Future::isDone);
        }

    }

    public static void setHttpProxy(Properties properties) {
        if (properties.getProperty("HTTP_PROXY_HOST") != null && properties.getProperty("HTTP_PROXY_PORT") != null) {
            System.setProperty("https.proxyHost", properties.getProperty("HTTP_PROXY_HOST"));
            System.setProperty("https.proxyPort", properties.getProperty("HTTP_PROXY_PORT"));
            System.setProperty("http.proxyHost", properties.getProperty("HTTP_PROXY_HOST"));
            System.setProperty("http.proxyPort", properties.getProperty("HTTP_PROXY_PORT"));
        }

    }

}
