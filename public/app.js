document.addEventListener('DOMContentLoaded', () => {
  const courseForm = document.getElementById('course-form');
  const courseList = document.getElementById('course-list');
  const gpaScore = document.getElementById('gpa-score');

  let courses = [];

  const GRADE_POINTS = {
    'A': 4.0, 'A-': 3.7,
    'B+': 3.3, 'B': 3.0, 'B-': 2.7,
    'C+': 2.3, 'C': 2.0, 'C-': 1.7,
    'D+': 1.3, 'D': 1.0, 'F': 0.0
  };

  function calculateGPA(coursesList) {
    const graded = coursesList.filter(c => c.grade && GRADE_POINTS[c.grade] !== undefined && Number(c.credits) > 0);
    if (graded.length === 0) return '0.00';
    const totals = graded.reduce((acc, c) => {
      const cr = Number(c.credits);
      acc.points += GRADE_POINTS[c.grade] * cr;
      acc.credits += cr;
      return acc;
    }, { points: 0, credits: 0 });
    return totals.credits > 0 ? (totals.points / totals.credits).toFixed(2) : '0.00';
  }

  function renderCourses() {
    courseList.innerHTML = '';
    courses.forEach((course) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${course.name}</td><td>${course.credits}</td><td>${course.grade}</td>`;
      courseList.appendChild(tr);
    });
    gpaScore.textContent = calculateGPA(courses);
  }

  courseForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const name = document.getElementById('course-name').value.trim();
    const credits = document.getElementById('course-credits').value;
    const grade = document.getElementById('course-grade').value;

    if (name && credits && grade) {
      courses.push({ name, credits, grade });
      renderCourses();
      courseForm.reset();
    }
  });
});
