package fees.thread;

import fees.db.FeeDAO;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Unit 3 Syllabus Concept: Thread for Due Reminders
 * Background thread that periodically scans database for students with pending fee dues
 * and dispatches reminder notifications.
 */
public class DueReminderThread extends Thread {

    public interface ReminderListener {
        void onReminderDispatched(String timestamp, String studentId, String studentName,
                                  String classGrade, double pendingAmount, String message);
    }

    private final List<ReminderListener> listeners = new ArrayList<>();
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    private int checkIntervalSeconds = 20; // Scans every 20 seconds

    public DueReminderThread() {
        super("DueReminderThread-Worker");
        setDaemon(true); // Daemon thread so JVM exits cleanly when UI window closes
    }

    public void addReminderListener(ReminderListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeReminderListener(ReminderListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void pauseReminders() {
        paused = true;
    }

    public void resumeReminders() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void stopThread() {
        running = false;
        resumeReminders();
        interrupt();
    }

    public void triggerManualCheck() {
        // Run check asynchronously
        new Thread(() -> performDuesScan(true), "ManualDueCheckTrigger").start();
    }

    @Override
    public void run() {
        // Initial delay so UI loads before first reminder scan
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        while (running) {
            synchronized (pauseLock) {
                while (paused && running) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        if (!running) return;
                    }
                }
            }

            performDuesScan(false);

            try {
                Thread.sleep(checkIntervalSeconds * 1000L);
            } catch (InterruptedException e) {
                if (!running) break;
            }
        }
    }

    private void performDuesScan(boolean isManualTrigger) {
        try {
            FeeDAO dao = FeeDAO.getInstance();
            List<Map<String, Object>> dues = dao.getPendingDuesListCallable();

            if (dues == null || dues.isEmpty()) {
                return;
            }

            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

            for (Map<String, Object> record : dues) {
                String id = String.valueOf(record.get("student_id"));
                String name = String.valueOf(record.get("name"));
                String grade = String.valueOf(record.get("class_grade"));
                double pending = 0.0;
                Object pObj = record.get("pending_amount");
                if (pObj instanceof Number) {
                    pending = ((Number) pObj).doubleValue();
                }

                if (pending > 0.0) {
                    String msg = String.format("[REMINDER DISPATCHED] Student %s (%s, %s): Fee due of Rs. %.2f outstanding. SMS/Email alert generated.",
                            name, id, grade, pending);

                    notifyListeners(timeStamp, id, name, grade, pending, msg);

                    // Slight pacing between dispatches
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ignored) {}
                }
            }
        } catch (Exception ex) {
            // Log quietly if database is busy
        }
    }

    private void notifyListeners(String timestamp, String studentId, String studentName,
                                 String classGrade, double pendingAmount, String message) {
        synchronized (listeners) {
            for (ReminderListener l : listeners) {
                try {
                    l.onReminderDispatched(timestamp, studentId, studentName, classGrade, pendingAmount, message);
                } catch (Exception ignored) {}
            }
        }
    }
}
