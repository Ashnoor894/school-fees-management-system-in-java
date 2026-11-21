package fees.db;

import fees.model.Fee;
import fees.model.Receipt;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PureJavaJDBC - Pure Java in-memory JDBC Engine
 * Unit 4 Syllabus Concept: JDBC PreparedStatement and CallableStatement
 * Database Tables: students, fees, receipts
 * 
 * Provides full JDBC Connection, PreparedStatement, and CallableStatement support
 * using standard Java SE interfaces (zero external JARs, zero configuration required).
 */
public class PureJavaJDBC {

    // In-memory Database Tables
    private static final List<Map<String, Object>> studentsTable = new CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> feesTable = new CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> receiptsTable = new CopyOnWriteArrayList<>();

    private static final AtomicInteger feeIdCounter = new AtomicInteger(100);
    private static final AtomicInteger receiptIdCounter = new AtomicInteger(500);

    private static volatile boolean initialized = false;

    public static synchronized void initializeDefaults() {
        if (initialized) return;

        // Pre-load Group 15 Students & Demo Data
        
        // Ashnoor - Member 1 (UID: 2510001384)
        insertStudentRaw("2510001384", "R-102", "Ashnoor", "Class 12A", "9812345678", "ashnoor@school.edu", "2026-06-16");
        insertFeeRaw("2510001384", 45000.0, 10000.0, 3000.0, 58000.0, 35000.0, 23000.0, "2026-08-10");

        // Sahaz - Member 2 (UID: 2510001385)
        insertStudentRaw("2510001385", "R-103", "Sahaz", "Class 12B", "9711223344", "sahaz@school.edu", "2026-06-17");
        insertFeeRaw("2510001385", 42000.0, 9000.0, 3000.0, 54000.0, 30000.0, 24000.0, "2026-08-15");

        // Ram - Member 3 (UID: 2510001396)
        insertStudentRaw("2510001396", "R-104", "Ram", "Class 11A", "9988776655", "ram@school.edu", "2026-06-18");
        insertFeeRaw("2510001396", 38000.0, 0.0, 2500.0, 40500.0, 40500.0, 0.0, "2026-07-12");

        // Additional Classmates for diverse class reporting & dues
        insertStudentRaw("2510001401", "R-105", "Aarav Sharma", "Class 11A", "9811002233", "aarav@school.edu", "2026-06-19");
        insertFeeRaw("2510001401", 38000.0, 8000.0, 2500.0, 48500.0, 20000.0, 28500.0, "2026-08-01");

        insertStudentRaw("2510001402", "R-106", "Priya Patel", "Class 10B", "9822334455", "priya@school.edu", "2026-06-20");
        insertFeeRaw("2510001402", 35000.0, 6000.0, 2000.0, 43000.0, 25000.0, 18000.0, "2026-07-25");

        // Initial sample receipts
        try {
            recordPaymentInternal("2510001376", 60000.0, "Lump-Sum", "NetBanking", "Full academic year fee payment cleared.");
            recordPaymentInternal("2510001384", 35000.0, "Tuition Fee", "Card", "First term tuition payment deposited.");
            recordPaymentInternal("2510001385", 30000.0, "Tuition Fee", "UPI", "Semester 1 installment received.");
            recordPaymentInternal("2510001396", 40500.0, "Lump-Sum", "Cash", "Annual fee deposited via bank counter.");
        } catch (SQLException ignored) {}

        initialized = true;
    }

