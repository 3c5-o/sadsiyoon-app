export type BatchYear = '2026' | '2027';

export interface Lesson {
  title: string;
  url: string;
  type: 'mp4' | 'hls' | 'embed' | 'none';
  access?: string; // 'standard' | 'premium' | 'unverified'
}

export interface Chapter {
  title: string;
  lessons: Lesson[];
}

export interface Teacher {
  index: number;
  name: string;
  subject: string;
  image: string;
  batch: BatchYear;
  chapters: Chapter[];
}

export interface LessonRef {
  teacher: Teacher;
  chapter: Chapter;
  lesson: Lesson;
  ti: number;
  ci: number;
  li: number;
  key: string; // e.g. "t1_c0_l3"
}

export interface ExamPaper {
  year: number;
  branch: 'scientific' | 'literary';
  subject: string;
  round: string;
  title: string;
  source: string;
  type: string;
  images: string[];
}

export interface PracticeQuestion {
  id?: string;
  text: string;
  options: [string, string, string, string];
  answer: number;
  explanation: string;
  chapter?: string;
  subject?: string;
  difficulty?: 'easy' | 'medium' | 'hard';
}

export interface ChapterQuiz {
  id: string;
  title: string;
  subject: string;
  branch: 'scientific' | 'literary' | 'both';
  chapterNumber: number;
  questions: PracticeQuestion[];
}

export interface NoteItem {
  title: string;
  body: string;
  createdAt: number;
  positionMs?: number;
}

export interface StudyMaterial {
  id: string;
  title: string;
  teacherName: string;
  subject: string;
  batch: string;
  pagesCount: number;
  description: string;
  topics: string[];
  pages: Array<{
    pageNumber: number;
    title: string;
    content: string;
    summaryNotes?: string[];
    keyFormulas?: string[];
  }>;
}

export interface GpaSubject {
  id: string;
  name: string;
  grade: number | '';
  required: boolean;
}

export interface RemoteContentRecord {
  id: string;
  title: string;
  subtitle?: string | null;
  kind: 'cohort' | 'subject' | 'teacher' | 'chapter' | 'lesson' | 'resource' | 'exam' | 'quiz' | 'announcement';
  parent_id?: string | null;
  status: 'draft' | 'published' | 'hidden';
  batch?: string | null;
  subject?: string | null;
  media_url?: string | null;
  media_type?: string | null;
  image_url?: string | null;
  source_url?: string | null;
  sort_index?: number;
  created_at?: string;
  extra?: Record<string, unknown> | null;
}

export type PageId =
  | 'home'
  | 'teachers'
  | 'teacher'
  | 'chapter'
  | 'exams'
  | 'tests'
  | 'library'
  | 'materials'
  | 'more'
  | 'search'
  | 'remote-catalog';
