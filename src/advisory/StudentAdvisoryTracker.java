package advisory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * =========================================================================================
 * ACE IT - STUDENT ADVISORY TRACKER
 * =========================================================================================
 *
 * -----------------------------------------------------------------------------------------
 * TEXTUAL ARCHITECTURE DIAGRAM
 * -----------------------------------------------------------------------------------------
 *
 * +---------------------------------------------------------------------------------------+
 * |                                   PRESENTATION LAYER                                  |
 * |                         (CLI & Student Advisory Tracker Application)                 |
 * +---------------------------------------------------------------------------------------+
 *                                            |
 *                                            v
 * +---------------------------------------------------------------------------------------+
 * |                                   SERVICE LAYER                                       |
 * |  +-----------------------+  +------------------------+  +--------------------------+  |
 * |  | AuthenticationService |  | StudentAdvisorManager  |  | DegreePlanningEngine     |  |
 * |  +-----------------------+  +------------------------+  +--------------------------+  |
 * +---------------------------------------------------------------------------------------+
 *                                            |
 *                                            v
 * +---------------------------------------------------------------------------------------+
 * |                                    DOMAIN MODEL                                       |
 * |  +-------------------+  +-------------------+  +-------------------+  +---------------+  |
 * |  | User (Abstract)   |  | Major             |  | Course            |  | Message       |  |
 * |  +-------------------+  +-------------------+  +-------------------+  +---------------+  |
 * |    ^               ^                                                                  |
 * |    |               |                                                                  |
 * |  +---------+   +---------+                                                            |
 * |  | Student |   | Advisor | (Also Admin)                                               |
 * |  +---------+   +---------+                                                            |
 * +---------------------------------------------------------------------------------------+
 *
 * -----------------------------------------------------------------------------------------
 * UML CLASS DIAGRAM (TEXTUAL REPRESENTATION)
 * -----------------------------------------------------------------------------------------
 *
 *  +---------------------------------------+
 *  | <<enum>> UserRole                     |
 *  +---------------------------------------+
 *  | ADMIN, ADVISOR, STUDENT               |
 *  +---------------------------------------+
 *
 *  +-------------------------------------------------------------+
 *  | <<abstract>> User                                           |
 *  +-------------------------------------------------------------+
 *  | - userId: String                                            |
 *  | - username: String                                          |
 *  | - passwordHash: String                                      |
 *  | - fullName: String                                          |
 *  | - email: String                                             |
 *  | - role: UserRole                                            |
 *  +-------------------------------------------------------------+
 *  | + authenticate(password: String): boolean                   |
 *  | + getters / setters                                         |
 *  +-------------------------------------------------------------+
 *         ^                         ^
 *         |                         |
 *  +---------------+         +---------------+
 *  | Student       |         | Advisor       |
 *  +---------------+         +---------------+
 *  | - major: Major|         | - dept: String|
 *  | - advisor:    |<>-------| - advisees:   |
 *  |   Advisor     | 0..*  1 |   List<Student|
 *  | - completed:  |         +---------------+
 *  |   List<Course>|
 *  | - gpa: double |
 *  +---------------+
 *
 *  +---------------------------------------+         +----------------------------------+
 *  | Major                                 |         | Course                           |
 *  +---------------------------------------+         +----------------------------------+
 *  | - majorCode: String                   |         | - courseCode: String             |
 *  | - title: String                       |         | - title: String                  |
 *  | - requiredCourses: List<Course>       |<>----->*| - credits: int                   |
 *  +---------------------------------------+         | - prerequisites: List<Course>    |
 *                                                    +----------------------------------+
 *
 *  +-------------------------------------------------------------+
 *  | Message                                                     |
 *  +-------------------------------------------------------------+
 *  | - messageId: String                                         |
 *  | - senderUsername: String                                    |
 *  | - recipientUsername: String                                 |
 *  | - content: String                                           |
 *  | - timestamp: LocalDateTime                                  |
 *  +-------------------------------------------------------------+
 *
 *  +-------------------------------------------------------------+
 *  | DegreePlanningEngine                                        |
 *  +-------------------------------------------------------------+
 *  | + generateBestCasePlan(student: Student): List<SemesterPlan>|
 *  +-------------------------------------------------------------+
 *
 * =========================================================================================
 */

