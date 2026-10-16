export interface AppUpdateInfo {
  hasUpdate: boolean;
  currentVersion: string;
  latestVersion: string;
  releaseName: string;
  releaseTag: string;
  publishedAt: string;
  downloadUrl: string;
  releaseUrl: string;
  fileSize?: string;
  changelog: string[];
  forceUpdate?: boolean;
}

export const CURRENT_APP_VERSION = '1.7.0';
export const CURRENT_VERSION_CODE = 20;
export const GITHUB_REPO = '3c5-o/sadsiyoon-app';

// Helper to compare semver versions (returns 1 if v1 > v2, -1 if v1 < v2, 0 if equal)
function compareVersions(v1: string, v2: string): number {
  const clean1 = v1.replace(/^v/i, '').split('.').map((n) => parseInt(n, 10) || 0);
  const clean2 = v2.replace(/^v/i, '').split('.').map((n) => parseInt(n, 10) || 0);

  const maxLen = Math.max(clean1.length, clean2.length);
  for (let i = 0; i < maxLen; i++) {
    const num1 = clean1[i] || 0;
    const num2 = clean2[i] || 0;
    if (num1 > num2) return 1;
    if (num1 < num2) return -1;
  }
  return 0;
}

/**
 * Checks for updates from GitHub Releases API first, with fallback to update.json
 */
export async function checkForAppUpdate(): Promise<AppUpdateInfo> {
  const fallbackChangelog = [
    'قارئ ملازم وملخصات PDF مدمج مع خلاصات القوانين',
    'حاسبة المعدل الوزاري للفرعين العلمي والأدبي وتوقع الكليات',
    'مؤقت دراسي بومودورو لتنظيم فترات المذاكرة والتركيز',
    'توسيع بنك الأسئلة باختبارات فصلية شاملة وتصحيح فوري',
    'واجهة متكيفة تلقائياً مع الشاشات الكبيرة (الأجهزة اللوحية والحواسيب)',
    'تحديثات تلقائية لملفات الـ APK مباشرة من GitHub'
  ];

  try {
    // 1. Try GitHub Releases API
    const res = await fetch(`https://api.github.com/repos/${GITHUB_REPO}/releases/latest`, {
      headers: {
        Accept: 'application/vnd.github.v3+json',
      },
    });

    if (res.ok) {
      const release = await res.json();
      const tagName: string = release.tag_name || release.name || 'v1.7.0';
      const cleanTagName = tagName.replace(/^v/i, '');

      // Look for .apk asset in release assets
      let apkDownloadUrl = '';
      let apkSize = '';
      if (Array.isArray(release.assets)) {
        const apkAsset = release.assets.find((a: { name?: string }) =>
          a.name && a.name.toLowerCase().endsWith('.apk')
        );
        if (apkAsset) {
          apkDownloadUrl = apkAsset.browser_download_url;
          if (apkAsset.size) {
            apkSize = `${(apkAsset.size / (1024 * 1024)).toFixed(1)} MB`;
          }
        }
      }

      if (!apkDownloadUrl) {
        apkDownloadUrl = `https://github.com/${GITHUB_REPO}/releases/download/${tagName}/sadsiyoon-${tagName}-release.apk`;
      }

      // Parse changelog lines from body
      let changelogLines: string[] = [];
      if (release.body) {
        changelogLines = release.body
          .split('\n')
          .map((line: string) => line.replace(/^[-*•\d.]\s*/, '').trim())
          .filter((line: string) => line.length > 5 && !line.startsWith('#') && !line.startsWith('---'));
      }
      if (changelogLines.length === 0) {
        changelogLines = fallbackChangelog;
      }

      const isNewer = compareVersions(cleanTagName, CURRENT_APP_VERSION) > 0;

      return {
        hasUpdate: isNewer,
        currentVersion: CURRENT_APP_VERSION,
        latestVersion: cleanTagName,
        releaseName: release.name || `سادسيون ${tagName}`,
        releaseTag: tagName,
        publishedAt: release.published_at ? release.published_at.slice(0, 10) : '2026-10-16',
        downloadUrl: apkDownloadUrl,
        releaseUrl: release.html_url || `https://github.com/${GITHUB_REPO}/releases`,
        fileSize: apkSize || '14.8 MB',
        changelog: changelogLines.slice(0, 8),
      };
    }
  } catch {
    // If GitHub API is unreachable, fall back to update.json
  }

  // 2. Fallback to /update.json or raw GitHub
  try {
    const rawRes = await fetch('/update.json');
    if (rawRes.ok) {
      const data = await rawRes.json();
      const isNewer = compareVersions(data.version, CURRENT_APP_VERSION) > 0;
      return {
        hasUpdate: isNewer,
        currentVersion: CURRENT_APP_VERSION,
        latestVersion: data.version,
        releaseName: data.name || `سادسيون v${data.version}`,
        releaseTag: data.releaseTag || `v${data.version}`,
        publishedAt: data.releaseDate || '2026-10-16',
        downloadUrl: data.downloadUrl || `https://github.com/${GITHUB_REPO}/releases/latest`,
        releaseUrl: data.latestReleaseUrl || `https://github.com/${GITHUB_REPO}/releases`,
        fileSize: data.fileSize || '14.8 MB',
        changelog: data.changelog || fallbackChangelog,
        forceUpdate: data.forceUpdate || false,
      };
    }
  } catch {
    // Fallback to static current version
  }

  return {
    hasUpdate: false,
    currentVersion: CURRENT_APP_VERSION,
    latestVersion: CURRENT_APP_VERSION,
    releaseName: `سادسيون v${CURRENT_APP_VERSION}`,
    releaseTag: `v${CURRENT_APP_VERSION}`,
    publishedAt: '2026-10-16',
    downloadUrl: `https://github.com/${GITHUB_REPO}/releases/latest`,
    releaseUrl: `https://github.com/${GITHUB_REPO}/releases`,
    changelog: fallbackChangelog,
  };
}

/**
 * Triggers APK download from URL
 */
export function downloadApkUpdate(downloadUrl: string): void {
  // If running in an Android WebView/Capacitor environment, window.open or intent may be used
  const anchor = document.createElement('a');
  anchor.href = downloadUrl;
  anchor.target = '_blank';
  anchor.rel = 'noopener noreferrer';
  anchor.download = 'sadsiyoon-update.apk';
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
}
