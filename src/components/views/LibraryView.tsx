import React, { useState } from 'react';
import { Play, Star, CheckCircle, Circle, Bookmark, Sparkles } from 'lucide-react';
import { LessonRef } from '../../types';
import {
  getFavorite,
  setFavorite,
  getCompleted,
  setCompleted,
  getPosition,
} from '../../utils/storage';

interface LibraryViewProps {
  allRefs: LessonRef[];
  initialFilter?: string;
  onOpenLesson: (ref: LessonRef) => void;
}

export const LibraryView: React.FC<LibraryViewProps> = ({
  allRefs,
  initialFilter = 'fav',
  onOpenLesson,
}) => {
  const [filter, setFilter] = useState<'fav' | 'done' | 'continue'>(
    initialFilter === 'done' || initialFilter === 'continue' ? initialFilter : 'fav'
  );
  const [displayLimit, setDisplayLimit] = useState<number>(50);
  const [, setTick] = useState<number>(0);

  // Filter items matching StudentLibraryFilter.java
  const items = allRefs.filter((ref) => {
    const isAvailable = ref.lesson.url && ref.lesson.type !== 'none';
    const isFav = getFavorite(ref.key);
    const isDone = getCompleted(ref.key);
    const pos = getPosition(ref.key);

    if (filter === 'fav') return isAvailable && isFav;
    if (filter === 'done') return isAvailable && isDone;
    if (filter === 'continue') return isAvailable && pos > 0 && !isDone;
    return false;
  });

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

  const visibleItems = items.slice(0, displayLimit);

  return (
    <div className="space-y-4 pb-24">
      <div className="text-xs text-[#A4B6D2]">
        مفضلاتك، المحاضرات المكتملة، واستكمال المشاهدة المحفوظة على جهازك فقط.
      </div>

      {/* Filter Tabs */}
      <div className="grid grid-cols-3 gap-2 bg-[#122238] p-1 rounded-2xl border border-[#213C60]">
        <button
          onClick={() => {
            setFilter('fav');
            setDisplayLimit(50);
          }}
          className={`py-2.5 rounded-xl font-bold text-xs transition ${
            filter === 'fav'
              ? 'bg-[#203E6B] text-white shadow-md'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          المفضلة
        </button>

        <button
          onClick={() => {
            setFilter('done');
            setDisplayLimit(50);
          }}
          className={`py-2.5 rounded-xl font-bold text-xs transition ${
            filter === 'done'
              ? 'bg-[#203E6B] text-white shadow-md'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          المكتملة
        </button>

        <button
          onClick={() => {
            setFilter('continue');
            setDisplayLimit(50);
          }}
          className={`py-2.5 rounded-xl font-bold text-xs transition ${
            filter === 'continue'
              ? 'bg-[#203E6B] text-white shadow-md'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          المتابعة
        </button>
      </div>

      <div className="text-xs text-[#A4B6D2] font-semibold px-1">
        {items.length} محاضرة في هذا القسم
      </div>

      {/* List */}
      {visibleItems.length > 0 ? (
        <div className="space-y-2.5">
          {visibleItems.map((ref) => {
            const isFav = getFavorite(ref.key);
            const isDone = getCompleted(ref.key);

            return (
              <div
                key={ref.key}
                onClick={() => onOpenLesson(ref)}
                className="bg-[#14263D] hover:bg-[#1A314E] border border-[#203D61] p-3.5 rounded-2xl flex items-center justify-between transition cursor-pointer shadow-sm"
              >
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-10 h-10 rounded-xl bg-[#1E3B66] text-[#20C7B4] flex items-center justify-center font-bold text-sm flex-shrink-0">
                    <Play size={16} fill="currentColor" />
                  </div>

                  <div className="flex-1 min-w-0 pr-1">
                    <h4 className="font-bold text-sm text-white truncate">
                      {ref.lesson.title}
                    </h4>
                    <div className="text-[11px] text-[#A4B6D2] mt-0.5 truncate">
                      {ref.teacher.name} • {ref.chapter.title} • دفعة {ref.teacher.batch}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-1 flex-shrink-0">
                  <button
                    onClick={(e) => toggleFav(e, ref.key)}
                    className="p-2 text-[#A4B6D2] hover:text-[#E6B74A] transition"
                  >
                    <Star
                      size={18}
                      className={isFav ? 'text-[#E6B74A]' : 'text-[#647999]'}
                      fill={isFav ? '#E6B74A' : 'none'}
                    />
                  </button>

                  <button
                    onClick={(e) => toggleDone(e, ref.key)}
                    className="p-2 text-[#A4B6D2] hover:text-[#20C7B4] transition"
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

          {displayLimit < items.length && (
            <button
              onClick={() => setDisplayLimit((prev) => prev + 50)}
              className="w-full py-3 rounded-2xl bg-[#1C3658] hover:bg-[#234470] text-xs font-bold text-[#20C7B4] transition cursor-pointer"
            >
              عرض المزيد ({items.length - displayLimit} متبقية)
            </button>
          )}
        </div>
      ) : (
        /* Empty State */
        <div className="bg-[#14263D] border border-[#203D61] p-8 rounded-3xl text-center space-y-3 shadow-md">
          <div className="w-14 h-14 rounded-full bg-[#18304E] text-[#20C7B4] flex items-center justify-center mx-auto">
            <Bookmark size={28} />
          </div>
          <h4 className="text-base font-bold text-white">لا توجد محاضرات في هذا القسم بعد</h4>
          <p className="text-xs text-[#A4B6D2] leading-relaxed max-w-sm mx-auto">
            {filter === 'continue'
              ? 'ابدأ مشاهدة أي محاضرة، وراح يُحفظ موضع تقدمك تلقائياً لتستكمل المشاهدة هنا.'
              : filter === 'done'
              ? 'علّم أي محاضرة كـ «مكتملة» من داخل المشغل أو القائمة لتظهر هنا.'
              : 'احفظ محاضراتك المفضلة بالنقر على أيقونة النجمة للوصول السريع إليها.'}
          </p>
        </div>
      )}
    </div>
  );
};
