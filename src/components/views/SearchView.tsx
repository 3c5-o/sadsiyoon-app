import React, { useState } from 'react';
import { Search, Play, Users, ChevronLeft } from 'lucide-react';
import { Teacher, LessonRef } from '../../types';

interface SearchViewProps {
  teachers: Teacher[];
  allRefs: LessonRef[];
  onSelectTeacher: (teacherIndex: number) => void;
  onOpenLesson: (ref: LessonRef) => void;
}

export const SearchView: React.FC<SearchViewProps> = ({
  teachers,
  allRefs,
  onSelectTeacher,
  onOpenLesson,
}) => {
  const [query, setQuery] = useState<string>('');

  const trimmed = query.trim().toLowerCase();

  const matchedTeachers =
    trimmed.length >= 2
      ? teachers.filter(
          (t) =>
            t.name.toLowerCase().includes(trimmed) ||
            t.subject.toLowerCase().includes(trimmed) ||
            t.batch.includes(trimmed)
        )
      : [];

  const matchedLessons =
    trimmed.length >= 2
      ? allRefs
          .filter(
            (r) =>
              r.lesson.title.toLowerCase().includes(trimmed) ||
              r.chapter.title.toLowerCase().includes(trimmed) ||
              r.teacher.name.toLowerCase().includes(trimmed)
          )
          .slice(0, 65)
      : [];

  return (
    <div className="space-y-4 pb-24">
      {/* Search Input Box */}
      <div className="relative">
        <Search
          size={18}
          className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[#A4B6D2]"
        />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="ابحث عن مدرس أو فصل أو محاضرة..."
          className="w-full bg-[#14263D] border border-[#23426A] rounded-2xl pr-10 pl-4 py-3 text-sm text-white placeholder-[#A4B6D2] focus:outline-none focus:border-[#20C7B4] shadow-md"
          autoFocus
        />
      </div>

      {trimmed.length < 2 ? (
        <div className="bg-[#14263D] border border-[#203D61] p-6 rounded-2xl text-center text-xs text-[#A4B6D2]">
          اكتب حرفين أو أكثر للبحث في جميع المدرسين والفصول والمحاضرات المفهرسة.
        </div>
      ) : (
        <div className="space-y-4">
          {/* Matched Teachers */}
          {matchedTeachers.length > 0 && (
            <div className="space-y-2">
              <h4 className="text-xs font-bold text-[#E6B74A] flex items-center gap-1.5 px-1">
                <Users size={14} />
                <span>المدرسون المطابقون ({matchedTeachers.length})</span>
              </h4>

              <div className="space-y-2">
                {matchedTeachers.map((t) => (
                  <div
                    key={t.index}
                    onClick={() => onSelectTeacher(t.index)}
                    className="bg-[#14263D] hover:bg-[#1A314E] border border-[#213C60] p-3 rounded-2xl flex items-center justify-between transition cursor-pointer"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl overflow-hidden bg-[#1B3252] flex items-center justify-center font-bold text-sm text-white">
                        {t.image ? (
                          <img
                            src={t.image}
                            alt={t.name}
                            className="w-full h-full object-cover"
                            onError={(e) => {
                              (e.target as HTMLElement).style.display = 'none';
                            }}
                          />
                        ) : (
                          <span>{t.name.charAt(0)}</span>
                        )}
                      </div>

                      <div>
                        <div className="font-bold text-sm text-white">{t.name}</div>
                        <div className="text-xs text-[#20C7B4]">
                          {t.subject} • دفعة {t.batch}
                        </div>
                      </div>
                    </div>

                    <ChevronLeft size={18} className="text-[#A4B6D2]" />
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Matched Lessons */}
          {matchedLessons.length > 0 && (
            <div className="space-y-2">
              <h4 className="text-xs font-bold text-[#20C7B4] flex items-center gap-1.5 px-1">
                <Play size={13} fill="#20C7B4" />
                <span>المحاضرات المطابقة ({matchedLessons.length})</span>
              </h4>

              <div className="space-y-2">
                {matchedLessons.map((ref) => {
                  const isAvailable = ref.lesson.url && ref.lesson.type !== 'none';

                  return (
                    <div
                      key={ref.key}
                      onClick={() => {
                        if (!isAvailable) {
                          alert('هذه المحاضرة غير متاحة حالياً');
                          return;
                        }
                        onOpenLesson(ref);
                      }}
                      className="bg-[#14263D] hover:bg-[#1A314E] border border-[#213C60] p-3 rounded-2xl flex items-center justify-between transition cursor-pointer"
                    >
                      <div className="flex items-center gap-3 flex-1 min-w-0">
                        <div
                          className={`w-8 h-8 rounded-lg flex items-center justify-center font-bold text-xs flex-shrink-0 ${
                            isAvailable
                              ? 'bg-[#1E3B66] text-[#20C7B4]'
                              : 'bg-[#192A40] text-[#6B7D98]'
                          }`}
                        >
                          <Play size={14} fill={isAvailable ? 'currentColor' : 'none'} />
                        </div>

                        <div className="flex-1 min-w-0 pr-1">
                          <h5 className="font-bold text-xs text-white truncate">
                            {ref.lesson.title}
                          </h5>
                          <div className="text-[10px] text-[#A4B6D2] mt-0.5 truncate">
                            {ref.teacher.name} • {ref.chapter.title} • دفعة {ref.teacher.batch}
                          </div>
                        </div>
                      </div>

                      <ChevronLeft size={16} className="text-[#A4B6D2] flex-shrink-0" />
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {matchedTeachers.length === 0 && matchedLessons.length === 0 && (
            <div className="bg-[#14263D] border border-[#203D61] p-8 rounded-2xl text-center text-xs text-[#A4B6D2]">
              لا توجد نتائج مطابقة لبحثك عن «{query}»
            </div>
          )}
        </div>
      )}
    </div>
  );
};
