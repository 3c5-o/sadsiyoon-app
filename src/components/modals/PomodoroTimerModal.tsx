import React, { useState, useEffect, useRef } from 'react';
import { X, Play, Pause, RotateCcw, SkipForward, Clock, Bell, Sparkles, CheckCircle } from 'lucide-react';

interface PomodoroTimerModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const PomodoroTimerModal: React.FC<PomodoroTimerModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [mode, setMode] = useState<'study' | 'break'>('study');
  const [durationPreset, setDurationPreset] = useState<25 | 50>(25);
  const [timeLeft, setTimeLeft] = useState<number>(25 * 60);
  const [isActive, setIsActive] = useState<boolean>(false);
  const [completedSessions, setCompletedSessions] = useState<number>(() => {
    try {
      return parseInt(localStorage.getItem('sadsiyoon_pomo_count') || '0', 10);
    } catch {
      return 0;
    }
  });

  const totalTime = mode === 'study' ? durationPreset * 60 : durationPreset === 25 ? 5 * 60 : 10 * 60;
  const timerRef = useRef<number | null>(null);

  // Synthesized chime via Web Audio API
  const playChime = () => {
    try {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
      osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.3); // A5

      gain.gain.setValueAtTime(0.3, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 1.2);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 1.2);
    } catch {
      // AudioContext policy
    }
  };

  useEffect(() => {
    if (isActive && timeLeft > 0) {
      timerRef.current = window.setInterval(() => {
        setTimeLeft((prev) => prev - 1);
      }, 1000);
    } else if (timeLeft === 0) {
      playChime();
      if (mode === 'study') {
        const nextCount = completedSessions + 1;
        setCompletedSessions(nextCount);
        try {
          localStorage.setItem('sadsiyoon_pomo_count', nextCount.toString());
        } catch {
          // ignore
        }
        setMode('break');
        setTimeLeft(durationPreset === 25 ? 5 * 60 : 10 * 60);
      } else {
        setMode('study');
        setTimeLeft(durationPreset * 60);
      }
      setIsActive(false);
    }

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [isActive, timeLeft, mode, durationPreset, completedSessions]);

  if (!isOpen) return null;

  const toggleTimer = () => {
    setIsActive(!isActive);
  };

  const resetTimer = () => {
    setIsActive(false);
    setTimeLeft(mode === 'study' ? durationPreset * 60 : durationPreset === 25 ? 5 * 60 : 10 * 60);
  };

  const switchMode = (newMode: 'study' | 'break') => {
    setIsActive(false);
    setMode(newMode);
    if (newMode === 'study') {
      setTimeLeft(durationPreset * 60);
    } else {
      setTimeLeft(durationPreset === 25 ? 5 * 60 : 10 * 60);
    }
  };

  const changePreset = (mins: 25 | 50) => {
    setIsActive(false);
    setDurationPreset(mins);
    if (mode === 'study') {
      setTimeLeft(mins * 60);
    } else {
      setTimeLeft(mins === 25 ? 5 * 60 : 10 * 60);
    }
  };

  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  const progressPercent = ((totalTime - timeLeft) / totalTime) * 100;

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-[#0E1A2E] border border-[#23426D] rounded-3xl w-full max-w-sm shadow-2xl p-6 text-center space-y-5 animate-in fade-in zoom-in-95">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Clock size={20} className="text-[#20C7B4]" />
            <span className="font-bold text-sm text-white">مؤقت بومودورو الدراسي</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-xl text-[#A4B6D2] hover:text-white transition"
          >
            <X size={18} />
          </button>
        </div>

        {/* Mode Switcher */}
        <div className="grid grid-cols-2 gap-2 bg-[#122238] p-1 rounded-2xl border border-[#213C60]">
          <button
            onClick={() => switchMode('study')}
            className={`py-2 rounded-xl font-bold text-xs transition ${
              mode === 'study'
                ? 'bg-[#203E6B] text-white shadow-sm'
                : 'text-[#A4B6D2] hover:text-white'
            }`}
          >
            جلسة مذاكرة
          </button>
          <button
            onClick={() => switchMode('break')}
            className={`py-2 rounded-xl font-bold text-xs transition ${
              mode === 'break'
                ? 'bg-[#1D4A43] text-[#20C7B4] shadow-sm'
                : 'text-[#A4B6D2] hover:text-white'
            }`}
          >
            استراحة
          </button>
        </div>

        {/* Preset selector */}
        {mode === 'study' && (
          <div className="flex justify-center gap-2 text-xs">
            <button
              onClick={() => changePreset(25)}
              className={`px-3 py-1 rounded-lg border transition ${
                durationPreset === 25
                  ? 'bg-[#20C7B4] text-[#0B1626] border-[#20C7B4] font-bold'
                  : 'bg-[#14263D] text-[#A4B6D2] border-[#233F64]'
              }`}
            >
              25 دقيقة (قياسي)
            </button>
            <button
              onClick={() => changePreset(50)}
              className={`px-3 py-1 rounded-lg border transition ${
                durationPreset === 50
                  ? 'bg-[#20C7B4] text-[#0B1626] border-[#20C7B4] font-bold'
                  : 'bg-[#14263D] text-[#A4B6D2] border-[#233F64]'
              }`}
            >
              50 دقيقة (جلسة عميقة)
            </button>
          </div>
        )}

        {/* Circular Display */}
        <div className="relative w-48 h-48 mx-auto flex items-center justify-center">
          <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
            {/* Background ring */}
            <circle
              cx="50"
              cy="50"
              r="44"
              className="stroke-[#172D4A]"
              strokeWidth="6"
              fill="transparent"
            />
            {/* Progress ring */}
            <circle
              cx="50"
              cy="50"
              r="44"
              className={`transition-all duration-300 ${
                mode === 'study' ? 'stroke-[#20C7B4]' : 'stroke-[#38BDF8]'
              }`}
              strokeWidth="6"
              strokeDasharray="276.46"
              strokeDashoffset={276.46 - (276.46 * progressPercent) / 100}
              strokeLinecap="round"
              fill="transparent"
            />
          </svg>

          <div className="absolute inset-0 flex flex-col items-center justify-center select-none">
            <div className="text-4xl font-black text-white font-mono tracking-wider">
              {minutes.toString().padStart(2, '0')}:{seconds.toString().padStart(2, '0')}
            </div>
            <div className="text-xs text-[#20C7B4] font-semibold mt-1">
              {mode === 'study' ? 'تركيز ومذاكرة' : 'استراحة مستحقة'}
            </div>
          </div>
        </div>

        {/* Controls */}
        <div className="flex items-center justify-center gap-4">
          <button
            onClick={resetTimer}
            className="p-3 rounded-full bg-[#182E4B] hover:bg-[#203D63] text-white/80 transition"
            title="إعادة التعيين"
          >
            <RotateCcw size={18} />
          </button>

          <button
            onClick={toggleTimer}
            className={`p-4 rounded-full transition shadow-xl transform active:scale-95 ${
              isActive
                ? 'bg-[#E68A8A] text-[#0B1626]'
                : 'bg-[#20C7B4] text-[#0B1626] hover:bg-[#18AFA0]'
            }`}
          >
            {isActive ? <Pause size={24} /> : <Play size={24} className="mr-0.5" />}
          </button>

          <button
            onClick={() => switchMode(mode === 'study' ? 'break' : 'study')}
            className="p-3 rounded-full bg-[#182E4B] hover:bg-[#203D63] text-white/80 transition"
            title="تخطي"
          >
            <SkipForward size={18} />
          </button>
        </div>

        {/* Stats & Motivation */}
        <div className="bg-[#122238] border border-[#203B5D] p-3 rounded-2xl flex items-center justify-between text-xs">
          <div className="flex items-center gap-1.5 text-[#20C7B4]">
            <CheckCircle size={15} />
            <span>جلسات المذاكرة المنجزة:</span>
          </div>
          <span className="font-bold text-white font-mono text-sm">
            {completedSessions} جلسة
          </span>
        </div>
      </div>
    </div>
  );
};
