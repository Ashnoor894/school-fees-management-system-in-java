# School Fee Management System
**CSE2003 | Object Oriented Programming using JAVA | B.Tech CSE Semester 3 | Mini Project**

---

### **Group 15 - Project Team**
| Role | Name | UID / Registration Number |
| :--- | :--- | :--- |
| **Leader** | **Sugandh** | `2510001376` |
| **Member 1** | **Ashnoor** | `2510001384` |
| **Member 2** | **Sahaz** | `2510001385` |
| **Member 3** | **Bharat** | `2510001356` |

---

## 📌 Project Overview
The **School Fee Management System** is a standalone desktop application built **strictly using 100% Pure Java SE (no JSON, no external libraries or JAR files)** to record student fee structures, collect installment payments, generate formatted receipts, track overdue pending dues, and produce class-wise analytical collection reports.

The project strictly implements all concepts specified across **Units 1 to 5** of the syllabus.

---

## 🏗️ 5 Core Modules Implemented

1. **Student Fee Structure (Module 1)**:
   - Form-based registration for new students (`Student`) with roll numbers, names, classes, contacts, and emails.
   - Dynamic breakdown calculation for **Tuition Fee** (with lab and library charges minus scholarship concessions), **Transport Fee** (with route distance and per-km slab rates), and **Exam Fee** (with subject counts and practical charges).
   - Searchable student ledger rendered via `PreparedStatement`.

2. **Fee Payment (Module 2)**:
   - Select student account and inspect real-time financial ledger (Total Scheduled Fee, Total Paid to Date, Outstanding Pending Balance).
   - Deposit fee installments (Tuition, Transport, Exam, or Lump-Sum).
   - Input validation: Negative amounts or amounts exceeding outstanding dues immediately throw `InvalidAmountException`.
   - Payment execution runs stored procedure logic via JDBC `CallableStatement`.
   - Automatically opens the generated official receipt upon payment confirmation.

3. **Receipt Generation (Module 3)**:
   - Authentic, official fee receipts constructed using `java.lang.StringBuffer`.
   - Incorporates `java.lang.Math` methods for token authentication code generation, currency rounding, and balance clamping.
   - Dual split-view: tabular receipts history and instant preview with one-click export to `.txt` files.

4. **Pending Dues List (Module 4)**:
   - Displays all students with outstanding fee balances via `CallableStatement` (`sp_get_pending_dues`).
   - Filter by class/grade.
   - Multi-threaded background service (`DueReminderThread`) that operates as a daemon, scanning for overdue fees and dispatching simulated notifications with a live UI feed.

5. **Class-Wise Collection Report (Module 5)**:
   - Aggregated executive metrics: Total Students, Total Scheduled Fees, Total Collections, Total Overdue Dues, and Collection Rate (%).
   - Class-by-class financial breakdown executed via `CallableStatement` (`sp_class_collection_report`).
   - One-click report summary export to `.txt`.

---

## 🎓 Syllabus Concepts Applied (Units 1 to 5)

| Syllabus Unit | Required Concepts | Implementation in This Project |
| :--- | :--- | :--- |
| **Unit 1** | **Classes `Student`, `Fee`, `Receipt`; `Math` class; `StringBuffer` for receipts** | • `fees.model.Student`: Model for student biodata.<br>• `fees.model.Fee`: Abstract base fee with `roundAmount()` and `calculateLateFine()`.<br>• `fees.model.Receipt`: Generated strictly using `StringBuffer.append()`.<br>• `java.lang.Math`: `Math.round()`, `Math.min()`, `Math.max()`, and `Math.random()`. |
| **Unit 2** | **Inheritance `Fee` -> `TuitionFee` / `TransportFee` / `ExamFee`; interface `Payable`; package `fees.model`** | • Package: `fees.model`<br>• Interface: `Payable` (defines `calculateTotalFee()`, `processPayment()`, `getPendingDue()`)<br>• `Fee` implements `Payable`<br>• `TuitionFee extends Fee`<br>• `TransportFee extends Fee`<br>• `ExamFee extends Fee` |
| **Unit 3** | **`InvalidAmountException`; thread for due reminders** | • Custom Exception: `fees.exception.InvalidAmountException` thrown on negative/zero or excess payments.<br>• Multi-threading: `fees.thread.DueReminderThread extends Thread` runs as a daemon service periodically checking for pending dues. |
| **Unit 4** | **JDBC `PreparedStatement` and `CallableStatement`; Database Tables `students`, `fees`, `receipts`** | • Database Tables: `students`, `fees`, `receipts`<br>• `PreparedStatement`: parameterized INSERT, UPDATE, DELETE, and SELECT searches in `FeeDAO`.<br>• `CallableStatement`: executes stored procedures (`sp_record_fee_payment`, `sp_get_pending_dues`, `sp_class_collection_report`).<br>• Runs on a pure Java JDBC in-memory engine out of the box with zero external database server setup. |
| **Unit 5** | **Swing `BorderLayout`, `GridLayout` forms; `ActionListener`** | • Layouts: `BorderLayout` across main window, cards, and tab panels; `GridLayout` for structured input forms, button bars, and financial stat cards.<br>• Event Handling: `ActionListener` attached to all form buttons, searches, filters, and menu items. |

