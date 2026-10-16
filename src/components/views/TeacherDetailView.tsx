import React from 'react';
import { ChevronLeft, Layers, Video, CheckCircle2 } from 'lucide-react';
import { Teacher } from '../../types';

interface TeacherDetailViewProps {
  teacher: Teacher;
  onSelectChapter: (chapterIndex: number) => void;
}

export const TeacherDetailView: React.FC<TeacherDetailViewProps> = ({
  teacher,
  onSelectChapter,
}) => {
  let totalLectures = 0;
  let readyCount = 0;
  teacher.chapters.forEach((c) => {
    totalLectures += c.lessons.length;
    c.lessons.forEach((l) => {
      if (l.url && l.type !== 'none') readyCount++;
    });
  });

  return (
    <div className="space-y-5 pb-24">
      {/* Teacher Hero Banner */}
      <div className="bg-gradient-to-br from-[#244776] to-[#152B4B] p-5 rounded-3xl border border-[#30578F]/60 shadow-xl relative overflow-hidden">
        <div className="flex items-center gap-4">
          <div className="w-20 h-20 rounded-2xl overflow-hidden bg-[#183154] border-2 border-[#294F80] flex items-center justify-center text-2xl font-bold text-white flex-shrink-0 shadow-lg">
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

          <div>
            <span className="text-[11px] font-bold text-[#E6B74A] bg-[#E6B74A]/10 px-2.5 py-0.5 rounded-full border border-[#E6B74A]/30">
              دفعة {teacher.batch}
            </span>
            <h2 className="text-xl font-bold text-white mt-1.5">
              الأستاذ {teacher.name}
            </h2>
            <div className="text-sm text-[#20C7B4] font-semibold mt-0.5">
              مادة {teacher.subject}
            </div>
          </div>
        </div>

        {/* Stats Row */}
        <div className="grid grid-cols-3 gap-2 mt-5 pt-4 border-t border-white/10 text-center">
          <div className="p-2 bg-black/20 rounded-xl">
            <div className="text-base font-bold text-white font-mono">{teacher.chapters.length}</div>
            <div className="text-[10px] text-[#A4B6D2]">الفصول والأقسام</div>
          </div>
          <div className="p-2 bg-black/20 rounded-xl">
            <div className="text-base font-bold text-[#20C7B4] font-mono">{totalLectures}</div>
            <div className="text-[10px] text-[#A4B6D2]">المحاضرات</div>
          </div>
          <div className="p-2 bg-black/20 rounded-xl">
            <div className="text-base font-bold text-[#E6B74A] font-mono">{readyCount}</div>
            <div className="text-[10px] text-[#A4B6D2]">روابط مفهرسة</div>
          </div>
        </div>
      </div>

      {/* Chapters Header */}
      <div>
        <h3 className="text-lg font-bold text-white mb-3">الفصول والمحاضرات</h3>

        <div className="space-y-3">
          {teacher.chapters.map((ch, idx) => (
            <div
              key={idx}
              onClick={() => onSelectChapter(idx)}
              className="bg-[#14263D] hover:bg-[#1A3252] border border-[#203D63] p-4 rounded-2xl flex items-center justify-between transition cursor-pointer group shadow-sm"
            >
              <div className="flex items-center gap-3.5">
                <div className="w-10 h-10 rounded-xl bg-[#1B3558] text-[#E6B74A] flex items-center justify-center font-bold text-base flex-shrink-0 group-hover:scale-105 transition-transform">
                  {idx + 1}
                </div>

                <div>
                  <h4 className="font-bold text-base text-white group-hover:text-[#20C7B4] transition-colors">
                    {ch.title}
                  </h4>
                  <div className="text-xs text-[#A4B6D2] mt-0.5">
                    {ch.lessons.length} محاضرة
                  </div>
                </div>
              </div>

              <ChevronLeft size={20} className="text-[#20C7B4]" />
            </div>
          ))}

          {teacher.chapters.length === 0 && (
            <div className="bg-[#14263D] border border-[#203D63] p-8 rounded-2xl text-center text-xs text-[#A4B6D2]">
              لم تتم إضافة فصول لهذا المدرس بعد.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
