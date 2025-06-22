package io.github.sinri.drydock.plugin.aliyun.sls;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DebugSignature extends KeelUnitTest {
    
    @Test
    public void debugSignatureCalculation() {
        String method = "POST";
        String contentMd5 = "SAMPLE_MD5_HASH";
        String contentType = "application/x-protobuf";
        String date = getGMTDate();
        String apiVersion = "0.6.0";
        String canonicalizedResource = "/logstores/test-logstore/shards/lb";
        
        // Build canonicalized headers
        StringBuilder canonicalizedHeaders = new StringBuilder();
        canonicalizedHeaders.append("x-log-apiversion:").append(apiVersion).append("\n");
        canonicalizedHeaders.append("x-log-compresstype:lz4").append("\n");
        canonicalizedHeaders.append("x-log-signaturemethod:hmac-sha1");
        
        // Build string to sign
        StringBuilder signStr = new StringBuilder();
        signStr.append(method).append("\n");
        signStr.append(contentMd5).append("\n");
        signStr.append(contentType).append("\n");
        signStr.append(date).append("\n");
        signStr.append(canonicalizedHeaders.toString()).append("\n");
        signStr.append(canonicalizedResource);
        
        System.out.println("=== 签名调试信息 ===");
        System.out.println("Method: " + method);
        System.out.println("Content-MD5: " + contentMd5);
        System.out.println("Content-Type: " + contentType);
        System.out.println("Date: " + date);
        System.out.println("Canonicalized Headers: " + canonicalizedHeaders.toString());
        System.out.println("Canonicalized Resource: " + canonicalizedResource);
        System.out.println();
        System.out.println("String to Sign:");
        System.out.println("\"" + signStr.toString() + "\"");
        System.out.println();
        System.out.println("String to Sign (with visible newlines):");
        System.out.println(signStr.toString().replace("\n", "\\n\n"));
    }
    
    private String getGMTDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }
} 