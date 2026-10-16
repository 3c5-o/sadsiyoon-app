import React, { useState } from 'react';
import { X, Calculator, Award, Sparkles, Check, CheckCircle2, AlertCircle } from 'lucide-react';
import {
  calculateMinisterialGpa,
  SCIENTIFIC_DEFAULT_SUBJECTS,
  LITERARY_DEFAULT_SUBJECTS,
  GpaCalculationOptions,
} from '../../utils/gpaCalculator';

interface GpaCalculatorModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const GpaCalculatorModal: React.FC<GpaCalculatorModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [branch, setBranch] = useState<'scientific' | 'literary'>('scientific');
  const [grades, setGrades] = useState<Record<string, number>>({});
  const [isFirstRoundPass, setIsFirstRoundPass] = useState<boolean>(true);
  const [hasFrench, setHasFrench] = useState<boolean>(false);
  const [frenchGrade, setFrenchGrade] = useState<number>(85);
  const [martyrBonus, setMartyrBonus] = useState<number>(0);

  if (!isOpen) return null;

  const subjects =
    branch === 'scientific' ? SCIENTIFIC_DEFAULT_SUBJECTS : LITERARY_DEFAULT_SUBJECTS;

  const handleGradeChange = (sub: string, val: string) => {
    const num = parseInt(val, 10);
    if (isNaN(num)) {
      const copy = { ...grades };
      delete copy[sub];
      setGrades(copy);
    } else {
      setGrades({ ...grades, [sub]: Math.min(100, Math.max(0, num)) });
    }
  };

  const options: GpaCalculationOptions = {
    branch,
    isFirstRoundPass,
    hasFrenchLanguage: hasFrench,
    frenchGrade: hasFrench ? frenchGrade : undefined,
    martyrBonus,
  };

  const result = calculateMinisterialGpa(grades, options);

  const filledCount = Object.keys(grades).filter((k) => subjects.includes(k)).length;
  const isComplete = filledCount === subjects.length;

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-3 sm:p-6 overflow-y-auto">
      <div className="bg-[#0E1A2E] border border-[#23426D] rounded-3xl w-full max-w-2xl max-h-[90vh] flex flex-col shadow-2xl overflow-hidden my-auto animate-in fade-in zoom-in-95">
        {/* Header */}
        <div className="bg-gradient-to-r from-[#142642] to-[#1B355A] px-5 py-4 flex items-center justify-between border-b border-[#254674]">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-xl bg-[#20C7B4]/20 text-[#20C7B4] flex items-center justify-center border border-[#20C7B4]/40">
              <Calculator size={22} />
            </div>
            <div>
              <h3 className="font-bold text-base text-white">حاسبة المعدل الوزاري العراقي</h3>
              <p className="text-xs text-[#A4B6D2]">
                حساب دقيق للمجموع والمعدل والدرجات المضافة والتخصصات المتاحة
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-1.5 rounded-xl bg-[#1D3657] hover:bg-[#254673] text-[#A4B6D2] hover:text-white transition"
          >
            <X size={18} />
          </button>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-5">
          {/* Branch Switcher */}
          <div className="grid grid-cols-2 gap-2 bg-[#122238] p-1.5 rounded-2xl border border-[#213C60]">
            <button
              onClick={() => {
                setBranch('scientific');
                setGrades({});
              }}
              className={`py-2.5 rounded-xl font-bold text-xs transition ${
                branch === 'scientific'
                  ? 'bg-[#203E6B] text-white shadow-md'
                  : 'text-[#A4B6D2] hover:text-white'
              }`}
            >
              الفرع العلمي (7 مواد)
            </button>

            <button
              onClick={() => {
                setBranch('literary');
                setGrades({});
              }}
              className={`py-2.5 rounded-xl font-bold text-xs transition ${
                branch === 'literary'
                  ? 'bg-[#203E6B] text-white shadow-md'
                  : 'text-[#A4B6D2] hover:text-white'
              }`}
            >
              الفرع الأدبي (7 مواد)
            </button>
          </div>

          {/* Grades Inputs Grid */}
          <div className="bg-[#122239] border border-[#213C62] p-4 rounded-2xl space-y-3">
            <div className="flex items-center justify-between text-xs">
              <span className="font-bold text-white">أدخل درجات المواد (من 100):</span>
              <span className="text-[#20C7B4] font-semibold">
                {filledCount} من {subjects.length} مواد مكتملة
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {subjects.map((sub) => {
                const val = grades[sub] ?? '';
                return (
                  <div
                    key={sub}
                    className="flex items-center justify-between bg-[#0B1626] border border-[#203D63] px-3.5 py-2.5 rounded-xl"
                  >
                    <label className="text-xs font-semibold text-[#D5E5F7] truncate max-w-[140px]">
                      {sub}
                    </label>
                    <input
                      type="number"
                      min="0"
                      max="100"
                      placeholder="0"
                      value={val}
                      onChange={(e) => handleGradeChange(sub, e.target.value)}
                      className="w-16 bg-[#162B48] text-center text-sm font-bold text-white border border-[#254977] rounded-lg py-1 px-1 focus:outline-none focus:border-[#20C7B4]"
                    />
                  </div>
                );
              })}
            </div>
          </div>

          {/* Ministerial Bonuses */}
          <div className="bg-[#122239] border border-[#213C62] p-4 rounded-2xl space-y-3">
            <h4 className="text-xs font-bold text-white">إضافات ودرجات المفاضلة الوزارية</h4>

            <div className="space-y-2">
              <label className="flex items-center justify-between p-2.5 rounded-xl bg-[#0B1626] border border-[#1E3758] cursor-pointer hover:bg-[#102035] transition">
                <div className="text-xs">
                  <div className="font-semibold text-white">درجة الدور الأول الوزاري (+1)</div>
                  <div className="text-[11px] text-[#A4B6D2]">
                    تُمنح للطالب الناجح من الدور الأول بجميع المواد
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={isFirstRoundPass}
                  onChange={(e) => setIsFirstRoundPass(e.target.checked)}
                  className="rounded accent-[#20C7B4] w-4 h-4 cursor-pointer"
                />
              </label>

              <div className="p-2.5 rounded-xl bg-[#0B1626] border border-[#1E3758] space-y-2">
                <div className="flex items-center justify-between cursor-pointer">
                  <div className="text-xs">
                    <div className="font-semibold text-white">درجة اللغة الفرنسية الإضافية</div>
                    <div className="text-[11px] text-[#A4B6D2]">
                      تُضاف نسبة 8% من درجة اللغة الفرنسية إلى المجموع الكلي
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    checked={hasFrench}
                    onChange={(e) => setHasFrench(e.target.checked)}
                    className="rounded accent-[#20C7B4] w-4 h-4 cursor-pointer"
                  />
                </div>

                {hasFrench && (
                  <div className="flex items-center gap-2 pt-2 border-t border-[#1E3758]">
                    <span className="text-xs text-[#A4B6D2]">درجة اللغة الفرنسية:</span>
                    <input
                      type="number"
                      min="50"
                      max="100"
                      value={frenchGrade}
                      onChange={(e) => setFrenchGrade(parseInt(e.target.value, 10) || 50)}
                      className="w-20 bg-[#162B48] text-center text-xs font-bold text-white border border-[#254977] rounded-lg py-1 px-1 focus:outline-none focus:border-[#20C7B4]"
                    />
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Results Summary Card */}
          <div className="bg-gradient-to-br from-[#1C3E69] to-[#122846] border border-[#2F5B96] p-5 rounded-3xl shadow-lg space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs text-[#20C7B4] font-semibold">المعدل النهائي التراكمي</span>
                <div className="text-3xl font-black text-white font-mono mt-0.5">
                  {result.finalGpa}%
                </div>
              </div>

              <div className="text-left">
                <span className="text-xs text-[#A4B6D2]">المجموع الكلي</span>
                <div className="text-xl font-bold text-[#E6B74A] font-mono mt-0.5">
                  {result.totalSum} / {result.maxTotal}
                </div>
              </div>
            </div>

            {/* Bonuses list */}
            {result.appliedBonuses.length > 0 && (
              <div className="flex flex-wrap gap-1.5 pt-2 border-t border-white/10 text-[11px]">
                {result.appliedBonuses.map((b, i) => (
                  <span
                    key={i}
                    className="bg-[#20C7B4]/15 border border-[#20C7B4]/30 text-[#20C7B4] px-2.5 py-0.5 rounded-full"
                  >
                    {b.name}: +{b.value}
                  </span>
                ))}
              </div>
            )}
          </div>

          {/* Predicted Specializations */}
          {result.finalGpa > 0 && (
            <div className="space-y-2">
              <h4 className="text-xs font-bold text-[#E6B74A] flex items-center gap-1.5">
                <Award size={15} />
                <span>الحدود الدنيا والكليات المتوقعة لمعدلك ({result.finalGpa}%)</span>
              </h4>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {result.predictedSpecializations.map((spec, i) => (
                  <div
                    key={i}
                    className={`p-3 rounded-xl border flex items-center justify-between text-xs ${
                      spec.suitable
                        ? 'bg-[#15342D] border-[#20C7B4]/50 text-white'
                        : 'bg-[#162132] border-[#22354E] text-[#6E819C] opacity-60'
                    }`}
                  >
                    <div>
                      <div className="font-bold">{spec.name}</div>
                      <div className="text-[10px] mt-0.5 opacity-80">
                        الحد الأدنى التقريبي: {spec.minGpa}%
                      </div>
                    </div>
                    {spec.suitable ? (
                      <CheckCircle2 size={16} className="text-[#20C7B4] flex-shrink-0" />
                    ) : (
                      <span className="text-[10px] text-[#A4B6D2]">أعلى من المعدل</span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="bg-[#111F35] px-5 py-3.5 flex items-center justify-between border-t border-[#203B5E]">
          <button
            onClick={() => setGrades({})}
            className="px-4 py-2 rounded-xl bg-[#1C3352] text-xs font-semibold text-[#A4B6D2] hover:text-white transition"
          >
            إعادة تعيين الدرجات
          </button>
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-[#20C7B4] text-[#0B1626] font-bold text-xs hover:bg-[#18AFA0] transition shadow-md"
          >
            إغلاق الحاسبة
          </button>
        </div>
      </div>
    </div>
  );
};
