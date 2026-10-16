import { NoteItem } from '../types';

const STORAGE_PREFIX = 'sadsiyoon_';

function getKey(key: string): string {
  return `${STORAGE_PREFIX}${key}`;
}

export function getFavorite(lessonKey: string): boolean {
  try {
    return localStorage.getItem(getKey(`fav_${lessonKey}`)) === 'true';
  } catch {
    return false;
  }
}

export function setFavorite(lessonKey: string, value: boolean): void {
  try {
    if (value) {
      localStorage.setItem(getKey(`fav_${lessonKey}`), 'true');
    } else {
      localStorage.removeItem(getKey(`fav_${lessonKey}`));
    }
  } catch (e) {
    console.error('Storage error:', e);
  }
}

export function getCompleted(lessonKey: string): boolean {
  try {
    return localStorage.getItem(getKey(`done_${lessonKey}`)) === 'true';
  } catch {
    return false;
  }
}

export function setCompleted(lessonKey: string, value: boolean): void {
  try {
    if (value) {
      localStorage.setItem(getKey(`done_${lessonKey}`), 'true');
    } else {
      localStorage.removeItem(getKey(`done_${lessonKey}`));
    }
  } catch (e) {
    console.error('Storage error:', e);
  }
}

export function getPosition(lessonKey: string): number {
  try {
    const val = localStorage.getItem(getKey(`position_${lessonKey}`));
    return val ? parseInt(val, 10) || 0 : 0;
  } catch {
    return 0;
  }
}

export function setPosition(lessonKey: string, positionMs: number): void {
  try {
    if (positionMs > 0) {
      localStorage.setItem(getKey(`position_${lessonKey}`), Math.round(positionMs).toString());
    }
  } catch (e) {
    console.error('Storage error:', e);
  }
}

export function getLastWatched(): { ti: number; ci: number; li: number } | null {
  try {
    const t = localStorage.getItem(getKey('last_t'));
    const c = localStorage.getItem(getKey('last_c'));
    const l = localStorage.getItem(getKey('last_l'));
    if (t !== null && c !== null && l !== null) {
      return {
        ti: parseInt(t, 10),
        ci: parseInt(c, 10),
        li: parseInt(l, 10),
      };
    }
    return null;
  } catch {
    return null;
  }
}

export function setLastWatched(ti: number, ci: number, li: number): void {
  try {
    localStorage.setItem(getKey('last_t'), ti.toString());
    localStorage.setItem(getKey('last_c'), ci.toString());
    localStorage.setItem(getKey('last_l'), li.toString());
  } catch (e) {
    console.error('Storage error:', e);
  }
}

export function getNickname(): string {
  try {
    return localStorage.getItem(getKey('nickname')) || 'طالب سادسيون';
  } catch {
    return 'طالب سادسيون';
  }
}

export function setNickname(name: string): void {
  try {
    localStorage.setItem(getKey('nickname'), name.trim());
  } catch (e) {
    console.error('Storage error:', e);
  }
}

export function getAutoplay(): boolean {
  try {
    const val = localStorage.getItem(getKey('autoplay'));
    return val === null ? true : val === 'true';
  } catch {
    return true;
  }
}

export function setAutoplay(val: boolean): void {
  try {
    localStorage.setItem(getKey('autoplay'), val ? 'true' : 'false');
  } catch (e) {
    console.error('Storage error:', e);
  }
}

export function getBestPracticeScore(branch: string): number {
  try {
    const val = localStorage.getItem(getKey(`practice_best_${branch}`));
    return val ? parseInt(val, 10) || 0 : 0;
  } catch {
    return 0;
  }
}

export function setBestPracticeScore(branch: string, score: number): void {
  try {
    const current = getBestPracticeScore(branch);
    if (score > current) {
      localStorage.setItem(getKey(`practice_best_${branch}`), score.toString());
    }
  } catch (e) {
    console.error('Storage error:', e);
  }
}

export function getNotes(lessonKey: string): NoteItem[] {
  try {
    const raw = localStorage.getItem(getKey(`note_cards_${lessonKey}`));
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (Array.isArray(parsed)) {
      return parsed;
    }
    return [];
  } catch {
    return [];
  }
}

