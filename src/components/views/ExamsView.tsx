import React, { useState } from 'react';
import { FileText, ExternalLink, ChevronLeft, ChevronRight, X, AlertCircle } from 'lucide-react';
import { ExamPaper } from '../../types';
import { getExamPapers } from '../../data/catalog';

export const ExamsView: React.FC = () => {
  const [examBranch, setExamBranch] = useState<'scientific' | 'literary'>('scientific');
  const [examSubject, setExamSubject] = useState<string>('الرياضيات');
  const [examRound, setExamRound] = useState<string>('الدور الأول');

  // Modal viewer state
  const [activePaper, setActivePaper] = useState<ExamPaper | null>(null);
  const [pageIndex, setPageIndex] = useState<number>(0);

  const scientificSubjects = [
    'الرياضيات',
    'الفيزياء',
    'الكيمياء',
    'الأحياء',
    'اللغة العربية',
    'اللغة الإنكليزية',
    'التربية الإسلامية',
  ];

  const literarySubjects = [
    'الرياضيات',
    'التاريخ',
    'الجغرافية',
    'الاقتصاد',
    'اللغة العربية',
    'اللغة الإنكليزية',
    'التربية الإسلامية',
  ];

  const currentSubjects = examBranch === 'scientific' ? scientificSubjects : literarySubjects;

  const rounds = ['الدور الأول', 'الدور الثاني', 'الدور الثالث', 'التمهيدي'];

  const papers = getExamPapers(2025, examBranch, examSubject, examRound);

  const aselatnaUrl =
    examBranch === 'scientific'
      ? 'https://www.aselatna.com/search/label/%D8%A7%D9%84%D8%B3%D8%A7%D8%AF%D8%B3%20%D8%A7%D9%84%D8%B9%D9%84%D9%85%D9%8A?m=1'
      : 'https://www.aselatna.com/search/label/%D8%A7%D9%84%D8%B3%D8%A7%D8%AF%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A?m=1';

  return (
    <div className="space-y-5 pb-24">
      {/* Year Banner */}
      <div className="bg-gradient-to-br from-[#203D65] to-[#142642] p-5 rounded-3xl border border-[#2B548A]/60 shadow-lg">
        <div className="text-3xl font-black text-[#E6B74A] font-mono">2025</div>
        <p className="text-xs text-[#A4B6D2] mt-1">
          أوراق وزارية 2025 — نماذج علمي وأدبي من ملازمنا • الاعتماد على السنة المكتوبة في الورقة.
        </p>
      </div>

      {/* Branch Switcher */}
      <div className="grid grid-cols-2 gap-2 bg-[#122238] p-1 rounded-2xl border border-[#213C60]">
        <button
          onClick={() => {
            setExamBranch('scientific');
            setExamSubject('الرياضيات');
          }}
          className={`py-2.5 rounded-xl font-bold text-xs transition ${
            examBranch === 'scientific'
              ? 'bg-[#203E6B] text-white shadow-md'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          السادس العلمي
        </button>

        <button
          onClick={() => {
            setExamBranch('literary');
            setExamSubject('الرياضيات');
          }}
          className={`py-2.5 rounded-xl font-bold text-xs transition ${
            examBranch === 'literary'
              ? 'bg-[#203E6B] text-white shadow-md'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          السادس الأدبي
        </button>
      </div>

      {/* Subject Filter Buttons */}
      <div>
        <h4 className="text-xs font-bold text-[#A4B6D2] mb-2">اختر المادة</h4>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
          {currentSubjects.map((sub) => {
            const isSelected = examSubject === sub;
            return (
              <button
                key={sub}
                onClick={() => setExamSubject(sub)}
                className={`py-2.5 px-3 rounded-xl text-xs font-bold transition border ${
                  isSelected
                    ? 'bg-[#20C7B4] text-[#0B1626] border-[#20C7B4] shadow-sm'
                    : 'bg-[#14263D] text-[#D0E0F2] border-[#203B5E] hover:bg-[#1A314E]'
                }`}
              >
                {sub}
              </button>
            );
          })}
        </div>
      </div>

      {/* Round Filter */}
      <div>
        <h4 className="text-xs font-bold text-[#A4B6D2] mb-2">اختر الدور</h4>
        <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
          {rounds.map((role) => {
            const isSelected = examRound === role;
            return (
              <button
                key={role}
                onClick={() => setExamRound(role)}
                className={`flex-shrink-0 px-4 py-2 rounded-xl text-xs font-bold transition border ${
                  isSelected
                    ? 'bg-[#203E6B] text-white border-[#20C7B4]'
                    : 'bg-[#14263D] text-[#A4B6D2] border-[#213B5E] hover:text-white'
                }`}
              >
                {role}
              </button>
            );
          })}
        </div>
      </div>

      {/* Papers Display */}
      <div>
        <h4 className="text-sm font-bold text-white mb-2">أوراق الامتحان</h4>

        {papers.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
            {papers.map((paper, idx) => (
              <div
                key={idx}
                className="bg-[#14263D] border border-[#213C60] p-4 rounded-2xl space-y-3 shadow-md"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h5 className="font-bold text-base text-white">{paper.title}</h5>
                    <div className="text-xs text-[#20C7B4] mt-0.5">
                      المصدر: ملازمنا • {paper.round} • {paper.subject}
                    </div>
                  </div>
                  <FileText size={24} className="text-[#20C7B4] flex-shrink-0" />
                </div>

                <div className="flex items-center gap-2 pt-1">
                  {paper.images.length > 0 ? (
                    <button
                      onClick={() => {
                        setActivePaper(paper);
                        setPageIndex(0);
                      }}
                      className="flex-1 py-2.5 rounded-xl bg-[#20C7B4] text-[#0B1626] font-bold text-xs transition cursor-pointer"
                    >
                      عرض صور الورقة ({paper.images.length} صفحة)
                    </button>
                  ) : (
                    <button
                      onClick={() => {
                        setActivePaper(paper);
                        setPageIndex(0);
                      }}
                      className="flex-1 py-2.5 rounded-xl bg-[#20C7B4] text-[#0B1626] font-bold text-xs transition cursor-pointer"
                    >
                      عرض ورقة ملازمنا
                    </button>
                  )}

                  {paper.source && (
                    <a
                      href={paper.source}
                      target="_blank"
                      rel="noreferrer"
                      className="px-3 py-2.5 rounded-xl bg-[#1D3557] hover:bg-[#254573] text-white text-xs flex items-center gap-1 transition"
                      title="فتح في ملازمنا"
                    >
                      <ExternalLink size={14} />
                      <span>المصدر</span>
                    </a>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-3">
            <div className="bg-[#14263D] border border-[#203D61] p-5 rounded-2xl text-center space-y-2">
              <div className="text-sm font-bold text-[#E6B74A]">
                لا توجد ورقة 2025 مؤكدة بهذا التصنيف حالياً
              </div>
              <p className="text-xs text-[#A4B6D2] leading-relaxed max-w-sm mx-auto">
                المتوفر حالياً من ملازمنا: رياضيات وعربي 2025 الدور الأول لكل فرع. بقية المواد والأدوار تنتظر فحص وتأكيد المصدر الرسمي.
              </p>
            </div>

            {/* Exam Placeholder Model Sheet */}
            <div className="relative bg-[#F4F5EF] text-[#15383B] p-6 rounded-2xl min-h-[220px] flex flex-col justify-between overflow-hidden shadow-inner border border-[#E0E2D8]">
              <div className="flex items-center justify-between text-xs font-bold">
                <span>سادسيون | نموذج معاينة المعرض</span>
                <span className="text-[#637276]">2025</span>
              </div>

              <div className="text-center py-6">
                <div className="text-base font-black text-[#253B40]">
                  مكان صورة ورقة الامتحان الأصلية
                </div>
                <div className="text-xs text-[#637276] mt-1">
                  تُعرض الأوراق كما طُبعت بالدقة الأصلية ودون تشويه
                </div>
              </div>

              <div className="text-center text-[11px] text-[#637276]">
                لا تحتوي هذه المعاينة على أسئلة وزارية فعلية
              </div>

              {/* Translucent Watermark Overlay */}
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none select-none">
                <span className="text-6xl font-black text-[#14745C]/15 rotate-[-15deg]">
                  سادسيون
                </span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Sources Card */}
      <div className="bg-[#14263D] border border-[#203D61] p-4 rounded-2xl space-y-2">
        <h5 className="text-sm font-bold text-white">المصادر والأرشيف</h5>
        <p className="text-xs text-[#A4B6D2] leading-relaxed">
          أوراق التجربة من ملازمنا مع الإبقاء على مصدرها وعلاماتها الأصلية. ويمكنك زيارة قسم أسئلتنا للاطلاع على السنوات السابقة.
        </p>
        <a
          href={aselatnaUrl}
          target="_blank"
          rel="noreferrer"
          className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-[#1C3658] hover:bg-[#234470] text-[#20C7B4] font-bold text-xs transition mt-1"
        >
          <ExternalLink size={14} />
          <span>زيارة قسم {examBranch === 'scientific' ? 'العلمي' : 'الأدبي'} في أسئلتنا</span>
        </a>
      </div>

      {/* Paper Image / Web Viewer Modal */}
      {activePaper && (
        <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-sm flex flex-col p-3 sm:p-6 justify-center items-center">
          <div className="bg-[#0B1626] border border-[#25426C] rounded-2xl w-full max-w-xl max-h-[90vh] flex flex-col overflow-hidden shadow-2xl">
            {/* Modal Header */}
            <div className="bg-[#111F34] px-4 py-3 flex items-center justify-between border-b border-[#203B5E]">
              <div>
                <h3 className="font-bold text-sm text-white">{activePaper.title}</h3>
                {activePaper.images.length > 0 && (
                  <div className="text-xs text-[#20C7B4]">
                    صفحة {pageIndex + 1} من {activePaper.images.length}
                  </div>
                )}
              </div>
              <button
                onClick={() => setActivePaper(null)}
                className="p-1 text-[#A4B6D2] hover:text-white"
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal Body */}
            <div className="flex-1 overflow-y-auto p-4 flex items-center justify-center min-h-[350px]">
              {activePaper.images.length > 0 ? (
                <div className="relative max-w-full max-h-[60vh] bg-white rounded-xl overflow-hidden flex items-center justify-center p-2">
                  <img
                    src={activePaper.images[pageIndex]}
                    alt={`ورقة امتحان ${activePaper.title} صفحة ${pageIndex + 1}`}
                    className="max-h-[55vh] object-contain"
                  />
                  {/* Sadsiyoon watermark overlay */}
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none select-none">
                    <span className="text-5xl font-black text-[#11795E]/20 rotate-[-15deg]">
                      سادسيون
                    </span>
                  </div>
                </div>
              ) : (
                <div className="w-full text-center space-y-4 py-6">
                  <div className="w-16 h-16 rounded-full bg-[#183152] text-[#20C7B4] flex items-center justify-center mx-auto">
                    <FileText size={32} />
                  </div>
                  <h4 className="text-base font-bold text-white">ورقة ملازمنا الأصلية</h4>
                  <p className="text-xs text-[#A4B6D2] max-w-md mx-auto leading-relaxed">
                    هذه الورقة مفحوصة ومربوطة بمصدرها الأصلي في موقع ملازمنا. يمكنك استعراضها مباشرة من صفحة المصدر الرسمية.
                  </p>
                  <a
                    href={activePaper.source}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#20C7B4] text-[#0B1626] font-bold text-xs shadow-md transition"
                  >
                    <span>فتح صفحة الورقة في ملازمنا</span>
                    <ExternalLink size={16} />
                  </a>
                </div>
              )}
            </div>

            {/* Modal Footer (Page controls if images exist) */}
            {activePaper.images.length > 1 && (
              <div className="bg-[#111F34] p-3 flex items-center justify-between border-t border-[#203B5E]">
                <button
                  onClick={() => setPageIndex((p) => Math.max(0, p - 1))}
                  disabled={pageIndex === 0}
                  className="px-4 py-2 rounded-xl bg-[#1C3658] text-xs font-bold text-white disabled:opacity-40"
                >
                  الصفحة السابقة
                </button>
                <button
                  onClick={() =>
                    setPageIndex((p) => Math.min(activePaper.images.length - 1, p + 1))
                  }
                  disabled={pageIndex === activePaper.images.length - 1}
                  className="px-4 py-2 rounded-xl bg-[#20C7B4] text-xs font-bold text-[#0B1626] disabled:opacity-40"
                >
                  الصفحة التالية
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
