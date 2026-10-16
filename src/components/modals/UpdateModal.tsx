import React from 'react';
import {
  X,
  Download,
  ExternalLink,
  CheckCircle2,
  Sparkles,
  RefreshCw,
  AlertCircle,
  FileCode,
} from 'lucide-react';
import { AppUpdateInfo, downloadApkUpdate, GITHUB_REPO } from '../../utils/appUpdater';

interface UpdateModalProps {
  isOpen: boolean;
  onClose: () => void;
  updateInfo: AppUpdateInfo | null;
  isChecking: boolean;
  onRecheck: () => void;
}

export const UpdateModal: React.FC<UpdateModalProps> = ({
  isOpen,
  onClose,
  updateInfo,
  isChecking,
  onRecheck,
}) => {
  if (!isOpen) return null;

  const handleDownload = () => {
    if (updateInfo?.downloadUrl) {
      downloadApkUpdate(updateInfo.downloadUrl);
    }
  };

  const hasUpdate = updateInfo?.hasUpdate ?? false;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div
        className="bg-[#101D30] border border-[#23426A] w-full max-w-lg rounded-3xl p-6 shadow-2xl relative text-right flex flex-col max-h-[90vh] overflow-y-auto"
        dir="rtl"
      >
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute left-4 top-4 p-2 text-[#A4B6D2] hover:text-white bg-[#172B45] hover:bg-[#1E3758] rounded-full transition"
          aria-label="إغلاق"
        >
          <X size={18} />
        </button>

        {/* Header Icon & Title */}
        <div className="flex items-center gap-3 mb-4">
          <div
            className={`w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0 ${
              hasUpdate
                ? 'bg-[#20C7B4]/20 text-[#20C7B4] border border-[#20C7B4]/40 animate-pulse'
                : 'bg-[#203E6B]/40 text-[#20C7B4] border border-[#244875]'
            }`}
          >
            {hasUpdate ? <Sparkles size={24} /> : <CheckCircle2 size={24} />}
          </div>
          <div>
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <span>تحديثات تطبيق سادسيون (APK)</span>
              {hasUpdate && (
                <span className="text-[11px] font-semibold bg-[#20C7B4] text-[#0B1626] px-2.5 py-0.5 rounded-full">
                  إصدار جديد متوفر
                </span>
              )}
            </h2>
            <p className="text-xs text-[#A4B6D2] mt-0.5">
              مزامنة وتحديث الحزمة مباشرة من مستودع GitHub
            </p>
          </div>
        </div>

        {/* Status Card */}
        <div className="bg-[#152740] border border-[#223E63] p-4 rounded-2xl mb-4 space-y-2.5">
          <div className="flex items-center justify-between text-xs">
            <span className="text-[#A4B6D2]">الإصدار المثبت حالياً:</span>
            <span className="font-bold text-white font-mono bg-[#1E385D] px-2.5 py-1 rounded-lg">
              v{updateInfo?.currentVersion || '1.7.0'}
            </span>
          </div>

          <div className="flex items-center justify-between text-xs">
            <span className="text-[#A4B6D2]">أحدث إصدار في GitHub:</span>
            <span
              className={`font-bold font-mono px-2.5 py-1 rounded-lg ${
                hasUpdate
                  ? 'bg-[#20C7B4]/20 text-[#20C7B4] border border-[#20C7B4]/40'
                  : 'bg-[#1E385D] text-white'
              }`}
            >
              v{updateInfo?.latestVersion || '1.7.0'}
            </span>
          </div>

          {updateInfo?.publishedAt && (
            <div className="flex items-center justify-between text-xs">
              <span className="text-[#A4B6D2]">تاريخ الإصدار:</span>
              <span className="text-[#D0E0F2] font-mono">{updateInfo.publishedAt}</span>
            </div>
          )}

          {updateInfo?.fileSize && (
            <div className="flex items-center justify-between text-xs">
              <span className="text-[#A4B6D2]">حجم ملف APK:</span>
              <span className="text-[#E6B74A] font-semibold">{updateInfo.fileSize}</span>
            </div>
          )}
        </div>

        {/* Checking state */}
        {isChecking && (
          <div className="bg-[#162C47] border border-[#234A77] p-3 rounded-2xl flex items-center justify-center gap-2 text-xs text-[#20C7B4] mb-4">
            <RefreshCw size={15} className="animate-spin" />
            <span>جارٍ فحص خوادم GitHub Releases عن أحدث نسخة...</span>
          </div>
        )}

        {/* Up to date notice */}
        {!hasUpdate && !isChecking && (
          <div className="bg-[#1A3323] border border-[#2D5A38] p-3 rounded-2xl flex items-center gap-2.5 text-xs text-[#BFE8C9] mb-4">
            <CheckCircle2 size={16} className="text-[#4AD66D] flex-shrink-0" />
            <span>
              أنت تستخدم بالفعل أحدث نسخة من تطبيق سادسيون ({updateInfo?.currentVersion}). تم دمج
              جميع الفهارس والميزات الجديدة بنجاح.
            </span>
          </div>
        )}

        {/* Changelog Section */}
        {updateInfo?.changelog && updateInfo.changelog.length > 0 && (
          <div className="mb-5 space-y-2">
            <div className="text-xs font-bold text-[#E6B74A] flex items-center gap-1.5">
              <Sparkles size={14} />
              <span>أبرز التحديثات والميزات في هذا الإصدار:</span>
            </div>
            <ul className="space-y-1.5 bg-[#0D1828] p-3.5 rounded-2xl border border-[#1C3658]">
              {updateInfo.changelog.map((item, idx) => (
                <li
                  key={idx}
                  className="text-xs text-[#D5E3F5] flex items-start gap-2 leading-relaxed"
                >
                  <span className="text-[#20C7B4] mt-1 font-bold">•</span>
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* Action Buttons */}
        <div className="space-y-2.5 mt-auto pt-2">
          {hasUpdate ? (
            <button
              onClick={handleDownload}
              className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-[#20C7B4] to-[#1AB3A1] hover:from-[#25DCB6] hover:to-[#1FC4B2] text-[#0B1626] font-bold text-sm flex items-center justify-center gap-2 transition shadow-lg shadow-[#20C7B4]/20 cursor-pointer"
            >
              <Download size={18} />
              <span>تحميل وتثبيت ملف APK الآن</span>
            </button>
          ) : (
            <button
              onClick={handleDownload}
              className="w-full py-3 rounded-2xl bg-[#1D3B62] hover:bg-[#254A7C] text-white font-bold text-xs flex items-center justify-center gap-2 transition cursor-pointer"
            >
              <Download size={16} />
              <span>إعادة تنزيل ملف APK الحالي (v{updateInfo?.currentVersion})</span>
            </button>
          )}

          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={onRecheck}
              disabled={isChecking}
              className="py-2.5 px-3 rounded-xl bg-[#14263D] hover:bg-[#1B3352] text-[#A4B6D2] hover:text-white border border-[#213F66] text-xs font-semibold flex items-center justify-center gap-1.5 transition cursor-pointer disabled:opacity-50"
            >
              <RefreshCw size={14} className={isChecking ? 'animate-spin' : ''} />
              <span>فحص مجدداً</span>
            </button>

            <a
              href={`https://github.com/${GITHUB_REPO}/releases`}
              target="_blank"
              rel="noopener noreferrer"
              className="py-2.5 px-3 rounded-xl bg-[#14263D] hover:bg-[#1B3352] text-[#A4B6D2] hover:text-white border border-[#213F66] text-xs font-semibold flex items-center justify-center gap-1.5 transition text-center"
            >
              <ExternalLink size={14} />
              <span>صفحة GitHub</span>
            </a>
          </div>
        </div>

        {/* Instruction Note */}
        <div className="mt-4 pt-3 border-t border-[#1C3554] text-[11px] text-[#A4B6D2]/80 leading-relaxed flex items-start gap-2">
          <AlertCircle size={14} className="text-[#E6B74A] flex-shrink-0 mt-0.5" />
          <span>
            بعد تنزيل ملف الـ APK، انقر عليه في شريط إشعارات هاتفك أو في مجلد التنزيلات ثم وافق على
            تثبيت التحديث للحفاظ على كافة ملاحظاتك ومفضلاتك السابقة دون فقدان أي بيانات.
          </span>
        </div>
      </div>
    </div>
  );
};
