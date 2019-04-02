package com.reltio.cst.dataload.util.mail;

import com.reltio.cst.dataload.domain.DataloaderInput;
import com.reltio.cst.util.Util;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;

public class SendMailTest {

    @Test
    @Ignore
    public void send() throws Exception {

        String path = "/home/vignesh/IdeaProjects/ROCS/util-dataload-processor/src/test/resources/sample-config.properties";

        Properties properties = Util.getProperties(path, "PASSWORD");

        final DataloaderInput dataloaderInput = new DataloaderInput(properties);

        SendMail sendMail = new SendMail();
        sendMail.send(dataloaderInput);
    }
}

