package io.github.sinri.drydock.plugin.aliyun.sls;

import io.github.sinri.drydock.plugin.aliyun.sls.kit.AliyunSLSIssueAdapterImpl;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import org.junit.Test;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AliyunSLSIssueAdapterImplTest extends KeelUnitTest {
    @Test
    public void test() throws InterruptedException {
        AliyunSLSIssueAdapterImpl aliyunSLSIssueAdapter = new AliyunSLSIssueAdapterImpl();
        aliyunSLSIssueAdapter.start();
        KeelIssueRecordCenter center = KeelIssueRecordCenter.build(aliyunSLSIssueAdapter);
        KeelIssueRecorder<KeelEventLog> issueRecorder = center.generateIssueRecorder("Test", KeelEventLog::new);

        async(() -> {
            return Keel.asyncCallStepwise(10, i -> {
                           issueRecorder.info("io.github.sinri.drydock.plugin.aliyun.sls.Test2.test", x -> x
                                   .put("i", i)
                           );
                           return Keel.asyncSleep(1000);
                       })
                       .compose(v -> {
                           issueRecorder.exception(new Exception("here an exception"));
                           return Future.succeededFuture();
                       })
                       .compose(v -> {
                           return Keel.asyncSleep(5000L);
                       });
        });

    }
}
