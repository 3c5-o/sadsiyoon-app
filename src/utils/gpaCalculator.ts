export interface SubjectGrade {
  name: string;
  grade: number;
}

export interface GpaCalculationOptions {
  branch: 'scientific' | 'literary';
  isFirstRoundPass: boolean;
  hasFrenchLanguage: boolean;
  frenchGrade?: number;
  martyrBonus: number; // 0, 5, or 7
}

export interface GpaCalculationResult {
  totalSum: number;
  maxTotal: number;
  rawAverage: number;
  finalGpa: number;
  appliedBonuses: Array<{ name: string; value: number }>;
  predictedSpecializations: Array<{
    name: string;
    minGpa: number;
    suitable: boolean;
    category: string;
  }>;
}

export const SCIENTIFIC_DEFAULT_SUBJECTS = [
  'التربية الإسلامية',
  'اللغة العربية',
  'اللغة الإنكليزية',
  'الأحياء',
  'الرياضيات',
  'الكيمياء',
  'الفيزياء',
];

export const LITERARY_DEFAULT_SUBJECTS = [
  'التربية الإسلامية',
  'اللغة العربية',
  'اللغة الإنكليزية',
  'التاريخ',
  'الجغرافية',
  'الاقتصاد',
  'الرياضيات',
];

export function calculateMinisterialGpa(
  grades: Record<string, number>,
  options: GpaCalculationOptions
): GpaCalculationResult {
  const subjectList =
    options.branch === 'scientific' ? SCIENTIFIC_DEFAULT_SUBJECTS : LITERARY_DEFAULT_SUBJECTS;

  let totalSum = 0;
  let validCount = 0;

  for (const sub of subjectList) {
    const val = grades[sub] ?? 0;
    totalSum += val;
    validCount++;
  }

  const rawAverage = validCount > 0 ? totalSum / validCount : 0;
  let bonusTotal = 0;
  const appliedBonuses: Array<{ name: string; value: number }> = [];

  // First Round bonus (+1 degree on final GPA per Iraqi ministry regulation)
  if (options.isFirstRoundPass) {
    bonusTotal += 1.0;
    appliedBonuses.push({ name: 'درجة الدور الأول الوزاري', value: 1.0 });
  }

  // French language bonus: 8% of French grade added to total sum (or 8/7 on average)
  if (options.hasFrenchLanguage && options.frenchGrade && options.frenchGrade >= 50) {
    const frenchBonus = Number(((options.frenchGrade * 0.08) / 7).toFixed(2));
    bonusTotal += frenchBonus;
    appliedBonuses.push({
      name: `إضافة اللغة الفرنسية (${options.frenchGrade})`,
      value: frenchBonus,
    });
  }

  // Martyr family bonus
  if (options.martyrBonus > 0) {
    bonusTotal += options.martyrBonus;
    appliedBonuses.push({
      name: `درجات ذوي الشهداء والمشمولين بالرعاية`,
      value: options.martyrBonus,
    });
  }

  const finalGpa = Number(Math.min(110, rawAverage + bonusTotal).toFixed(2));

  // Determine predicted university tiers based on official Iraqi Central Admission thresholds
  const specializations =
    options.branch === 'scientific'
      ? [
          { name: 'الطب العام (بغداد / النهرين / بابل / البصرة)', minGpa: 99.0, category: 'كليات الطب' },
          { name: 'طب الأسنان (الجامعات الحكومية)', minGpa: 97.5, category: 'المجموعة الطبية' },
          { name: 'الصيدلة (الجامعات الحكومية)', minGpa: 96.5, category: 'المجموعة الطبية' },
          { name: 'هندسة النفط والمعماري والطيران', minGpa: 93.0, category: 'المجموعة الهندسية' },
          { name: 'هندسة الحاسوب والذكاء الاصطناعي', minGpa: 89.0, category: 'المجموعة الهندسية' },
          { name: 'كلية التمريض والتقنيات الطبية', minGpa: 86.0, category: 'المعاهد الطبية والتمريض' },
          { name: 'كلية العلوم (حاسوب، كيمياء، أدلة جنائية)', minGpa: 76.0, category: 'العلوم الصرفة' },
          { name: 'كلية التربية للعلوم الصرفة', minGpa: 70.0, category: 'التربية' },
          { name: 'المعاهد التكنولوجية والهندسية', minGpa: 60.0, category: 'المعاهد التقنية' },
        ]
      : [
          { name: 'كلية القانون (جامعة بغداد / المستنصرية)', minGpa: 85.0, category: 'القانون والسياسة' },
          { name: 'كلية العلوم السياسية', minGpa: 80.0, category: 'القانون والسياسة' },
          { name: 'كلية اللغات والترجمة (إنكليزي / فرنسي)', minGpa: 77.0, category: 'اللغات والآداب' },
          { name: 'كلية الإدارة والاقتصاد', minGpa: 72.0, category: 'العلوم الاقتصادية' },
          { name: 'كلية التربية للعلوم الإنسانية (تاريخ / جغرافية)', minGpa: 68.0, category: 'التربية' },
          { name: 'كلية الآداب والإعلام', minGpa: 65.0, category: 'الإعلام والآداب' },
          { name: 'كلية التربية الأساسية', minGpa: 62.0, category: 'التربية الأساسية' },
          { name: 'المعاهد الإدارية والفنون الجميلة', minGpa: 60.0, category: 'المعاهد' },
        ];

  const predictedSpecializations = specializations.map((spec) => ({
    ...spec,
    suitable: finalGpa >= spec.minGpa,
  }));

  return {
    totalSum,
    maxTotal: validCount * 100,
    rawAverage: Number(rawAverage.toFixed(2)),
    finalGpa,
    appliedBonuses,
    predictedSpecializations,
  };
}
