import React, { useState, useRef } from 'react';
import {
  User,
  Settings,
  Download,
  Upload,
  Trash2,
  CheckCircle,
  AlertTriangle,
  Sparkles,
} from 'lucide-react';
import {
  getNickname,
  setNickname,
  getAutoplay,
  setAutoplay,
  exportBackup,
  importBackup,
  clearAllProgress,
} from '../../utils/storage';

interface AccountViewProps {
  onRefreshData: () => void;
}

export const AccountView: React.FC<AccountViewProps> = ({ onRefreshData }) => {
  const [nickname, setNicknameState] = useState<string>(getNickname());
  const [isEditingName, setIsEditingName] = useState<boolean>(false);
  const [tempName, setTempName] = useState<string>(nickname);
  const [autoplay, setAutoplayState] = useState<boolean>(getAutoplay());
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const handleSaveName = () => {
    const trimmed = tempName.trim();
    if (trimmed) {
      setNickname(trimmed);
      setNicknameState(trimmed);
      setIsEditingName(false);
      setStatusMessage('تم تحديث اسم الطالب بنجاح');
      setTimeout(() => setStatusMessage(null), 3000);
    }
  };

  const handleToggleAutoplay = () => {
    const next = !autoplay;
    setAutoplay(next);
    setAutoplayState(next);
  };

  const handleExport = () => {
    try {
      const json = exportBackup();
      const blob = new Blob([json], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `sadsiyoon-backup-${new Date().toISOString().slice(0, 10)}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      setStatusMessage('تم تصدير ملف النسخة الاحتياطية بنجاح');
      setTimeout(() => setStatusMessage(null), 3000);
    } catch {
      alert('تعذر تصدير النسخة الاحتياطية');
    }
  };

  const handleImportFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target?.result as string;
      const res = importBackup(content);
      if (res.success) {
        setStatusMessage(`تم استيراد ${res.count} عنصر من بياناتك بنجاح!`);
        setNicknameState(getNickname());
        setAutoplayState(getAutoplay());
        onRefreshData();
      } else {
        alert(res.error || 'تعذر استيراد الملف. تأكد من اختيار ملف سادسيون صالح.');
      }
      setTimeout(() => setStatusMessage(null), 3500);
    };
    reader.readAsText(file);
    e.target.value = '';
  };

  const handleResetProgress = () => {
    if (
      window.confirm(
        'هل أنت متأكد من رغبتك في مسح تقدم المشاهدة والمفضلة والملاحظات المحفوظة على هذا الجهاز؟'
      )
    ) {
      clearAllProgress();
      onRefreshData();
      setStatusMessage('تم مسح جميع بيانات التقدم والمفضلة');
      setTimeout(() => setStatusMessage(null), 3000);
    }
  };

  return (
    <div className="space-y-4 pb-24">
      {statusMessage && (
        <div className="bg-[#1A453A] border border-[#20C7B4]/50 text-[#C5F3EC] p-3 rounded-2xl flex items-center gap-2 text-xs font-semibold animate-in fade-in">
          <CheckCircle size={16} className="text-[#20C7B4]" />
          <span>{statusMessage}</span>
        </div>
      )}

      {/* Profile Card */}
      <div className="bg-gradient-to-br from-[#1E3A63] to-[#132642] p-5 rounded-3xl border border-[#2A4E7E]/60 shadow-lg space-y-3">
        <div className="flex items-center gap-3.5">
          <div className="w-14 h-14 rounded-2xl bg-[#183152] border border-[#2A5285] text-[#20C7B4] flex items-center justify-center font-bold text-xl shadow-md">
            <User size={28} />
          </div>

          <div>
            <div className="text-lg font-bold text-white">{nickname}</div>
            <div className="text-xs text-[#A4B6D2]">
              تقدمك وملاحظاتك محفوظة على هذا الجهاز فقط
            </div>
          </div>
        </div>

        <button
          onClick={() => {
            setTempName(nickname);
            setIsEditingName(true);
          }}
          className="w-full py-2.5 rounded-xl bg-[#20C7B4] hover:bg-[#19B5A3] text-[#0B1626] font-bold text-xs transition cursor-pointer"
        >
          تعديل الاسم
        </button>
      </div>

      {/* Edit Name Modal */}
      {isEditingName && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#111F35] border border-[#23426D] rounded-3xl p-6 max-w-sm w-full space-y-4 shadow-2xl">
            <h4 className="text-base font-bold text-white">تعديل اسم الطالب</h4>
            <input
              type="text"
              value={tempName}
              onChange={(e) => setTempName(e.target.value)}
              maxLength={30}
              className="w-full bg-[#0D1A2D] border border-[#24426A] rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-[#20C7B4]"
              placeholder="اسمك أو لقبك الدراسي"
            />
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setIsEditingName(false)}
                className="px-3.5 py-2 rounded-xl bg-[#1C3352] text-xs text-[#A4B6D2]"
              >
                إلغاء
              </button>
              <button
                onClick={handleSaveName}
                className="px-4 py-2 rounded-xl bg-[#20C7B4] text-xs font-bold text-[#0B1626]"
              >
                حفظ
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Autoplay & Playback Settings */}
      <div className="bg-[#14263D] border border-[#213C60] p-4 rounded-2xl space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <div className="font-bold text-sm text-white">
              التشغيل التلقائي للمحاضرة التالية
            </div>
            <div className="text-[11px] text-[#A4B6D2] mt-0.5">
              الانتقال التلقائي للمحاضرة التالية عند انتهاء الحالية
            </div>
          </div>

          <button
            onClick={handleToggleAutoplay}
            className={`w-12 h-6 rounded-full p-1 transition-colors ${
              autoplay ? 'bg-[#20C7B4]' : 'bg-[#253E5E]'
            }`}
          >
            <div
              className={`w-4 h-4 rounded-full bg-white transition-transform ${
                autoplay ? 'translate-x-0' : '-translate-x-6'
              }`}
            />
          </button>
        </div>

        <div className="text-[11px] text-[#A4B6D2] pt-2 border-t border-[#203959]">
          مشغل سادسيون يدعم ملء الشاشة وتسريع الفيديو وتدوين الملاحظات المتزامنة بالدقيقة.
        </div>
      </div>

      {/* Backup and Restore */}
      <div className="bg-[#14263D] border border-[#213C60] p-4 rounded-2xl space-y-3">
        <div className="font-bold text-sm text-white">النسخ الاحتياطي المحلي</div>
        <p className="text-xs text-[#A4B6D2] leading-relaxed">
          صدّر مفضلاتك وبطاقات ملاحظاتك وتقدمك إلى ملف JSON، أو استعدها على أي جهاز آخر دون الحاجة إلى إنشاء حساب.
        </p>

        <div className="grid grid-cols-2 gap-2 pt-1">
          <button
            onClick={handleExport}
            className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-[#20C7B4] hover:bg-[#19B5A3] text-[#0B1626] font-bold text-xs transition cursor-pointer"
          >
            <Download size={14} />
            <span>تصدير نسخة احتياطية</span>
          </button>

          <button
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-[#1C3658] hover:bg-[#234570] text-white font-bold text-xs transition cursor-pointer"
          >
            <Upload size={14} />
            <span>استيراد نسخة</span>
          </button>
        </div>

        <input
          ref={fileInputRef}
          type="file"
          accept=".json,application/json"
          onChange={handleImportFile}
          className="hidden"
        />
      </div>

      {/* Clear Progress Button */}
      <button
        onClick={handleResetProgress}
        className="w-full py-3 rounded-2xl bg-[#251A22] hover:bg-[#341F2C] border border-[#522938] text-xs font-bold text-[#E68A8A] flex items-center justify-center gap-2 transition cursor-pointer"
      >
        <Trash2 size={15} />
        <span>مسح تقدم المشاهدة والمفضلة</span>
      </button>

      {/* Version note */}
      <div className="text-center text-[11px] text-[#A4B6D2]/70 pt-4">
        سادسيون V1.7.0 — نسخة الويب المحلية الكاملة لطلبة السادس الإعدادي.
      </div>
    </div>
  );
};
