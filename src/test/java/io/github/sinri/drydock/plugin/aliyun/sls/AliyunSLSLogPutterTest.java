package io.github.sinri.drydock.plugin.aliyun.sls;

import io.github.sinri.drydock.plugin.aliyun.sls.kit.*;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogContent;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogGroup;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogItem;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogTag;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.Test;

import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AliyunSLSLogPutterTest extends KeelUnitTest {
    @Test
    public void test1() {
        KeelConfigElement configElement = Keel.getConfiguration().extract("aliyun", "sls");
        var accessKeyId = configElement.readString(List.of("accessKeyId"));
        var accessKeySecret = configElement.readString(List.of("accessKeySecret"));
        var endpoint = configElement.readString(List.of("endpoint"));

        var project = configElement.readString(List.of("project"));
        var logstore = configElement.readString(List.of("logstore"));

        AliyunSLSLogPutter aliyunSLSLogPutter = new AliyunSLSLogPutter(accessKeyId, accessKeySecret, endpoint);

        try {
            async(() -> {
                return aliyunSLSLogPutter.putLogs(
                        project,
                        logstore,
                        new LogGroup("Test", "local")
                                .addLogTags(List.of(
                                        new LogTag("K", "V")
                                ))
                                .addLogItems(List.of(
                                        new LogItem(
                                                Math.toIntExact(System.currentTimeMillis() / 1000)
                                        )
                                                .addContent(new LogContent("text", "God Bless!"))
                                ))
                );
            });
        } finally {
            aliyunSLSLogPutter.close();
        }

    }
}
