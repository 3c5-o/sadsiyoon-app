import React, { useState, useEffect, useRef } from 'react';
import {
  X,
  Play,
  Pause,
  RotateCcw,
  RotateCw,
  Maximize,
  Minimize,
  Lock,
  Unlock,
  Star,
  CheckCircle,
  ChevronRight,
  ChevronLeft,
  Settings,
  Plus,
  Trash2,
  Edit2,
  Clock,
  BookOpen,
  FileText,
  ListVideo,
  AlertCircle,
  ExternalLink,
} from 'lucide-react';
import Hls from 'hls.js';
import { LessonRef, NoteItem, StudyMaterial } from '../../types';
import { getStudyMaterialsByTeacher, getStudyMaterialsBySubject } from '../../data/studyMaterials';
import { PdfBookViewerModal } from '../modals/PdfBookViewerModal';
import {
  getFavorite,
  setFavorite,
  getCompleted,
  setCompleted,
  getPosition,
  setPosition,
  getNotes,
  saveNote,
  deleteNote,
  getAutoplay,
} from '../../utils/storage';

interface PlayerModalProps {
  currentRef: LessonRef;
  onClose: () => void;
  onSelectRef: (ref: LessonRef) => void;
}

function formatTime(seconds: number): string {
  if (isNaN(seconds) || seconds < 0) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

export const PlayerModal: React.FC<PlayerModalProps> = ({
  currentRef,
  onClose,
  onSelectRef,
}) => {
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [duration, setDuration] = useState<number>(0);
  const [playbackSpeed, setPlaybackSpeed] = useState<number>(1);
  const [controlsLocked, setControlsLocked] = useState<boolean>(false);
  const [showControls, setShowControls] = useState<boolean>(true);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);
  const [showSettings, setShowSettings] = useState<boolean>(false);
  const [activeTab, setActiveTab] = useState<'lessons' | 'notes' | 'books'>('lessons');
  const [playbackError, setPlaybackError] = useState<string | null>(null);
  const [selectedMaterial, setSelectedMaterial] = useState<StudyMaterial | null>(null);

  // Notes state
  const [notes, setNotes] = useState<NoteItem[]>([]);
  const [isAddingNote, setIsAddingNote] = useState<boolean>(false);
  const [editingNoteIndex, setEditingNoteIndex] = useState<number | null>(null);
  const [noteTitle, setNoteTitle] = useState<string>('');
  const [noteBody, setNoteBody] = useState<string>('');
  const [attachTimestamp, setAttachTimestamp] = useState<boolean>(true);

  // Favorite & Completed state
  const [isFav, setIsFav] = useState<boolean>(false);
  const [isDone, setIsDone] = useState<boolean>(false);

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const hlsRef = useRef<Hls | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const hideControlsTimeout = useRef<number | null>(null);

  const isNative = currentRef.lesson.type === 'mp4' || currentRef.lesson.type === 'hls';
  const isEmbed = currentRef.lesson.type === 'embed';

  // Load initial notes and status
  useEffect(() => {
    setIsFav(getFavorite(currentRef.key));
    setIsDone(getCompleted(currentRef.key));
    setNotes(getNotes(currentRef.key));
    setPlaybackError(null);
  }, [currentRef.key]);

  // Video setup & cleanup
  useEffect(() => {
    const video = videoRef.current;
    if (!video || !isNative) return;

    if (hlsRef.current) {
      hlsRef.current.destroy();
      hlsRef.current = null;
    }

    const savedPosMs = getPosition(currentRef.key);
    const startSeconds = savedPosMs > 0 ? savedPosMs / 1000 : 0;

    if (currentRef.lesson.type === 'hls') {
      if (Hls.isSupported()) {
        const hls = new Hls({ enableWorker: true });
        hlsRef.current = hls;
        hls.loadSource(currentRef.lesson.url);
        hls.attachMedia(video);
        hls.on(Hls.Events.MANIFEST_PARSED, () => {
          if (startSeconds > 0) video.currentTime = startSeconds;
          video.play().catch(() => setIsPlaying(false));
        });
        hls.on(Hls.Events.ERROR, (_, data) => {
          if (data.fatal) {
            setPlaybackError('تعذر تحميل بث الفيديو. قد يكون الرابط غير متاح حالياً.');
          }
        });
      } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
        video.src = currentRef.lesson.url;
        if (startSeconds > 0) video.currentTime = startSeconds;
        video.play().catch(() => setIsPlaying(false));
      }
    } else if (currentRef.lesson.type === 'mp4') {
      video.src = currentRef.lesson.url;
      if (startSeconds > 0) video.currentTime = startSeconds;
      video.play().catch(() => setIsPlaying(false));
    }

    return () => {
      if (hlsRef.current) {
        hlsRef.current.destroy();
        hlsRef.current = null;
      }
    };
  }, [currentRef.lesson.url, currentRef.lesson.type, currentRef.key, isNative]);

  // Save playback position periodically
  useEffect(() => {
    const interval = setInterval(() => {
      if (videoRef.current && isNative && isPlaying) {
        const pos = videoRef.current.currentTime * 1000;
        if (pos > 0) {
          setPosition(currentRef.key, pos);
        }
      }
    }, 4000);
    return () => clearInterval(interval);
  }, [currentRef.key, isNative, isPlaying]);

  // Auto-hide controls timer
  const resetHideControlsTimer = () => {
    if (controlsLocked) return;
    setShowControls(true);
    if (hideControlsTimeout.current) {
      window.clearTimeout(hideControlsTimeout.current);
    }
    if (isPlaying) {
      hideControlsTimeout.current = window.setTimeout(() => {
        if (!showSettings) {
          setShowControls(false);
        }
      }, 3500);
    }
  };

  const togglePlay = () => {
    const video = videoRef.current;
    if (!video) return;
    if (video.paused) {
      video.play();
      setIsPlaying(true);
    } else {
      video.pause();
      setIsPlaying(false);
    }
    resetHideControlsTimer();
  };

  const seekRelative = (deltaSeconds: number) => {
    const video = videoRef.current;
    if (!video) return;
    video.currentTime = Math.max(0, Math.min(video.duration || 0, video.currentTime + deltaSeconds));
    resetHideControlsTimer();
  };

  const toggleSpeed = () => {
    const speeds = [1, 1.25, 1.5, 2, 0.75];
    const nextIdx = (speeds.indexOf(playbackSpeed) + 1) % speeds.length;
    const nextSpeed = speeds[nextIdx];
    setPlaybackSpeed(nextSpeed);
    if (videoRef.current) {
      videoRef.current.playbackRate = nextSpeed;
    }
  };

  const toggleFullscreen = () => {
    if (!containerRef.current) return;
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen().catch(() => {});
      setIsFullscreen(true);
    } else {
      document.exitFullscreen().catch(() => {});
      setIsFullscreen(false);
    }
  };

  // Next / Prev lesson helpers
  const currentChapter = currentRef.chapter;
  const hasPrev = currentRef.li > 0;
  const hasNext = currentRef.li < currentChapter.lessons.length - 1;

  const stepLesson = (direction: -1 | 1) => {
    const targetIdx = currentRef.li + direction;
    if (targetIdx >= 0 && targetIdx < currentChapter.lessons.length) {
      const nextRef: LessonRef = {
        ...currentRef,
        lesson: currentChapter.lessons[targetIdx],
        li: targetIdx,
        key: `t${currentRef.ti}_c${currentRef.ci}_l${targetIdx}`,
      };
      if (videoRef.current) {
        setPosition(currentRef.key, videoRef.current.currentTime * 1000);
      }
      onSelectRef(nextRef);
    }
  };

  // Note saving handler
  const handleSaveNote = () => {
    if (!noteTitle.trim() || !noteBody.trim()) return;
    const posMs = attachTimestamp && isNative ? currentTime * 1000 : -1;
    saveNote(currentRef.key, { title: noteTitle, body: noteBody, positionMs: posMs }, editingNoteIndex ?? undefined);
    setNotes(getNotes(currentRef.key));
    setNoteTitle('');
    setNoteBody('');
    setIsAddingNote(false);
    setEditingNoteIndex(null);
  };

  const handleEditNote = (index: number) => {
    const n = notes[index];
    if (!n) return;
    setNoteTitle(n.title);
    setNoteBody(n.body);
    setAttachTimestamp((n.positionMs ?? -1) >= 0);
    setEditingNoteIndex(index);
    setIsAddingNote(true);
  };

  const handleDeleteNote = (index: number) => {
    if (window.confirm('هل تريد حذف هذه الملاحظة؟')) {
      deleteNote(currentRef.key, index);
      setNotes(getNotes(currentRef.key));
    }
  };

  const seekToTime = (posMs: number) => {
    if (videoRef.current && posMs >= 0) {
      videoRef.current.currentTime = posMs / 1000;
      videoRef.current.play().catch(() => {});
      setIsPlaying(true);
    }
  };

  return (
    <div
      ref={containerRef}
      className="fixed inset-0 z-50 bg-[#0B1626] flex flex-col overflow-hidden text-white select-none"
      onMouseMove={resetHideControlsTimer}
      onClick={resetHideControlsTimer}
    >
      {/* Top Bar */}
      <div className="bg-[#111F34] px-4 py-3 flex items-center justify-between border-b border-[#254066]/60">
        <button
          onClick={onClose}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#1D3558] hover:bg-[#254573] text-sm text-white font-medium transition cursor-pointer"
        >
          <X size={18} className="text-[#20C7B4]" />
          <span>إغلاق</span>
        </button>

        <div className="text-center px-2 flex-1 truncate">
          <h2 className="text-sm sm:text-base font-bold text-white truncate">
            {currentRef.lesson.title}
          </h2>
          <p className="text-xs text-[#A4B6D2] truncate">
            {currentRef.teacher.name} • {currentRef.chapter.title} • دفعة {currentRef.teacher.batch}
          </p>
        </div>

        <button
          onClick={() => {
            const val = !isFav;
            setIsFav(val);
            setFavorite(currentRef.key, val);
          }}
          className="p-2 rounded-xl bg-[#1D3558] hover:bg-[#254573] text-[#E6B74A] transition cursor-pointer"
          title={isFav ? 'إزالة من المفضلة' : 'إضافة إلى المفضلة'}
        >
          <Star size={18} fill={isFav ? '#E6B74A' : 'none'} />
        </button>
      </div>

      {/* Video Container Stage */}
      <div className="relative w-full bg-black aspect-video max-h-[50vh] flex items-center justify-center overflow-hidden">
        {isNative && (
          <video
            ref={videoRef}
            className="w-full h-full object-contain"
            playsInline
            onTimeUpdate={() => {
              if (videoRef.current) {
                setCurrentTime(videoRef.current.currentTime);
                setDuration(videoRef.current.duration || 0);
              }
            }}
            onPlay={() => setIsPlaying(true)}
            onPause={() => setIsPlaying(false)}
            onEnded={() => {
              setIsPlaying(false);
              setIsDone(true);
              setCompleted(currentRef.key, true);
              if (getAutoplay() && hasNext) {
                stepLesson(1);
              }
            }}
          />
        )}

        {isEmbed && (
          <div className="w-full h-full relative">
            <iframe
              src={currentRef.lesson.url}
              className="w-full h-full border-0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowFullScreen
            />
          </div>
        )}

        {playbackError && (
          <div className="absolute inset-0 bg-[#0B1626]/90 flex flex-col items-center justify-center p-6 text-center z-30">
            <AlertCircle size={40} className="text-[#E6B74A] mb-3" />
            <h3 className="text-base font-bold text-white mb-1">تعذر تشغيل الفيديو</h3>
            <p className="text-xs text-[#A4B6D2] max-w-sm mb-4">{playbackError}</p>
            <div className="flex gap-2">
              <button
                onClick={() => {
                  setPlaybackError(null);
                  if (videoRef.current) {
                    videoRef.current.load();
                  }
                }}
                className="px-4 py-2 rounded-xl bg-[#20C7B4] text-[#0B1626] font-bold text-xs"
              >
                إعادة المحاولة
              </button>
              {currentRef.lesson.url && (
                <a
                  href={currentRef.lesson.url}
                  target="_blank"
                  rel="noreferrer"
                  className="px-4 py-2 rounded-xl bg-[#1F395E] text-white font-medium text-xs flex items-center gap-1"
                >
                  <ExternalLink size={14} />
                  فتح الرابط خارجياً
                </a>
              )}
            </div>
          </div>
        )}

        {/* Native Video Controls Overlay */}
        {isNative && !controlsLocked && showControls && (
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-black/60 flex flex-col justify-between p-3 pointer-events-auto transition-opacity duration-300">
            {/* Header overlay */}
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-white/90 drop-shadow">
                مشغل سادسيون
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setControlsLocked(true)}
                  className="p-1.5 rounded-lg bg-black/40 hover:bg-black/60 text-white/80"
                  title="قفل أدوات التحكم"
                >
                  <Lock size={16} />
                </button>
                <button
                  onClick={() => setShowSettings(!showSettings)}
                  className="p-1.5 rounded-lg bg-black/40 hover:bg-black/60 text-white/80"
                  title="الإعدادات"
                >
                  <Settings size={16} />
                </button>
              </div>
            </div>

            {/* Center Play/Pause button */}
            <div className="flex items-center justify-center gap-6">
              <button
                onClick={() => seekRelative(-10)}
                className="p-2.5 rounded-full bg-black/50 hover:bg-black/70 text-white/90 transition"
                title="تراجع 10 ثوانٍ"
              >
                <RotateCcw size={22} />
              </button>

              <button
                onClick={togglePlay}
                className="p-4 rounded-full bg-[#20C7B4] hover:bg-[#18AFA0] text-[#0B1626] shadow-xl transition transform active:scale-95"
                title={isPlaying ? 'إيقاف مؤقت' : 'تشغيل'}
              >
                {isPlaying ? <Pause size={28} /> : <Play size={28} className="mr-0.5" />}
              </button>

              <button
                onClick={() => seekRelative(10)}
                className="p-2.5 rounded-full bg-black/50 hover:bg-black/70 text-white/90 transition"
                title="تقديم 10 ثوانٍ"
              >
                <RotateCw size={22} />
              </button>
            </div>

            {/* Bottom Timeline & Tools */}
            <div className="space-y-1.5">
              <div className="flex items-center gap-2">
                <input
                  type="range"
                  min="0"
                  max={duration || 100}
                  step="0.5"
                  value={currentTime}
                  onChange={(e) => {
                    const next = parseFloat(e.target.value);
                    setCurrentTime(next);
                    if (videoRef.current) {
                      videoRef.current.currentTime = next;
                    }
                  }}
                  className="w-full h-1.5 bg-white/30 rounded-lg appearance-none cursor-pointer accent-[#20C7B4]"
                />
              </div>

              <div className="flex items-center justify-between text-xs text-white/90 font-mono">
                <span>
                  {formatTime(currentTime)} / {formatTime(duration)}
                </span>

                <div className="flex items-center gap-3">
                  <button
                    onClick={toggleSpeed}
                    className="px-2 py-0.5 rounded bg-black/40 text-xs text-[#20C7B4] font-semibold"
                  >
                    {playbackSpeed}×
                  </button>

                  <button
                    onClick={toggleFullscreen}
                    className="p-1 text-white/80 hover:text-white"
                  >
                    {isFullscreen ? <Minimize size={16} /> : <Maximize size={16} />}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Lock Mode Floating Unlock Button */}
        {isNative && controlsLocked && (
          <button
            onClick={() => setControlsLocked(false)}
            className="absolute top-4 right-4 z-40 flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#20C7B4] text-[#0B1626] font-bold text-xs shadow-lg animate-pulse"
          >
            <Unlock size={14} />
            <span>إلغاء القفل</span>
          </button>
        )}
      </div>

      {/* Embedded notice for provider iframes */}
      {isEmbed && (
        <div className="bg-[#12243B] px-4 py-2 border-b border-[#254066]/60 flex items-center justify-between text-xs text-[#A4B6D2]">
          <span>
            {currentRef.lesson.access === 'premium'
              ? 'قد تتطلب المشاهدة اشتراكاً لدى مزود الفيديو.'
              : 'مشغّل المحاضرة المضمّن — تحكّم بالعرض من أدوات المزود.'}
          </span>
          <div className="flex items-center gap-2">
            {hasNext && (
              <button
                onClick={() => stepLesson(1)}
                className="px-2 py-1 rounded bg-[#1F395E] text-white text-[11px]"
              >
                التالية
              </button>
            )}
          </div>
        </div>
      )}

      {/* Quick Navigation and Lecture Status Bar */}
      <div className="bg-[#14263E] px-4 py-2 flex items-center justify-between border-b border-[#223B5E] text-xs">
        <div className="flex items-center gap-2">
          <button
            onClick={() => stepLesson(-1)}
            disabled={!hasPrev}
            className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs transition ${
              hasPrev
                ? 'bg-[#1E375B] text-white hover:bg-[#254673]'
                : 'bg-[#1E375B]/40 text-white/30 cursor-not-allowed'
            }`}
          >
            <ChevronRight size={14} />
            <span>السابقة</span>
          </button>

          <button
            onClick={() => stepLesson(1)}
            disabled={!hasNext}
            className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs transition ${
              hasNext
                ? 'bg-[#1E375B] text-white hover:bg-[#254673]'
                : 'bg-[#1E375B]/40 text-white/30 cursor-not-allowed'
            }`}
          >
            <span>التالية</span>
            <ChevronLeft size={14} />
          </button>
        </div>

        <button
          onClick={() => {
            const val = !isDone;
            setIsDone(val);
            setCompleted(currentRef.key, val);
          }}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
            isDone
              ? 'bg-[#1B4345] text-[#20C7B4] border border-[#20C7B4]/50'
              : 'bg-[#1D3558] text-[#A4B6D2] hover:bg-[#254573]'
          }`}
        >
          <CheckCircle size={14} />
          <span>{isDone ? 'مكتملة' : 'تحديد كمكتملة'}</span>
        </button>
      </div>

      {/* Lower Tabs & Content */}
      <div className="flex-1 flex flex-col overflow-hidden bg-[#0B1626]">
        {/* Tab Switcher */}
        <div className="flex border-b border-[#1F385B] bg-[#102035] px-3 pt-2 gap-2">
          <button
            onClick={() => setActiveTab('lessons')}
            className={`flex items-center gap-1.5 py-2 px-3 text-xs font-bold rounded-t-xl transition ${
              activeTab === 'lessons'
                ? 'bg-[#0B1626] text-[#20C7B4] border-t-2 border-[#20C7B4]'
                : 'text-[#A4B6D2] hover:text-white'
            }`}
          >
            <ListVideo size={15} />
            <span>المحاضرات ({currentChapter.lessons.length})</span>
          </button>

          <button
            onClick={() => setActiveTab('notes')}
            className={`flex items-center gap-1.5 py-2 px-3 text-xs font-bold rounded-t-xl transition ${
              activeTab === 'notes'
                ? 'bg-[#0B1626] text-[#20C7B4] border-t-2 border-[#20C7B4]'
                : 'text-[#A4B6D2] hover:text-white'
            }`}
          >
            <FileText size={15} />
            <span>الملاحظات ({notes.length})</span>
          </button>

          <button
            onClick={() => setActiveTab('books')}
            className={`flex items-center gap-1.5 py-2 px-3 text-xs font-bold rounded-t-xl transition ${
              activeTab === 'books'
                ? 'bg-[#0B1626] text-[#20C7B4] border-t-2 border-[#20C7B4]'
                : 'text-[#A4B6D2] hover:text-white'
            }`}
          >
            <BookOpen size={15} />
            <span>الملازم</span>
          </button>
        </div>

        {/* Tab Body */}
        <div className="flex-1 overflow-y-auto p-4">
          {activeTab === 'lessons' && (
            <div className="space-y-2">
              <div className="text-xs text-[#A4B6D2] mb-2 font-medium">
                فهرس {currentChapter.title} — {currentChapter.lessons.length} محاضرة
              </div>

              {currentChapter.lessons.map((lesson, idx) => {
                const isCurrent = idx === currentRef.li;
                const lessonKey = `t${currentRef.ti}_c${currentRef.ci}_l${idx}`;
                const done = getCompleted(lessonKey);

                return (
                  <div
                    key={idx}
                    onClick={() => {
                      if (lesson.type === 'none') {
                        alert('هذه المحاضرة غير متاحة للتشغيل حالياً');
                        return;
                      }
                      onSelectRef({
                        ...currentRef,
                        lesson,
                        li: idx,
                        key: lessonKey,
                      });
                    }}
                    className={`flex items-center justify-between p-3 rounded-xl border transition cursor-pointer ${
                      isCurrent
                        ? 'bg-[#1C3A63] border-[#20C7B4] text-white shadow-md'
                        : 'bg-[#14263D] border-[#233D61] text-[#E0EBF7] hover:bg-[#1A314E]'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-7 h-7 rounded-lg flex items-center justify-center font-bold text-xs ${
                          isCurrent
                            ? 'bg-[#20C7B4] text-[#0B1626]'
                            : done
                            ? 'bg-[#1F4640] text-[#20C7B4]'
                            : 'bg-[#1E3658] text-[#A4B6D2]'
                        }`}
                      >
                        {idx + 1}
                      </div>

                      <div>
                        <div className="text-sm font-semibold leading-snug">
                          {lesson.title}
                        </div>
                        <div className="text-[11px] text-[#A4B6D2] mt-0.5">
                          {isCurrent
                            ? 'قيد المشاهدة حالياً'
                            : done
                            ? 'مكتملة'
                            : lesson.type === 'none'
                            ? 'غير متاحة للتشغيل'
                            : lesson.access === 'premium'
                            ? 'يتطلب اشتراك لدى المزود'
                            : 'جاهزة للمشاهدة'}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-1">
                      {isCurrent && (
                        <span className="flex h-2 w-2 rounded-full bg-[#20C7B4] animate-ping" />
                      )}
                      {done && !isCurrent && (
                        <CheckCircle size={16} className="text-[#20C7B4]" />
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {activeTab === 'notes' && (
            <div className="space-y-3">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs text-[#A4B6D2]">
                  الملاحظات محفوظة محلياً على هذا الجهاز
                </span>
                {!isAddingNote && (
                  <button
                    onClick={() => {
                      setNoteTitle('');
                      setNoteBody('');
                      setEditingNoteIndex(null);
                      setIsAddingNote(true);
                    }}
                    className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-[#20C7B4] hover:bg-[#19B5A3] text-[#0B1626] font-bold text-xs transition cursor-pointer"
                  >
                    <Plus size={14} />
                    <span>ملاحظة جديدة</span>
                  </button>
                )}
              </div>

              {isAddingNote && (
                <div className="bg-[#14263E] border border-[#23426A] p-3 rounded-2xl space-y-3 mb-4">
                  <h4 className="text-xs font-bold text-[#20C7B4]">
                    {editingNoteIndex !== null ? 'تعديل الملاحظة' : 'إضافة ملاحظة جديدة'}
                  </h4>
                  <input
                    type="text"
                    placeholder="عنوان الملاحظة (مثال: قانون أوم، ملاحظة مهمة)"
                    value={noteTitle}
                    onChange={(e) => setNoteTitle(e.target.value)}
                    maxLength={90}
                    className="w-full bg-[#0D1A2D] border border-[#25426C] rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-[#20C7B4]"
                  />
                  <textarea
                    placeholder="نص الملاحظة أو التلخيص..."
                    value={noteBody}
                    onChange={(e) => setNoteBody(e.target.value)}
                    rows={3}
                    maxLength={4000}
                    className="w-full bg-[#0D1A2D] border border-[#25426C] rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-[#20C7B4]"
                  />
                  {isNative && (
                    <label className="flex items-center gap-2 text-xs text-[#A4B6D2] cursor-pointer">
                      <input
                        type="checkbox"
                        checked={attachTimestamp}
                        onChange={(e) => setAttachTimestamp(e.target.checked)}
                        className="rounded accent-[#20C7B4]"
                      />
                      <span>ربط الملاحظة بتوقيت الفيديو الحالي ({formatTime(currentTime)})</span>
                    </label>
                  )}
                  <div className="flex justify-end gap-2">
                    <button
                      onClick={() => {
                        setIsAddingNote(false);
                        setEditingNoteIndex(null);
                      }}
                      className="px-3 py-1.5 rounded-xl bg-[#1B3252] text-xs text-[#A4B6D2]"
                    >
                      إلغاء
                    </button>
                    <button
                      onClick={handleSaveNote}
                      className="px-4 py-1.5 rounded-xl bg-[#20C7B4] text-xs font-bold text-[#0B1626]"
                    >
                      حفظ
                    </button>
                  </div>
                </div>
              )}

              {notes.length === 0 && !isAddingNote ? (
                <div className="bg-[#14263D] border border-[#213B5E] p-6 rounded-2xl text-center">
                  <h4 className="text-sm font-bold text-white mb-1">ابدأ بأول ملاحظة</h4>
                  <p className="text-xs text-[#A4B6D2]">
                    دوّن القوانين والأفكار المهمة أثناء متابعة المحاضرة، ويمكنك ربط الملاحظة بدقيقة محددة في الفيديو.
                  </p>
                </div>
              ) : (
                notes.map((note, idx) => (
                  <div
                    key={idx}
                    className="bg-[#14263D] border border-[#213C60] rounded-xl p-3 space-y-2"
                  >
                    <div className="flex items-center justify-between">
                      <h4 className="text-sm font-bold text-[#20C7B4]">{note.title}</h4>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleEditNote(idx)}
                          className="p-1 text-[#A4B6D2] hover:text-white"
                          title="تعديل"
                        >
                          <Edit2 size={14} />
                        </button>
                        <button
                          onClick={() => handleDeleteNote(idx)}
                          className="p-1 text-[#E68A8A] hover:text-red-400"
                          title="حذف"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </div>

                    <p className="text-xs text-white/90 whitespace-pre-wrap leading-relaxed select-text">
                      {note.body}
                    </p>

                    {(note.positionMs ?? -1) >= 0 && (
                      <button
                        onClick={() => seekToTime(note.positionMs!)}
                        className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-[#1B3454] hover:bg-[#23426A] text-[#E6B74A] text-xs font-mono font-medium transition cursor-pointer"
                      >
                        <Clock size={12} />
                        <span>عند {formatTime(note.positionMs! / 1000)}</span>
                      </button>
                    )}
                  </div>
                ))
              )}
            </div>
          )}

          {activeTab === 'books' && (
            (() => {
              const relevantMaterials = getStudyMaterialsByTeacher(currentRef.teacher.name).concat(
                getStudyMaterialsBySubject(currentRef.teacher.subject).filter(
                  (m) => !m.teacherName.includes(currentRef.teacher.name)
                )
              );

              return (
                <div className="space-y-3">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="text-xs font-bold text-[#E6B74A]">
                      الملازم ومخططات القوانين — {currentRef.teacher.name}
                    </h4>
                    <span className="text-[11px] text-[#A4B6D2]">
                      {relevantMaterials.length} مصادر دراسية متاحة
                    </span>
                  </div>

                  {relevantMaterials.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      {relevantMaterials.map((mat) => (
                        <div
                          key={mat.id}
                          className="bg-[#14263D] border border-[#213E64] p-4 rounded-2xl space-y-2.5 shadow-md hover:border-[#20C7B4]/60 transition"
                        >
                          <div className="flex items-start justify-between">
                            <div>
                              <span className="text-[10px] font-bold text-[#20C7B4] bg-[#20C7B4]/10 px-2 py-0.5 rounded border border-[#20C7B4]/30">
                                {mat.subject} • {mat.pagesCount} صفحات
                              </span>
                              <h5 className="font-bold text-sm text-white mt-1.5 leading-snug">
                                {mat.title}
                              </h5>
                            </div>
                            <BookOpen size={20} className="text-[#20C7B4] flex-shrink-0 mr-2" />
                          </div>

                          <p className="text-xs text-[#A4B6D2] line-clamp-2 leading-relaxed">
                            {mat.description}
                          </p>

                          <button
                            onClick={() => setSelectedMaterial(mat)}
                            className="w-full mt-2 py-2 rounded-xl bg-[#1C3A62] hover:bg-[#20C7B4] text-[#D8E6F7] hover:text-[#0B1626] text-xs font-bold transition flex items-center justify-center gap-1.5 cursor-pointer"
                          >
                            <FileText size={14} />
                            <span>تصفح الملزمة كاملة</span>
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="bg-[#14263D] border border-[#213B5E] p-6 rounded-2xl text-center space-y-2">
                      <BookOpen size={32} className="mx-auto text-[#20C7B4]" />
                      <h4 className="text-sm font-bold text-white">ملازم الأستاذ {currentRef.teacher.name}</h4>
                      <p className="text-xs text-[#A4B6D2] max-w-sm mx-auto leading-relaxed">
                        لا توجد ملازم مضافة لهذا المدرس حالياً. يمكنك تصفح ملازم المواد الأخرى من الشاشة الرئيسية.
                      </p>
                    </div>
                  )}
                </div>
              );
            })()
          )}
        </div>
      </div>

      {/* Embedded Study Guide PDF Viewer Modal */}
      {selectedMaterial && (
        <PdfBookViewerModal
          material={selectedMaterial}
          onClose={() => setSelectedMaterial(null)}
        />
      )}
    </div>
  );
};
