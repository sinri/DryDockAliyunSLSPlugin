package io.github.sinri.drydock.plugin.aliyun.sls.kit;

import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogGroup;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogGroupList;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.protocol.Lz4Utils;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AliyunSLSLogPutter {
    // Constants for SLS API
    private static final String API_VERSION = "0.6.0";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String GMT_TIMEZONE = "GMT";
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    // HTTP Header names
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_DATE = "Date";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_LOG_API_VERSION = "x-log-apiversion";
    private static final String HEADER_LOG_SIGNATURE_METHOD = "x-log-signaturemethod";
    private static final String HEADER_LOG_BODY_RAW_SIZE = "x-log-bodyrawsize";
    private static final String HEADER_LOG_CONTENT_MD5 = "Content-MD5";
    private static final String HEADER_LOG_COMPRESS_TYPE = "x-log-compresstype";
    private final String accessKeyId;
    private final String accessKeySecret;
    private final WebClient webClient;
    private final String endpoint;

    public AliyunSLSLogPutter(String accessKeyId, String accessKeySecret, String endpoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.webClient = WebClient.create(Keel.getVertx());
        this.endpoint = endpoint;
    }

    public void close() {
        this.webClient.close();
    }

    /**
     * 调用PutLogs API。
     *
     * @param project  Project name
     * @param logstore Logstore name
     * @param logGroup LogGroup to be sent
     * @return Future of void if successful, or failed future with error message
     */
    public Future<Void> putLogs(String project, String logstore, LogGroup logGroup) {
        // Construct the URL for PutLogs API
        String uri = String.format("/logstores/%s/shards/lb", logstore);
        String url = String.format("https://%s.%s%s", project, endpoint, uri);
        HttpRequest<Buffer> request = this.webClient.postAbs(url);

        Map<String, String> headers = new HashMap<>();
        // Add common request headers

        String date = getGMTDate();
        String contentType = "application/x-protobuf";

        headers.put(HEADER_DATE, date);
        headers.put(HEADER_CONTENT_TYPE, contentType);
        headers.put(HEADER_LOG_API_VERSION, API_VERSION);
        headers.put(HEADER_LOG_SIGNATURE_METHOD, "hmac-sha1");
        headers.put(HEADER_LOG_COMPRESS_TYPE, "lz4");
        headers.put("Host", project + "." + endpoint);
        // Convert LogGroup to protobuf format
        Buffer raw = serializeLogGroup(logGroup);
        headers.put(HEADER_LOG_BODY_RAW_SIZE, String.valueOf(raw.length()));
        Buffer payload = Lz4Utils.compress(raw);
        headers.put(HEADER_CONTENT_LENGTH, String.valueOf(payload.length()));

        try {
            var contentMd5 = Base64.getEncoder().encodeToString(
                    java.security.MessageDigest.getInstance("MD5").digest(payload.getBytes())
            );
            headers.put(HEADER_LOG_CONTENT_MD5, contentMd5);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }

        // Calculate signature and add authorization header
        String signature = calculateSignature(
                "POST",
                payload,
                contentType,
                date,
                headers,
                uri,
                null

        );
        headers.put(HEADER_AUTHORIZATION, "LOG " + accessKeyId + ":" + signature);
        headers.forEach(request::putHeader);

        // Send the request
        return request.sendBuffer(payload)
                      .compose(bufferHttpResponse -> {
                          if (bufferHttpResponse.statusCode() == 200) {
                              return Future.succeededFuture();
                          }
                          return Future.failedFuture("put log failed [" + bufferHttpResponse.statusCode() + "] "
                                  + bufferHttpResponse.bodyAsString());
                      });
    }

    /**
     * Serialize LogGroup to protobuf format.
     * Note: This is a simplified implementation. In a real-world scenario,
     * you would use protobuf libraries to properly serialize the LogGroup.
     *
     * @param logGroupList LogGroups to serialize
     * @return Buffer containing serialized LogGroup
     */
    @Deprecated
    private Buffer serializeLogGroup(List<LogGroup> logGroupList) {
        // According to Aliyun SLS API documentation, we should send LogGroupList
        // But let's try sending just the first LogGroup to see if that works
        //        if (logGroupList.size() == 1) {
        //            return Buffer.buffer(logGroupList.get(0).toProtobuf().toByteArray());
        //        } else {
        // For multiple log groups, use LogGroupList
        return Buffer.buffer(new LogGroupList(logGroupList).toProtobuf().toByteArray());
        //        }
    }

    /**
     * According to Aliyun SLS API documentation, we should send LogGroupList;
     * But let's try sending just the first LogGroup to see if that works.
     */
    private Buffer serializeLogGroup(LogGroup logGroup) {
        return Buffer.buffer(logGroup.toProtobuf().toByteArray());
    }

    /**
     * Get current date in GMT format as required by SLS API.
     *
     * @return Date string in RFC1123 format
     */
    private String getGMTDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(RFC1123_PATTERN, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(GMT_TIMEZONE));
        return sdf.format(new Date());
    }

    private String calculateSignature(
            String method,
            Buffer body,
            String contentType,
            String date,
            Map<String, String> headers,
            String uri,
            String queries
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append("\n");
        if (body != null) {
            String md5 = Keel.digestHelper().MD5(body.getBytes());
            sb.append(md5).append("\n");
        }
        sb.append(contentType).append("\n");
        sb.append(date).append("\n");

        List<String> headerLines = headers.keySet().stream()
                                          .filter(headerName -> headerName.startsWith("x-log-") || headerName.startsWith("x-acs-"))
                                          .sorted().map(x -> x + ":" + headers.get(x))
                                          .toList();
        headerLines.forEach(x -> {
            sb.append(x).append("\n");
        });

        sb.append(uri);
        if (queries != null && !queries.isBlank()) {
            sb.append("?").append(queries);
        }

        var signStr = sb.toString();

        try {
            // Calculate HMAC-SHA1 signature
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(
                    accessKeySecret.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA1_ALGORITHM
            );
            mac.init(signingKey);
            byte[] signatureBytes = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));

            // Encode signature in Base64
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
