package ch.yoinc.tasks;

import net.dv8tion.jda.api.JDA;

import java.util.*;

/**
 * Manages and schedules all registered ch.yoinc.tasks.
 */
public class TaskScheduler {

    private final List<ScheduledTask> tasks;
    private final Properties properties;

    private JDA jda;

    public TaskScheduler(Properties properties) {
        this.tasks = new ArrayList<>();
        this.properties = properties;

        registerTask(new LeetifyTask());
    }

    /**
     * Register a new task to be scheduled.
     *
     * @param task The task to register
     */
    public void registerTask(ScheduledTask task) {
        tasks.add(task);
    }

    /**
     * Start all registered ch.yoinc.tasks.
     *
     * @param jda The JDA instance
     */
    public void startAllTasks(JDA jda) {
        this.jda = jda;

        for (ScheduledTask task : tasks) {
            startTask(task);
        }
    }

    /**
     * Start a specific task.
     *
     * @param task The task to start
     */
    private void startTask(ScheduledTask task) {
        Timer timer = new Timer("Task-" + task.getTaskName());

        long initialDelay = task.getInitialDelayMs();
        long interval = task.getIntervalMs();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.execute(jda, properties);
            }
        };

        timer.scheduleAtFixedRate(timerTask, initialDelay, interval);
    }
}