---

## 🗄️ Database Tables Specification

1. **`students` Table**:
   - `student_id` (VARCHAR) - Primary Key
   - `roll_no` (VARCHAR)
   - `name` (VARCHAR)
   - `class_grade` (VARCHAR)
   - `contact` (VARCHAR)
   - `email` (VARCHAR)
   - `admission_date` (VARCHAR)

2. **`fees` Table**:
   - `fee_id` (INT) - Primary Key
   - `student_id` (VARCHAR) - Foreign Key -> `students(student_id)`
   - `tuition_fee` (DOUBLE)
   - `transport_fee` (DOUBLE)
   - `exam_fee` (DOUBLE)
   - `total_fee` (DOUBLE)
   - `paid_amount` (DOUBLE)
   - `pending_amount` (DOUBLE)
   - `last_payment_date` (VARCHAR)

3. **`receipts` Table**:
   - `receipt_id` (INT) - Primary Key
   - `receipt_no` (VARCHAR)
   - `student_id` (VARCHAR) - Foreign Key -> `students(student_id)`
   - `student_name` (VARCHAR)
   - `amount_paid` (DOUBLE)
   - `payment_type` (VARCHAR)
   - `payment_date` (VARCHAR)
   - `payment_mode` (VARCHAR)
   - `remarks` (VARCHAR)
   - `receipt_text` (TEXT)

---

## 📁 Source Code Structure

```text
java project/
│
├── compile.bat                        # One-click Windows compilation script
├── run.bat                            # One-click application launcher
├── README.md                          # Project documentation and guide
│
├── bin/                               # Compiled Java class files (.class)
│
└── src/
    └── fees/
        ├── Main.java                  # Main application launcher
        ├── TestAllConcepts.java       # Automated verification test suite
        │
        ├── model/                     # Unit 1 & Unit 2: OOP & Data Models
        │   ├── Payable.java           # Unit 2: Interface Payable
        │   ├── Fee.java               # Unit 1 & 2: Abstract base class Fee (Math class)
        │   ├── TuitionFee.java        # Unit 2: Inheritance Fee -> TuitionFee
        │   ├── TransportFee.java      # Unit 2: Inheritance Fee -> TransportFee
        │   ├── ExamFee.java           # Unit 2: Inheritance Fee -> ExamFee
        │   ├── Student.java           # Unit 1: Class Student
        │   └── Receipt.java           # Unit 1: Class Receipt (StringBuffer)
        │
        ├── exception/                 # Unit 3: Custom Exceptions
        │   └── InvalidAmountException.java
        │
        ├── thread/                    # Unit 3: Multi-threading
        │   └── DueReminderThread.java # Background thread for overdue reminders
        │
        ├── db/                        # Unit 4: JDBC Database Layer
        │   ├── DBConnection.java      # JDBC Connection Manager
        │   ├── PureJavaJDBC.java      # Pure Java in-memory JDBC Engine
        │   └── FeeDAO.java            # PreparedStatement & CallableStatement DAO
        │
        └── ui/                        # Unit 5: Swing GUI & Event Handling
            ├── MainFrame.java         # Main Application Window (BorderLayout)
            ├── StudentFeePanel.java   # Module 1: Student Fee Structure (GridLayout)
            ├── FeePaymentPanel.java   # Module 2: Fee Payment (GridLayout form)
            ├── ReceiptPanel.java      # Module 3: Receipts History & StringBuffer View
            ├── PendingDuesPanel.java  # Module 4: Pending Dues & Reminder Thread
            ├── ClassReportPanel.java  # Module 5: Class-Wise Collection Report
            └── ReceiptDialog.java     # Pop-up receipt display & file export
```

---

## 🚀 How to Run (Dead Simple)

### 🌟 Easiest Method (One-Click Launch):
Simply **double-click** either of these right from File Explorer:
1. **`SchoolFeeManagementSystem.jar`** *(Direct executable Java application - launches immediately with no terminal window)*
2. **`run.bat`** *(Auto-compiles if needed and launches the GUI instantly)*

---

### Command Line Method:
```powershell
# Run using the standalone executable JAR:
java -jar SchoolFeeManagementSystem.jar

# Or run automated concept verification tests:
java -cp SchoolFeeManagementSystem.jar fees.TestAllConcepts
```