public class StudentAdvisoryTracker {

    // --- ENUMS & ROLE-BASED AUTHENTICATION ---

    public enum UserRole {
        ADMIN, ADVISOR, STUDENT
    }

    public static abstract class User {
        private String userId;
        private String username;
        private String password;
        private String fullName;
        private String email;
        private UserRole role;

        public User(String userId, String username, String password, String fullName, String email, UserRole role) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
        }

        public boolean authenticate(String passwordAttempt) {
            return this.password.equals(passwordAttempt);
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public UserRole getRole() { return role; }

        @Override
        public String toString() {
            return String.format("%s [%s] - %s (%s)", fullName, role, username, email);
        }
    }

    public static class AdminUser extends User {
        public AdminUser(String userId, String username, String password, String fullName, String email) {
            super(userId, username, password, fullName, email, UserRole.ADMIN);
        }
    }

    // --- AGGREGATED DOMAIN CLASSES: COURSE & MAJOR ---

    public static class Course {
        private String courseCode;
        private String title;
        private int credits;
        private List<Course> prerequisites;

        public Course(String courseCode, String title, int credits) {
            this.courseCode = courseCode;
            this.title = title;
            this.credits = credits;
            this.prerequisites = new ArrayList<>();
        }

        public void addPrerequisite(Course prereq) {
            this.prerequisites.add(prereq);
        }

        public String getCourseCode() { return courseCode; }
        public String getTitle() { return title; }
        public int getCredits() { return credits; }
        public List<Course> getPrerequisites() { return Collections.unmodifiableList(prerequisites); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Course)) return false;
            Course course = (Course) o;
            return Objects.equals(courseCode, course.courseCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(courseCode);
        }

