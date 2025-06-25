package io.github.sinri.drydock.plugin.aliyun.sls.kit;

import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogContent;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogGroup;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogItem;
import io.github.sinri.drydock.plugin.aliyun.sls.kit.entity.LogTag;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AliyunSLSLogPutterTest extends KeelUnitTest {
    @Test
    public void test1() {
        KeelConfigElement configElement = Keel.getConfiguration().extract("aliyun", "sls");
        Objects.requireNonNull(configElement);
        AliyunSlsConfigElement aliyunSlsConfigElement = new AliyunSlsConfigElement(configElement);

        AliyunSLSLogPutter aliyunSLSLogPutter = new AliyunSLSLogPutter(
                aliyunSlsConfigElement.getAccessKeyId(),
                aliyunSlsConfigElement.getAccessKeySecret(),
                aliyunSlsConfigElement.getEndpoint()
        );

        try {
            async(() -> aliyunSLSLogPutter.putLogs(
                    aliyunSlsConfigElement.getProject(),
                    aliyunSlsConfigElement.getLogstore(),
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
            ));
        } finally {
            aliyunSLSLogPutter.close();
        }

    }
}
