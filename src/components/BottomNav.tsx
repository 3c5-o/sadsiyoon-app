import React from 'react';
import { Home, Users, FileText, Bookmark, User, CheckCircle } from 'lucide-react';
import { PageId } from '../types';

interface BottomNavProps {
  currentPage: PageId;
  onNavigate: (page: PageId) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ currentPage, onNavigate }) => {
  const tabs: Array<{ id: PageId; label: string; icon: React.ReactNode }> = [
    { id: 'home', label: 'الرئيسية', icon: <Home size={20} /> },
    { id: 'teachers', label: 'المدرسون', icon: <Users size={20} /> },
    { id: 'exams', label: 'الوزاريات', icon: <FileText size={20} /> },
    { id: 'tests', label: 'الاختبارات', icon: <CheckCircle size={20} /> },
    { id: 'library', label: 'مكتبتي', icon: <Bookmark size={20} /> },
    { id: 'more', label: 'حسابي', icon: <User size={20} /> },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#111F34]/95 border-t border-[#2C4062]/60 backdrop-blur-md px-2 py-1.5 shadow-2xl max-w-md md:max-w-4xl lg:max-w-5xl xl:max-w-6xl mx-auto sm:rounded-t-2xl">
      <div className="flex items-center justify-around">
        {tabs.map((tab) => {
          const isActive = currentPage === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onNavigate(tab.id)}
              className={`flex flex-col items-center justify-center flex-1 py-1 px-1 rounded-xl transition-all ${
                isActive
                  ? 'bg-[#203E6B] text-white shadow-sm'
                  : 'text-[#A4B6D2] hover:text-[#F5F8FF] hover:bg-[#1A2E4C]/50'
              }`}
            >
              <div
                className={`transition-transform duration-200 ${
                  isActive ? 'text-[#20C7B4] scale-110' : ''
                }`}
              >
                {tab.icon}
              </div>
              <span
                className={`text-[11px] mt-0.5 tracking-tight font-medium ${
                  isActive ? 'text-white font-bold' : ''
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
