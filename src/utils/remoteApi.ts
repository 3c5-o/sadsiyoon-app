import { RemoteContentRecord } from '../types';

export const SUPABASE_URL = 'https://wvelvyxxfyvzgawzrawg.supabase.co';
export const SUPABASE_PUBLISHABLE_KEY = 'sb_publishable_QuU0Spr5NS04aGFy7YWajQ_5Wu22UOV';

const CACHE_KEY = 'sadsiyoon_published_catalog_cache';

export async function fetchPublishedContent(): Promise<{ records: RemoteContentRecord[]; fromCache: boolean; error?: string }> {
  try {
    const res = await fetch(
      `${SUPABASE_URL}/rest/v1/sadesion_content?select=*&status=eq.published&order=sort_index.asc,created_at.asc&limit=1000`,
      {
        headers: {
          apikey: SUPABASE_PUBLISHABLE_KEY,
          Accept: 'application/json',
        },
      }
    );

    if (!res.ok) {
      throw new Error(`HTTP error: ${res.status}`);
    }

    const data: RemoteContentRecord[] = await res.json();
    try {
      localStorage.setItem(CACHE_KEY, JSON.stringify(data));
    } catch {
      // ignore quota exceeded
    }
    return { records: data, fromCache: false };
  } catch (err) {
    console.warn('Network error fetching published content, falling back to cache:', err);
    try {
      const cached = localStorage.getItem(CACHE_KEY);
      if (cached) {
        return { records: JSON.parse(cached), fromCache: true };
      }
    } catch {
      // ignore
    }
    return { records: [], fromCache: false, error: 'تعذر الاتصال بالمحتوى المنشور' };
  }
}

export function getCachedPublishedContent(): RemoteContentRecord[] {
  try {
    const cached = localStorage.getItem(CACHE_KEY);
    return cached ? JSON.parse(cached) : [];
  } catch {
    return [];
  }
}
