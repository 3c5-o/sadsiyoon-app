import React, { useState } from 'react';
import {
  X,
  BookOpen,
  ChevronRight,
  ChevronLeft,
  ZoomIn,
  ZoomOut,
  Download,
  List,
  Sparkles,
  FileText,
} from 'lucide-react';
import { StudyMaterial } from '../../types';

interface PdfBookViewerModalProps {
  material: StudyMaterial | null;
  onClose: () => void;
}

export const PdfBookViewerModal: React.FC<PdfBookViewerModalProps> = ({
  material,
  onClose,
}) => {
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [zoomLevel, setZoomLevel] = useState<number>(100);
  const [showToc, setShowToc] = useState<boolean>(false);

  if (!material) return null;

  const totalPages = material.pages.length;
  const pageData = material.pages[currentPage] || material.pages[0];

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) setCurrentPage((p) => p + 1);
  };

  const handlePrevPage = () => {
    if (currentPage > 0) setCurrentPage((p) => p - 1);
  };

  const handleDownloadText = () => {
    const fullText = material.pages
      .map(
        (p) =>
          `=== صفحة ${p.pageNumber}: ${p.title} ===\n\n${p.content}\n\nالقوانين:\n${(
            p.keyFormulas || []
          ).join('\n')}\n\nالملاحظات:\n${(p.summaryNotes || []).join('\n')}\n\n`
      )
      .join('\n-----------------------------------------\n\n');

    const blob = new Blob([`${material.title}\nالأستاذ: ${material.teacherName}\nالمادة: ${material.subject}\n\n${fullText}`], {
      type: 'text/plain;charset=utf-8',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${material.title}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/90 backdrop-blur-md flex items-center justify-center p-2 sm:p-5">
      <div className="bg-[#0B1626] border border-[#23426D] rounded-3xl w-full max-w-4xl h-[92vh] flex flex-col shadow-2xl overflow-hidden animate-in fade-in zoom-in-95">
        {/* Header Toolbar */}
        <div className="bg-[#111F34] px-4 py-3 flex items-center justify-between border-b border-[#213C60] flex-shrink-0">
          <div className="flex items-center gap-3 truncate">
            <div className="w-9 h-9 rounded-xl bg-[#20C7B4]/20 text-[#20C7B4] flex items-center justify-center flex-shrink-0 border border-[#20C7B4]/40">
              <BookOpen size={18} />
            </div>

            <div className="truncate">
              <h3 className="font-bold text-sm text-white truncate">{material.title}</h3>
              <p className="text-[11px] text-[#A4B6D2] truncate">
                {material.teacherName} • {material.subject} • دفعة {material.batch}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowToc(!showToc)}
              className={`p-2 rounded-xl transition ${
                showToc
                  ? 'bg-[#20C7B4] text-[#0B1626]'
                  : 'bg-[#183152] text-[#A4B6D2] hover:text-white'
              }`}
              title="فهرس الموضوعات"
            >
              <List size={16} />
            </button>

            <button
              onClick={handleDownloadText}
              className="p-2 rounded-xl bg-[#183152] text-[#20C7B4] hover:bg-[#20406A] transition"
              title="تنزيل الملزمة كملف نصي"
            >
              <Download size={16} />
            </button>

            <button
              onClick={onClose}
              className="p-2 rounded-xl bg-[#183152] text-[#A4B6D2] hover:text-white transition"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Reader Center with Side Table of Contents if toggled */}
        <div className="flex-1 flex overflow-hidden relative">
          {/* TOC Sidebar */}
          {showToc && (
            <div className="w-64 bg-[#0E1A2C] border-l border-[#203D63] p-4 overflow-y-auto space-y-2 flex-shrink-0">
              <h4 className="text-xs font-bold text-[#E6B74A] mb-3">فهرس الصفحات والمواضيع</h4>
              {material.pages.map((p, idx) => (
                <button
                  key={idx}
                  onClick={() => {
                    setCurrentPage(idx);
                    setShowToc(false);
                  }}
                  className={`w-full text-right p-2.5 rounded-xl text-xs font-medium transition flex items-center justify-between ${
                    currentPage === idx
                      ? 'bg-[#203E6B] text-white font-bold'
                      : 'text-[#A4B6D2] hover:bg-[#14263D] hover:text-white'
                  }`}
                >
                  <span className="truncate">{p.title}</span>
                  <span className="text-[10px] text-[#20C7B4] mr-2">ص {p.pageNumber}</span>
                </button>
              ))}
            </div>
          )}

          {/* Page Display Stage */}
          <div className="flex-1 overflow-y-auto p-4 sm:p-8 flex items-start justify-center bg-[#070D17]">
            <div
              className="w-full max-w-2xl bg-[#0F1C30] border border-[#23436C] rounded-2xl shadow-xl p-6 sm:p-8 space-y-6 transition-all"
              style={{ fontSize: `${zoomLevel}%` }}
            >
              {/* Sheet Header */}
              <div className="flex items-center justify-between border-b border-[#213C60] pb-4">
                <div>
                  <span className="text-[10px] font-bold text-[#20C7B4] bg-[#20C7B4]/15 px-2.5 py-0.5 rounded-full border border-[#20C7B4]/30">
                    صفحة {pageData.pageNumber} من {totalPages}
                  </span>
                  <h2 className="text-lg font-bold text-white mt-2">{pageData.title}</h2>
                </div>
                <div className="text-right text-xs text-[#E6B74A] font-semibold">
                  سادسيون 2026/2027
                </div>
              </div>

              {/* Main Content Body */}
              <div className="text-sm text-[#D7E6F7] whitespace-pre-wrap leading-relaxed">
                {pageData.content}
              </div>

              {/* Key Formulas Section */}
              {pageData.keyFormulas && pageData.keyFormulas.length > 0 && (
                <div className="bg-[#132742] border border-[#294F80] p-4 rounded-xl space-y-2">
                  <h4 className="text-xs font-bold text-[#20C7B4] flex items-center gap-1.5">
                    <Sparkles size={14} />
                    <span>القوانين الذهبية والعلاقات الرياضية:</span>
                  </h4>
                  <ul className="space-y-1 text-xs font-mono text-[#F0F6FF]">
                    {pageData.keyFormulas.map((f, i) => (
                      <li key={i} className="bg-[#0B1626]/70 p-2 rounded-lg border border-[#1E3A60]">
                        {f}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Summary Notes */}
              {pageData.summaryNotes && pageData.summaryNotes.length > 0 && (
                <div className="bg-[#1C2C28] border border-[#2E594F] p-4 rounded-xl space-y-2">
                  <h4 className="text-xs font-bold text-[#E6B74A]">ملاحظات وزارية وتنبيهات مهمة:</h4>
                  <ul className="space-y-1.5 text-xs text-[#D8EFEB]">
                    {pageData.summaryNotes.map((n, i) => (
                      <li key={i} className="flex items-start gap-2">
                        <span className="text-[#20C7B4] font-bold">•</span>
                        <span>{n}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Reader Bottom Navigation Bar */}
        <div className="bg-[#111F34] px-4 py-3 flex items-center justify-between border-t border-[#213C60] flex-shrink-0">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setZoomLevel((z) => Math.max(80, z - 10))}
              className="p-1.5 rounded-lg bg-[#183152] text-[#A4B6D2] hover:text-white"
              title="تصغير الخط"
            >
              <ZoomOut size={16} />
            </button>
            <span className="text-xs text-[#A4B6D2] font-mono">{zoomLevel}%</span>
            <button
              onClick={() => setZoomLevel((z) => Math.min(140, z + 10))}
              className="p-1.5 rounded-lg bg-[#183152] text-[#A4B6D2] hover:text-white"
              title="تكبير الخط"
            >
              <ZoomIn size={16} />
            </button>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handlePrevPage}
              disabled={currentPage === 0}
              className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-[#1C3658] text-white text-xs font-bold disabled:opacity-40 hover:bg-[#254673] transition"
            >
              <ChevronRight size={16} />
              <span>السابقة</span>
            </button>

            <span className="text-xs font-bold text-white font-mono">
              {currentPage + 1} / {totalPages}
            </span>

            <button
              onClick={handleNextPage}
              disabled={currentPage === totalPages - 1}
              className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-[#20C7B4] text-[#0B1626] text-xs font-bold disabled:opacity-40 hover:bg-[#18AFA0] transition"
            >
              <span>التالية</span>
              <ChevronLeft size={16} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
