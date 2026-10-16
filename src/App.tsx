import React, { useState } from 'react';
import { PageId, BatchYear, LessonRef, StudyMaterial } from './types';
import { getAllTeachers, getAllRefs, getTeacher } from './data/catalog';
import { Navbar } from './components/Navbar';
import { BottomNav } from './components/BottomNav';
import { HomeView } from './components/views/HomeView';
import { TeachersView } from './components/views/TeachersView';
import { TeacherDetailView } from './components/views/TeacherDetailView';
import { ChapterDetailView } from './components/views/ChapterDetailView';
import { ExamsView } from './components/views/ExamsView';
import { TestsView } from './components/views/TestsView';
import { StudyMaterialsView } from './components/views/StudyMaterialsView';
import { LibraryView } from './components/views/LibraryView';
import { AccountView } from './components/views/AccountView';
import { SearchView } from './components/views/SearchView';
import { RemoteCatalogView } from './components/views/RemoteCatalogView';
import { PlayerModal } from './components/player/PlayerModal';
import { GpaCalculatorModal } from './components/modals/GpaCalculatorModal';
import { PomodoroTimerModal } from './components/modals/PomodoroTimerModal';
import { PdfBookViewerModal } from './components/modals/PdfBookViewerModal';
import { setLastWatched } from './utils/storage';

