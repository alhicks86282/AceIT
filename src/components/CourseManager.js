/**
 * AceIT - Course Management Module
 * Handles adding, retrieving, updating, and deleting courses,
 * credit tracking, and meeting time management.
 */

class CourseManager {
  constructor() {
    this.courses = [];
  }

  /**
   * Add a new course to the system.
   * @param {Object} courseData - Course information.
   * @param {string} courseData.code - Course code (e.g., "CS101").
   * @param {string} courseData.name - Course title (e.g., "Introduction to Computer Science").
   * @param {number} courseData.credits - Credit hours.
   * @param {string} [courseData.grade] - Letter grade or percentage (optional).
   * @param {string} [courseData.instructor] - Instructor name (optional).
   * @param {Array<string>} [courseData.meetingTimes] - Meeting schedule (optional).
   * @param {string} [courseData.semester] - Term/Semester (optional).
   * @returns {Object} The created course object.
   */
  addCourse(courseData) {
    if (!courseData || typeof courseData !== 'object') {
      throw new Error('Invalid course data provided.');
    }

    const { code = '', name, credits, grade = null, instructor = '', meetingTimes = [], semester = '' } = courseData;

    if (!name || typeof name !== 'string' || name.trim() === '') {
      throw new Error('Course name is required and must be a non-empty string.');
    }

    const parsedCredits = Number(credits);
    if (isNaN(parsedCredits) || parsedCredits < 0) {
      throw new Error('Course credits must be a valid non-negative number.');
    }

    const newCourse = {
      id: Date.now().toString(36) + Math.random().toString(36).substr(2, 5),
      code: code.trim().toUpperCase(),
      name: name.trim(),
      credits: parsedCredits,
      grade: grade ? String(grade).trim() : null,
      instructor: String(instructor).trim(),
      meetingTimes: Array.isArray(meetingTimes) ? meetingTimes : [meetingTimes],
      semester: String(semester).trim(),
      createdAt: new Date().toISOString()
    };

    this.courses.push(newCourse);
    return newCourse;
  }

  /**
   * Get all managed courses.
   * @returns {Array<Object>} List of course objects.
   */
  getCourses() {
    return [...this.courses];
  }

  /**
   * Find a course by ID or code.
   * @param {string} query - Course ID or code.
   * @returns {Object|null}
   */
  findCourse(query) {
    if (!query) return null;
    const q = String(query).trim().toLowerCase();
    return this.courses.find(c => c.id.toLowerCase() === q || c.code.toLowerCase() === q) || null;
  }

  /**
   * Update an existing course by ID.
   * @param {string} id - Course ID.
   * @param {Object} updates - Fields to update.
   * @returns {Object|null} Updated course object or null.
   */
  updateCourse(id, updates) {
    const course = this.findCourse(id);
    if (!course) return null;

    if (updates.name !== undefined) {
      if (!updates.name || typeof updates.name !== 'string' || updates.name.trim() === '') {
        throw new Error('Course name cannot be empty.');
      }
      course.name = updates.name.trim();
    }

    if (updates.credits !== undefined) {
      const parsedCredits = Number(updates.credits);
      if (isNaN(parsedCredits) || parsedCredits < 0) {
        throw new Error('Credits must be a valid non-negative number.');
      }
      course.credits = parsedCredits;
    }

    if (updates.grade !== undefined) course.grade = updates.grade ? String(updates.grade).trim() : null;
    if (updates.code !== undefined) course.code = String(updates.code).trim().toUpperCase();
    if (updates.instructor !== undefined) course.instructor = String(updates.instructor).trim();
    if (updates.meetingTimes !== undefined) {
      course.meetingTimes = Array.isArray(updates.meetingTimes) ? updates.meetingTimes : [updates.meetingTimes];
    }
    if (updates.semester !== undefined) course.semester = String(updates.semester).trim();

    course.updatedAt = new Date().toISOString();
    return course;
  }

  /**
   * Delete a course by ID.
   * @param {string} id - Course ID.
   * @returns {boolean} True if deleted.
   */
  deleteCourse(id) {
    const index = this.courses.findIndex(c => c.id === id);
    if (index !== -1) {
      this.courses.splice(index, 1);
      return true;
    }
    return false;
  }

  /**
   * Calculate total enrolled credits.
   * @returns {number}
   */
  getTotalCredits() {
    return this.courses.reduce((sum, c) => sum + c.credits, 0);
  }
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = CourseManager;
} else if (typeof window !== 'undefined') {
  window.CourseManager = CourseManager;
}
