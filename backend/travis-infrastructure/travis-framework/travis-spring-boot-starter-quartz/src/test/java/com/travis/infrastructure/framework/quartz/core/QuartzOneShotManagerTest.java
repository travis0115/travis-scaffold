package com.travis.infrastructure.framework.quartz.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

class QuartzOneShotManagerTest {

    private Scheduler scheduler;
    private QuartzOneShotManager manager;

    @BeforeEach
    void setUp() throws Exception {
        var properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "oneShotManagerTest");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.threadPool.threadCount", "1");
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        scheduler = new StdSchedulerFactory(properties).getScheduler();
        manager = new QuartzOneShotManager(scheduler);
        manager.registerGroup("test-group", "test-owner");
    }

    @AfterEach
    void tearDown() throws Exception {
        scheduler.shutdown(true);
    }

    @Test
    void shouldCreateAndRescheduleOneShotTask() throws Exception {
        Instant firstTime = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        var firstTask = task("task-1", firstTime, 1L);

        manager.sync(firstTask);
        manager.sync(firstTask);

        var jobKey = JobKey.jobKey("task-1", "test-group");
        var triggerKey = TriggerKey.triggerKey("task-1-trigger", "test-group");
        assertThat(scheduler.getJobDetail(jobKey).getJobDataMap().getLong("id")).isEqualTo(1L);
        assertThat(scheduler.getTriggersOfJob(jobKey)).hasSize(1);
        assertThat(scheduler.getTrigger(triggerKey).getNextFireTime().toInstant())
                .isEqualTo(firstTime);

        Instant nextTime = firstTime.plus(1, ChronoUnit.HOURS);
        manager.sync(task("task-1", nextTime, 2L));

        assertThat(scheduler.getJobDetail(jobKey).getJobDataMap().getLong("id")).isEqualTo(2L);
        assertThat(scheduler.getTriggersOfJob(jobKey)).hasSize(1);
        assertThat(scheduler.getTrigger(triggerKey).getNextFireTime().toInstant())
                .isEqualTo(nextTime);

        scheduler.unscheduleJob(triggerKey);
        manager.sync(task("task-1", nextTime, 2L));

        assertThat(scheduler.getTrigger(triggerKey).getNextFireTime().toInstant())
                .isEqualTo(nextTime);
    }

    @Test
    void shouldDeleteUnexpectedTasks() throws Exception {
        Instant executeAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        manager.sync(task("expected", executeAt, 1L));
        manager.sync(task("orphan", executeAt, 2L));

        manager.reconcile(
                "test-group",
                "test-owner",
                consumer -> consumer.accept(task("expected", executeAt, 1L)));

        assertThat(scheduler.checkExists(JobKey.jobKey("expected", "test-group"))).isTrue();
        assertThat(scheduler.checkExists(JobKey.jobKey("orphan", "test-group"))).isFalse();
    }

    @Test
    void shouldReconcileScannedTasksAndDeleteOrphans() throws Exception {
        Instant executeAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS);
        manager.sync(task("orphan", executeAt, 1L));
        var expected = task("expected", executeAt, 2L);

        manager.reconcile("test-group", "test-owner", consumer -> consumer.accept(expected));

        assertThat(scheduler.checkExists(JobKey.jobKey("expected", "test-group"))).isTrue();
        assertThat(scheduler.checkExists(JobKey.jobKey("orphan", "test-group"))).isFalse();
    }

    @Test
    void shouldRejectAnotherOwnerForRegisteredGroup() {
        manager.registerGroup("shared-group", "first-owner");

        assertThatThrownBy(() -> manager.registerGroup("shared-group", "second-owner"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shared-group")
                .hasMessageContaining("first-owner");
    }

    @Test
    void shouldRejectReconciliationBeforeGroupRegistration() {
        assertThatThrownBy(
                        () -> manager.reconcile("unregistered-group", "test-owner", consumer -> {}))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unregistered-group");
    }

    private QuartzOneShotTask task(String taskName, Instant executeAt, Long id) {
        return new QuartzOneShotTask(
                "test-group", taskName, TestJob.class, Map.of("id", id), executeAt, "test");
    }

    public static class TestJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {}
    }
}
