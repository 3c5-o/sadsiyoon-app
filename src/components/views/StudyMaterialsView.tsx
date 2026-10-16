import React, { useState } from 'react';
import { BookOpen, FileText, Sparkles, Download, ChevronLeft, Search } from 'lucide-react';
import { StudyMaterial } from '../../types';
import { getStudyMaterials } from '../../data/studyMaterials';

interface StudyMaterialsViewProps {
  onSelectMaterial: (material: StudyMaterial) => void;
}

export const StudyMaterialsView: React.FC<StudyMaterialsViewProps> = ({
  onSelectMaterial,
}) => {
  const [selectedSubject, setSelectedSubject] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const allMaterials = getStudyMaterials();

  const subjects = ['الرياضيات', 'الفيزياء', 'اللغة العربية', 'الكيمياء', 'الأحياء'];

  const filtered = allMaterials.filter((m) => {
    const matchesSubject = !selectedSubject || m.subject.includes(selectedSubject);
    const query = searchQuery.trim().toLowerCase();
    const matchesQuery =
      !query ||
      m.title.toLowerCase().includes(query) ||
      m.teacherName.toLowerCase().includes(query) ||
      m.description.toLowerCase().includes(query) ||
      m.topics.some((t) => t.toLowerCase().includes(query));
    return matchesSubject && matchesQuery;
  });

  return (
    <div className="space-y-4 pb-24">
      {/* Banner */}
      <div className="bg-gradient-to-br from-[#1C3A63] to-[#122642] p-5 rounded-3xl border border-[#2B548A]/60 shadow-lg space-y-2">
        <div className="flex items-center gap-2 text-xs font-bold text-[#20C7B4]">
          <BookOpen size={16} />
          <span>المكتبة الوزارية الذكية</span>
        </div>
        <h2 className="text-xl font-bold text-white">ملازم وخلاصات القوانين المعتمدة</h2>
        <p className="text-xs text-[#A4B6D2] leading-relaxed">
          مخططات ذهنية وخلاصات القوانين الوزارية لأشهر أساتذة العراق مرتبة بحسب الفصول مع أمثلة وتوضيحات شاملة.
        </p>
      </div>

      {/* Search Input */}
      <div className="relative">
        <Search
          size={16}
          className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[#A4B6D2]"
        />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="ابحث في عناوين الملازم وموضوعاتها..."
          className="w-full bg-[#14263D] border border-[#23426A] rounded-2xl pr-10 pl-4 py-2.5 text-xs text-white placeholder-[#A4B6D2] focus:outline-none focus:border-[#20C7B4]"
        />
      </div>

      {/* Subject Filter Chips */}
      <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
        <button
          onClick={() => setSelectedSubject('')}
          className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex-shrink-0 transition ${
            !selectedSubject
              ? 'bg-[#20C7B4] text-[#0B1626]'
              : 'bg-[#14263D] text-[#A4B6D2] hover:text-white border border-[#213B5E]'
          }`}
        >
          الكل ({allMaterials.length})
        </button>

        {subjects.map((sub) => {
          const isSelected = selectedSubject === sub;
          return (
            <button
              key={sub}
              onClick={() => setSelectedSubject(isSelected ? '' : sub)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold flex-shrink-0 transition border ${
                isSelected
                  ? 'bg-[#20C7B4] text-[#0B1626] border-[#20C7B4]'
                  : 'bg-[#14263D] text-[#A4B6D2] hover:text-white border-[#213B5E]'
              }`}
            >
              {sub}
            </button>
          );
        })}
      </div>

      {/* Materials List Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3.5">
        {filtered.map((mat) => (
          <div
            key={mat.id}
            className="bg-[#14263D] border border-[#213E64] p-4 rounded-2xl space-y-3 shadow-md hover:border-[#20C7B4]/60 transition flex flex-col justify-between"
          >
            <div className="space-y-2">
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-[10px] font-bold text-[#20C7B4] bg-[#20C7B4]/10 px-2.5 py-0.5 rounded-full border border-[#20C7B4]/30">
                    {mat.subject} • {mat.pagesCount} صفحات
                  </span>
                  <h3 className="font-bold text-base text-white mt-1.5 leading-snug">
                    {mat.title}
                  </h3>
                  <div className="text-xs text-[#E6B74A] font-medium mt-0.5">
                    إعداد الأستاذ: {mat.teacherName}
                  </div>
                </div>

                <div className="w-10 h-10 rounded-xl bg-[#1B3558] text-[#20C7B4] flex items-center justify-center flex-shrink-0 mr-2 shadow-inner">
                  <FileText size={20} />
                </div>
              </div>

              <p className="text-xs text-[#D0E0F2] leading-relaxed">{mat.description}</p>

              {/* Topics tags */}
              <div className="flex flex-wrap gap-1 pt-1">
                {mat.topics.slice(0, 3).map((topic, i) => (
                  <span
                    key={i}
                    className="text-[10px] text-[#A4B6D2] bg-[#0E1A2C] px-2 py-0.5 rounded-md border border-[#1E3758]"
                  >
                    {topic}
                  </span>
                ))}
              </div>
            </div>

            <button
              onClick={() => onSelectMaterial(mat)}
              className="w-full mt-3 py-2.5 rounded-xl bg-[#20C7B4] hover:bg-[#18AFA0] text-[#0B1626] font-bold text-xs transition flex items-center justify-center gap-1.5 cursor-pointer shadow-md"
            >
              <BookOpen size={15} />
              <span>فتح وتصفح الملزمة</span>
            </button>
          </div>
        ))}

        {filtered.length === 0 && (
          <div className="col-span-full bg-[#14263D] border border-[#203D61] p-8 rounded-2xl text-center text-xs text-[#A4B6D2]">
            لا توجد ملازم مطابقة لبحثك في هذا القسم.
          </div>
        )}
      </div>
    </div>
  );
};