export const App: React.FC = () => {
  const [page, setPage] = useState<PageId>('home');
  const [navHistory, setNavHistory] = useState<PageId[]>([]);
  const [selectedBatch, setSelectedBatch] = useState<BatchYear>('2026');
  const [selectedTeacherIndex, setSelectedTeacherIndex] = useState<number>(0);
  const [selectedChapterIndex, setSelectedChapterIndex] = useState<number>(0);
  const [subjectFilter, setSubjectFilter] = useState<string>('');
  const [libraryFilter, setLibraryFilter] = useState<string>('fav');
  const [activeLessonRef, setActiveLessonRef] = useState<LessonRef | null>(null);
  const [, setRefreshTick] = useState<number>(0);

  // New productivity and study material modals
  const [isGpaModalOpen, setIsGpaModalOpen] = useState<boolean>(false);
  const [isPomodoroModalOpen, setIsPomodoroModalOpen] = useState<boolean>(false);
  const [activeStudyMaterial, setActiveStudyMaterial] = useState<StudyMaterial | null>(null);

  const allTeachers = getAllTeachers();
  const allRefs = getAllRefs();

  const isRootPage = (p: PageId) =>
    p === 'home' ||
    p === 'teachers' ||
    p === 'exams' ||
    p === 'tests' ||
    p === 'materials' ||
    p === 'library' ||
    p === 'more';

  const navigateTo = (nextPage: PageId, filter?: string) => {
    if (filter) {
      if (nextPage === 'teachers') setSubjectFilter(filter);
      if (nextPage === 'library') setLibraryFilter(filter);
    }

    if (nextPage === page) return;

    if (isRootPage(nextPage)) {
      setNavHistory([]);
    } else {
      setNavHistory((prev) => [...prev.slice(-25), page]);
    }

    setPage(nextPage);
    window.scrollTo({ top: 0, behavior: 'instant' });
  };

  const handleBack = () => {
    if (navHistory.length > 0) {
      const prev = navHistory[navHistory.length - 1];
      setNavHistory((hist) => hist.slice(0, hist.length - 1));
      setPage(prev);
    } else if (page === 'chapter') {
      setPage('teacher');
    } else if (page === 'teacher') {
      setPage('teachers');
    } else {
      setPage('home');
    }
    window.scrollTo({ top: 0, behavior: 'instant' });
  };

  const handleSelectTeacher = (index: number) => {
    const t = getTeacher(index);
    if (!t) return;
    setSelectedBatch(t.batch);
    setSelectedTeacherIndex(index);
    setSelectedChapterIndex(0);
    navigateTo('teacher');
  };

  const handleSelectChapter = (chapterIndex: number) => {
    setSelectedChapterIndex(chapterIndex);
    navigateTo('chapter');
  };

  const handleOpenLesson = (ref: LessonRef) => {
    setLastWatched(ref.ti, ref.ci, ref.li);
    setActiveLessonRef(ref);
  };

  const selectedTeacher = getTeacher(selectedTeacherIndex) || allTeachers[0];

  return (
    <div className="min-h-screen bg-[#0B1626] text-[#F5F8FF] flex flex-col max-w-md md:max-w-4xl lg:max-w-5xl xl:max-w-6xl mx-auto border-x border-[#182F4D]/50 relative shadow-2xl transition-all duration-300">
      {/* Top Bar */}
      <Navbar
        page={page}
        selectedBatch={selectedBatch}
        onBack={handleBack}
        onNavigate={navigateTo}
        onOpenGpaModal={() => setIsGpaModalOpen(true)}
        onOpenPomodoroModal={() => setIsPomodoroModalOpen(true)}
      />

      {/* Main Page Content */}
      <main className="flex-1 p-4 md:p-6">
        {page === 'home' && (
          <HomeView
            teachers={allTeachers}
            allRefs={allRefs}
            selectedBatch={selectedBatch}
            onSelectBatch={setSelectedBatch}
            onSelectTeacher={handleSelectTeacher}
            onOpenLesson={handleOpenLesson}
            onNavigate={navigateTo}
            onOpenGpaModal={() => setIsGpaModalOpen(true)}
            onOpenPomodoroModal={() => setIsPomodoroModalOpen(true)}
          />
        )}

        {page === 'teachers' && (
          <TeachersView
            teachers={allTeachers}
            selectedBatch={selectedBatch}
            subjectFilter={subjectFilter}
            onSelectBatch={setSelectedBatch}
            onSelectSubjectFilter={setSubjectFilter}
            onSelectTeacher={handleSelectTeacher}
          />
        )}

        {page === 'teacher' && selectedTeacher && (
          <TeacherDetailView
            teacher={selectedTeacher}
            onSelectChapter={handleSelectChapter}
          />
        )}

        {page === 'chapter' && selectedTeacher && (
          <ChapterDetailView
            teacher={selectedTeacher}
            chapterIndex={selectedChapterIndex}
            onOpenLesson={handleOpenLesson}
          />
        )}

        {page === 'exams' && <ExamsView />}

        {page === 'tests' && <TestsView onNavigate={navigateTo} />}

        {page === 'materials' && (
          <StudyMaterialsView
            onSelectMaterial={(material) => setActiveStudyMaterial(material)}
          />
        )}

        {page === 'library' && (
          <LibraryView
            allRefs={allRefs}
            initialFilter={libraryFilter}
            onOpenLesson={handleOpenLesson}
          />
        )}

        {page === 'more' && (
          <AccountView onRefreshData={() => setRefreshTick((t) => t + 1)} />
        )}

        {page === 'search' && (
          <SearchView
            teachers={allTeachers}
            allRefs={allRefs}
            onSelectTeacher={handleSelectTeacher}
            onOpenLesson={handleOpenLesson}
          />
        )}

        {page === 'remote-catalog' && <RemoteCatalogView />}
      </main>

      {/* Bottom Navigation */}
      <BottomNav currentPage={page} onNavigate={navigateTo} />

      {/* Video Player Modal */}
      {activeLessonRef && (
        <PlayerModal
          currentRef={activeLessonRef}
          onClose={() => setActiveLessonRef(null)}
          onSelectRef={(nextRef) => {
            setLastWatched(nextRef.ti, nextRef.ci, nextRef.li);
            setActiveLessonRef(nextRef);
          }}
        />
      )}

      {/* Ministerial GPA Calculator Modal */}
      <GpaCalculatorModal
        isOpen={isGpaModalOpen}
        onClose={() => setIsGpaModalOpen(false)}
      />

      {/* Pomodoro Focus Timer Modal */}
      <PomodoroTimerModal
        isOpen={isPomodoroModalOpen}
        onClose={() => setIsPomodoroModalOpen(false)}
      />

      {/* Study Materials & Summary PDF Reader Modal */}
      <PdfBookViewerModal
        material={activeStudyMaterial}
        onClose={() => setActiveStudyMaterial(null)}
      />
    </div>
  );
};
