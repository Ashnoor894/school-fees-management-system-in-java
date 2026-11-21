package fees.db;

import fees.exception.InvalidAmountException;
import fees.model.*;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object (DAO) for School Fee Management System
 * Unit 4 Syllabus Concept: JDBC PreparedStatement and CallableStatement
 * Database Tables: students, fees, receipts
 */
public class FeeDAO {

    private static FeeDAO instance;

    private FeeDAO() {
    }

    public static synchronized FeeDAO getInstance() {
        if (instance == null) {
            instance = new FeeDAO();
        }
        return instance;
    }

    /* =========================================================================
     * SECTION A: JDBC PreparedStatement OPERATIONS (Unit 4 Syllabus Concept)
     * ========================================================================= */

    /**
     * Inserts a new student and their calculated fee structure using PreparedStatement.
     */
    public boolean addStudentWithFeeStructure(Student student, TuitionFee tuition, TransportFee transport, ExamFee exam) throws SQLException {
        Connection conn = DBConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();

        String insertStudentSQL = "INSERT INTO students (student_id, roll_no, name, class_grade, contact, email, admission_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertFeeSQL = "INSERT INTO fees (student_id, tuition_fee, transport_fee, exam_fee, total_fee, paid_amount, pending_amount, last_payment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            // 1. Insert Student using PreparedStatement
            try (PreparedStatement pstmtStudent = conn.prepareStatement(insertStudentSQL)) {
                pstmtStudent.setString(1, student.getStudentId());
                pstmtStudent.setString(2, student.getRollNo());
                pstmtStudent.setString(3, student.getName());
                pstmtStudent.setString(4, student.getClassGrade());
                pstmtStudent.setString(5, student.getContact());
                pstmtStudent.setString(6, student.getEmail());
                pstmtStudent.setString(7, student.getAdmissionDate());
                pstmtStudent.executeUpdate();
            }

            // 2. Insert Fee structure using PreparedStatement
            double tuitionAmt = tuition != null ? tuition.calculateTotalFee() : 0.0;
            double transportAmt = transport != null ? transport.calculateTotalFee() : 0.0;
            double examAmt = exam != null ? exam.calculateTotalFee() : 0.0;
            double totalAmt = Fee.roundAmount(tuitionAmt + transportAmt + examAmt);
            double paidAmt = 0.0;
            double pendingAmt = totalAmt;

            try (PreparedStatement pstmtFee = conn.prepareStatement(insertFeeSQL)) {
                pstmtFee.setString(1, student.getStudentId());
                pstmtFee.setDouble(2, tuitionAmt);
                pstmtFee.setDouble(3, transportAmt);
                pstmtFee.setDouble(4, examAmt);
                pstmtFee.setDouble(5, totalAmt);
                pstmtFee.setDouble(6, paidAmt);
                pstmtFee.setDouble(7, pendingAmt);
                pstmtFee.setString(8, "None");
                pstmtFee.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * Updates an existing student record using PreparedStatement.
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, name = ?, class_grade = ?, contact = ?, email = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getRollNo());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getClassGrade());
            pstmt.setString(4, student.getContact());
            pstmt.setString(5, student.getEmail());
            pstmt.setString(6, student.getStudentId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a student and associated fee records using PreparedStatement.
     */
    public boolean deleteStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all student records with fee status using PreparedStatement.
     */
    public List<Map<String, Object>> getAllStudentsWithFees() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.roll_no, s.name, s.class_grade, s.contact, s.email, s.admission_date, " +
                "f.tuition_fee, f.transport_fee, f.exam_fee, f.total_fee, f.paid_amount, f.pending_amount, f.last_payment_date " +
                "FROM students s LEFT JOIN fees f ON s.student_id = f.student_id " +
                "ORDER BY s.class_grade, s.roll_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("student_id", rs.getString("student_id"));
                map.put("roll_no", rs.getString("roll_no"));
                map.put("name", rs.getString("name"));
                map.put("class_grade", rs.getString("class_grade"));
                map.put("contact", rs.getString("contact"));
                map.put("email", rs.getString("email"));
                map.put("admission_date", rs.getString("admission_date"));
                map.put("tuition_fee", rs.getDouble("tuition_fee"));
                map.put("transport_fee", rs.getDouble("transport_fee"));
                map.put("exam_fee", rs.getDouble("exam_fee"));
                map.put("total_fee", rs.getDouble("total_fee"));
                map.put("paid_amount", rs.getDouble("paid_amount"));
                map.put("pending_amount", rs.getDouble("pending_amount"));
                map.put("last_payment_date", rs.getString("last_payment_date"));
                list.add(map);
            }
        }
        return list;
    }

    /**
     * Searches students by keyword across name, ID, roll, class using PreparedStatement.
     */
    public List<Map<String, Object>> searchStudents(String keyword) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.roll_no, s.name, s.class_grade, s.contact, s.email, s.admission_date, " +
                "f.tuition_fee, f.transport_fee, f.exam_fee, f.total_fee, f.paid_amount, f.pending_amount, f.last_payment_date " +
                "FROM students s LEFT JOIN fees f ON s.student_id = f.student_id " +
                "WHERE LOWER(s.student_id) LIKE ? OR LOWER(s.name) LIKE ? OR LOWER(s.class_grade) LIKE ? OR LOWER(s.roll_no) LIKE ? " +
                "ORDER BY s.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + (keyword != null ? keyword.trim().toLowerCase() : "") + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("student_id", rs.getString("student_id"));
                    map.put("roll_no", rs.getString("roll_no"));
                    map.put("name", rs.getString("name"));
                    map.put("class_grade", rs.getString("class_grade"));
                    map.put("contact", rs.getString("contact"));
                    map.put("email", rs.getString("email"));
                    map.put("admission_date", rs.getString("admission_date"));
                    map.put("tuition_fee", rs.getDouble("tuition_fee"));
                    map.put("transport_fee", rs.getDouble("transport_fee"));
                    map.put("exam_fee", rs.getDouble("exam_fee"));
                    map.put("total_fee", rs.getDouble("total_fee"));
                    map.put("paid_amount", rs.getDouble("paid_amount"));
                    map.put("pending_amount", rs.getDouble("pending_amount"));
                    map.put("last_payment_date", rs.getString("last_payment_date"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * Gets fee details for a specific student using PreparedStatement.
     */
    public Map<String, Object> getStudentFeeDetails(String studentId) throws SQLException {
        String sql = "SELECT s.student_id, s.roll_no, s.name, s.class_grade, s.contact, s.email, " +
                "f.tuition_fee, f.transport_fee, f.exam_fee, f.total_fee, f.paid_amount, f.pending_amount, f.last_payment_date " +
                "FROM students s LEFT JOIN fees f ON s.student_id = f.student_id " +
                "WHERE s.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("student_id", rs.getString("student_id"));
                    map.put("roll_no", rs.getString("roll_no"));
                    map.put("name", rs.getString("name"));
                    map.put("class_grade", rs.getString("class_grade"));
                    map.put("contact", rs.getString("contact"));
                    map.put("email", rs.getString("email"));
                    map.put("tuition_fee", rs.getDouble("tuition_fee"));
                    map.put("transport_fee", rs.getDouble("transport_fee"));
                    map.put("exam_fee", rs.getDouble("exam_fee"));
                    map.put("total_fee", rs.getDouble("total_fee"));
                    map.put("paid_amount", rs.getDouble("paid_amount"));
                    map.put("pending_amount", rs.getDouble("pending_amount"));
                    map.put("last_payment_date", rs.getString("last_payment_date"));
                    return map;
                }
            }
        }
        return null;
    }

    /**
     * Gets all receipts using PreparedStatement on receipts table.
     */
    public List<Receipt> getAllReceipts() throws SQLException {
        List<Receipt> list = new ArrayList<>();
        String sql = "SELECT * FROM receipts ORDER BY receipt_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Receipt r = new Receipt();
                r.setReceiptId(rs.getInt("receipt_id"));
                r.setReceiptNo(rs.getString("receipt_no"));
                r.setStudentId(rs.getString("student_id"));
                r.setStudentName(rs.getString("student_name"));
                r.setAmountPaid(rs.getDouble("amount_paid"));
                r.setPaymentType(rs.getString("payment_type"));
                r.setPaymentDate(rs.getString("payment_date"));
                r.setPaymentMode(rs.getString("payment_mode"));
                r.setRemarks(rs.getString("remarks"));
                r.setReceiptText(rs.getString("receipt_text"));
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Gets receipt by receipt number using PreparedStatement.
     */
    public Receipt getReceiptByNo(String receiptNo) throws SQLException {
        String sql = "SELECT * FROM receipts WHERE receipt_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, receiptNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Receipt r = new Receipt();
                    r.setReceiptId(rs.getInt("receipt_id"));
                    r.setReceiptNo(rs.getString("receipt_no"));
                    r.setStudentId(rs.getString("student_id"));
                    r.setStudentName(rs.getString("student_name"));
                    r.setAmountPaid(rs.getDouble("amount_paid"));
                    r.setPaymentType(rs.getString("payment_type"));
                    r.setPaymentDate(rs.getString("payment_date"));
                    r.setPaymentMode(rs.getString("payment_mode"));
                    r.setRemarks(rs.getString("remarks"));
                    r.setReceiptText(rs.getString("receipt_text"));
                    return r;
                }
            }
        }
        return null;
    }

    /* =========================================================================
     * SECTION B: JDBC CallableStatement OPERATIONS (Unit 4 Syllabus Concept)
     * ========================================================================= */

    /**
     * Unit 4 Syllabus: CallableStatement for stored procedure 'sp_record_fee_payment'
     * Records fee payment, recalculates balance, and returns generated Receipt Number.
     * Throws InvalidAmountException if amount is negative, zero, or exceeds pending dues.
     */
    public String recordFeePaymentCallable(String studentId, double amount, String paymentType,
                                          String paymentMode, String remarks) throws SQLException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Payment amount must be greater than zero.", amount);
        }

        Connection conn = DBConnection.getConnection();
        String receiptNo = null;

        // Unit 4 Concept: CallableStatement execution
        String callSQL = "{call sp_record_fee_payment(?, ?, ?, ?, ?)}";
        try (CallableStatement cstmt = conn.prepareCall(callSQL)) {
            cstmt.setString(1, studentId);
            cstmt.setDouble(2, Fee.roundAmount(amount));
            cstmt.setString(3, paymentType);
            cstmt.setString(4, paymentMode);
            cstmt.setString(5, remarks);
            cstmt.registerOutParameter(1, Types.VARCHAR);

            cstmt.execute();
            receiptNo = cstmt.getString(1);
        }

        return receiptNo;
    }

    /**
     * Unit 4 Syllabus: CallableStatement for stored procedure 'sp_get_pending_dues'
     * Retrieves list of all students with outstanding fee balance.
     */
    public List<Map<String, Object>> getPendingDuesListCallable() throws SQLException {
        List<Map<String, Object>> dues = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        // Unit 4 Concept: CallableStatement execution
        String callSQL = "{call sp_get_pending_dues()}";
        try (CallableStatement cstmt = conn.prepareCall(callSQL);
             ResultSet rs = cstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("student_id", rs.getString("student_id"));
                row.put("roll_no", rs.getString("roll_no"));
                row.put("name", rs.getString("name"));
                row.put("class_grade", rs.getString("class_grade"));
                row.put("contact", rs.getString("contact"));
                row.put("email", rs.getString("email"));
                row.put("total_fee", rs.getDouble("total_fee"));
                row.put("paid_amount", rs.getDouble("paid_amount"));
                row.put("pending_amount", rs.getDouble("pending_amount"));
                row.put("last_payment_date", rs.getString("last_payment_date"));
                dues.add(row);
            }
        }
        return dues;
    }

    /**
     * Unit 4 Syllabus: CallableStatement for stored procedure 'sp_class_collection_report'
     * Computes class-wise fee aggregates and collection efficiency percentages.
     */
    public List<Map<String, Object>> getClassCollectionReportCallable() throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        // Unit 4 Concept: CallableStatement execution
        String callSQL = "{call sp_class_collection_report()}";
        try (CallableStatement cstmt = conn.prepareCall(callSQL);
             ResultSet rs = cstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("class_grade", rs.getString("class_grade"));
                row.put("student_count", rs.getInt("student_count"));
                row.put("total_fee", rs.getDouble("total_fee"));
                row.put("total_collected", rs.getDouble("total_collected"));
                row.put("total_pending", rs.getDouble("total_pending"));
                row.put("collection_percentage", rs.getDouble("collection_percentage"));
                report.add(row);
            }
        }
        return report;
    }
}