    private static void insertStudentRaw(String id, String roll, String name, String grade, String contact, String email, String date) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("student_id", id);
        row.put("roll_no", roll);
        row.put("name", name);
        row.put("class_grade", grade);
        row.put("contact", contact);
        row.put("email", email);
        row.put("admission_date", date);
        studentsTable.add(row);
    }

    private static void insertFeeRaw(String studentId, double tuition, double transport, double exam,
                                     double total, double paid, double pending, String lastDate) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("fee_id", feeIdCounter.incrementAndGet());
        row.put("student_id", studentId);
        row.put("tuition_fee", Fee.roundAmount(tuition));
        row.put("transport_fee", Fee.roundAmount(transport));
        row.put("exam_fee", Fee.roundAmount(exam));
        row.put("total_fee", Fee.roundAmount(total));
        row.put("paid_amount", Fee.roundAmount(paid));
        row.put("pending_amount", Fee.roundAmount(pending));
        row.put("last_payment_date", lastDate);
        feesTable.add(row);
    }

    /**
     * Creates a new JDBC Connection proxy.
     */
    public static Connection createConnection() {
        initializeDefaults();
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new ConnectionHandler()
        );
    }

    private static class ConnectionHandler implements InvocationHandler {
        private boolean autoCommit = true;
        private boolean closed = false;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                closed = true;
                return null;
            }
            if ("isClosed".equals(name)) {
                return closed;
            }
            if ("setAutoCommit".equals(name)) {
                this.autoCommit = (Boolean) args[0];
                return null;
            }
            if ("getAutoCommit".equals(name)) {
                return autoCommit;
            }
            if ("commit".equals(name) || "rollback".equals(name)) {
                return null;
            }
            if ("prepareStatement".equals(name)) {
                String sql = (String) args[0];
                return createPreparedStatement(sql);
            }
            if ("prepareCall".equals(name)) {
                String callSql = (String) args[0];
                return createCallableStatement(callSql);
            }
            if ("createStatement".equals(name)) {
                return createStatement();
            }
            return null;
        }
    }

    private static Statement createStatement() {
        return (Statement) Proxy.newProxyInstance(
                Statement.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("execute".equals(name) || "executeUpdate".equals(name)) {
                        return 1;
                    }
                    if ("executeQuery".equals(name)) {
                        return createResultSet(new ArrayList<>());
                    }
                    if ("close".equals(name)) return null;
                    return null;
                }
        );
    }

    /**
     * Unit 4 Syllabus: PreparedStatement Proxy Implementation
     */
    public static PreparedStatement createPreparedStatement(String sql) {
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                new PreparedStatementHandler(sql)
        );
    }

    private static class PreparedStatementHandler implements InvocationHandler {
        private final String sql;
        private final Map<Integer, Object> params = new ConcurrentHashMap<>();

        public PreparedStatementHandler(String sql) {
            this.sql = sql;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            // Parameter Setters
            if (name.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer) {
                int index = (Integer) args[0];
                params.put(index, args[1]);
                return null;
            }

            if ("close".equals(name)) {
                return null;
            }

            if ("executeUpdate".equals(name)) {
                return executeUpdateInternal(sql, params);
            }

            if ("executeQuery".equals(name)) {
                return executeQueryInternal(sql, params);
            }

            return null;
        }
    }

    /**
     * Unit 4 Syllabus: CallableStatement Proxy Implementation
     */
    public static CallableStatement createCallableStatement(String callSql) {
        return (CallableStatement) Proxy.newProxyInstance(
                CallableStatement.class.getClassLoader(),
                new Class<?>[]{CallableStatement.class},
                new CallableStatementHandler(callSql)
        );
    }

    private static class CallableStatementHandler implements InvocationHandler {
        private final String callSql;
        private final Map<Integer, Object> inParams = new ConcurrentHashMap<>();
        private final Map<Integer, Object> outParams = new ConcurrentHashMap<>();
        private List<Map<String, Object>> callableResultList = null;

        public CallableStatementHandler(String callSql) {
            this.callSql = callSql;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            if (name.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer) {
                int index = (Integer) args[0];
                inParams.put(index, args[1]);
                return null;
            }

            if ("registerOutParameter".equals(name) && args != null && args.length >= 2 && args[0] instanceof Integer) {
                int index = (Integer) args[0];
                outParams.put(index, "");
                return null;
            }

            if ("close".equals(name)) {
                return null;
            }

            if ("execute".equals(name)) {
                executeCallableInternal();
                return true;
            }

            if ("executeQuery".equals(name)) {
                executeCallableInternal();
                return createResultSet(callableResultList != null ? callableResultList : new ArrayList<>());
            }

            if ("getString".equals(name) && args != null && args.length == 1 && args[0] instanceof Integer) {
                int index = (Integer) args[0];
                return outParams.get(index);
            }

            return null;
        }

        private void executeCallableInternal() throws SQLException {
            if (callSql.contains("sp_record_fee_payment")) {
                // Determine parameter indices based on whether function return placeholder {? = call ...} was used
                boolean hasReturnPlaceholder = callSql.trim().startsWith("{?");
                int offset = hasReturnPlaceholder ? 1 : 0;

                String studentId = (String) inParams.get(1 + offset);
                Number amtNum = (Number) inParams.get(2 + offset);
                double amount = amtNum != null ? amtNum.doubleValue() : 0.0;
                String paymentType = (String) inParams.get(3 + offset);
                String paymentMode = (String) inParams.get(4 + offset);
                String remarks = (String) inParams.get(5 + offset);

                String recNo = recordPaymentInternal(studentId, amount, paymentType, paymentMode, remarks);
                if (hasReturnPlaceholder) {
                    outParams.put(1, recNo);
                } else {
                    outParams.put(1, recNo); // Support 1st index lookup
                }
            } else if (callSql.contains("sp_get_pending_dues")) {
                callableResultList = getPendingDuesInternal();
            } else if (callSql.contains("sp_class_collection_report")) {
                callableResultList = getClassCollectionReportInternal();
            }
        }
    }

    // =========================================================================
    // SQL ENGINE EXECUTION (PREPAREDSTATEMENT & CALLABLESTATEMENT IMPLEMENTATION)
    // =========================================================================

    private static int executeUpdateInternal(String sql, Map<Integer, Object> params) throws SQLException {
        String cleanSql = sql.trim().replaceAll("\\s+", " ");
        String upper = cleanSql.toUpperCase();

        if (upper.startsWith("INSERT INTO STUDENTS")) {
            // INSERT INTO students (student_id, roll_no, name, class_grade, contact, email, admission_date) VALUES (?, ?, ?, ?, ?, ?, ?)
            String id = String.valueOf(params.get(1));
            // Check uniqueness
            for (Map<String, Object> s : studentsTable) {
                if (id.equalsIgnoreCase(String.valueOf(s.get("student_id")))) {
                    throw new SQLException("Student with ID " + id + " already exists.");
                }
            }
            insertStudentRaw(
                    id,
                    String.valueOf(params.get(2)),
                    String.valueOf(params.get(3)),
                    String.valueOf(params.get(4)),
                    String.valueOf(params.get(5)),
                    String.valueOf(params.get(6)),
                    String.valueOf(params.get(7))
            );
            return 1;
        }

        if (upper.startsWith("INSERT INTO FEES")) {
            // INSERT INTO fees (student_id, tuition_fee, transport_fee, exam_fee, total_fee, paid_amount, pending_amount, last_payment_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            insertFeeRaw(
                    String.valueOf(params.get(1)),
                    ((Number) params.get(2)).doubleValue(),
                    ((Number) params.get(3)).doubleValue(),
                    ((Number) params.get(4)).doubleValue(),
                    ((Number) params.get(5)).doubleValue(),
                    ((Number) params.get(6)).doubleValue(),
                    ((Number) params.get(7)).doubleValue(),
                    String.valueOf(params.get(8))
            );
            return 1;
        }

        if (upper.startsWith("UPDATE STUDENTS SET")) {
            // UPDATE students SET roll_no = ?, name = ?, class_grade = ?, contact = ?, email = ? WHERE student_id = ?
            String roll = String.valueOf(params.get(1));
            String name = String.valueOf(params.get(2));
            String grade = String.valueOf(params.get(3));
            String contact = String.valueOf(params.get(4));
            String email = String.valueOf(params.get(5));
            String id = String.valueOf(params.get(6));

            for (Map<String, Object> s : studentsTable) {
                if (id.equalsIgnoreCase(String.valueOf(s.get("student_id")))) {
                    s.put("roll_no", roll);
                    s.put("name", name);
                    s.put("class_grade", grade);
                    s.put("contact", contact);
                    s.put("email", email);
                    return 1;
                }
            }
            return 0;
        }

        if (upper.startsWith("UPDATE FEES SET")) {
            // UPDATE fees SET paid_amount = ?, pending_amount = ?, last_payment_date = ? WHERE student_id = ?
            double paid = ((Number) params.get(1)).doubleValue();
            double pending = ((Number) params.get(2)).doubleValue();
            String date = String.valueOf(params.get(3));
            String id = String.valueOf(params.get(4));

            for (Map<String, Object> f : feesTable) {
                if (id.equalsIgnoreCase(String.valueOf(f.get("student_id")))) {
                    f.put("paid_amount", Fee.roundAmount(paid));
                    f.put("pending_amount", Fee.roundAmount(pending));
                    f.put("last_payment_date", date);
                    return 1;
                }
            }
            return 0;
        }

        if (upper.startsWith("DELETE FROM STUDENTS")) {
            // DELETE FROM students WHERE student_id = ?
            String id = String.valueOf(params.get(1));
            studentsTable.removeIf(s -> id.equalsIgnoreCase(String.valueOf(s.get("student_id"))));
            feesTable.removeIf(f -> id.equalsIgnoreCase(String.valueOf(f.get("student_id"))));
            receiptsTable.removeIf(r -> id.equalsIgnoreCase(String.valueOf(r.get("student_id"))));
            return 1;
        }

        return 1;
    }

    private static ResultSet executeQueryInternal(String sql, Map<Integer, Object> params) throws SQLException {
        String cleanSql = sql.trim().replaceAll("\\s+", " ");
        String upper = cleanSql.toUpperCase();

        List<Map<String, Object>> resultRows = new ArrayList<>();

        if (upper.contains("FROM STUDENTS") && upper.contains("LEFT JOIN FEES")) {
            if (upper.contains("WHERE S.STUDENT_ID = ?")) {
                String id = String.valueOf(params.get(1));
                for (Map<String, Object> s : studentsTable) {
                    if (id.equalsIgnoreCase(String.valueOf(s.get("student_id")))) {
                        resultRows.add(joinStudentAndFee(s));
                        break;
                    }
                }
            } else if (upper.contains("LIKE ?")) {
                String pattern = String.valueOf(params.get(1)).replace("%", "").toLowerCase();
                for (Map<String, Object> s : studentsTable) {
                    String id = String.valueOf(s.get("student_id")).toLowerCase();
                    String name = String.valueOf(s.get("name")).toLowerCase();
                    String grade = String.valueOf(s.get("class_grade")).toLowerCase();
                    String roll = String.valueOf(s.get("roll_no")).toLowerCase();
                    if (id.contains(pattern) || name.contains(pattern) || grade.contains(pattern) || roll.contains(pattern)) {
                        resultRows.add(joinStudentAndFee(s));
                    }
                }
            } else {
                // All students with fees
                for (Map<String, Object> s : studentsTable) {
                    resultRows.add(joinStudentAndFee(s));
                }
            }
        } else if (upper.contains("FROM RECEIPTS")) {
            if (upper.contains("WHERE RECEIPT_NO = ?")) {
                String recNo = String.valueOf(params.get(1));
                for (Map<String, Object> r : receiptsTable) {
                    if (recNo.equalsIgnoreCase(String.valueOf(r.get("receipt_no")))) {
                        resultRows.add(new LinkedHashMap<>(r));
                        break;
                    }
                }
            } else {
                // All receipts (most recent first)
                for (int i = receiptsTable.size() - 1; i >= 0; i--) {
                    resultRows.add(new LinkedHashMap<>(receiptsTable.get(i)));
                }
            }
        }

        return createResultSet(resultRows);
    }

    private static Map<String, Object> joinStudentAndFee(Map<String, Object> student) {
        Map<String, Object> joined = new LinkedHashMap<>(student);
        String sid = String.valueOf(student.get("student_id"));
        Map<String, Object> matchedFee = null;
        for (Map<String, Object> f : feesTable) {
            if (sid.equalsIgnoreCase(String.valueOf(f.get("student_id")))) {
                matchedFee = f;
                break;
            }
        }
        if (matchedFee != null) {
            joined.putAll(matchedFee);
        } else {
            joined.put("tuition_fee", 0.0);
            joined.put("transport_fee", 0.0);
            joined.put("exam_fee", 0.0);
            joined.put("total_fee", 0.0);
            joined.put("paid_amount", 0.0);
            joined.put("pending_amount", 0.0);
            joined.put("last_payment_date", "None");
        }
        return joined;
    }

    /**
     * Stored procedure implementation: sp_record_fee_payment
     */
    private static synchronized String recordPaymentInternal(String studentId, double amount,
                                                              String paymentType, String paymentMode, String remarks) throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Payment amount must be greater than zero.");
        }

        // Find Student
        String studentName = "Unknown";
        for (Map<String, Object> s : studentsTable) {
            if (studentId.equalsIgnoreCase(String.valueOf(s.get("student_id")))) {
                studentName = String.valueOf(s.get("name"));
                break;
            }
        }

        // Find Fee record
        Map<String, Object> feeRecord = null;
        for (Map<String, Object> f : feesTable) {
            if (studentId.equalsIgnoreCase(String.valueOf(f.get("student_id")))) {
                feeRecord = f;
                break;
            }
        }

        if (feeRecord == null) {
            throw new SQLException("Student ID " + studentId + " does not exist in fee records.");
        }

        double totalFee = ((Number) feeRecord.get("total_fee")).doubleValue();
        double paidAmount = ((Number) feeRecord.get("paid_amount")).doubleValue();
        double pendingAmount = ((Number) feeRecord.get("pending_amount")).doubleValue();

        if (amount > pendingAmount + 0.01) {
            throw new SQLException("Payment amount (" + amount + ") exceeds current pending dues (" + pendingAmount + ").");
        }

        // Unit 1 Math class: round and clamp new values
        double newPaid = Fee.roundAmount(paidAmount + amount);
        double newPending = Math.max(0.0, Fee.roundAmount(totalFee - newPaid));
        String currentDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        feeRecord.put("paid_amount", newPaid);
        feeRecord.put("pending_amount", newPending);
        feeRecord.put("last_payment_date", currentDateStr);

        // Generate receipt number using Math.round & random
        int rId = receiptIdCounter.incrementAndGet();
        long randomSuffix = Math.round(Math.random() * 9000.0) + 1000L;
        String receiptNo = "REC-2026-" + rId + "-" + randomSuffix;

        // Unit 1 Concept: Generate receipt text using StringBuffer
        Receipt receiptObj = new Receipt(rId, receiptNo, studentId, studentName, amount,
                paymentType, currentDateStr, paymentMode, remarks);
        String receiptText = receiptObj.generateReceiptStringBuffer(newPending);

        Map<String, Object> recRow = new LinkedHashMap<>();
        recRow.put("receipt_id", rId);
        recRow.put("receipt_no", receiptNo);
        recRow.put("student_id", studentId);
        recRow.put("student_name", studentName);
        recRow.put("amount_paid", amount);
        recRow.put("payment_type", paymentType);
        recRow.put("payment_date", currentDateStr);
        recRow.put("payment_mode", paymentMode);
        recRow.put("remarks", remarks);
        recRow.put("receipt_text", receiptText);

        receiptsTable.add(recRow);
        return receiptNo;
    }

    /**
     * Stored procedure implementation: sp_get_pending_dues
     */
    private static List<Map<String, Object>> getPendingDuesInternal() {
        List<Map<String, Object>> dues = new ArrayList<>();
        for (Map<String, Object> s : studentsTable) {
            Map<String, Object> joined = joinStudentAndFee(s);
            double pending = ((Number) joined.get("pending_amount")).doubleValue();
            if (pending > 0.0) {
                dues.add(joined);
            }
        }
        return dues;
    }

    /**
     * Stored procedure implementation: sp_class_collection_report
     */
    private static List<Map<String, Object>> getClassCollectionReportInternal() {
        Map<String, int[]> statsByClass = new TreeMap<>(); // class -> [studentCount, total, paid, pending]
        Map<String, double[]> amountsByClass = new TreeMap<>();

        for (Map<String, Object> s : studentsTable) {
            String grade = String.valueOf(s.get("class_grade"));
            Map<String, Object> joined = joinStudentAndFee(s);
            double total = ((Number) joined.get("total_fee")).doubleValue();
            double paid = ((Number) joined.get("paid_amount")).doubleValue();
            double pending = ((Number) joined.get("pending_amount")).doubleValue();

            if (!amountsByClass.containsKey(grade)) {
                amountsByClass.put(grade, new double[]{0.0, 0.0, 0.0, 0.0}); // count, total, paid, pending
            }
            double[] arr = amountsByClass.get(grade);
            arr[0] += 1.0;
            arr[1] = Fee.roundAmount(arr[1] + total);
            arr[2] = Fee.roundAmount(arr[2] + paid);
            arr[3] = Fee.roundAmount(arr[3] + pending);
        }

        List<Map<String, Object>> report = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : amountsByClass.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            double[] v = entry.getValue();
            row.put("class_grade", entry.getKey());
            row.put("student_count", (int) v[0]);
            row.put("total_fee", v[1]);
            row.put("total_collected", v[2]);
            row.put("total_pending", v[3]);
            double pct = v[1] > 0.0 ? Math.round((v[2] / v[1]) * 10000.0) / 100.0 : 0.0;
            row.put("collection_percentage", pct);
            report.add(row);
        }
        return report;
    }

    /**
     * Creates a ResultSet proxy over a list of row maps.
     */
    public static ResultSet createResultSet(List<Map<String, Object>> rows) {
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                new ResultSetHandler(rows)
        );
    }

    private static class ResultSetHandler implements InvocationHandler {
        private final List<Map<String, Object>> rows;
        private int cursor = -1;

        public ResultSetHandler(List<Map<String, Object>> rows) {
            this.rows = rows;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            if ("next".equals(name)) {
                cursor++;
                return cursor < rows.size();
            }

            if ("close".equals(name)) {
                return null;
            }

            if (cursor < 0 || cursor >= rows.size()) {
                return null;
            }

            Map<String, Object> currentRow = rows.get(cursor);

            if ("getString".equals(name) && args != null && args.length == 1) {
                Object col = args[0];
                Object val = col instanceof String ? getCaseInsensitive(currentRow, (String) col) : getByIndex(currentRow, (Integer) col);
                return val != null ? String.valueOf(val) : "";
            }

            if ("getDouble".equals(name) && args != null && args.length == 1) {
                Object col = args[0];
                Object val = col instanceof String ? getCaseInsensitive(currentRow, (String) col) : getByIndex(currentRow, (Integer) col);
                if (val instanceof Number) return ((Number) val).doubleValue();
                if (val != null) {
                    try { return Double.parseDouble(String.valueOf(val)); } catch (Exception ignored) {}
                }
                return 0.0;
            }

            if ("getInt".equals(name) && args != null && args.length == 1) {
                Object col = args[0];
                Object val = col instanceof String ? getCaseInsensitive(currentRow, (String) col) : getByIndex(currentRow, (Integer) col);
                if (val instanceof Number) return ((Number) val).intValue();
                if (val != null) {
                    try { return Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) {}
                }
                return 0;
            }

            if ("getObject".equals(name) && args != null && args.length == 1) {
                Object col = args[0];
                return col instanceof String ? getCaseInsensitive(currentRow, (String) col) : getByIndex(currentRow, (Integer) col);
            }

            return null;
        }

        private Object getCaseInsensitive(Map<String, Object> map, String key) {
            if (map.containsKey(key)) return map.get(key);
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (e.getKey().equalsIgnoreCase(key)) return e.getValue();
            }
            return null;
        }

        private Object getByIndex(Map<String, Object> map, int colIndex) {
            int i = 1;
            for (Object val : map.values()) {
                if (i == colIndex) return val;
                i++;
            }
            return null;
        }
    }
}
