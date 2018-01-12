/**
 *
 */
package com.reltio.cst.dataload;

import com.google.gson.Gson;

/**
 *
 *
 */
public final class DataloadConstants {

    public static final String PC_SOURCE_SYSTEM = "configuration/sources/Reltio";
    public static final Gson GSON = new Gson();
    public static final Integer MAX_FAILURE_COUNT = 1000000;
    public static final String FAILURE_LOG_KEY = "FailedToResentJson|";
    public static final Integer DEFAULT_ERROR_CODE = 000;
    public static final Integer INVALID_JSON_FILE = 001;
    public static final String INVALID_JSON_FILE_MESSAGE = "Invalid JSON in the input File";
    public static final Integer MAX_FAILURE_PER_ERROR_CODE = 10;
    public static final Integer MAX_QUEUE_SIZE = 500000;
    public static final String JSON_FILE_TYPE_PIPE = "PIPE_ARRAY";
    public static final String JSON_FILE_TYPE_ARRAY = "ARRAY";
    public static final String JSON_FILE_TYPE_OBJECT = "OBJECT";
    public static final Integer DEFAULT_TIMEOUT_IN_MINUTES = 5;


    public static final Integer RECORDS_PER_POST = 30;
    public static final Integer THREAD_COUNT = 5;
    public static final Integer MAX_OBJECTS_TO_UPDATE = 5; //changes


    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    public static final String MAIL_SMTP_HOST = "email-smtp.us-east-1.amazonaws.com";
    public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";
    public static final String FROM_EMAIL = "no-reply@reltio.com";
    public static final String FROM_EMAIL_NAME = "Reltio No-reply";
    public static final String TO_EMAIL = "toEmail";
    public static final String CC_EMAIL = "ccEmail";
    public static final String BCC_EMAIL = "bccEmail";
    public static final String SUBJECT_LINE = "subjectLine";
    public static final String MESSAGE_BODY = "messageBody";
    public static final String MIME_TYPE = "mimeType";
    public static final String TIME_TO_EXPIRE = "timeToExpire";

    public static final String DEFAULT_BCC_EMAIL = "";

    public static final String SMTP_USERNAME = ""; // Replace with your SMTP username.
    public static final String SMTP_PASSWORD = ""; // Replace with your SMTP password.


    public static final String[] FAILED_LOG_FILE_HEADER = {"Crosswalk Type",
            "Crosswalk Value", "Error Code", "Error Message",
            "Error Detailed Message"};

}
