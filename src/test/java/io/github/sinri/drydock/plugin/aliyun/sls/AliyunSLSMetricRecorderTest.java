package io.github.sinri.drydock.plugin.aliyun.sls;

import io.github.sinri.drydock.plugin.aliyun.sls.kit.AliyunSLSMetricRecorder;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.github.sinri.keel.logger.metric.KeelMetricRecord;
import org.junit.Test;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AliyunSLSMetricRecorderTest extends KeelUnitTest {

    @Test
    public void test() {
        AliyunSLSMetricRecorder aliyunSLSMetricRecorder = new AliyunSLSMetricRecorder();
        aliyunSLSMetricRecorder.start();
        async(() -> {
            return Keel.asyncCallStepwise(10, i -> {
                           aliyunSLSMetricRecorder.recordMetric(
                                   new KeelMetricRecord("test_metric", i * i / 10.0)
                                           .label("client", "AliyunSLSMetricRecorderTest")
                           );
                           return Keel.asyncSleep(1000L);
                       })
                       .compose(v -> {
                           return Keel.asyncSleep(3000L);
                       });
        });

    }
}
