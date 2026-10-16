import React from 'react';
import { ArrowRight, Search, Globe, Calculator, Clock, BookOpen } from 'lucide-react';
import { PageId } from '../types';

interface NavbarProps {
  page: PageId;
  selectedBatch: string;
  onBack: () => void;
  onNavigate: (page: PageId) => void;
  onOpenGpaModal: () => void;
  onOpenPomodoroModal: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  page,
  selectedBatch,
  onBack,
  onNavigate,
  onOpenGpaModal,
  onOpenPomodoroModal,
}) => {
  const isRootPage =
    page === 'home' ||
    page === 'teachers' ||
    page === 'exams' ||
    page === 'tests' ||
    page === 'library' ||
    page === 'materials' ||
    page === 'more';

  const getTitle = () => {
    switch (page) {
      case 'home':
        return 'سادسيون';
      case 'teachers':
        return 'المدرسون';
      case 'exams':
        return 'الوزاريات';
      case 'tests':
        return 'الاختبارات';
      case 'materials':
        return 'الملازم والملخصات';
      case 'library':
        return 'مكتبتي';
      case 'more':
        return 'حسابي';
      case 'teacher':
        return 'المدرس';
      case 'chapter':
        return 'المحاضرات';
      case 'search':
        return 'البحث';
      case 'remote-catalog':
        return 'محتوى سادسيون';
      default:
        return 'سادسيون';
    }
  };

  const getSubtitle = () => {
    if (isRootPage) {
      return 'تعلم بوضوح، وتقدم بثقة';
    }
    return `دفعة ${selectedBatch}`;
  };

  return (
    <header className="sticky top-0 z-40 bg-gradient-to-b from-[#152C51] to-[#0B1626]/95 border-b border-[#1E3A60]/40 backdrop-blur-md px-4 py-3 flex items-center justify-between shadow-md">
      <div className="flex items-center gap-3">
        {!isRootPage ? (
          <button
            onClick={onBack}
            className="flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#1B3252] hover:bg-[#23436E] text-sm text-[#F5F8FF] font-medium transition cursor-pointer"
            aria-label="رجوع"
          >
            <ArrowRight size={16} className="text-[#20C7B4]" />
            <span>رجوع</span>
          </button>
        ) : (
          <div className="w-10 h-10 rounded-xl overflow-hidden bg-[#1B3252] flex items-center justify-center border border-[#25426C] shadow-sm flex-shrink-0">
            <img
              src="/logo_icon.webp"
              alt="سادسيون"
              className="w-full h-full object-cover"
              onError={(e) => {
                (e.target as HTMLElement).style.display = 'none';
              }}
            />
          </div>
        )}

        <div>
          <h1 className="text-lg font-bold text-white tracking-wide leading-tight">
            {getTitle()}
          </h1>
          <p className="text-xs text-[#A4B6D2] font-normal">{getSubtitle()}</p>
        </div>
      </div>

      <div className="flex items-center gap-1.5 sm:gap-2">
        {/* GPA Calculator Shortcut */}
        <button
          onClick={onOpenGpaModal}
          className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-[#142944] hover:bg-[#1E3B63] text-[#E6B74A] text-xs font-semibold transition border border-[#244570]"
          title="حاسبة المعدل الوزاري"
        >
          <Calculator size={14} />
          <span className="hidden sm:inline">المعدل</span>
        </button>

        {/* Pomodoro Timer Shortcut */}
        <button
          onClick={onOpenPomodoroModal}
          className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-[#142944] hover:bg-[#1E3B63] text-[#20C7B4] text-xs font-semibold transition border border-[#244570]"
          title="مؤقت المذاكرة بومودورو"
        >
          <Clock size={14} />
          <span className="hidden sm:inline">المؤقت</span>
        </button>

        {/* Remote Supabase Catalog Shortcut */}
        <button
          onClick={() => onNavigate('remote-catalog')}
          className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-[#142944] hover:bg-[#1E3B63] text-[#38BDF8] text-xs font-semibold transition border border-[#244570]"
          title="محتوى سادسيون المنشور"
        >
          <Globe size={14} />
          <span className="hidden md:inline">المنشور</span>
        </button>

        {/* Search button */}
        {page !== 'search' && (
          <button
            onClick={() => onNavigate('search')}
            className="flex items-center justify-center w-9 h-9 rounded-xl bg-[#1B3252] hover:bg-[#23436E] text-[#F5F8FF] transition border border-[#264573]/50 cursor-pointer"
            aria-label="البحث عن مدرس أو محاضرة"
          >
            <Search size={16} className="text-[#A4B6D2]" />
          </button>
        )}
      </div>
    </header>
  );
};
