import React from 'react';
import {
  Sparkles,
  Play,
  Users,
  FileText,
  CheckCircle,
  Bookmark,
  ChevronLeft,
  Globe,
  Calculator,
  Clock,
  BookOpen,
  Award,
} from 'lucide-react';
import { Teacher, LessonRef, PageId, BatchYear } from '../../types';
import { getNickname, getLastWatched, getCompleted, getFavorite } from '../../utils/storage';
import { getRef } from '../../data/catalog';

interface HomeViewProps {
  teachers: Teacher[];
  allRefs: LessonRef[];
  selectedBatch: BatchYear;
  onSelectBatch: (batch: BatchYear) => void;
  onSelectTeacher: (teacherIndex: number) => void;
  onOpenLesson: (ref: LessonRef) => void;
  onNavigate: (page: PageId, filter?: string) => void;
  onOpenGpaModal: () => void;
  onOpenPomodoroModal: () => void;
}

export const HomeView: React.FC<HomeViewProps> = ({
  teachers,
  allRefs,
  selectedBatch,
  onSelectBatch,
  onSelectTeacher,
  onOpenLesson,
  onNavigate,
  onOpenGpaModal,
  onOpenPomodoroModal,
}) => {
  const nickname = getNickname();
  const lastWatched = getLastWatched();
  const lastRef = lastWatched ? getRef(lastWatched.ti, lastWatched.ci, lastWatched.li) : null;

  // Stats calculation
  let completedCount = 0;
  let favoriteCount = 0;
  allRefs.forEach((r) => {
    if (getCompleted(r.key)) completedCount++;
    if (getFavorite(r.key)) favoriteCount++;
  });
  const totalCount = allRefs.length;
  const progressPercent = totalCount > 0 ? Math.round((completedCount * 100) / totalCount) : 0;

  // Filter teachers by selected batch
  const batchTeachers = teachers.filter((t) => t.batch === selectedBatch);

  const subjects = [
    { name: 'الرياضيات', symbol: '∑' },
    { name: 'الفيزياء', symbol: 'Φ' },
    { name: 'الكيمياء', symbol: '⚗' },
    { name: 'الأحياء', symbol: 'DNA' },
    { name: 'اللغة العربية', symbol: 'ع' },
    { name: 'اللغة الإنكليزية', symbol: 'En' },
    { name: 'التربية الإسلامية', symbol: '✧' },
  ];

  return (
    <div className="space-y-6 pb-24">
      {/* Hero Card */}
      <div className="bg-gradient-to-br from-[#244A85] via-[#1C3A66] to-[#12243F] p-5 sm:p-6 rounded-3xl border border-[#335B96]/60 shadow-xl relative overflow-hidden">
        <div className="absolute top-0 left-0 w-56 h-56 bg-[#20C7B4]/10 rounded-full blur-3xl -ml-20 -mt-20 pointer-events-none" />

        <div className="flex items-center gap-2 text-xs font-semibold text-[#20C7B4] mb-1">
          <Sparkles size={14} />
          <span>سادسيون رفيقك الدراسي الذكي</span>
        </div>

        <h2 className="text-2xl sm:text-3xl font-black text-white tracking-wide">
          مرحباً، {nickname}
        </h2>
        <p className="text-xs sm:text-sm text-[#D6E6FF] mt-1 font-normal">
          خطوة جديدة واثقة نحو هدفك ومعدلك الوزاري في السادس الإعدادي
        </p>

        {/* Progress Section */}
        <div className="mt-5 space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="font-semibold text-white/90">إنجازك في المحاضرات</span>
            <span className="font-bold text-[#20C7B4] font-mono">{progressPercent}%</span>
          </div>

          <div className="w-full bg-[#13243D] h-2.5 rounded-full overflow-hidden p-0.5 border border-[#2B4973]/50">
            <div
              className="bg-gradient-to-r from-[#17A393] to-[#20C7B4] h-full rounded-full transition-all duration-500 shadow-sm"
              style={{ width: `${Math.max(2, progressPercent)}%` }}
            />
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-3 gap-2.5 mt-5 pt-4 border-t border-white/10 text-center">
          <div className="p-2.5 bg-black/25 backdrop-blur-sm rounded-2xl border border-white/5">
            <div className="text-lg sm:text-xl font-bold text-[#20C7B4] font-mono">{completedCount}</div>
            <div className="text-[11px] text-[#A4B6D2]">محاضرة مكتملة</div>
          </div>
          <div className="p-2.5 bg-black/25 backdrop-blur-sm rounded-2xl border border-white/5">
            <div className="text-lg sm:text-xl font-bold text-[#E6B74A] font-mono">{favoriteCount}</div>
            <div className="text-[11px] text-[#A4B6D2]">محفوظة</div>
          </div>
          <div className="p-2.5 bg-black/25 backdrop-blur-sm rounded-2xl border border-white/5">
            <div className="text-lg sm:text-xl font-bold text-white font-mono">{totalCount}</div>
            <div className="text-[11px] text-[#A4B6D2]">المحاضرات</div>
          </div>
        </div>
      </div>

      {/* Resume Card (if user has watched a lecture) */}
      {lastRef && (
        <div className="bg-gradient-to-r from-[#1D375B] to-[#14263F] p-4 sm:p-5 rounded-2xl border border-[#2B4E7C]/60 shadow-md">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-[#20C7B4] flex items-center gap-1.5">
              <Play size={13} fill="#20C7B4" />
              أكمل من حيث توقفت
            </span>
            <button
              onClick={() => onNavigate('library', 'continue')}
              className="text-xs text-[#A4B6D2] hover:text-white"
            >
              مكتبتي
            </button>
          </div>

          <h3 className="text-base font-bold text-white leading-tight">
            {lastRef.lesson.title}
          </h3>
          <p className="text-xs text-[#A4B6D2] mt-1">
            {lastRef.teacher.name} • {lastRef.chapter.title}
          </p>

          <button
            onClick={() => onOpenLesson(lastRef)}
            className="w-full mt-3 flex items-center justify-center gap-2 py-2.5 rounded-xl bg-[#20C7B4] hover:bg-[#18AFA0] text-[#0B1626] font-bold text-xs shadow-md transition cursor-pointer"
          >
            <span>متابعة المشاهدة</span>
            <ChevronLeft size={16} />
          </button>
        </div>
      )}

      {/* Student Productivity Tools Strip */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {/* GPA Calculator Card */}
        <button
          onClick={onOpenGpaModal}
          className="bg-gradient-to-br from-[#1E385D] to-[#12243D] p-4 rounded-2xl border border-[#2D5487]/60 text-right transition hover:border-[#E6B74A]/60 shadow-md group flex items-center justify-between"
        >
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-[#E6B74A]/15 text-[#E6B74A] flex items-center justify-center border border-[#E6B74A]/30 flex-shrink-0 group-hover:scale-105 transition-transform">
              <Calculator size={22} />
            </div>
            <div>
              <div className="font-bold text-sm text-white flex items-center gap-1.5">
                <span>حاسبة المعدل الوزاري</span>
                <span className="text-[10px] text-[#E6B74A] bg-[#E6B74A]/15 px-2 py-0.5 rounded-full">
                  جديد
                </span>
              </div>
              <div className="text-[11px] text-[#A4B6D2] mt-0.5">
                احسب معدلك وتعرف على الكليات المتوقعة
              </div>
            </div>
          </div>
          <ChevronLeft size={18} className="text-[#E6B74A]" />
        </button>

        {/* Pomodoro Timer Card */}
        <button
          onClick={onOpenPomodoroModal}
          className="bg-gradient-to-br from-[#16384C] to-[#102535] p-4 rounded-2xl border border-[#245D78]/60 text-right transition hover:border-[#20C7B4]/60 shadow-md group flex items-center justify-between"
        >
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-[#20C7B4]/15 text-[#20C7B4] flex items-center justify-center border border-[#20C7B4]/30 flex-shrink-0 group-hover:scale-105 transition-transform">
              <Clock size={22} />
            </div>
            <div>
              <div className="font-bold text-sm text-white flex items-center gap-1.5">
                <span>مؤقت المذاكرة بومودورو</span>
                <span className="text-[10px] text-[#20C7B4] bg-[#20C7B4]/15 px-2 py-0.5 rounded-full">
                  تركيز
                </span>
              </div>
              <div className="text-[11px] text-[#A4B6D2] mt-0.5">
                نظم فترات الدراسة والاستراحة دون تشتت
              </div>
            </div>
          </div>
          <ChevronLeft size={18} className="text-[#20C7B4]" />
        </button>
      </div>

      {/* Quick Navigation Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <button
          onClick={() => onNavigate('teachers')}
          className="bg-[#14263F] hover:bg-[#1A3152] p-4 rounded-2xl border border-[#24426A]/60 text-right transition flex flex-col justify-between h-28 group cursor-pointer shadow-sm"
        >
          <div className="w-9 h-9 rounded-xl bg-[#203E6B] text-[#20C7B4] flex items-center justify-center group-hover:scale-105 transition-transform">
            <Users size={18} />
          </div>
          <div>
            <div className="font-bold text-sm text-white">المدرسون</div>
            <div className="text-[11px] text-[#A4B6D2]">الدروس والفصول</div>
          </div>
        </button>

        <button
          onClick={() => onNavigate('exams')}
          className="bg-[#14263F] hover:bg-[#1A3152] p-4 rounded-2xl border border-[#24426A]/60 text-right transition flex flex-col justify-between h-28 group cursor-pointer shadow-sm"
        >
          <div className="w-9 h-9 rounded-xl bg-[#203E6B] text-[#20C7B4] flex items-center justify-center group-hover:scale-105 transition-transform">
            <FileText size={18} />
          </div>
          <div>
            <div className="font-bold text-sm text-white">الوزاريات</div>
            <div className="text-[11px] text-[#A4B6D2]">أوراق 2025 الأصلية</div>
          </div>
        </button>

        <button
          onClick={() => onNavigate('tests')}
          className="bg-[#14263F] hover:bg-[#1A3152] p-4 rounded-2xl border border-[#24426A]/60 text-right transition flex flex-col justify-between h-28 group cursor-pointer shadow-sm"
        >
          <div className="w-9 h-9 rounded-xl bg-[#203E6B] text-[#20C7B4] flex items-center justify-center group-hover:scale-105 transition-transform">
            <CheckCircle size={18} />
          </div>
          <div>
            <div className="font-bold text-sm text-white">الاختبارات</div>
            <div className="text-[11px] text-[#A4B6D2]">شاملة وفصول</div>
          </div>
        </button>

        <button
          onClick={() => onNavigate('materials')}
          className="bg-[#14263F] hover:bg-[#1A3152] p-4 rounded-2xl border border-[#24426A]/60 text-right transition flex flex-col justify-between h-28 group cursor-pointer shadow-sm"
        >
          <div className="w-9 h-9 rounded-xl bg-[#203E6B] text-[#E6B74A] flex items-center justify-center group-hover:scale-105 transition-transform">
            <BookOpen size={18} />
          </div>
          <div>
            <div className="font-bold text-sm text-white">الملازم</div>
            <div className="text-[11px] text-[#A4B6D2]">مخططات وقوانين</div>
          </div>
        </button>
      </div>

      {/* Cohort Selector */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-bold text-white">اختر دفعتك</h3>
          <button
            onClick={() => onNavigate('teachers')}
            className="text-xs text-[#20C7B4] font-semibold"
          >
            عرض الكل
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {(['2026', '2027'] as BatchYear[]).map((batch) => {
            const bTeachers = teachers.filter((t) => t.batch === batch);
            let bLectures = 0;
            bTeachers.forEach((t) =>
              t.chapters.forEach((c) => {
                bLectures += c.lessons.length;
              })
            );

            const isCurrent = selectedBatch === batch;

            return (
              <div
                key={batch}
                onClick={() => {
                  onSelectBatch(batch);
                  onNavigate('teachers');
                }}
                className={`p-4 rounded-2xl border transition cursor-pointer relative overflow-hidden shadow-sm ${
                  isCurrent
                    ? 'bg-gradient-to-br from-[#244E83] to-[#152B4B] border-[#20C7B4]'
                    : 'bg-[#14263D] border-[#223F66] hover:bg-[#1A314E]'
                }`}
              >
                <div className="text-xl font-black text-white">دفعة {batch}</div>
                <div className="text-xs text-[#A4B6D2] mt-1">
                  {bTeachers.length} مدرس • {bLectures} محاضرة مفهرسة
                </div>
                <div className="mt-3 text-xs font-bold text-[#20C7B4] flex items-center gap-1">
                  <span>استعراض مدرسي الدفعة</span>
                  <ChevronLeft size={13} />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Featured Teachers Horizontal Slider */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-bold text-white">أساتذتنا (دفعة {selectedBatch})</h3>
          <button
            onClick={() => onNavigate('teachers')}
            className="text-xs text-[#20C7B4] font-semibold"
          >
            عرض الجميع
          </button>
        </div>

        <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-none -mx-4 px-4">
          {batchTeachers.slice(0, 10).map((teacher) => (
            <div
              key={teacher.index}
              onClick={() => onSelectTeacher(teacher.index)}
              className="flex-shrink-0 w-32 bg-[#14263D] hover:bg-[#1A3250] border border-[#213B5E] p-3 rounded-2xl text-center transition cursor-pointer group shadow-sm"
            >
              <div className="w-16 h-16 mx-auto rounded-full overflow-hidden bg-[#1B3456] border-2 border-[#264A78] flex items-center justify-center text-xl font-bold text-white shadow-md group-hover:scale-105 transition-transform">
                {teacher.image ? (
                  <img
                    src={teacher.image}
                    alt={teacher.name}
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      (e.target as HTMLElement).style.display = 'none';
                    }}
                  />
                ) : (
                  <span>{teacher.name.charAt(0)}</span>
                )}
              </div>

              <div className="font-bold text-xs text-white mt-2 truncate">
                {teacher.name}
              </div>
              <div className="text-[11px] text-[#20C7B4] mt-0.5 truncate">
                {teacher.subject}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Subjects Grid */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-bold text-white">المواد الدراسية</h3>
          <button
            onClick={() => onNavigate('teachers')}
            className="text-xs text-[#20C7B4] font-semibold"
          >
            الكل
          </button>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
          {subjects.map((sub, i) => {
            const count = batchTeachers.filter((t) => t.subject === sub.name).length;
            return (
              <div
                key={i}
                onClick={() => onNavigate('teachers', sub.name)}
                className="bg-[#14263D] hover:bg-[#1B3250] border border-[#223B5C] p-3.5 rounded-2xl flex items-center justify-between transition cursor-pointer shadow-sm"
              >
                <div>
                  <div className="font-bold text-sm text-white">{sub.name}</div>
                  <div className="text-[11px] text-[#A4B6D2]">{count} مدرس</div>
                </div>
                <div className="text-xl font-black text-[#20C7B4] font-mono px-1">
                  {sub.symbol}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Published Content Card */}
      <div className="bg-gradient-to-r from-[#154659] to-[#102838] p-4.5 rounded-2xl border border-[#216782]/50 shadow-md">
        <div className="flex items-center gap-2 text-xs font-bold text-[#20C7B4] mb-1">
          <Globe size={14} />
          <span>محتوى سادسيون المنشور</span>
        </div>
        <h3 className="text-base font-bold text-white">المحاضرات والملازم والتحديثات الحية</h3>
        <p className="text-xs text-[#D0E2ED] mt-1 leading-relaxed">
          تحديثات الإدارة تظهر هنا عند الاتصال، وتخزن تلقائياً لتبقى متاحة للمذاكرة دون إنترنت.
        </p>
        <button
          onClick={() => onNavigate('remote-catalog')}
          className="mt-3 inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#20C7B4] hover:bg-[#19B5A3] text-[#0B1626] font-bold text-xs transition cursor-pointer shadow-sm"
        >
          <span>استعراض المحتوى المنشور</span>
          <ChevronLeft size={14} />
        </button>
      </div>

      {/* Disclaimer note */}
      <div className="text-center text-[11px] text-[#A4B6D2]/80 pt-4 leading-relaxed border-t border-[#1C3352]/40">
        قد تتطلب بعض محاضرات المزودين اشتراكاً، وتظهر حالة توفرها داخل كل محاضرة.
      </div>
    </div>
  );
};