export function saveNote(
  lessonKey: string,
  note: { title: string; body: string; positionMs?: number },
  editIndex?: number
): boolean {
  try {
    const notes = getNotes(lessonKey);
    if (editIndex !== undefined && editIndex >= 0 && editIndex < notes.length) {
      notes[editIndex] = {
        title: note.title.trim(),
        body: note.body.trim(),
        createdAt: notes[editIndex].createdAt,
        positionMs: note.positionMs ?? notes[editIndex].positionMs,
      };
    } else {
      if (notes.length >= 60) return false;
      notes.push({
        title: note.title.trim(),
        body: note.body.trim(),
        createdAt: Date.now(),
        positionMs: note.positionMs ?? -1,
      });
    }
    localStorage.setItem(getKey(`note_cards_${lessonKey}`), JSON.stringify(notes));
    return true;
  } catch (e) {
    console.error('Error saving note:', e);
    return false;
  }
}

export function deleteNote(lessonKey: string, index: number): boolean {
  try {
    const notes = getNotes(lessonKey);
    if (index >= 0 && index < notes.length) {
      notes.splice(index, 1);
      localStorage.setItem(getKey(`note_cards_${lessonKey}`), JSON.stringify(notes));
      return true;
    }
    return false;
  } catch (e) {
    console.error('Error deleting note:', e);
    return false;
  }
}

export function clearAllProgress(): void {
  try {
    const keysToRemove: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const k = localStorage.key(i);
      if (k && k.startsWith(STORAGE_PREFIX)) {
        if (
          k.includes('fav_') ||
          k.includes('done_') ||
          k.includes('position_') ||
          k.includes('note_') ||
          k.includes('last_')
        ) {
          keysToRemove.push(k);
        }
      }
    }
    keysToRemove.forEach((k) => localStorage.removeItem(k));
  } catch (e) {
    console.error('Error clearing progress:', e);
  }
}

export function exportBackup(): string {
  const data: Record<string, unknown> = {};
  for (let i = 0; i < localStorage.length; i++) {
    const k = localStorage.key(i);
    if (k && k.startsWith(STORAGE_PREFIX)) {
      const rawKey = k.replace(STORAGE_PREFIX, '');
      const rawVal = localStorage.getItem(k);
      if (rawVal !== null) {
        if (rawKey.startsWith('fav_') || rawKey.startsWith('done_')) {
          data[rawKey] = rawVal === 'true';
        } else if (rawKey.startsWith('position_') || rawKey.startsWith('last_') || rawKey.startsWith('practice_best_')) {
          data[rawKey] = parseInt(rawVal, 10);
        } else {
          data[rawKey] = rawVal;
        }
      }
    }
  }

  const payload = {
    format: 'sadsiyoon-backup-v1',
    version: 1,
    created_at: Date.now(),
    data,
  };

  return JSON.stringify(payload, null, 2);
}

export function importBackup(jsonString: string): { success: boolean; count: number; error?: string } {
  try {
    const parsed = JSON.parse(jsonString);
    if (parsed.format !== 'sadsiyoon-backup-v1' || parsed.version !== 1 || !parsed.data) {
      return { success: false, count: 0, error: 'صيغة ملف النسخ الاحتياطي غير متوافقة' };
    }

    let count = 0;
    const items = parsed.data as Record<string, unknown>;
    for (const [key, value] of Object.entries(items)) {
      // Validate key shape
      const isValidKey =
        /^(fav|done|position|note|note_cards)_t[0-9]+_c[0-9]+_l[0-9]+$/.test(key) ||
        /^(last_t|last_c|last_l|nickname|autoplay|practice_best_(scientific|literary))$/.test(key);

      if (isValidKey) {
        const fullKey = getKey(key);
        if (typeof value === 'boolean') {
          localStorage.setItem(fullKey, value ? 'true' : 'false');
        } else if (typeof value === 'number') {
          localStorage.setItem(fullKey, value.toString());
        } else if (typeof value === 'string') {
          localStorage.setItem(fullKey, value);
        }
        count++;
      }
    }

    return { success: true, count };
  } catch {
    return { success: false, count: 0, error: 'تعذر قراءة ملف JSON' };
  }
}
