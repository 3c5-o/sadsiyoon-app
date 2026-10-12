package com.sadsiyoon.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Public reference lesson metadata. Video stays on its original hosting service. */
public final class LessonCatalog {
    public static final String BASE = "https://edson.hweeriew.workers.dev/smyzx66-dot/housien/releases/download/v1/";
    public static final String TEACHER = "حيدر عبدالائمة";
    public static final String CHAPTER = "الفصل الأول: الأعداد المركبة";
    private static final List<Lesson> LESSONS;
    static {
        List<Lesson> list = new ArrayList<>();
        list.add(new Lesson(0, "المحاضرة التعريفية", "lec_012_1789279642.m3u8"));
        list.add(new Lesson(1, "المحاضرة الأولى – مقدمة عن الأعداد المركبة", "lec_001_1789278887.m3u8"));
        list.add(new Lesson(2, "المحاضرة الثانية – أسس الأعداد المركبة", "lec_002_1789278927.m3u8"));
        list.add(new Lesson(3, "المحاضرة الثالثة – العدد المركب", "lec_003_1789278974.m3u8"));
        list.add(new Lesson(4, "المحاضرة الرابعة – ضرب الأعداد المركبة", "lec_004_1789279051.m3u8"));
        list.add(new Lesson(5, "المحاضرة الخامسة – مرافق العدد المركب", "lec_017_1789279826.m3u8"));
        list.add(new Lesson(6, "المحاضرة السادسة – تبسيط العدد المركب", "lec_018_1789279885.m3u8"));
        list.add(new Lesson(7, "المحاضرة السابعة – تبسيط العدد المركب (الجزء الثاني)", "lec_019_1789279925.m3u8"));
        list.add(new Lesson(8, "المحاضرة الثامنة – إيجاد الثوابت X,Y", "lec_020_1789279969.m3u8"));
        list.add(new Lesson(9, "المحاضرة التاسعة – وزاريات إيجاد الثوابت X,Y", "lec_021_1789280028.m3u8"));
        list.add(new Lesson(10, "المحاضرة العاشرة – وزاريات إيجاد الثوابت X,Y (الجزء الثاني)", "lec_022_1789280061.m3u8"));
        list.add(new Lesson(11, "المحاضرة الحادية عشرة – وزاريات إيجاد الثوابت X,Y (الجزء الثالث)", "lec_023_1789280130.m3u8"));
        list.add(new Lesson(12, "المحاضرة الثانية عشرة – تحليل الفرق بين مربعين", "lec_024_1789280159.m3u8"));
        list.add(new Lesson(13, "المحاضرة الثالثة عشرة – الجذور التربيعية للعدد المركب", "lec_025_1789280213.m3u8"));
        list.add(new Lesson(14, "المحاضرة الرابعة عشرة – الجذور التربيعية للعدد المركب (النوع الثالث)", "lec_026_1789280243.m3u8"));
        list.add(new Lesson(15, "المحاضرة الخامسة عشرة – حل المعادلة من الدرجة الثانية", "lec_027_1789280295.m3u8"));
        list.add(new Lesson(16, "المحاضرة السادسة عشرة – حل المعادلة من الدرجة الثانية (وزاريات)", "lec_028_1789280338.m3u8"));
        list.add(new Lesson(17, "المحاضرة السابعة عشرة – تكوين المعادلة التربيعية", "lec_029_1789280382.m3u8"));
        list.add(new Lesson(18, "المحاضرة الثامنة عشرة – إيجاد الثوابت في المعادلة التربيعية", "lec_030_1789280422.m3u8"));
        list.add(new Lesson(19, "المحاضرة التاسعة عشرة – إيجاد الثوابت في المعادلة التربيعية (وزاريات)", "lec_031_1789280464.m3u8"));
        list.add(new Lesson(20, "المحاضرة العشرون – التمثيل الهندسي للأعداد المركبة (شكل أرجاند)", "lec_032_1789280503.m3u8"));
        list.add(new Lesson(21, "المحاضرة الحادية والعشرون – المقياس والقيمة الأساسية للسعة", "lec_033_1789280542.m3u8"));
        list.add(new Lesson(22, "المحاضرة الثانية والعشرون – الزوايا في ديموافر", "lec_034_1789280597.m3u8"));
        list.add(new Lesson(23, "ديموافر – الأسئلة الوزارية", "lec_035_1789280647.m3u8"));
        list.add(new Lesson(24, "المحاضرة الرابعة والعشرون – مبرهنة ديموافر", "lec_036_1789280699.m3u8"));
        list.add(new Lesson(25, "تطبيق مبرهنة ديموافر", "lec_037_1789280744.m3u8"));
        list.add(new Lesson(26, "المحاضرة السادسة والعشرون – مبرهنة ديموافر (وزاريات)", "lec_038_1789280781.m3u8"));
        list.add(new Lesson(27, "المحاضرة السابعة والعشرون – مبرهنة ديموافر (إثرائيات)", "lec_039_1789280817.m3u8"));
        list.add(new Lesson(28, "المحاضرة الثامنة والعشرون – مبرهنة ديموافر (الأسئلة الوزارية)", "lec_040_1789280858.m3u8"));
        list.add(new Lesson(29, "المحاضرة التاسعة والعشرون – نتيجة مبرهنة ديموافر", "lec_041_1789280900.m3u8"));
        list.add(new Lesson(30, "المحاضرة الثلاثون – وزاريات نتيجة مبرهنة ديموافر", "lec_042_1789280945.m3u8"));
        list.add(new Lesson(31, "المحاضرة الحادية والثلاثون – وزاريات نتيجة مبرهنة ديموافر (الجزء الثاني)", "lec_043_1789280987.m3u8"));
        list.add(new Lesson(32, "المحاضرة الثانية والثلاثون – الأوميكا", "lec_044_1789281027.m3u8"));
        list.add(new Lesson(33, "المحاضرة الثالثة والثلاثون – وزاريات الأوميكا", "lec_045_1789281086.m3u8"));
        list.add(new Lesson(34, "المحاضرة الرابعة والثلاثون – وزاريات الأوميكا (الجزء الثاني)", "lec_046_1789281136.m3u8"));
        list.add(new Lesson(35, "المحاضرة الخامسة والثلاثون – المحاضرة الأخيرة", "lec_047_1789281178.m3u8"));
        list.add(new Lesson(36, "مراجعة الفصل الأول – الجزء الأول", "lec_010_1789279520.m3u8"));
        list.add(new Lesson(37, "مراجعة الفصل الأول – الجزء الثاني", "lec_008_1789279299.m3u8"));
        list.add(new Lesson(38, "مراجعة الفصل الأول – الجزء الثالث", "lec_009_1789279395.m3u8"));
        list.add(new Lesson(39, "إثرائيات الفصل الأول", "lec_011_1789279616.m3u8"));
        LESSONS = Collections.unmodifiableList(list);
    }
    private LessonCatalog() {}
    public static List<Lesson> all() { return LESSONS; }
    public static Lesson at(int i) { return LESSONS.get(Math.max(0, Math.min(i, LESSONS.size()-1))); }
    public static int size() { return LESSONS.size(); }
    public static final class Lesson {
        public final int index;
        public final String title;
        public final String fileName;
        private Lesson(int index, String title, String fileName) { this.index=index; this.title=title; this.fileName=fileName; }
        public String streamUrl() { return BASE + fileName; }
        /** Server-hosted browser player; not an HLS manifest. */
        public String serverPlayerUrl() { return streamUrl() + "?player=1"; }
    }
}
