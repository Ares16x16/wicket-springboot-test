package com.tutorial.web.pages;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import java.time.Duration;

import com.tutorial.scheduler.LoggingJob;
import com.tutorial.security.AuthenticatedWebPage;
import com.tutorial.session.CustomSession;

public class SchedulerMonitorPage extends AuthenticatedWebPage {
    // Remove or ignore the injected scheduler for logging actions
    // @SpringBean private Scheduler scheduler; 

    private final JobKey jobKey = new JobKey("loggingJob", "group1");
    private final TriggerKey triggerKey = new TriggerKey("loggingTrigger", "group1");
    private Model<String> statusModel = Model.of("Job is not scheduled.");
    private Model<String> timeModel = Model.of(LoggingJob.lastExecutionTime);

    public SchedulerMonitorPage() {
        if (!CustomSession.get().hasRole("ADMIN")) {
            setResponsePage(LoginPage.class);
        }
        
        // Link to go back to Admin page
        add(new Link<Void>("backToAdmin") {
            @Override
            public void onClick() {
                setResponsePage(AdminPage.class);
            }
        });
        
        add(new Label("statusLabel", statusModel));
        
        Label timeLabel = new Label("timeLabel", timeModel);
        timeLabel.setOutputMarkupId(true);
        add(timeLabel);
        
        // Refresh timeLabel’s content every second
        timeLabel.add(new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(1)) {
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                timeModel.setObject(LoggingJob.lastExecutionTime);
            }
        });

        Form<?> form = new Form<>("monitorForm");
        add(form);

        // Updated button: start logging job using factory
        form.add(new Button("startLogging") {
            @Override
            public void onSubmit() {
                try {
                    StdSchedulerFactory factory = new StdSchedulerFactory();
                    Scheduler factoryScheduler = factory.getScheduler();
                    if (!factoryScheduler.checkExists(jobKey)) {
                        JobDetail jobDetail = JobBuilder.newJob(LoggingJob.class)
                                .withIdentity(jobKey)
                                .build();
                        Trigger trigger = TriggerBuilder.newTrigger()
                                .withIdentity(triggerKey)
                                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(1)
                                    .repeatForever())
                                .startNow()
                                .build();
                        factoryScheduler.scheduleJob(jobDetail, trigger);
                        // start the scheduler
                        factoryScheduler.start();
                        statusModel.setObject("Logging job scheduled using factory.");
                    } else {
                        statusModel.setObject("Job already scheduled using factory.");
                    }
                } catch (SchedulerException e) {
                    statusModel.setObject("Error scheduling job using factory.");
                }
            }
        });

        // Updated button: stop logging job using factory
        form.add(new Button("stopLogging") {
            @Override
            public void onSubmit() {
                try {
                    StdSchedulerFactory factory = new StdSchedulerFactory();
                    Scheduler factoryScheduler = factory.getScheduler();
                    if (factoryScheduler.checkExists(jobKey)) {
                        factoryScheduler.deleteJob(jobKey);
                        statusModel.setObject("Logging job stopped using factory.");
                    } else {
                        statusModel.setObject("No scheduling job to stop using factory.");
                    }
                } catch (SchedulerException e) {
                    statusModel.setObject("Error stopping job using factory.");
                }
            }
        });
    }
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forUrl("css/schedulermonitor-styles.css"));
    }
}
