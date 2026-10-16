import React, { useState, useEffect } from 'react';
import {
  Globe,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  Play,
  FileText,
  Bookmark,
  Layers,
  GraduationCap,
  BookOpen,
} from 'lucide-react';
import { RemoteContentRecord } from '../../types';
import { fetchPublishedContent } from '../../utils/remoteApi';

export const RemoteCatalogView: React.FC = () => {
  const [records, setRecords] = useState<RemoteContentRecord[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [notice, setNotice] = useState<string>('جارٍ التحقق من المحتوى المنشور...');
  const [parentStack, setParentStack] = useState<Array<{ id: string; title: string }>>([]);

  const loadData = async () => {
    setLoading(true);
    setNotice('جارٍ تحديث قائمة المحتوى...');
    const res = await fetchPublishedContent();
    setRecords(res.records);
    setLoading(false);
    if (res.records.length > 0) {
      setNotice(
        res.fromCache
          ? 'تعذر الاتصال المباشر، يتم عرض آخر نسخة محفوظة دون إنترنت.'
          : 'المحتوى المنشور • تحديث مباشر من قاعدة البيانات'
      );
    } else {
      setNotice(
        res.error ||
          'لا يوجد محتوى منشور متاح حالياً. المحاضرات المحلية باقية في قسم المدرسين.'
      );
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const currentParent = parentStack.length > 0 ? parentStack[parentStack.length - 1] : null;
  const currentParentId = currentParent ? currentParent.id : '';

  // Filter items matching parent ID logic from RemoteCatalogActivity.java
  const existingIds = new Set(records.map((r) => r.id));

  const visibleItems = records.filter((r) => {
    let parent = r.parent_id || '';
    if (parent === 'null') parent = '';

    if (!currentParentId) {
      if (parent && existingIds.has(parent)) return false;
      return true;
    } else {
      return parent === currentParentId;
    }
  });

  const getCategoryLabel = (kind: string) => {
    switch (kind) {
      case 'cohort':
        return 'دفعة';
      case 'subject':
        return 'مادة';
      case 'teacher':
        return 'أستاذ';
      case 'chapter':
        return 'فصل';
      case 'lesson':
        return 'محاضرة';
      case 'resource':
        return 'ملزمة';
      case 'exam':
        return 'وزاري';
      case 'quiz':
        return 'اختبار';
      case 'announcement':
        return 'إعلان';
      default:
        return 'عنصر';
    }
  };

  const getCategoryIcon = (kind: string) => {
    switch (kind) {
      case 'teacher':
        return <GraduationCap size={16} />;
      case 'lesson':
        return <Play size={16} />;
      case 'resource':
      case 'exam':
        return <FileText size={16} />;
      case 'chapter':
        return <Layers size={16} />;
      default:
        return <BookOpen size={16} />;
    }
  };

  const handleDrillDown = (item: RemoteContentRecord) => {
    const hasChildren = records.some((r) => r.parent_id === item.id);
    if (hasChildren) {
      setParentStack((prev) => [...prev, { id: item.id, title: item.title }]);
    } else if (item.media_url || item.source_url) {
      window.open(item.media_url || item.source_url!, '_blank');
    }
  };

  const handlePopStack = () => {
    setParentStack((prev) => prev.slice(0, prev.length - 1));
  };

  return (
    <div className="space-y-4 pb-24">
      {/* Header and Refresh Bar */}
      <div className="bg-[#12233B] border border-[#203D63] p-3.5 rounded-2xl flex items-center justify-between shadow-md">
        <div className="flex items-center gap-2">
          <Globe size={18} className="text-[#20C7B4]" />
          <div>
            <h3 className="font-bold text-sm text-white">محتوى سادسيون المنشور</h3>
            <p className="text-[11px] text-[#A4B6D2]">{notice}</p>
          </div>
        </div>

        <button
          onClick={loadData}
          disabled={loading}
          className="p-2 rounded-xl bg-[#1B3558] hover:bg-[#254773] text-[#20C7B4] transition cursor-pointer disabled:opacity-50"
          title="تحديث المحتوى"
        >
          <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
        </button>
      </div>

      {/* Breadcrumb if navigated into children */}
      {parentStack.length > 0 && (
        <div className="flex items-center gap-2 text-xs">
          <button
            onClick={handlePopStack}
            className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-[#193252] text-[#20C7B4] font-semibold"
          >
            <ChevronRight size={14} />
            <span>رجوع</span>
          </button>
          <span className="text-[#A4B6D2]">
            المسار: {parentStack.map((p) => p.title).join(' / ')}
          </span>
        </div>
      )}

      {/* Items List */}
      <div className="space-y-2.5">
        {visibleItems.map((item) => {
          const hasChildren = records.some((r) => r.parent_id === item.id);

          return (
            <div
              key={item.id}
              onClick={() => handleDrillDown(item)}
              className="bg-[#14263D] hover:bg-[#1A314E] border border-[#213C60] p-4 rounded-2xl flex items-center justify-between transition cursor-pointer shadow-sm group"
            >
              <div className="flex items-center gap-3.5 flex-1 min-w-0">
                <div className="w-10 h-10 rounded-xl bg-[#1B3558] text-[#20C7B4] flex items-center justify-center flex-shrink-0 group-hover:scale-105 transition-transform">
                  {getCategoryIcon(item.kind)}
                </div>

                <div className="flex-1 min-w-0 pr-1">
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-bold text-[#20C7B4] bg-[#20C7B4]/10 px-2 py-0.5 rounded border border-[#20C7B4]/30">
                      {getCategoryLabel(item.kind)}
                    </span>
                    {item.batch && (
                      <span className="text-[10px] text-[#E6B74A]">دفعة {item.batch}</span>
                    )}
                  </div>

                  <h4 className="font-bold text-sm text-white mt-1 truncate">
                    {item.title}
                  </h4>

                  {item.subtitle && (
                    <p className="text-xs text-[#A4B6D2] mt-0.5 truncate">
                      {item.subtitle}
                    </p>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-2 flex-shrink-0 mr-2">
                {hasChildren ? (
                  <ChevronLeft size={18} className="text-[#20C7B4]" />
                ) : item.media_url || item.source_url ? (
                  <ExternalLink size={16} className="text-[#E6B74A]" />
                ) : null}
              </div>
            </div>
          );
        })}

        {visibleItems.length === 0 && !loading && (
          <div className="bg-[#14263D] border border-[#203D61] p-8 rounded-2xl text-center space-y-2">
            <h4 className="text-sm font-bold text-white">لا توجد عناصر في هذا القسم</h4>
            <p className="text-xs text-[#A4B6D2]">
              تظهر هنا المواد والمحاضرات المنشورة من لوحة الإدارة.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};