        @Override
        public String toString() {
            return String.format("%s: %s (%d Credits)", courseCode, title, credits);
        }
    }

    public static class Major {
        private String majorCode;
        private String title;
        private List<Course> requiredCourses;

        public Major(String majorCode, String title) {
            this.majorCode = majorCode;
            this.title = title;
            this.requiredCourses = new ArrayList<>();
        }

        public void addRequiredCourse(Course course) {
            this.requiredCourses.add(course);
        }

        public String getMajorCode() { return majorCode; }
        public String getTitle() { return title; }
        public List<Course> getRequiredCourses() { return Collections.unmodifiableList(requiredCourses); }
    }

    // --- AGGREGATED DOMAIN CLASSES: ADVISOR & STUDENT ---

    public static class Advisor extends User {
        private String department;
        private List<Student> advisees;
        private List<Message> inbox;

        public Advisor(String userId, String username, String password, String fullName, String email, String department) {
            super(userId, username, password, fullName, email, UserRole.ADVISOR);
            this.department = department;
            this.advisees = new ArrayList<>();
            this.inbox = new ArrayList<>();
        }

        public void addAdvisee(Student student) {
            if (!advisees.contains(student)) {
                advisees.add(student);
            }
        }

        public void receiveMessage(Message msg) {
            inbox.add(msg);
        }

        public String getDepartment() { return department; }
        public List<Student> getAdvisees() { return Collections.unmodifiableList(advisees); }
        public List<Message> getInbox() { return Collections.unmodifiableList(inbox); }
    }

    public static class Student extends User {
        private Major major;
        private Advisor advisor;
        private List<Course> completedCourses;
        private double gpa;
        private List<Message> sentMessages;

        public Student(String userId, String username, String password, String fullName, String email, Major major, Advisor advisor) {
            super(userId, username, password, fullName, email, UserRole.STUDENT);
            this.major = major;
            this.advisor = advisor;
            this.completedCourses = new ArrayList<>();
            this.gpa = 0.0;
            this.sentMessages = new ArrayList<>();
            if (advisor != null) {
                advisor.addAdvisee(this);
            }
        }

        public void completeCourse(Course course) {
            if (!completedCourses.contains(course)) {
                completedCourses.add(course);
            }
        }

        public void sendMessageToAdvisor(String content) {
            if (advisor == null) {
                System.out.println("No advisor assigned to student!");
                return;
            }
            Message msg = new Message(UUID.randomUUID().toString(), this.getUsername(), advisor.getUsername(), content);
            sentMessages.add(msg);
            advisor.receiveMessage(msg);
            System.out.println("Message sent successfully from Student " + getFullName() + " to Advisor " + advisor.getFullName());
        }

        public Major getMajor() { return major; }
        public Advisor getAdvisor() { return advisor; }
        public List<Course> getCompletedCourses() { return Collections.unmodifiableList(completedCourses); }
        public double getGpa() { return gpa; }
        public void setGpa(double gpa) { this.gpa = gpa; }
        public List<Message> getSentMessages() { return Collections.unmodifiableList(sentMessages); }
    }

    // --- MESSAGING CLASS ---

    public static class Message {
        private String messageId;
        private String senderUsername;
        private String recipientUsername;
        private String content;
        private LocalDateTime timestamp;

        public Message(String messageId, String senderUsername, String recipientUsername, String content) {
            this.messageId = messageId;
            this.senderUsername = senderUsername;
            this.recipientUsername = recipientUsername;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }

        public String getMessageId() { return messageId; }
        public String getSenderUsername() { return senderUsername; }
        public String getRecipientUsername() { return recipientUsername; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] From %s to %s: %s", timestamp, senderUsername, recipientUsername, content);
        }
    }

    // --- DEGREE PLANNING ENGINE (BEST-CASE SCENARIO ROADMAP) ---

    public static class SemesterPlan {
        private int semesterNumber;
        private List<Course> scheduledCourses;

        public SemesterPlan(int semesterNumber) {
            this.semesterNumber = semesterNumber;
            this.scheduledCourses = new ArrayList<>();
        }

        public void addCourse(Course course) {
            scheduledCourses.add(course);
        }

        public int getTotalCredits() {
            return scheduledCourses.stream().mapToInt(Course::getCredits).sum();
        }

        public int getSemesterNumber() { return semesterNumber; }
        public List<Course> getScheduledCourses() { return Collections.unmodifiableList(scheduledCourses); }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Semester %d (%d Credits):\n", semesterNumber, getTotalCredits()));
            for (Course c : scheduledCourses) {
                sb.append("   - ").append(c.toString()).append("\n");
            }
            return sb.toString();
        }
    }

    public static class DegreePlanningEngine {
        private static final int MAX_CREDITS_PER_SEMESTER = 16;

        public static List<SemesterPlan> generateBestCasePlan(Student student) {
            List<SemesterPlan> plans = new ArrayList<>();
            if (student.getMajor() == null) {
                return plans;
            }

            Set<Course> completed = new HashSet<>(student.getCompletedCourses());
            Set<Course> remaining = new HashSet<>(student.getMajor().getRequiredCourses());
            remaining.removeAll(completed);

            int semesterNum = 1;
            while (!remaining.isEmpty()) {
                SemesterPlan plan = new SemesterPlan(semesterNum);
                List<Course> eligibleThisSemester = new ArrayList<>();

                for (Course candidate : remaining) {
                    boolean prereqsMet = candidate.getPrerequisites().stream().allMatch(completed::contains);
                    if (prereqsMet) {
                        eligibleThisSemester.add(candidate);
                    }
                }

                if (eligibleThisSemester.isEmpty()) {
                    System.err.println("Warning: Unmet prerequisite dependency detected!");
                    break;
                }

                eligibleThisSemester.sort((c1, c2) -> Integer.compare(c2.getCredits(), c1.getCredits()));

                for (Course course : eligibleThisSemester) {
                    if (plan.getTotalCredits() + course.getCredits() <= MAX_CREDITS_PER_SEMESTER) {
                        plan.addCourse(course);
                    }
                }

                if (plan.getScheduledCourses().isEmpty()) {
                    Course c = eligibleThisSemester.get(0);
                    plan.addCourse(c);
                }

                for (Course c : plan.getScheduledCourses()) {
                    remaining.remove(c);
                    completed.add(c);
                }

                plans.add(plan);
                semesterNum++;
            }

            return plans;
        }
    }

    // --- AUTHENTICATION & MANAGEMENT SERVICE ---

    public static class AuthenticationService {
        private Map<String, User> userDatabase = new HashMap<>();

        public void registerUser(User user) {
            userDatabase.put(user.getUsername().toLowerCase(), user);
        }

        public User login(String username, String password) {
            User user = userDatabase.get(username.toLowerCase());
            if (user != null && user.authenticate(password)) {
                System.out.println("Login Successful! Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
                return user;
            }
            System.out.println("Invalid username or password for: " + username);
            return null;
        }
    }

    // --- MAIN DEMONSTRATION PROGRAM ---

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   ACE IT - STUDENT ADVISORY TRACKER DEMONSTRATION");
        System.out.println("=================================================================\n");

        Course cs101 = new Course("CS101", "Introduction to Computer Science", 4);
        Course cs102 = new Course("CS102", "Data Structures & Algorithms", 4);
        cs102.addPrerequisite(cs101);

        Course cs201 = new Course("CS201", "Object-Oriented Programming", 3);
        cs201.addPrerequisite(cs101);

        Course cs301 = new Course("CS301", "Database Systems", 3);
        cs301.addPrerequisite(cs102);

        Course cs302 = new Course("CS302", "Software Engineering", 4);
        cs302.addPrerequisite(cs201);

        Course cs401 = new Course("CS401", "Senior Capstone Project", 4);
        cs401.addPrerequisite(cs301);
        cs401.addPrerequisite(cs302);

        Major csMajor = new Major("BS-CS", "Bachelor of Science in Computer Science");
        csMajor.addRequiredCourse(cs101);
        csMajor.addRequiredCourse(cs102);
        csMajor.addRequiredCourse(cs201);
        csMajor.addRequiredCourse(cs301);
        csMajor.addRequiredCourse(cs302);
        csMajor.addRequiredCourse(cs401);

        AuthenticationService authService = new AuthenticationService();

        AdminUser admin = new AdminUser("U001", "admin", "adminPass123", "System Administrator", "admin@aceit.edu");
        Advisor advisor = new Advisor("U002", "advisor_smith", "advPass123", "Dr. Jane Smith", "jsmith@aceit.edu", "Computer Science");
        Student student = new Student("U003", "student_john", "studPass123", "John Doe", "jdoe@aceit.edu", csMajor, advisor);

        authService.registerUser(admin);
        authService.registerUser(advisor);
        authService.registerUser(student);

        System.out.println("--- 1. Testing Role-Based Authentication ---");
        authService.login("student_john", "wrongPass");
        User loggedInUser = authService.login("student_john", "studPass123");

        System.out.println("\n--- 2. Testing Student Messaging to Advisor ---");
        if (loggedInUser instanceof Student) {
            Student activeStudent = (Student) loggedInUser;
            activeStudent.sendMessageToAdvisor("Hello Dr. Smith, could we review my semester roadmap for Spring?");
        }

        System.out.println("\nAdvisor Inbox:");
        for (Message msg : advisor.getInbox()) {
            System.out.println("  " + msg);
        }

        System.out.println("\n--- 3. Testing Best-Case Degree Path Planning ---");
        System.out.println("Generating optimal degree plan for " + student.getFullName() + " (" + student.getMajor().getTitle() + ")...");

        student.completeCourse(cs101);
        System.out.println("Completed courses: " + student.getCompletedCourses());

        List<SemesterPlan> roadmap = DegreePlanningEngine.generateBestCasePlan(student);

        System.out.println("\nRECOMMENDED BEST-CASE ROADMAP:");
        for (SemesterPlan plan : roadmap) {
            System.out.print(plan);
        }

        System.out.println("=================================================================");
        System.out.println("   DEMONSTRATION COMPLETED SUCCESSFULLY");
        System.out.println("=================================================================");
    }
}
