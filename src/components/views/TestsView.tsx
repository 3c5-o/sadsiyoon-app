import React, { useState } from 'react';
import {
  FileText,
  RotateCcw,
  Award,
  CheckCircle2,
  XCircle,
  Layers,
  ChevronLeft,
  Sparkles,
} from 'lucide-react';
import { SCIENTIFIC_QUESTIONS, LITERARY_QUESTIONS } from '../../data/practiceBank';
import { getAllChapterQuizzes, ChapterQuiz } from '../../data/expandedQuizzes';
import { getBestPracticeScore, setBestPracticeScore } from '../../utils/storage';
import { PageId, PracticeQuestion } from '../../types';

interface TestsViewProps {
  onNavigate: (page: PageId) => void;
}

export const TestsView: React.FC<TestsViewProps> = ({ onNavigate }) => {
  const [testMode, setTestMode] = useState<'general' | 'chapters'>('general');
  const [practiceBranch, setPracticeBranch] = useState<'scientific' | 'literary'>('scientific');
  const [selectedChapterQuiz, setSelectedChapterQuiz] = useState<ChapterQuiz | null>(null);

  // Active quiz runner state
  const [questionIndex, setQuestionIndex] = useState<number>(0);
  const [quizScore, setQuizScore] = useState<number>(0);
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const [showExplanationModal, setShowExplanationModal] = useState<boolean>(false);

  // Determine active questions list
  const activeQuestions: PracticeQuestion[] = selectedChapterQuiz
    ? selectedChapterQuiz.questions
    : practiceBranch === 'scientific'
    ? SCIENTIFIC_QUESTIONS
    : LITERARY_QUESTIONS;

  const isFinished = questionIndex >= activeQuestions.length;
  const currentQ = !isFinished ? activeQuestions[questionIndex] : null;

  const handleSelectOption = (index: number) => {
    if (selectedOption !== null || !currentQ) return;
    setSelectedOption(index);
    const isRight = index === currentQ.answer;
    if (isRight) {
      setQuizScore((s) => s + 1);
    }
    setShowExplanationModal(true);
  };

  const handleNextQuestion = () => {
    setShowExplanationModal(false);
    setSelectedOption(null);
    const nextIdx = questionIndex + 1;
    setQuestionIndex(nextIdx);

    if (nextIdx >= activeQuestions.length) {
      const finalScore = selectedOption === currentQ?.answer ? quizScore + 1 : quizScore;
      const scoreKey = selectedChapterQuiz ? `quiz_${selectedChapterQuiz.id}` : practiceBranch;
      setBestPracticeScore(scoreKey, finalScore);
    }
  };

  const handleRetry = () => {
    setQuestionIndex(0);
    setQuizScore(0);
    setSelectedOption(null);
    setShowExplanationModal(false);
  };

  const handleSelectChapterQuiz = (quiz: ChapterQuiz) => {
    setSelectedChapterQuiz(quiz);
    setQuestionIndex(0);
    setQuizScore(0);
    setSelectedOption(null);
    setShowExplanationModal(false);
  };

  const chapterQuizzes = getAllChapterQuizzes().filter(
    (q) => q.branch === 'both' || q.branch === practiceBranch
  );

  const bestScore = getBestPracticeScore(
    selectedChapterQuiz ? `quiz_${selectedChapterQuiz.id}` : practiceBranch
  );

  return (
    <div className="space-y-4 pb-24">
      {/* Top Banner & Waziriat Shortcut */}
      <button
        onClick={() => onNavigate('exams')}
        className="w-full flex items-center justify-between p-3.5 rounded-2xl bg-gradient-to-r from-[#203D65] to-[#152843] border border-[#2B548A]/50 text-white shadow-md hover:bg-[#254673] transition cursor-pointer"
      >
        <div className="flex items-center gap-2.5">
          <FileText size={18} className="text-[#20C7B4]" />
          <span className="text-xs font-bold">افتح الأسئلة الوزارية المصورة (2025)</span>
        </div>
        <span className="text-xs text-[#20C7B4] font-semibold">عرض الوزاريات ←</span>
      </button>

      {/* Main Mode Switcher: Comprehensive vs Chapter-by-Chapter */}
      <div className="grid grid-cols-2 gap-2 bg-[#102035] p-1.5 rounded-2xl border border-[#213C60]">
        <button
          onClick={() => {
            setTestMode('general');
            setSelectedChapterQuiz(null);
            handleRetry();
          }}
          className={`py-2 rounded-xl font-bold text-xs transition ${
            testMode === 'general' && !selectedChapterQuiz
              ? 'bg-[#203E6B] text-white shadow-sm'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          اختبار شامل للفرع
        </button>

        <button
          onClick={() => {
            setTestMode('chapters');
            handleRetry();
          }}
          className={`py-2 rounded-xl font-bold text-xs transition ${
            testMode === 'chapters' || selectedChapterQuiz
              ? 'bg-[#203E6B] text-white shadow-sm'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          اختبارات الفصول التخصصية
        </button>
      </div>

      {/* Branch Selector */}
      <div className="grid grid-cols-2 gap-2 bg-[#122238] p-1 rounded-2xl border border-[#213C60]">
        <button
          onClick={() => {
            if (practiceBranch !== 'scientific') {
              setPracticeBranch('scientific');
              setSelectedChapterQuiz(null);
              handleRetry();
            }
          }}
          className={`py-2 rounded-xl font-bold text-xs transition ${
            practiceBranch === 'scientific'
              ? 'bg-[#20C7B4] text-[#0B1626] font-black shadow-md'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          السادس العلمي
        </button>

        <button
          onClick={() => {
            if (practiceBranch !== 'literary') {
              setPracticeBranch('literary');
              setSelectedChapterQuiz(null);
              handleRetry();
            }
          }}
          className={`py-2 rounded-xl font-bold text-xs transition ${
            practiceBranch === 'literary'
              ? 'bg-[#20C7B4] text-[#0B1626] font-black shadow-md'
              : 'text-[#A4B6D2] hover:text-white'
          }`}
        >
          السادس الأدبي
        </button>
      </div>

      {/* Chapter Selection View (if in chapter mode and no quiz active) */}
      {testMode === 'chapters' && !selectedChapterQuiz && (
        <div className="space-y-3">
          <div className="flex items-center justify-between text-xs text-[#A4B6D2] px-1">
            <span>اختر الفصل لإجراء اختبار تدريبي وزاري مخصص:</span>
            <span>{chapterQuizzes.length} فصول متاحة</span>
          </div>

          <div className="space-y-2.5">
            {chapterQuizzes.map((quiz) => (
              <div
                key={quiz.id}
                onClick={() => handleSelectChapterQuiz(quiz)}
                className="bg-[#14263D] hover:bg-[#1A3250] border border-[#213E64] p-4 rounded-2xl flex items-center justify-between transition cursor-pointer group shadow-sm"
              >
                <div className="flex items-center gap-3.5">
                  <div className="w-10 h-10 rounded-xl bg-[#1B3558] text-[#20C7B4] flex items-center justify-center font-bold text-sm flex-shrink-0 group-hover:scale-105 transition-transform">
                    {quiz.chapterNumber}
                  </div>
                  <div>
                    <span className="text-[10px] font-bold text-[#E6B74A]">
                      مادة {quiz.subject}
                    </span>
                    <h4 className="font-bold text-sm text-white group-hover:text-[#20C7B4] transition-colors">
                      {quiz.title}
                    </h4>
                    <span className="text-[11px] text-[#A4B6D2]">
                      {quiz.questions.length} أسئلة وزارية مدعمة بالشرح
                    </span>
                  </div>
                </div>

                <ChevronLeft size={18} className="text-[#20C7B4]" />
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Quiz Runner Active Stage */}
      {(testMode === 'general' || selectedChapterQuiz) && (
        <>
          {selectedChapterQuiz && (
            <div className="flex items-center justify-between bg-[#122238] px-3.5 py-2 rounded-xl border border-[#213C60] text-xs">
              <span className="font-bold text-[#20C7B4]">
                اختبار: {selectedChapterQuiz.title} ({selectedChapterQuiz.subject})
              </span>
              <button
                onClick={() => {
                  setSelectedChapterQuiz(null);
                  handleRetry();
                }}
                className="text-[#E68A8A] hover:underline"
              >
                تغيير الفصل
              </button>
            </div>
          )}

          {!isFinished && currentQ ? (
            <div className="space-y-4">
              <div className="bg-gradient-to-br from-[#213C63] to-[#14263F] border border-[#2C5287]/60 p-5 rounded-3xl shadow-lg space-y-3">
                <div className="flex items-center justify-between text-xs font-bold text-[#E6B74A]">
                  <span>
                    السؤال {questionIndex + 1} من {activeQuestions.length}
                  </span>
                  <span className="font-mono text-[#20C7B4]">
                    النتيجة: {quizScore}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-white leading-relaxed">
                  {currentQ.text}
                </h3>
              </div>

              {/* Options */}
              <div className="space-y-2.5">
                {currentQ.options.map((opt, idx) => (
                  <button
                    key={idx}
                    onClick={() => handleSelectOption(idx)}
                    className="w-full text-right p-4 rounded-2xl bg-[#14263D] hover:bg-[#1B3454] border border-[#213D62] text-sm font-semibold text-white transition flex items-center justify-between group cursor-pointer shadow-sm"
                  >
                    <span>{opt}</span>
                    <span className="w-6 h-6 rounded-full bg-[#1B3252] border border-[#294B77] flex items-center justify-center text-xs text-[#A4B6D2] group-hover:border-[#20C7B4] group-hover:text-[#20C7B4]">
                      {idx + 1}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          ) : (
            /* Result Screen */
            <div className="bg-gradient-to-br from-[#254B79] to-[#152B46] border border-[#305C94]/60 p-6 rounded-3xl text-center space-y-4 shadow-xl">
              <div className="w-16 h-16 rounded-full bg-[#1B3A63] text-[#E6B74A] flex items-center justify-center mx-auto shadow-inner">
                <Award size={36} />
              </div>

              <div>
                <h3 className="text-2xl font-black text-white">
                  النتيجة: {quizScore} من {activeQuestions.length}
                </h3>
                <p className="text-xs text-[#D5E5F7] mt-1.5">
                  أفضل نتيجة محفوظة: {Math.max(quizScore, bestScore)} من {activeQuestions.length}
                </p>
              </div>

              <div className="flex flex-col sm:flex-row gap-2">
                <button
                  onClick={handleRetry}
                  className="flex-1 py-3 rounded-2xl bg-[#20C7B4] hover:bg-[#18AFA0] text-[#0B1626] font-bold text-xs shadow-md transition flex items-center justify-center gap-1.5 cursor-pointer"
                >
                  <RotateCcw size={16} />
                  <span>إعادة الاختبار</span>
                </button>

                {selectedChapterQuiz && (
                  <button
                    onClick={() => {
                      setSelectedChapterQuiz(null);
                      setTestMode('chapters');
                    }}
                    className="flex-1 py-3 rounded-2xl bg-[#1B3454] hover:bg-[#23426A] text-white font-bold text-xs transition"
                  >
                    اختبار فصل آخر
                  </button>
                )}
              </div>
            </div>
          )}
        </>
      )}

      {/* Answer & Explanation Modal */}
      {showExplanationModal && currentQ && selectedOption !== null && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#111F35] border border-[#24436E] rounded-3xl p-6 max-w-sm w-full space-y-4 shadow-2xl animate-in fade-in zoom-in-95">
            <div className="flex items-center gap-3">
              {selectedOption === currentQ.answer ? (
                <CheckCircle2 size={32} className="text-[#20C7B4]" />
              ) : (
                <XCircle size={32} className="text-[#E68A8A]" />
              )}
              <div>
                <h4 className="text-lg font-bold text-white">
                  {selectedOption === currentQ.answer ? 'إجابة صحيحة!' : 'راجع الحل'}
                </h4>
                {selectedOption !== currentQ.answer && (
                  <p className="text-xs text-[#20C7B4] font-semibold mt-0.5">
                    الصحيح: {currentQ.options[currentQ.answer]}
                  </p>
                )}
              </div>
            </div>

            <div className="bg-[#0D182A] p-3.5 rounded-2xl border border-[#1E375B] text-xs text-[#D2E2F5] leading-relaxed">
              <span className="font-bold text-[#E6B74A] block mb-1">الشرح والتوضيح:</span>
              {currentQ.explanation}
            </div>

            <button
              onClick={handleNextQuestion}
              className="w-full py-2.5 rounded-xl bg-[#20C7B4] hover:bg-[#18AFA0] text-[#0B1626] font-bold text-xs transition cursor-pointer"
            >
              {questionIndex < activeQuestions.length - 1 ? 'السؤال التالي' : 'عرض النتيجة النهائية'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
