import catalogData from './catalog.json';
import examsData from './exams.json';
import { Teacher, LessonRef, ExamPaper, BatchYear } from '../types';

export const teachers: Teacher[] = catalogData as unknown as Teacher[];

export function getAllTeachers(): Teacher[] {
  return teachers;
}

export function getTeachersByBatch(batch: BatchYear): Teacher[] {
  return teachers.filter((t) => t.batch === batch);
}

export function getTeacher(index: number): Teacher | null {
  if (index >= 0 && index < teachers.length) {
    return teachers[index];
  }
  return null;
}

export function getRef(ti: number, ci: number, li: number): LessonRef | null {
  const teacher = getTeacher(ti);
  if (!teacher || ci < 0 || ci >= teacher.chapters.length) return null;
  const chapter = teacher.chapters[ci];
  if (!chapter || li < 0 || li >= chapter.lessons.length) return null;
  const lesson = chapter.lessons[li];
  return {
    teacher,
    chapter,
    lesson,
    ti,
    ci,
    li,
    key: `t${ti}_c${ci}_l${li}`,
  };
}

let cachedAllRef: LessonRef[] | null = null;
export function getAllRefs(): LessonRef[] {
  if (cachedAllRef) return cachedAllRef;
  const list: LessonRef[] = [];
  for (const t of teachers) {
    for (let ci = 0; ci < t.chapters.length; ci++) {
      const ch = t.chapters[ci];
      for (let li = 0; li < ch.lessons.length; li++) {
        const ref = getRef(t.index, ci, li);
        if (ref) list.push(ref);
      }
    }
  }
  cachedAllRef = list;
  return list;
}

interface ExamsJson {
  papers: Array<{
    year: number;
    branch: 'scientific' | 'literary';
    subject: string;
    round: string;
    title: string;
    source: string;
    type: string;
    images?: string[];
  }>;
}

export function getExamPapers(
  year: number,
  branch: string,
  subject: string,
  round: string
): ExamPaper[] {
  const data = examsData as unknown as ExamsJson;
  if (!data || !Array.isArray(data.papers)) return [];
  return data.papers
    .filter(
      (p) =>
        p.year === year &&
        p.branch === branch &&
        p.subject === subject &&
        p.round === round
    )
    .map((p) => ({
      year: p.year,
      branch: p.branch,
      subject: p.subject,
      round: p.round,
      title: p.title,
      source: p.source,
      type: p.type,
      images: p.images || [],
    }));
}
