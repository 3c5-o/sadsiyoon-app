import React, { useState } from 'react';
import { Play, Star, CheckCircle, Circle, AlertCircle } from 'lucide-react';
import { Teacher, LessonRef } from '../../types';
import { getFavorite, setFavorite, getCompleted, setCompleted } from '../../utils/storage';

interface ChapterDetailViewProps {
  teacher: Teacher;
  chapterIndex: number;
  onOpenLesson: (ref: LessonRef) => void;
}

export const ChapterDetailView: React.FC<ChapterDetailViewProps> = ({
  teacher,
  chapterIndex,
  onOpenLesson,
}) => {
  const chapter = teacher.chapters[chapterIndex];
  const [, setTick] = useState<number>(0);

  if (!chapter) {
    return <div className="text-white text-center py-8">الفصل غير موجود</div>;
  }

  const toggleFav = (e: React.MouseEvent, key: string) => {
    e.stopPropagation();
    const current = getFavorite(key);
    setFavorite(key, !current);
    setTick((t) => t + 1);
  };

  const toggleDone = (e: React.MouseEvent, key: string) => {
    e.stopPropagation();
    const current = getCompleted(key);
    setCompleted(key, !current);
    setTick((t) => t + 1);
  };

  return (
    <div className="space-y-4 pb-24">
      {/* Chapter Header Banner */}
      <div className="bg-gradient-to-br from-[#203E6B] to-[#122644] p-5 rounded-3xl border border-[#2B5187]/60 shadow-lg">
        <div className="text-xs font-bold text-[#E6B74A]">
          {teacher.name} • دفعة {teacher.batch}
        </div>
        <h2 className="text-xl font-bold text-white mt-1.5">{chapter.title}</h2>
        <div className="text-xs text-[#A4B6D2] mt-1">
          {chapter.lessons.length} محاضرة مفهرسة
        </div>
      </div>

      {/* Lectures List */}
      <div className="space-y-2.5">
        {chapter.lessons.map((lesson, idx) => {
          const key = `t${teacher.index}_c${chapterIndex}_l${idx}`;
          const isFav = getFavorite(key);
          const isDone = getCompleted(key);
          const isAvailable = lesson.url && lesson.type !== 'none';

          const ref: LessonRef = {
            teacher,
            chapter,
            lesson,
            ti: teacher.index,
            ci: chapterIndex,
            li: idx,
            key,
          };

          const statusLabel = isDone
            ? 'مكتملة'
            : !isAvailable
            ? 'غير متاحة للتشغيل حالياً'
            : lesson.access === 'premium'
            ? 'يتطلب اشتراك مشاهدة لدى المزود'
            : lesson.access === 'unverified'
            ? 'مشغّل مضمن — التشغيل غير مؤكّد'
            : 'رابط موجود — التشغيل غير مفحوص';

          return (
            <div
              key={idx}
              onClick={() => {
                if (!isAvailable) {
                  alert('هذه المحاضرة غير متاحة للمشاهدة حالياً');
                  return;
                }
                onOpenLesson(ref);
              }}
              className={`bg-[#14263D] hover:bg-[#1A314E] border p-3.5 rounded-2xl flex items-center justify-between transition cursor-pointer ${
                isDone
                  ? 'border-[#1C423E]/80 bg-[#122435]'
                  : 'border-[#203D61]'
              }`}
            >
              <div className="flex items-center gap-3 flex-1 min-w-0">
                <div
                  className={`w-10 h-10 rounded-xl flex items-center justify-center font-bold text-sm flex-shrink-0 ${
                    isDone
                      ? 'bg-[#19403B] text-[#20C7B4]'
                      : isAvailable
                      ? 'bg-[#1E3B66] text-[#20C7B4]'
                      : 'bg-[#192A40] text-[#6B7D98]'
                  }`}
                >
                  {isAvailable ? <Play size={16} fill="currentColor" /> : '—'}
                </div>

                <div className="flex-1 min-w-0 pr-1">
                  <h4 className="font-bold text-sm text-white truncate">
                    {lesson.title}
                  </h4>
                  <div
                    className={`text-[11px] mt-0.5 truncate ${
                      isDone
                        ? 'text-[#20C7B4] font-medium'
                        : isAvailable
                        ? 'text-[#A4B6D2]'
                        : 'text-[#E68A8A]'
                    }`}
                  >
                    {statusLabel}
                  </div>
                </div>
              </div>

              {/* Action buttons: Favorite & Completed */}
              <div className="flex items-center gap-1 flex-shrink-0">
                <button
                  onClick={(e) => toggleFav(e, key)}
                  className="p-2 text-[#A4B6D2] hover:text-[#E6B74A] transition"
                  title={isFav ? 'إزالة من المفضلة' : 'إضافة إلى المفضلة'}
                >
                  <Star
                    size={18}
                    className={isFav ? 'text-[#E6B74A]' : 'text-[#647999]'}
                    fill={isFav ? '#E6B74A' : 'none'}
                  />
                </button>

                <button
                  onClick={(e) => toggleDone(e, key)}
                  className="p-2 text-[#A4B6D2] hover:text-[#20C7B4] transition"
                  title={isDone ? 'إلغاء اكتمال المحاضرة' : 'تحديد المحاضرة كمكتملة'}
                >
                  {isDone ? (
                    <CheckCircle size={18} className="text-[#20C7B4]" />
                  ) : (
                    <Circle size={18} className="text-[#647999]" />
                  )}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
