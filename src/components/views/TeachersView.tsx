import React from 'react';
import { ChevronLeft, AlertTriangle } from 'lucide-react';
import { Teacher, BatchYear } from '../../types';

interface TeachersViewProps {
  teachers: Teacher[];
  selectedBatch: BatchYear;
  subjectFilter: string;
  onSelectBatch: (batch: BatchYear) => void;
  onSelectSubjectFilter: (subject: string) => void;
  onSelectTeacher: (teacherIndex: number) => void;
}

export const TeachersView: React.FC<TeachersViewProps> = ({
  teachers,
  selectedBatch,
  subjectFilter,
  onSelectBatch,
  onSelectSubjectFilter,
  onSelectTeacher,
}) => {
  const batchTeachers = teachers.filter((t) => t.batch === selectedBatch);

  // Calculate missing links count for 2026 notice
  let missingLinks = 0;
  if (selectedBatch === '2026') {
    batchTeachers.forEach((t) => {
      t.chapters.forEach((c) => {
        c.lessons.forEach((l) => {
          if (!l.url || l.type === 'none') missingLinks++;
        });
      });
    });
  }

  const subjects = [
    'الرياضيات',
    'الفيزياء',
    'الكيمياء',
    'الأحياء',
    'اللغة العربية',
    'اللغة الإنكليزية',
    'التربية الإسلامية',
  ];

  const filteredTeachers = subjectFilter
    ? batchTeachers.filter((t) => t.subject === subjectFilter)
    : batchTeachers;

  return (
    <div className="space-y-4 pb-24">
      {/* Batch Switcher Tabs */}
      <div className="grid grid-cols-2 gap-2 bg-[#122238] p-1 rounded-2xl border border-[#213C60]">
        {(['2026', '2027'] as BatchYear[]).map((batch) => (
          <button
            key={batch}
            onClick={() => {
              onSelectBatch(batch);
              onSelectSubjectFilter('');
            }}
            className={`py-2.5 rounded-xl font-bold text-xs transition ${
              selectedBatch === batch
                ? 'bg-[#203E6B] text-white shadow-md'
                : 'text-[#A4B6D2] hover:text-white'
            }`}
          >
            دفعة {batch}
          </button>
        ))}
      </div>

      {/* Missing links notice if 2026 */}
      {selectedBatch === '2026' && missingLinks > 0 && (
        <div className="bg-[#243522] border border-[#3E5C35] p-3 rounded-2xl flex items-center gap-2.5 text-xs text-[#C5E8BF]">
          <AlertTriangle size={16} className="text-[#E6B74A] flex-shrink-0" />
          <span>
            تنبيه: {missingLinks} سجل محاضرة بلا رابط مفهرس في دفعة 2026. لا يتم اختلاق أي روابط غير مؤكدة.
          </span>
        </div>
      )}

      {/* Subject Filter Chips */}
      <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none -mx-1 px-1">
        <button
          onClick={() => onSelectSubjectFilter('')}
          className={`flex-shrink-0 px-3 py-1.5 rounded-xl text-xs font-semibold transition ${
            !subjectFilter
              ? 'bg-[#20C7B4] text-[#0B1626]'
              : 'bg-[#14263D] text-[#A4B6D2] hover:text-white border border-[#213B5E]'
          }`}
        >
          الكل ({batchTeachers.length})
        </button>

        {subjects.map((sub) => {
          const count = batchTeachers.filter((t) => t.subject === sub).length;
          if (count === 0) return null;
          const isSelected = subjectFilter === sub;
          return (
            <button
              key={sub}
              onClick={() => onSelectSubjectFilter(isSelected ? '' : sub)}
              className={`flex-shrink-0 px-3 py-1.5 rounded-xl text-xs font-semibold transition ${
                isSelected
                  ? 'bg-[#20C7B4] text-[#0B1626]'
                  : 'bg-[#14263D] text-[#A4B6D2] hover:text-white border border-[#213B5E]'
              }`}
            >
              {sub} ({count})
            </button>
          );
        })}
      </div>

      {/* Teacher Cards List */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3.5">
        {filteredTeachers.map((t) => {
          let lectureCount = 0;
          t.chapters.forEach((c) => {
            lectureCount += c.lessons.length;
          });

          return (
            <div
              key={t.index}
              onClick={() => onSelectTeacher(t.index)}
              className="bg-[#14263D] hover:bg-[#1A3252] border border-[#203C62] p-3.5 rounded-2xl flex items-center justify-between transition cursor-pointer group shadow-sm"
            >
              <div className="flex items-center gap-3.5">
                <div className="w-14 h-14 rounded-2xl overflow-hidden bg-[#1B3252] border border-[#264875] flex items-center justify-center text-lg font-bold text-white flex-shrink-0 group-hover:scale-105 transition-transform">
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
                  <h3 className="font-bold text-base text-white group-hover:text-[#20C7B4] transition-colors">
                    {t.name}
                  </h3>
                  <div className="text-xs text-[#20C7B4] font-medium mt-0.5">
                    {t.subject} • {t.chapters.length} أقسام
                  </div>
                  <div className="text-[11px] text-[#A4B6D2] mt-0.5">
                    {lectureCount} محاضرة مفهرسة
                  </div>
                </div>
              </div>

              <ChevronLeft size={20} className="text-[#E6B74A] flex-shrink-0" />
            </div>
          );
        })}

        {filteredTeachers.length === 0 && (
          <div className="bg-[#14263D] border border-[#203C62] p-8 rounded-2xl text-center text-xs text-[#A4B6D2]">
            لا يوجد مدرسون مطابقون لهذا الفلتر في دفعة {selectedBatch}.
          </div>
        )}
      </div>
    </div>
  );
};
