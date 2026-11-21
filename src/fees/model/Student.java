package fees.model;

/**
 * Unit 1 Syllabus Concept: Class Student
 * Represents student bio-data and academic registration.
 * Corresponds to Database Table: students
 */
public class Student {

    private String studentId;     // e.g., UID: "2510001376"
    private String rollNo;        // e.g., "R-101"
    private String name;          // e.g., "Sugandh"
    private String classGrade;    // e.g., "Class 12A"
    private String contact;       // e.g., "9876543210"
    private String email;         // e.g., "sugandh@school.edu"
    private String admissionDate; // e.g., "2026-06-15"

    public Student() {
    }

    public Student(String studentId, String rollNo, String name, String classGrade,
                   String contact, String email, String admissionDate) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.name = name;
        this.classGrade = classGrade;
        this.contact = contact;
        this.email = email;
        this.admissionDate = admissionDate;
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClassGrade() { return classGrade; }
    public void setClassGrade(String classGrade) { this.classGrade = classGrade; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

    @Override
    public String toString() {
        return name + " (" + studentId + " - " + classGrade + ")";
    }
}
