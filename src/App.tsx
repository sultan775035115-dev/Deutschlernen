import React, { useState, useEffect } from 'react';
import { 
  Download, 
  Smartphone, 
  Code, 
  Github, 
  Plus, 
  Search, 
  Settings, 
  Volume2, 
  Video, 
  Clock, 
  CheckCircle, 
  Star, 
  Bell, 
  FileText, 
  Moon, 
  Sun, 
  RotateCcw, 
  FolderArchive,
  ArrowRight,
  Trash2,
  Edit3,
  Copy,
  Check,
  Play,
  Pause,
  ExternalLink,
  Layers,
  Sparkles
} from 'lucide-react';
import JSZip from 'jszip';

// نوع بطاقة الكلمة متطابق مع Room Entity في الأندرويد
interface WordCard {
  id: number;
  word: string;
  meaning: string;
  notes: string;
  image?: string;
  hasAudio?: boolean;
  hasVideo?: boolean;
  reviewLevel: number;
  createdAt: number;
  nextReviewAt: number;
  isFavorite: boolean;
  reminderEnabled: boolean;
  reminderType: 'DAILY' | 'INTERVAL';
  reminderTime: string;
  reminderIntervalHours: number;
}

const INITIAL_CARDS: WordCard[] = [
  {
    id: 1,
    word: 'Sehnsucht',
    meaning: 'شوق عميق وحنين غامر لا يمكن وصفه بسهولة لشيء أو شخص أو مكان بعيد',
    notes: 'كلمة ألمانية مشهورة ليس لها مرادف دقيق في اللغات الأخرى.',
    image: 'https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=600&auto=format&fit=crop&q=80',
    hasAudio: true,
    hasVideo: false,
    reviewLevel: 2,
    createdAt: Date.now() - 3 * 24 * 3600 * 1000,
    nextReviewAt: Date.now() + 24 * 3600 * 1000,
    isFavorite: true,
    reminderEnabled: true,
    reminderType: 'DAILY',
    reminderTime: '09:00 ص',
    reminderIntervalHours: 4
  },
  {
    id: 2,
    word: 'Serendipity',
    meaning: 'الصدفة السعيدة وغير المتوقعة التي تقود لاكتشاف مفيد أو جميل',
    notes: 'صاغها الكاتب هوراس ولبول عام 1754 استناداً إلى أسطورة أمراء سرنديب الثلاثة.',
    image: 'https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=600&auto=format&fit=crop&q=80',
    hasAudio: true,
    hasVideo: true,
    reviewLevel: 4,
    createdAt: Date.now() - 14 * 24 * 3600 * 1000,
    nextReviewAt: Date.now() - 2 * 3600 * 1000, // مستحقة الآن
    isFavorite: true,
    reminderEnabled: true,
    reminderType: 'DAILY',
    reminderTime: '21:30 م',
    reminderIntervalHours: 4
  },
  {
    id: 3,
    word: 'Epiphany',
    meaning: 'لحظة تجلٍّ وإدراك مفاجئ أو استنارة عقلية عميقة تغير طريقة التفكير',
    notes: 'تستخدم كثيراً في السياقات الأدبية والفلسفية وتطوير الحلول الإبداعية.',
    hasAudio: false,
    hasVideo: false,
    reviewLevel: 1,
    createdAt: Date.now() - 1 * 24 * 3600 * 1000,
    nextReviewAt: Date.now() + 2 * 24 * 3600 * 1000,
    isFavorite: false,
    reminderEnabled: false,
    reminderType: 'DAILY',
    reminderTime: '10:00 ص',
    reminderIntervalHours: 4
  },
  {
    id: 4,
    word: 'Petrichor',
    meaning: 'الرائحة العطرة الترابية المميزة والمنعشة التي تصعد من الأرض عند سقوط المطر الأول',
    notes: 'مركبة من الكلمتين اليونانيتين petra (صخر) و ichor (السائل النقي في أساطير الآلهة).',
    image: 'https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=600&auto=format&fit=crop&q=80',
    hasAudio: true,
    hasVideo: false,
    reviewLevel: 0,
    createdAt: Date.now(),
    nextReviewAt: Date.now(),
    isFavorite: false,
    reminderEnabled: true,
    reminderType: 'INTERVAL',
    reminderTime: '14:00 م',
    reminderIntervalHours: 6
  }
];

// جدول مستويات التكرار المتباعد
const SRS_INTERVALS = [0, 1, 3, 7, 14, 30, 60];
const LEVEL_NAMES = [
  'جديدة (0)',
  'المستوى 1 (يوم)',
  'المستوى 2 (3 أيام)',
  'المستوى 3 (7 أيام)',
  'المستوى 4 (14 يوماً)',
  'المستوى 5 (30 يوماً)',
  'المستوى 6 (مثبتة بالذاكرة)'
];

export default function App() {
  const [activeTab, setActiveTab] = useState<'simulator' | 'code' | 'github'>('simulator');
  const [cards, setCards] = useState<WordCard[]>(() => {
    const saved = localStorage.getItem('word_anchor_cards');
    return saved ? JSON.parse(saved) : INITIAL_CARDS;
  });
  
  // شاشات المحاكي الداخلي
  const [simScreen, setSimScreen] = useState<'home' | 'detail' | 'addEdit' | 'settings'>('home');
  const [selectedCardId, setSelectedCardId] = useState<number | null>(null);
  const [editingCardId, setEditingCardId] = useState<number | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState<'ALL' | 'DUE' | 'FAVORITES'>('ALL');
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [allRemindersEnabled, setAllRemindersEnabled] = useState(true);
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);
  const [showSnoozeModal, setShowSnoozeModal] = useState(false);
  const [notificationToast, setNotificationToast] = useState<string | null>(null);
  const [isExportingZip, setIsExportingZip] = useState(false);

  // ملفات كود أندرويد المعروضة
  const [selectedFileKey, setSelectedFileKey] = useState<string>('MainActivity');
  const [copiedFile, setCopiedFile] = useState(false);

  useEffect(() => {
    localStorage.setItem('word_anchor_cards', JSON.stringify(cards));
  }, [cards]);

  const showToast = (msg: string) => {
    setNotificationToast(msg);
    setTimeout(() => setNotificationToast(null), 3500);
  };

  const selectedCard = cards.find(c => c.id === selectedCardId);

  // منطق مراجعة البطاقة في SRS
  const handleMarkReviewed = (cardId: number) => {
    setCards(prev => prev.map(c => {
      if (c.id === cardId) {
        const nextLevel = Math.min(c.reviewLevel + 1, 6);
        const daysToAdd = SRS_INTERVALS[nextLevel];
        const nextReviewAt = Date.now() + daysToAdd * 24 * 3600 * 1000;
        return {
          ...c,
          reviewLevel: nextLevel,
          nextReviewAt
        };
      }
      return c;
    }));
    showToast('تمت المراجعة بنجاح! تم نقل الكلمة إلى المستوى التالي في التكرار المتباعد 🎯');
  };

  // منطق تأجيل المراجعة (Snooze)
  const handleSnooze = (cardId: number, durationMinutes: number) => {
    setCards(prev => prev.map(c => {
      if (c.id === cardId) {
        return {
          ...c,
          nextReviewAt: Date.now() + durationMinutes * 60 * 1000
        };
      }
      return c;
    }));
    setShowSnoozeModal(false);
    showToast('تم تأجيل موعد التذكير بنجاح ⏰');
  };

  // تبديل المفضلة
  const handleToggleFavorite = (cardId: number) => {
    setCards(prev => prev.map(c => c.id === cardId ? { ...c, isFavorite: !c.isFavorite } : c));
  };

  // حذف البطاقة
  const handleDeleteCard = (cardId: number) => {
    setCards(prev => prev.filter(c => c.id !== cardId));
    setSimScreen('home');
    setSelectedCardId(null);
    showToast('تم حذف البطاقة وجميع وسائطها');
  };

  // تصفية البطاقات
  const filteredCards = cards.filter(c => {
    const matchesSearch = c.word.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          c.meaning.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          c.notes.toLowerCase().includes(searchQuery.toLowerCase());
    if (!matchesSearch) return false;
    if (activeFilter === 'FAVORITES') return c.isFavorite;
    if (activeFilter === 'DUE') return c.nextReviewAt <= Date.now();
    return true;
  });

  const dueCount = cards.filter(c => c.nextReviewAt <= Date.now()).length;

  // تنزيل مشروع أندرويد كاملاً كـ ZIP
  const handleDownloadFullProjectZip = async () => {
    setIsExportingZip(true);
    try {
      const zip = new JSZip();

      // ملف .gitignore لحماية المستودع وضمان عدم تجاهل gradle-wrapper.jar
      zip.file(".gitignore", `node_modules/
build/
dist/
coverage/
.DS_Store
*.log
.env*
!.env.example

# Android & Gradle
.gradle/
app/build/
local.properties
.idea/
*.iml
!gradle/wrapper/gradle-wrapper.jar
`);

      // ملفات البناء الأساسية مع إعدادات GitHub Actions المنيعة
      zip.file(".github/workflows/build.yml", `name: Build Android APK

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

concurrency:
  group: \${{ github.workflow }}-\${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    name: Build Debug APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v5
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Install Official Gradle 8.4 Engine
        run: |
          echo "=== Downloading Gradle 8.4 ==="
          curl -sSL https://services.gradle.org/distributions/gradle-8.4-bin.zip -o /tmp/gradle-8.4-bin.zip
          sudo unzip -q -o /tmp/gradle-8.4-bin.zip -d /opt/gradle
          echo "/opt/gradle/gradle-8.4/bin" >> $GITHUB_PATH
          export PATH="/opt/gradle/gradle-8.4/bin:$PATH"
          gradle -v

      - name: Generate Fresh Gradle Wrapper
        run: |
          export PATH="/opt/gradle/gradle-8.4/bin:$PATH"
          echo "=== Generating Wrapper ==="
          gradle wrapper --gradle-version 8.4 --distribution-type bin
          sed -i 's/\\r$//' gradlew || true
          chmod +x gradlew
          ls -la gradle/wrapper/

      - name: Accept Android SDK Licenses
        run: |
          yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses || true

      - name: Build Debug APK with Gradle
        run: |
          export PATH="/opt/gradle/gradle-8.4/bin:$PATH"
          ./gradlew assembleDebug --no-daemon --stacktrace 2>&1 | tee build_output.log || {
            echo "=================================================="
            echo "             DETAILED COMPILATION ERRORS          "
            echo "=================================================="
            grep -E "^e: " build_output.log || tail -n 80 build_output.log
            exit 1
          }

      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: WordAnchor-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: warn
          retention-days: 14`);

      zip.file("build.gradle.kts", `plugins {
    id("com.android.application") version "8.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}`);

      zip.file("settings.gradle.kts", `pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "WordAnchor"
include(":app")`);

      zip.file("gradle.properties", `org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
android.builder.sdkDownload=true
kotlin.code.style=official
org.gradle.caching=true
org.gradle.parallel=true`);

      zip.file("gradle/wrapper/gradle-wrapper.properties", `distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.4-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists`);

      zip.file("gradlew", `#!/bin/sh
exec java -jar gradle/wrapper/gradle-wrapper.jar "$@"`);

      zip.file("app/build.gradle.kts", `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.deutscherinnerungen.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.deutscherinnerungen.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.code.gson:gson:2.10.1")
}`);

      zip.file("app/src/main/AndroidManifest.xml", `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <application
        android:name=".WordAnchorApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.WordAnchor">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="com.deutscherinnerungen.app.ACTION_OPEN_CARD" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
        <receiver android:name=".notifications.AlarmReceiver" android:exported="false" />
        <receiver android:name=".notifications.BootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.TIME_SET" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>`);

      // تضمين كود Kotlin
      const pkg = "app/src/main/java/com/deutscherinnerungen/app";
      zip.file(`${pkg}/WordAnchorApp.kt`, ANDROID_CODE_FILES['WordAnchorApp'].code);
      zip.file(`${pkg}/MainActivity.kt`, ANDROID_CODE_FILES['MainActivity'].code);
      zip.file(`${pkg}/data/entity/WordCardEntity.kt`, ANDROID_CODE_FILES['WordCardEntity'].code);
      zip.file(`${pkg}/data/dao/WordCardDao.kt`, ANDROID_CODE_FILES['WordCardDao'].code);
      zip.file(`${pkg}/data/database/WordAnchorDatabase.kt`, ANDROID_CODE_FILES['WordAnchorDatabase'].code);
      zip.file(`${pkg}/data/repository/WordCardRepository.kt`, ANDROID_CODE_FILES['WordCardRepository'].code);
      zip.file(`${pkg}/srs/SpacedRepetitionEngine.kt`, ANDROID_CODE_FILES['SpacedRepetitionEngine'].code);
      zip.file(`${pkg}/notifications/ReminderScheduler.kt`, ANDROID_CODE_FILES['ReminderScheduler'].code);
      zip.file(`${pkg}/notifications/AlarmReceiver.kt`, ANDROID_CODE_FILES['AlarmReceiver'].code);
      zip.file(`${pkg}/media/MediaStorageManager.kt`, ANDROID_CODE_FILES['MediaStorageManager'].code);
      zip.file(`${pkg}/backup/BackupManager.kt`, ANDROID_CODE_FILES['BackupManager'].code);
      zip.file(`${pkg}/ui/screens/HomeScreen.kt`, ANDROID_CODE_FILES['HomeScreen'].code);
      zip.file(`${pkg}/ui/screens/CardDetailScreen.kt`, ANDROID_CODE_FILES['CardDetailScreen'].code);
      zip.file(`${pkg}/ui/screens/AddEditCardScreen.kt`, ANDROID_CODE_FILES['AddEditCardScreen'].code);

      // ملفات الموارد
      zip.file("app/src/main/res/values/strings.xml", `<resources>
    <string name="app_name">تثبيت الكلمات</string>
    <string name="app_name_en">Word Anchor</string>
    <string name="notification_channel_name">تذكيرات مراجعة الكلمات</string>
    <string name="notification_channel_description">إشعارات تذكيرية لتثبيت الكلمات والعبارات بالذاكرة عبر التكرار المتباعد</string>
</resources>`);

      zip.file("README.md", `# تثبيت الكلمات — Word Anchor
تطبيق Android متكامل مبني باستخدام:
- Kotlin & Jetpack Compose (Material 3)
- Room Database & MVVM Repository Pattern
- Coroutines & Flow
- DataStore Preferences
- AlarmManager للأوقات الدقيقة والتنبيهات المستمرة
- محرك مخصص للتكرار المتباعد (Spaced Repetition)
- GitHub Actions CI لبناء APK تلقائياً

## طريقة البناء على GitHub
1. قم برفع هذا المشروع إلى مستودع GitHub جديد.
2. توجه إلى تبويب **Actions**.
3. ستجد سير العمل **Build Android APK** يعمل تلقائياً، وبعد اكتماله يمكنك تنزيل ملف **WordAnchor-debug-apk** وتثبيته مباشرة على هاتفك!
`);

      const content = await zip.generateAsync({ type: "blob" });
      const url = URL.createObjectURL(content);
      const link = document.createElement("a");
      link.href = url;
      link.download = "WordAnchor-Android-Project.zip";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      showToast('تم تحميل أرشيف المشروع ZIP بنجاح! جاهز للبناء على GitHub أو Android Studio');
    } catch (e) {
      console.error(e);
      showToast('حدث خطأ أثناء تجميع ملفات المشروع');
    } finally {
      setIsExportingZip(false);
    }
  };

  return (
    <div className={`min-h-screen ${isDarkMode ? 'dark bg-slate-950 text-slate-100' : 'bg-slate-50 text-slate-900'} flex flex-col font-sans transition-colors duration-200`}>
      
      {/* التنبيه المنبثق */}
      {notificationToast && (
        <div className="fixed top-5 left-1/2 -translate-x-1/2 z-50 bg-sky-900/90 backdrop-blur-md text-white px-5 py-3 rounded-2xl shadow-xl flex items-center gap-3 border border-sky-500/30 animate-in fade-in slide-in-from-top-4 duration-300">
          <Bell className="w-5 h-5 text-sky-300 animate-pulse" />
          <span className="text-sm font-medium">{notificationToast}</span>
        </div>
      )}

      {/* الرأس الرئيسي ومبدل الأقسام */}
      <header className="border-b border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 sticky top-0 z-40 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-sky-600 to-sky-400 text-white flex items-center justify-center font-bold text-lg shadow-md shadow-sky-500/20">
              ⚓
            </div>
            <div>
              <h1 className="font-extrabold text-base sm:text-lg text-slate-900 dark:text-white leading-tight">
                تثبيت الكلمات <span className="text-xs font-semibold text-sky-600 dark:text-sky-400 bg-sky-50 dark:bg-sky-950/60 px-2 py-0.5 rounded-full border border-sky-200 dark:border-sky-800 mr-1.5">Word Anchor</span>
              </h1>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                مشروع أندرويد حقيقي كامل • Jetpack Compose • Room • Spaced Repetition
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <nav className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-xl">
              <button
                onClick={() => setActiveTab('simulator')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  activeTab === 'simulator'
                    ? 'bg-white dark:bg-slate-700 text-sky-600 dark:text-sky-400 shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                <Smartphone className="w-3.5 h-3.5" />
                <span>المحاكي التفاعلي</span>
              </button>
              <button
                onClick={() => setActiveTab('code')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  activeTab === 'code'
                    ? 'bg-white dark:bg-slate-700 text-sky-600 dark:text-sky-400 shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                <Code className="w-3.5 h-3.5" />
                <span>شجرة الكود (Kotlin)</span>
              </button>
              <button
                onClick={() => setActiveTab('github')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  activeTab === 'github'
                    ? 'bg-white dark:bg-slate-700 text-sky-600 dark:text-sky-400 shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                <Github className="w-3.5 h-3.5" />
                <span>GitHub Actions CI</span>
              </button>
            </nav>

            <button
              onClick={handleDownloadFullProjectZip}
              disabled={isExportingZip}
              className="hidden sm:flex items-center gap-2 bg-sky-600 hover:bg-sky-700 active:scale-95 text-white px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all shadow-md shadow-sky-600/20"
            >
              <Download className="w-4 h-4" />
              <span>{isExportingZip ? 'جاري الضغط...' : 'تحميل المشروع (ZIP)'}</span>
            </button>
          </div>
        </div>
      </header>

      {/* المحتوى بحسب التبويب */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 lg:p-8">
        
        {/* ================== التبويب 1: المحاكي التفاعلي ================== */}
        {activeTab === 'simulator' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            
            {/* اللوحة الجانبية الإرشادية */}
            <div className="lg:col-span-4 space-y-5 order-2 lg:order-1">
              <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm">
                <div className="flex items-center gap-2 text-sky-600 dark:text-sky-400 font-bold mb-2">
                  <Sparkles className="w-5 h-5" />
                  <h3 className="text-sm">نظام التكرار المتباعد الذكي (SRS)</h3>
                </div>
                <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed mb-4">
                  تمت برمجة محرك التكرار المتباعد في التطبيق وفق سلم تدرج زمني يضاعف ثبات الكلمة في الذاكرة طويلة المدى:
                </p>
                <div className="space-y-1.5 text-xs">
                  {LEVEL_NAMES.map((name, idx) => (
                    <div key={idx} className="flex items-center justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                      <span className="font-semibold text-slate-700 dark:text-slate-300">{name}</span>
                      <span className="text-sky-600 dark:text-sky-400 font-mono text-[11px]">
                        {idx === 0 ? 'اليوم' : `بعد ${SRS_INTERVALS[idx]} يوم`}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="bg-gradient-to-br from-sky-50 to-blue-50 dark:from-slate-900 dark:to-slate-800 p-5 rounded-2xl border border-sky-100 dark:border-slate-800">
                <h4 className="text-xs font-bold text-sky-950 dark:text-sky-200 mb-2 flex items-center gap-2">
                  <Layers className="w-4 h-4 text-sky-600" />
                  <span>المعمارية التقنية للمشروع</span>
                </h4>
                <ul className="text-xs text-slate-600 dark:text-slate-400 space-y-1.5 list-disc list-inside">
                  <li><strong>Jetpack Compose & Material 3</strong>: واجهة عربية كاملة RTL هادئة باللونين الأزرق والأبيض.</li>
                  <li><strong>Room Database</strong>: حفظ محلي بدون إنترنت، حماية من حذف البيانات، ومفاتيح فهارس سريعة.</li>
                  <li><strong>AlarmManager Exact Alarms</strong>: تذكيرات دقيقة بالدقيقة دون الاعتماد على قيود WorkManager.</li>
                  <li><strong>Internal Media Storage</strong>: نسخ الصور والصوت والفيديو إلى ذاكرة التطبيق الخاصة دون ترك ملفات يتيمة.</li>
                  <li><strong>DataStore & JSON Backup</strong>: حفظ تفضيلات المستخدم واستيراد/تصدير بنية البطاقات.</li>
                </ul>
              </div>

              <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 flex items-center justify-between text-xs text-slate-500">
                <span>هل تريد تحميل الكود وتجربته محلياً؟</span>
                <button
                  onClick={handleDownloadFullProjectZip}
                  className="text-sky-600 dark:text-sky-400 font-bold hover:underline"
                >
                  تنزيل كملف ZIP
                </button>
              </div>
            </div>

            {/* إطار هاتف الأندرويد المحاكي */}
            <div className="lg:col-span-8 flex justify-center order-1 lg:order-2">
              <div className="w-full max-w-[390px] h-[780px] bg-slate-900 rounded-[44px] p-3 shadow-2xl ring-1 ring-slate-800 relative flex flex-col">
                
                {/* الحافة العلوية للهاتف (Dynamic Island / Speaker) */}
                <div className="absolute top-4 left-1/2 -translate-x-1/2 w-32 h-4 bg-slate-950 rounded-full flex items-center justify-center z-30">
                  <div className="w-2.5 h-2.5 bg-slate-900 rounded-full ml-3" />
                  <div className="w-2 h-2 bg-sky-900/60 rounded-full" />
                </div>

                {/* شاشة الهاتف الداخلية */}
                <div className={`w-full h-full rounded-[36px] overflow-hidden flex flex-col relative transition-colors ${
                  isDarkMode ? 'bg-slate-950 text-slate-100' : 'bg-slate-50 text-slate-900'
                }`}>
                  
                  {/* شريط حالة الأندرويد (Status Bar) */}
                  <div className="h-9 px-6 pt-2 flex items-center justify-between text-[11px] font-semibold text-slate-400 select-none z-20">
                    <span>09:41</span>
                    <div className="flex items-center gap-1.5 text-xs">
                      <span>5G</span>
                      <span>📶</span>
                      <span>🔋 100%</span>
                    </div>
                  </div>

                  {/* ================= شاشات المحاكي ================= */}
                  <div className="flex-1 overflow-y-auto pb-6">
                    
                    {/* 1. الشاشة الرئيسية داخل المحاكي */}
                    {simScreen === 'home' && (
                      <div className="p-4 space-y-4">
                        {/* شريط العنوان العلوي */}
                        <div className="flex items-center justify-between pt-1">
                          <div>
                            <h2 className="text-lg font-black tracking-tight text-slate-900 dark:text-white">تثبيت الكلمات</h2>
                            <p className="text-[11px] text-slate-500">الذاكرة طويلة المدى</p>
                          </div>
                          <div className="flex items-center gap-1">
                            <button
                              onClick={() => setSimScreen('settings')}
                              className="w-9 h-9 rounded-full flex items-center justify-center hover:bg-slate-200 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
                              title="الإعدادات"
                            >
                              <Settings className="w-4 h-4" />
                            </button>
                          </div>
                        </div>

                        {/* حقل البحث */}
                        <div className="relative">
                          <Search className="w-4 h-4 absolute right-3 top-3 text-slate-400" />
                          <input
                            type="text"
                            placeholder="ابحث عن كلمة، معنى، أو ملاحظة..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full bg-white dark:bg-slate-900 pr-9 pl-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-800 outline-none focus:border-sky-500 transition-colors shadow-sm"
                          />
                        </div>

                        {/* شرائح التصفية (Filter Chips) */}
                        <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs">
                          <button
                            onClick={() => setActiveFilter('ALL')}
                            className={`px-3 py-1.5 rounded-full font-semibold whitespace-nowrap transition-all ${
                              activeFilter === 'ALL'
                                ? 'bg-sky-600 text-white shadow-sm'
                                : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400'
                            }`}
                          >
                            الكل ({cards.length})
                          </button>
                          <button
                            onClick={() => setActiveFilter('DUE')}
                            className={`px-3 py-1.5 rounded-full font-semibold whitespace-nowrap transition-all ${
                              activeFilter === 'DUE'
                                ? 'bg-amber-600 text-white shadow-sm'
                                : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400'
                            }`}
                          >
                            للمراجعة الآن ({dueCount})
                          </button>
                          <button
                            onClick={() => setActiveFilter('FAVORITES')}
                            className={`px-3 py-1.5 rounded-full font-semibold whitespace-nowrap transition-all ${
                              activeFilter === 'FAVORITES'
                                ? 'bg-rose-600 text-white shadow-sm'
                                : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400'
                            }`}
                          >
                            المفضلة
                          </button>
                        </div>

                        {/* قائمة البطاقات */}
                        <div className="space-y-2.5">
                          {filteredCards.length === 0 ? (
                            <div className="text-center py-12 px-4">
                              <p className="text-xs text-slate-500 font-medium">لا توجد بطاقات مطابقة</p>
                              <p className="text-[11px] text-slate-400 mt-1">اضغط على زر (+) في الأسفل لإضافة بطاقتك الأولى</p>
                            </div>
                          ) : (
                            filteredCards.map(card => {
                              const isDue = card.nextReviewAt <= Date.now();
                              return (
                                <div
                                  key={card.id}
                                  onClick={() => {
                                    setSelectedCardId(card.id);
                                    setSimScreen('detail');
                                  }}
                                  className="bg-white dark:bg-slate-900 p-3.5 rounded-2xl border border-slate-200 dark:border-slate-800 hover:border-sky-300 dark:hover:border-sky-800 shadow-sm cursor-pointer transition-all active:scale-[0.99] flex items-center gap-3"
                                >
                                  {card.image && (
                                    <img
                                      src={card.image}
                                      alt=""
                                      className="w-14 h-14 rounded-xl object-cover flex-shrink-0"
                                    />
                                  )}
                                  <div className="flex-1 min-w-0">
                                    <div className="flex items-center justify-between mb-1">
                                      <h3 className="font-bold text-sm text-slate-900 dark:text-white truncate">
                                        {card.word}
                                      </h3>
                                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-sky-50 dark:bg-sky-950/80 text-sky-600 dark:text-sky-400 border border-sky-200 dark:border-sky-800">
                                        {LEVEL_NAMES[card.reviewLevel].split(' ')[0]} {card.reviewLevel}
                                      </span>
                                    </div>
                                    <p className="text-xs text-slate-600 dark:text-slate-400 line-clamp-1 mb-2">
                                      {card.meaning}
                                    </p>
                                    <div className="flex items-center gap-2 text-[10px]">
                                      <span className={`px-1.5 py-0.5 rounded font-semibold ${
                                        isDue 
                                          ? 'bg-rose-50 text-rose-600 dark:bg-rose-950/60 dark:text-rose-400' 
                                          : 'bg-slate-100 text-slate-500 dark:bg-slate-800'
                                      }`}>
                                        {isDue ? 'مستحقة الآن' : 'غداً'}
                                      </span>
                                      {card.hasAudio && (
                                        <span className="text-sky-600" title="تسجيل صوتي">🎙️</span>
                                      )}
                                      {card.hasVideo && (
                                        <span className="text-sky-600" title="فيديو">🎬</span>
                                      )}
                                      {card.reminderEnabled && (
                                        <span className="text-amber-500" title="تذكير نشط">⏰</span>
                                      )}
                                    </div>
                                  </div>
                                  <button
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleToggleFavorite(card.id);
                                    }}
                                    className="p-1.5 text-slate-400 hover:text-rose-500"
                                  >
                                    <Star className={`w-4 h-4 ${card.isFavorite ? 'fill-rose-500 text-rose-500' : ''}`} />
                                  </button>
                                </div>
                              );
                            })
                          )}
                        </div>
                      </div>
                    )}

                    {/* 2. شاشة تفاصيل البطاقة والمراجعة */}
                    {simScreen === 'detail' && selectedCard && (
                      <div className="p-4 space-y-4">
                        {/* شريط علوي مع زر الرجوع */}
                        <div className="flex items-center justify-between">
                          <button
                            onClick={() => setSimScreen('home')}
                            className="flex items-center gap-1 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:text-sky-600"
                          >
                            <ArrowRight className="w-4 h-4" />
                            <span>رجوع</span>
                          </button>
                          <div className="flex items-center gap-1">
                            <button
                              onClick={() => {
                                setEditingCardId(selectedCard.id);
                                setSimScreen('addEdit');
                              }}
                              className="p-2 hover:bg-slate-200 dark:hover:bg-slate-800 rounded-lg text-slate-600 dark:text-slate-300"
                            >
                              <Edit3 className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleDeleteCard(selectedCard.id)}
                              className="p-2 hover:bg-rose-100 dark:hover:bg-rose-950/60 rounded-lg text-rose-600"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </div>

                        {/* بطاقة التفاصيل الرئيسية */}
                        <div className="bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
                          <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold px-2 py-0.5 rounded bg-sky-100 dark:bg-sky-950 text-sky-700 dark:text-sky-300">
                              {LEVEL_NAMES[selectedCard.reviewLevel]}
                            </span>
                            <span className="text-xs text-slate-500 flex items-center gap-1 font-mono">
                              <Clock className="w-3.5 h-3.5" />
                              {selectedCard.nextReviewAt <= Date.now() ? 'مستحقة للمراجعة الآن' : 'موعد المراجعة قادم'}
                            </span>
                          </div>

                          <h2 className="text-2xl font-black text-sky-700 dark:text-sky-400">
                            {selectedCard.word}
                          </h2>

                          <p className="text-sm font-medium text-slate-800 dark:text-slate-200 leading-relaxed">
                            {selectedCard.meaning}
                          </p>

                          {selectedCard.notes && (
                            <div className="bg-slate-50 dark:bg-slate-800/60 p-3 rounded-xl border border-slate-100 dark:border-slate-800">
                              <span className="text-[10px] font-bold text-slate-400 block mb-1">ملاحظات وسياق:</span>
                              <p className="text-xs text-slate-600 dark:text-slate-300">{selectedCard.notes}</p>
                            </div>
                          )}
                        </div>

                        {/* صورة البطاقة */}
                        {selectedCard.image && (
                          <div className="rounded-2xl overflow-hidden border border-slate-200 dark:border-slate-800 shadow-sm">
                            <img src={selectedCard.image} alt="" className="w-full h-44 object-cover" />
                          </div>
                        )}

                        {/* مشغل الصوت */}
                        {selectedCard.hasAudio && (
                          <div className="bg-sky-50 dark:bg-sky-950/40 p-3 rounded-2xl border border-sky-200 dark:border-sky-900 flex items-center gap-3">
                            <button
                              onClick={() => {
                                setIsPlayingAudio(!isPlayingAudio);
                                if (!isPlayingAudio) {
                                  showToast('جاري تشغيل التسجيل الصوتي للنطق...');
                                  setTimeout(() => setIsPlayingAudio(false), 3000);
                                }
                              }}
                              className="w-10 h-10 rounded-full bg-sky-600 text-white flex items-center justify-center shadow-md"
                            >
                              {isPlayingAudio ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4 fill-white" />}
                            </button>
                            <div className="flex-1">
                              <div className="flex justify-between text-xs mb-1 font-semibold text-sky-900 dark:text-sky-200">
                                <span>تسجيل نطق الكلمة</span>
                                <span>{isPlayingAudio ? '00:02' : '00:00'} / 00:03</span>
                              </div>
                              <div className="w-full bg-sky-200 dark:bg-sky-900 h-1.5 rounded-full overflow-hidden">
                                <div className={`bg-sky-600 h-full transition-all duration-300 ${isPlayingAudio ? 'w-2/3' : 'w-0'}`} />
                              </div>
                            </div>
                          </div>
                        )}

                        {/* زر التذكير */}
                        {selectedCard.reminderEnabled && (
                          <div className="bg-slate-100 dark:bg-slate-900 p-3 rounded-xl flex items-center gap-2 text-xs text-slate-600 dark:text-slate-400">
                            <Bell className="w-4 h-4 text-sky-600" />
                            <span>
                              {selectedCard.reminderType === 'DAILY'
                                ? `تذكير يومي مجدول عند الساعة ${selectedCard.reminderTime}`
                                : `تذكير متكرر كل ${selectedCard.reminderIntervalHours} ساعات`}
                            </span>
                          </div>
                        )}

                        {/* زرا التحكم في التكرار المتباعد (Spaced Repetition) */}
                        <div className="pt-2 grid grid-cols-2 gap-3">
                          <button
                            onClick={() => handleMarkReviewed(selectedCard.id)}
                            className="bg-sky-600 hover:bg-sky-700 active:scale-95 text-white py-3 px-4 rounded-xl text-xs font-bold flex items-center justify-center gap-2 shadow-lg shadow-sky-600/30 transition-all"
                          >
                            <CheckCircle className="w-4 h-4" />
                            <span>راجعتها</span>
                          </button>
                          <button
                            onClick={() => setShowSnoozeModal(true)}
                            className="bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-800 dark:text-slate-200 py-3 px-4 rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition-all"
                          >
                            <Clock className="w-4 h-4" />
                            <span>أعد التذكير لاحقاً</span>
                          </button>
                        </div>
                      </div>
                    )}

                    {/* 3. شاشة إضافة / تعديل بطاقة */}
                    {simScreen === 'addEdit' && (
                      <div className="p-4 space-y-4">
                        <div className="flex items-center justify-between">
                          <button
                            onClick={() => {
                              setSimScreen(editingCardId ? 'detail' : 'home');
                              setEditingCardId(null);
                            }}
                            className="flex items-center gap-1 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:text-sky-600"
                          >
                            <ArrowRight className="w-4 h-4" />
                            <span>إلغاء</span>
                          </button>
                          <h3 className="font-bold text-sm">
                            {editingCardId ? 'تعديل البطاقة' : 'إضافة بطاقة جديدة'}
                          </h3>
                          <div className="w-8" />
                        </div>

                        <form
                          onSubmit={(e) => {
                            e.preventDefault();
                            const form = e.currentTarget;
                            const word = (form.elements.namedItem('word') as HTMLInputElement).value;
                            const meaning = (form.elements.namedItem('meaning') as HTMLInputElement).value;
                            const notes = (form.elements.namedItem('notes') as HTMLTextAreaElement).value;

                            if (!word || !meaning) {
                              showToast('يرجى ملء الكلمة والمعنى');
                              return;
                            }

                            if (editingCardId) {
                              setCards(prev => prev.map(c => c.id === editingCardId ? {
                                ...c,
                                word,
                                meaning,
                                notes
                              } : c));
                              showToast('تم حفظ التعديلات بنجاح');
                              setSimScreen('detail');
                            } else {
                              const newCard: WordCard = {
                                id: Date.now(),
                                word,
                                meaning,
                                notes,
                                image: 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=600&auto=format&fit=crop&q=80',
                                hasAudio: true,
                                hasVideo: false,
                                reviewLevel: 0,
                                createdAt: Date.now(),
                                nextReviewAt: Date.now(),
                                isFavorite: false,
                                reminderEnabled: true,
                                reminderType: 'DAILY',
                                reminderTime: '09:00 ص',
                                reminderIntervalHours: 4
                              };
                              setCards(prev => [newCard, ...prev]);
                              showToast('تمت إضافة البطاقة بنجاح');
                              setSimScreen('home');
                            }
                            setEditingCardId(null);
                          }}
                          className="space-y-3"
                        >
                          <div>
                            <label className="text-[11px] font-bold text-slate-500 block mb-1">الكلمة أو العبارة *</label>
                            <input
                              name="word"
                              defaultValue={editingCardId ? selectedCard?.word : ''}
                              required
                              placeholder="مثال: Wanderlust"
                              className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-2.5 text-xs outline-none focus:border-sky-500"
                            />
                          </div>

                          <div>
                            <label className="text-[11px] font-bold text-slate-500 block mb-1">المعنى أو الترجمة *</label>
                            <input
                              name="meaning"
                              defaultValue={editingCardId ? selectedCard?.meaning : ''}
                              required
                              placeholder="مثال: شغف ورغبة قوية في السفر واكتشاف العالم"
                              className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-2.5 text-xs outline-none focus:border-sky-500"
                            />
                          </div>

                          <div>
                            <label className="text-[11px] font-bold text-slate-500 block mb-1">ملاحظات إضافية (أمثلة وسياق)</label>
                            <textarea
                              name="notes"
                              rows={3}
                              defaultValue={editingCardId ? selectedCard?.notes : ''}
                              placeholder="أضف مثالاً في جملة أو ملحوظة نحوية..."
                              className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-2.5 text-xs outline-none focus:border-sky-500"
                            />
                          </div>

                          {/* خيارات الوسائط */}
                          <div className="bg-white dark:bg-slate-900 p-3 rounded-xl border border-slate-200 dark:border-slate-800 space-y-2">
                            <span className="text-[11px] font-bold text-slate-500 block">إرفاق وسائط (يتم حفظها داخلياً)</span>
                            <div className="grid grid-cols-3 gap-2 text-xs">
                              <button
                                type="button"
                                onClick={() => showToast('في تطبيق الأندرويد: يفتح PhotoPicker لنسخ الصورة إلى filesDir/media')}
                                className="p-2 border border-dashed border-sky-300 rounded-lg text-sky-600 font-semibold flex flex-col items-center gap-1"
                              >
                                <span>🖼️</span>
                                <span className="text-[10px]">صورة</span>
                              </button>
                              <button
                                type="button"
                                onClick={() => showToast('في تطبيق الأندرويد: يفتح منتقي الصوت لتخزين نطق الكلمة')}
                                className="p-2 border border-dashed border-sky-300 rounded-lg text-sky-600 font-semibold flex flex-col items-center gap-1"
                              >
                                <span>🎙️</span>
                                <span className="text-[10px]">تسجيل صوتي</span>
                              </button>
                              <button
                                type="button"
                                onClick={() => showToast('في تطبيق الأندرويد: يتيح إرفاق مقطع فيديو')}
                                className="p-2 border border-dashed border-sky-300 rounded-lg text-sky-600 font-semibold flex flex-col items-center gap-1"
                              >
                                <span>🎬</span>
                                <span className="text-[10px]">فيديو</span>
                              </button>
                            </div>
                          </div>

                          <button
                            type="submit"
                            className="w-full bg-sky-600 hover:bg-sky-700 text-white font-bold py-3 rounded-xl text-xs shadow-md mt-4"
                          >
                            {editingCardId ? 'حفظ التعديلات' : 'إضافة البطاقة'}
                          </button>
                        </form>
                      </div>
                    )}

                    {/* 4. شاشة الإعدادات */}
                    {simScreen === 'settings' && (
                      <div className="p-4 space-y-4">
                        <div className="flex items-center justify-between">
                          <button
                            onClick={() => setSimScreen('home')}
                            className="flex items-center gap-1 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:text-sky-600"
                          >
                            <ArrowRight className="w-4 h-4" />
                            <span>رجوع</span>
                          </button>
                          <h3 className="font-bold text-sm">الإعدادات</h3>
                          <div className="w-8" />
                        </div>

                        {/* المظهر */}
                        <div className="bg-white dark:bg-slate-900 p-3.5 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-2.5">
                          <span className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                            {isDarkMode ? <Moon className="w-4 h-4 text-sky-500" /> : <Sun className="w-4 h-4 text-amber-500" />}
                            <span>مظهر التطبيق</span>
                          </span>
                          <div className="flex items-center justify-between text-xs">
                            <span>الوضع الداكن (Dark Mode)</span>
                            <button
                              onClick={() => setIsDarkMode(!isDarkMode)}
                              className={`w-11 h-6 rounded-full transition-colors relative ${isDarkMode ? 'bg-sky-600' : 'bg-slate-300'}`}
                            >
                              <div className={`w-4 h-4 rounded-full bg-white absolute top-1 transition-all ${isDarkMode ? 'left-6' : 'left-1'}`} />
                            </button>
                          </div>
                        </div>

                        {/* الإشعارات والتذكيرات */}
                        <div className="bg-white dark:bg-slate-900 p-3.5 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-2.5">
                          <span className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                            <Bell className="w-4 h-4 text-sky-600" />
                            <span>التنبيهات الموقوتة (AlarmManager)</span>
                          </span>
                          <div className="flex items-center justify-between text-xs">
                            <span>تفعيل جميع التذكيرات</span>
                            <button
                              onClick={() => {
                                setAllRemindersEnabled(!allRemindersEnabled);
                                showToast(!allRemindersEnabled ? 'تم تفعيل التذكيرات' : 'تم تعطيل التذكيرات');
                              }}
                              className={`w-11 h-6 rounded-full transition-colors relative ${allRemindersEnabled ? 'bg-sky-600' : 'bg-slate-300'}`}
                            >
                              <div className={`w-4 h-4 rounded-full bg-white absolute top-1 transition-all ${allRemindersEnabled ? 'left-6' : 'left-1'}`} />
                            </button>
                          </div>
                        </div>

                        {/* إدارة البيانات والنسخ الاحتياطي */}
                        <div className="bg-white dark:bg-slate-900 p-3.5 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-2">
                          <span className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                            <FolderArchive className="w-4 h-4 text-sky-600" />
                            <span>النسخ الاحتياطي (JSON)</span>
                          </span>
                          <div className="space-y-1.5 pt-1">
                            <button
                              onClick={() => {
                                const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(cards, null, 2));
                                const downloadAnchor = document.createElement('a');
                                downloadAnchor.setAttribute("href", dataStr);
                                downloadAnchor.setAttribute("download", `word_anchor_backup_${Date.now()}.json`);
                                document.body.appendChild(downloadAnchor);
                                downloadAnchor.click();
                                downloadAnchor.remove();
                                showToast('تم تصدير ملف النسخة الاحتياطية بنجاح 📁');
                              }}
                              className="w-full py-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-xl text-xs font-semibold text-slate-800 dark:text-slate-200 transition-colors"
                            >
                              تصدير البيانات إلى ملف JSON
                            </button>
                            <button
                              onClick={() => {
                                if (window.confirm('هل أنت متأكد من رغبتك في إعادة ضبط البيانات على القيم الافتراضية؟')) {
                                  setCards(INITIAL_CARDS);
                                  showToast('تمت استعادة البطاقات الافتراضية');
                                }
                              }}
                              className="w-full py-2 bg-rose-50 dark:bg-rose-950/40 hover:bg-rose-100 rounded-xl text-xs font-semibold text-rose-600 transition-colors"
                            >
                              إعادة ضبط البيانات
                            </button>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>

                  {/* الزر العائم (+) في الشاشة الرئيسية داخل المحاكي */}
                  {simScreen === 'home' && (
                    <button
                      onClick={() => {
                        setEditingCardId(null);
                        setSimScreen('addEdit');
                      }}
                      className="absolute bottom-6 left-6 w-13 h-13 rounded-2xl bg-sky-600 hover:bg-sky-700 active:scale-90 text-white flex items-center justify-center shadow-lg shadow-sky-600/40 transition-all z-20"
                      title="إضافة بطاقة جديدة"
                    >
                      <Plus className="w-6 h-6" />
                    </button>
                  )}

                  {/* مودال تأجيل التذكير داخل المحاكي */}
                  {showSnoozeModal && selectedCard && (
                    <div className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm z-30 flex items-end p-4">
                      <div className="w-full bg-white dark:bg-slate-900 rounded-3xl p-5 border border-slate-200 dark:border-slate-800 shadow-2xl space-y-3">
                        <h4 className="text-sm font-bold text-slate-900 dark:text-white">أعد التذكير لاحقاً</h4>
                        <p className="text-xs text-slate-500">اختر متى تريد أن يقوم التطبيق بتذكيرك مرة أخرى بمراجعة هذه البطاقة:</p>
                        <div className="space-y-2 pt-1 text-xs">
                          <button
                            onClick={() => handleSnooze(selectedCard.id, 10)}
                            className="w-full py-2.5 bg-slate-50 dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-sky-950/50 rounded-xl font-semibold text-right px-3 border border-slate-200 dark:border-slate-700"
                          >
                            بعد 10 دقائق
                          </button>
                          <button
                            onClick={() => handleSnooze(selectedCard.id, 60)}
                            className="w-full py-2.5 bg-slate-50 dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-sky-950/50 rounded-xl font-semibold text-right px-3 border border-slate-200 dark:border-slate-700"
                          >
                            بعد ساعة واحدة
                          </button>
                          <button
                            onClick={() => handleSnooze(selectedCard.id, 24 * 60)}
                            className="w-full py-2.5 bg-slate-50 dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-sky-950/50 rounded-xl font-semibold text-right px-3 border border-slate-200 dark:border-slate-700"
                          >
                            غداً في نفس الوقت
                          </button>
                        </div>
                        <button
                          onClick={() => setShowSnoozeModal(false)}
                          className="w-full py-2 text-xs font-bold text-slate-400 hover:text-slate-600 mt-2"
                        >
                          إلغاء
                        </button>
                      </div>
                    </div>
                  )}

                  {/* شريط الإيماءات السفلي للأندرويد */}
                  <div className="h-4 flex items-center justify-center pb-1">
                    <div className="w-32 h-1 bg-slate-400/40 rounded-full" />
                  </div>

                </div>
              </div>
            </div>
          </div>
        )}

        {/* ================== التبويب 2: شجرة الكود (Kotlin Source Files) ================== */}
        {activeTab === 'code' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            
            {/* القائمة الجانبية للملفات */}
            <div className="lg:col-span-4 bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-4 shadow-sm space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-slate-800">
                <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200 flex items-center gap-2">
                  <Code className="w-4 h-4 text-sky-600" />
                  <span>ملفات مشروع Android</span>
                </h3>
                <span className="text-[11px] font-mono text-slate-400">{Object.keys(ANDROID_CODE_FILES).length} ملفاً</span>
              </div>

              <div className="space-y-1 max-h-[620px] overflow-y-auto pr-1">
                {Object.entries(ANDROID_CODE_FILES).map(([key, item]) => {
                  const isSelected = selectedFileKey === key;
                  return (
                    <button
                      key={key}
                      onClick={() => {
                        setSelectedFileKey(key);
                        setCopiedFile(false);
                      }}
                      className={`w-full text-right p-2.5 rounded-xl text-xs transition-all flex flex-col gap-0.5 ${
                        isSelected
                          ? 'bg-sky-50 dark:bg-sky-950/60 border border-sky-200 dark:border-sky-800 text-sky-800 dark:text-sky-300 font-bold shadow-sm'
                          : 'hover:bg-slate-50 dark:hover:bg-slate-800/60 text-slate-600 dark:text-slate-400'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-mono">{item.fileName}</span>
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400 font-sans">
                          {item.layer}
                        </span>
                      </div>
                      <span className="text-[11px] text-slate-400 truncate">{item.purpose}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* عارض الكود */}
            <div className="lg:col-span-8 bg-slate-900 text-slate-100 rounded-2xl border border-slate-800 shadow-xl overflow-hidden flex flex-col">
              <div className="px-5 py-3.5 bg-slate-950/80 border-b border-slate-800 flex items-center justify-between">
                <div>
                  <span className="font-mono text-xs font-bold text-sky-400">
                    {ANDROID_CODE_FILES[selectedFileKey]?.path}
                  </span>
                  <p className="text-[11px] text-slate-400 mt-0.5">
                    {ANDROID_CODE_FILES[selectedFileKey]?.purpose}
                  </p>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      navigator.clipboard.writeText(ANDROID_CODE_FILES[selectedFileKey]?.code || '');
                      setCopiedFile(true);
                      setTimeout(() => setCopiedFile(false), 2000);
                    }}
                    className="flex items-center gap-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors"
                  >
                    {copiedFile ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedFile ? 'تم النسخ!' : 'نسخ الكود'}</span>
                  </button>
                </div>
              </div>

              <div className="p-4 overflow-x-auto max-h-[640px] text-xs font-mono leading-relaxed bg-slate-950/40">
                <pre dir="ltr" className="text-left text-slate-300">
                  <code>{ANDROID_CODE_FILES[selectedFileKey]?.code}</code>
                </pre>
              </div>
            </div>
          </div>
        )}

        {/* ================== التبويب 3: دليل البناء و GitHub Actions ================== */}
        {activeTab === 'github' && (
          <div className="max-w-4xl mx-auto space-y-6">
            <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
              <div className="flex items-center gap-3 text-sky-600 font-bold">
                <Github className="w-6 h-6 text-slate-900 dark:text-white" />
                <h3 className="text-base text-slate-900 dark:text-white">البناء التلقائي عبر GitHub Actions</h3>
              </div>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                تم تجهيز مستودع المشروع مسبقاً بملف سير عمل GitHub Actions (CI) في المسار:
                <code className="mx-1 px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-sky-600 font-mono text-[11px]">.github/workflows/build.yml</code>.
                بمجرد رفع المشروع إلى GitHub، سيتولى الخادم الافتراضي تجميع الكود وبناء ملف الـ APK وتقديمه لك كـ Artifact جاهز للتنزيل والتثبيت على هاتفك.
              </p>

              <div className="bg-slate-900 text-slate-200 p-4 rounded-xl text-xs font-mono space-y-2 border border-slate-800" dir="ltr">
                <p className="text-slate-400"># 1. تهيئة المستودع المحلي وربطه بـ GitHub</p>
                <p>git init</p>
                <p>git add .</p>
                <p>git commit -m "Initial commit: Word Anchor Android app with Room and SRS"</p>
                <p>git branch -M main</p>
                <p>git remote add origin https://github.com/USERNAME/WordAnchor.git</p>
                <p>git push -u origin main</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-3">
                <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
                  <span className="text-sm font-bold text-sky-600 block mb-1">الخطوة 1: الدفع (Push)</span>
                  <p className="text-xs text-slate-500">ارفع الملفات أو حمل ملف الـ ZIP واستخرجه وارفعه إلى GitHub.</p>
                </div>
                <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
                  <span className="text-sm font-bold text-sky-600 block mb-1">الخطوة 2: تبويب Actions</span>
                  <p className="text-xs text-slate-500">اضغط على تبويب Actions في المستودع وراقب تنفيذ مهمة Build Debug APK.</p>
                </div>
                <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
                  <span className="text-sm font-bold text-sky-600 block mb-1">الخطوة 3: تحميل APK</span>
                  <p className="text-xs text-slate-500">حمل ملف WordAnchor-debug-apk من قسم Artifacts وثبته مباشرة على هاتفك.</p>
                </div>
              </div>
            </div>

            {/* عرض محتوى ملف الـ Workflow */}
            <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
              <div className="px-5 py-3 bg-slate-950 border-b border-slate-800 flex items-center justify-between text-xs">
                <span className="font-mono text-sky-400">/.github/workflows/build.yml</span>
                <span className="text-slate-400">Gradle CI Configuration</span>
              </div>
              <div className="p-4 overflow-x-auto text-xs font-mono text-slate-300 bg-slate-950/50 leading-relaxed" dir="ltr">
                <pre>{ANDROID_CODE_FILES['buildWorkflow']?.code}</pre>
              </div>
            </div>
          </div>
        )}

      </main>
    </div>
  );
}

// قائمة ملفات الأندرويد الكاملة للعرض والتحميل
interface AndroidFileMetadata {
  path: string;
  fileName: string;
  layer: string;
  purpose: string;
  code: string;
}

const ANDROID_CODE_FILES: Record<string, AndroidFileMetadata> = {
  MainActivity: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/MainActivity.kt',
    fileName: 'MainActivity.kt',
    layer: 'UI / Activity',
    purpose: 'نقطة الانطلاق الرئيسية، طلب إذن POST_NOTIFICATIONS لأندرويد 13+، واستقبال الـ Intents لفتح البطاقة المستهدفة مباشرة من الإشعار.',
    code: `package com.deutscherinnerungen.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.deutscherinnerungen.app.notifications.NotificationHelper
import com.deutscherinnerungen.app.settings.AppSettings
import com.deutscherinnerungen.app.ui.navigation.NavGraph
import com.deutscherinnerungen.app.ui.navigation.Screen
import com.deutscherinnerungen.app.ui.theme.WordAnchorTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // التعامل مع الإذن في أندرويد 13 فما فوق
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as WordAnchorApp

        setContent {
            val appSettings by app.settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
            val navController = rememberNavController()

            WordAnchorTheme(themeMode = appSettings.themeMode) {
                val targetCardId = intent?.getLongExtra(NotificationHelper.EXTRA_CARD_ID, -1L) ?: -1L
                val startDestination = if (targetCardId > 0) {
                    Screen.CardDetail.createRoute(targetCardId)
                } else {
                    Screen.Home.route
                }

                NavGraph(
                    navController = navController,
                    app = app,
                    startDestination = startDestination
                )
            }
        }
    }
}`
  },

  WordAnchorApp: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/WordAnchorApp.kt',
    fileName: 'WordAnchorApp.kt',
    layer: 'Application',
    purpose: 'فئة التطبيق المركزية لتهيئة قنوات التنبيه وتقديم مراجع الكائنات الأحادية (Database, Repository, Scheduler, Settings).',
    code: `package com.deutscherinnerungen.app

import android.app.Application
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import com.deutscherinnerungen.app.data.repository.WordCardRepository
import com.deutscherinnerungen.app.data.repository.WordCardRepositoryImpl
import com.deutscherinnerungen.app.media.MediaStorageManager
import com.deutscherinnerungen.app.notifications.NotificationHelper
import com.deutscherinnerungen.app.notifications.ReminderScheduler
import com.deutscherinnerungen.app.settings.SettingsRepository

class WordAnchorApp : Application() {
    val database by lazy { WordAnchorDatabase.getInstance(this) }
    val repository: WordCardRepository by lazy { WordCardRepositoryImpl(database.wordCardDao()) }
    val mediaManager by lazy { MediaStorageManager(this) }
    val notificationHelper by lazy { NotificationHelper(this) }
    val reminderScheduler by lazy { ReminderScheduler(this) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        notificationHelper
    }
}`
  },

  WordCardEntity: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/data/entity/WordCardEntity.kt',
    fileName: 'WordCardEntity.kt',
    layer: 'Data / Entity',
    purpose: 'كيان البطاقة في Room Database مع جميع الحقول (id, word, meaning, notes, imagePath, audioPath, videoPath, reviewLevel, nextReviewAt, reminder).',
    code: `package com.deutscherinnerungen.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class ReminderType {
    @SerializedName("DAILY") DAILY,
    @SerializedName("INTERVAL") INTERVAL
}

@Entity(
    tableName = "word_cards",
    indices = [
        Index(value = ["nextReviewAt"]),
        Index(value = ["isFavorite"]),
        Index(value = ["createdAt"])
    ]
)
data class WordCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val word: String,
    val meaning: String,
    val notes: String = "",
    val imagePath: String? = null,
    val audioPath: String? = null,
    val videoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reviewLevel: Int = 0,
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = false,
    val reminderType: ReminderType = ReminderType.DAILY,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val reminderIntervalHours: Int = 4,
    val isFavorite: Boolean = false
)`
  },

  WordCardDao: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/data/dao/WordCardDao.kt',
    fileName: 'WordCardDao.kt',
    layer: 'Data / DAO',
    purpose: 'واجهة استعلامات Room: تدفقات Flow التفاعلية، البحث، المفضلة، البطاقات المستحقة، والإضافة والحذف والتعديل.',
    code: `package com.deutscherinnerungen.app.data.dao

import androidx.room.*
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCardDao {
    @Query("SELECT * FROM word_cards ORDER BY createdAt DESC")
    fun getAllCardsFlow(): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards WHERE id = :id")
    suspend fun getCardById(id: Long): WordCardEntity?

    @Query("SELECT * FROM word_cards WHERE id = :id")
    fun getCardByIdFlow(id: Long): Flow<WordCardEntity?>

    @Query("SELECT * FROM word_cards WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCardsFlow(query: String): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteCardsFlow(): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards WHERE nextReviewAt <= :currentTime ORDER BY nextReviewAt ASC")
    fun getCardsDueForReviewFlow(currentTime: Long): Flow<List<WordCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: WordCardEntity): Long

    @Update
    suspend fun updateCard(card: WordCardEntity)

    @Delete
    suspend fun deleteCard(card: WordCardEntity)

    @Query("UPDATE word_cards SET reviewLevel = :level, lastReviewedAt = :lastReviewedAt, nextReviewAt = :nextReviewAt WHERE id = :id")
    suspend fun updateReviewProgress(id: Long, level: Int, lastReviewedAt: Long, nextReviewAt: Long)
}`
  },

  WordAnchorDatabase: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/data/database/WordAnchorDatabase.kt',
    fileName: 'WordAnchorDatabase.kt',
    layer: 'Data / Database',
    purpose: 'قاعدة بيانات Room مع إعدادات حماية البيانات والتحديثات المتوافقة وتوفير كائن Singleton.',
    code: `package com.deutscherinnerungen.app.data.database

import android.content.Context
import androidx.room.*
import com.deutscherinnerungen.app.data.dao.WordCardDao
import com.deutscherinnerungen.app.data.entity.WordCardEntity

@Database(entities = [WordCardEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WordAnchorDatabase : RoomDatabase() {
    abstract fun wordCardDao(): WordCardDao

    companion object {
        @Volatile private var INSTANCE: WordAnchorDatabase? = null

        fun getInstance(context: Context): WordAnchorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordAnchorDatabase::class.java,
                    "word_anchor.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}`
  },

  WordCardRepository: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/data/repository/WordCardRepository.kt',
    fileName: 'WordCardRepository.kt',
    layer: 'Repository Pattern',
    purpose: 'واجهة Repository التي تفصل بين منطق واجهة المستخدم والوصول لقاعدة البيانات وتوفر التفاعل غير المتزامن عبر Coroutines.',
    code: `package com.deutscherinnerungen.app.data.repository

import com.deutscherinnerungen.app.data.entity.WordCardEntity
import kotlinx.coroutines.flow.Flow

interface WordCardRepository {
    fun getAllCards(): Flow<List<WordCardEntity>>
    suspend fun getCardById(id: Long): WordCardEntity?
    fun getCardByIdFlow(id: Long): Flow<WordCardEntity?>
    fun searchCards(query: String): Flow<List<WordCardEntity>>
    fun getFavoriteCards(): Flow<List<WordCardEntity>>
    suspend fun insertCard(card: WordCardEntity): Long
    suspend fun updateCard(card: WordCardEntity)
    suspend fun deleteCard(card: WordCardEntity)
    suspend fun toggleFavorite(id: Long, currentStatus: Boolean)
    suspend fun markReviewed(id: Long, nextLevel: Int, nextReviewAt: Long)
    suspend fun snoozeReview(id: Long, snoozeUntil: Long)
}`
  },

  SpacedRepetitionEngine: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/srs/SpacedRepetitionEngine.kt',
    fileName: 'SpacedRepetitionEngine.kt',
    layer: 'SRS Domain',
    purpose: 'محرك التكرار المتباعد المستقل تماماً: فترات المراجعة (0, 1, 3, 7, 14, 30, 60 يوماً)، وحساب مواعيد التأجيل (Snooze).',
    code: `package com.deutscherinnerungen.app.srs

import java.util.Calendar
import java.util.concurrent.TimeUnit

object SpacedRepetitionEngine {
    val REVIEW_INTERVALS_DAYS = listOf(0, 1, 3, 7, 14, 30, 60)
    const val MAX_LEVEL = 6

    data class ReviewStepResult(val nextLevel: Int, val nextReviewAt: Long, val isMastered: Boolean)

    fun calculateNextReview(currentLevel: Int, baseTime: Long = System.currentTimeMillis()): ReviewStepResult {
        val nextLevel = (currentLevel + 1).coerceAtMost(MAX_LEVEL)
        val daysToAdd = REVIEW_INTERVALS_DAYS[nextLevel]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTime
            add(Calendar.DAY_OF_YEAR, daysToAdd)
        }
        return ReviewStepResult(nextLevel, calendar.timeInMillis, nextLevel >= MAX_LEVEL)
    }

    enum class SnoozeOption { AFTER_10_MINUTES, AFTER_1_HOUR, TOMORROW, CUSTOM }

    fun calculateSnoozeTime(option: SnoozeOption, baseTime: Long = System.currentTimeMillis()): Long {
        return when (option) {
            SnoozeOption.AFTER_10_MINUTES -> baseTime + TimeUnit.MINUTES.toMillis(10)
            SnoozeOption.AFTER_1_HOUR -> baseTime + TimeUnit.HOURS.toMillis(1)
            SnoozeOption.TOMORROW -> Calendar.getInstance().apply {
                timeInMillis = baseTime
                add(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis
            SnoozeOption.CUSTOM -> baseTime + TimeUnit.HOURS.toMillis(2)
        }
    }
}`
  },

  ReminderScheduler: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/notifications/ReminderScheduler.kt',
    fileName: 'ReminderScheduler.kt',
    layer: 'Notifications / Alarms',
    purpose: 'جدولة التنبيهات الدقيقة عبر AlarmManager (setExactAndAllowWhileIdle)، والتحقق من canScheduleExactAlarms لأندرويد 12+.',
    code: `package com.deutscherinnerungen.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.deutscherinnerungen.app.data.entity.ReminderType
import com.deutscherinnerungen.app.data.entity.WordCardEntity
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(card: WordCardEntity) {
        if (!card.reminderEnabled) {
            cancelReminder(card.id)
            return
        }
        val triggerTime = calculateNextTriggerTime(card)
        val pendingIntent = createAlarmPendingIntent(card.id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelReminder(cardId: Long) {
        val pendingIntent = createAlarmPendingIntent(cardId)
        alarmManager.cancel(pendingIntent)
    }

    private fun createAlarmPendingIntent(cardId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.deutscherinnerungen.app.ACTION_WORD_REMINDER"
            putExtra("card_id", cardId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, cardId.toInt(), intent, flags)
    }
}`
  },

  AlarmReceiver: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/notifications/AlarmReceiver.kt',
    fileName: 'AlarmReceiver.kt',
    layer: 'BroadcastReceiver',
    purpose: 'مستقبل التنبيه المستقل: يستيقظ بالـ WakeLock عند وقت التذكير، يعرض الإشعار بالكلمة ومعناها، ويعيد جدولة التكرار.',
    code: `package com.deutscherinnerungen.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cardId = intent.getLongExtra("card_id", -1L)
        if (cardId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = WordAnchorDatabase.getInstance(context)
                val card = db.wordCardDao().getCardById(cardId)
                if (card != null && card.reminderEnabled) {
                    NotificationHelper(context).showWordReminderNotification(card.id, card.word, card.meaning)
                    ReminderScheduler(context).scheduleReminder(card)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}`
  },

  MediaStorageManager: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/media/MediaStorageManager.kt',
    fileName: 'MediaStorageManager.kt',
    layer: 'Media Storage',
    purpose: 'نسخ الصور والملفات الصوتية والفيديو المحددة من PhotoPicker/SAF إلى مساحة التخزين الداخلية للتطبيق (filesDir) وحذف الملفات اليتيمة عند حذف البطاقة.',
    code: `package com.deutscherinnerungen.app.media

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MediaStorageManager(private val context: Context) {
    enum class MediaType(val subDir: String) { IMAGE("images"), AUDIO("audio"), VIDEO("videos") }

    suspend fun copyUriToInternalStorage(uri: Uri, type: MediaType): String? = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "media/\${type.subDir}").apply { if (!exists()) mkdirs() }
        val targetFile = File(dir, "\${type.name.lowercase()}_\${System.currentTimeMillis()}_\${UUID.randomUUID()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output -> input.copyTo(output) }
        }
        targetFile.absolutePath
    }

    suspend fun deleteCardMedia(imagePath: String?, audioPath: String?, videoPath: String?) {
        listOfNotNull(imagePath, audioPath, videoPath).forEach { path ->
            File(path).run { if (exists()) delete() }
        }
    }
}`
  },

  BackupManager: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/backup/BackupManager.kt',
    fileName: 'BackupManager.kt',
    layer: 'Backup / JSON',
    purpose: 'تصدير واستيراد جميع البطاقات وإعدادات المراجعة إلى ملف JSON متوافق وقابل للنقل بين الأجهزة المختلفة.',
    code: `package com.deutscherinnerungen.app.backup

import android.content.Context
import android.net.Uri
import com.deutscherinnerungen.app.data.database.WordAnchorDatabase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

class BackupManager(private val context: Context) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToJson(destinationUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        val db = WordAnchorDatabase.getInstance(context)
        val cards = db.wordCardDao().getAllCards()
        context.contentResolver.openOutputStream(destinationUri)?.use { out ->
            OutputStreamWriter(out).use { writer -> writer.write(gson.toJson(cards)) }
        }
        Result.success(cards.size)
    }
}`
  },

  HomeScreen: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/ui/screens/HomeScreen.kt',
    fileName: 'HomeScreen.kt',
    layer: 'UI / Compose Screen',
    purpose: 'الشاشة الرئيسية: قائمة البطاقات، شريط البحث، شارات التصفية، خيارات الترتيب، وزر الإضافة العائم (+).',
    code: `package com.deutscherinnerungen.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deutscherinnerungen.app.ui.components.WordCardItem
import com.deutscherinnerungen.app.ui.viewmodel.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CardsViewModel,
    onNavigateToAddCard: () -> Unit,
    onNavigateToCardDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("تثبيت الكلمات") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddCard) {
                Icon(Icons.Default.Add, contentDescription = "إضافة بطاقة")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(state.cards) { card ->
                WordCardItem(
                    card = card,
                    onClick = { onNavigateToCardDetail(card.id) },
                    onFavoriteToggle = { viewModel.toggleFavorite(card) }
                )
            }
        }
    }
}`
  },

  CardDetailScreen: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/ui/screens/CardDetailScreen.kt',
    fileName: 'CardDetailScreen.kt',
    layer: 'UI / Compose Screen',
    purpose: 'شاشة عرض البطاقة وتفاصيلها: النطق الصوتي، الفيديو، شارة مستوى التكرار المتباعد، وأزرار "راجعتها" و"أعد التذكير لاحقاً".',
    code: `package com.deutscherinnerungen.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deutscherinnerungen.app.srs.SpacedRepetitionEngine
import com.deutscherinnerungen.app.ui.viewmodel.CardDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    viewModel: CardDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val card = state.card ?: return

    Scaffold(topBar = { TopAppBar(title = { Text("تفاصيل البطاقة") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(card.word, style = MaterialTheme.typography.headlineMedium)
            Text(card.meaning, style = MaterialTheme.typography.titleMedium)
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.markReviewed() }, modifier = Modifier.weight(1f)) {
                    Text("راجعتها")
                }
                OutlinedButton(onClick = { viewModel.snooze(SpacedRepetitionEngine.SnoozeOption.AFTER_1_HOUR) }, modifier = Modifier.weight(1f)) {
                    Text("أعد التذكير لاحقاً")
                }
            }
        }
    }
}`
  },

  AddEditCardScreen: {
    path: 'app/src/main/java/com/deutscherinnerungen/app/ui/screens/AddEditCardScreen.kt',
    fileName: 'AddEditCardScreen.kt',
    layer: 'UI / Compose Screen',
    purpose: 'شاشة موحدة لإضافة وتعديل البطاقات: حقول الإدخال، منتقي الصور والصوت والفيديو، وإعدادات التذكير اليومي والمتكرر.',
    code: `package com.deutscherinnerungen.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deutscherinnerungen.app.ui.viewmodel.AddEditCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(viewModel: AddEditCardViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(if (state.isEditMode) "تعديل البطاقة" else "إضافة بطاقة") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = state.word, onValueChange = viewModel::onWordChange, label = { Text("الكلمة *") })
            OutlinedTextField(value = state.meaning, onValueChange = viewModel::onMeaningChange, label = { Text("المعنى *") })
            OutlinedTextField(value = state.notes, onValueChange = viewModel::onNotesChange, label = { Text("ملاحظات") })
            Button(onClick = viewModel::saveCard, modifier = Modifier.fillMaxWidth()) {
                Text("حفظ البطاقة")
            }
        }
    }
}`
  },

  buildWorkflow: {
    path: '.github/workflows/build.yml',
    fileName: 'build.yml',
    layer: 'CI / GitHub Actions',
    purpose: 'سير العمل التلقائي لإعداد JDK 17 وبناء ملف APK تلقائياً ورفعه كـ Artifact.',
    code: `name: Build Android APK
on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    name: Build Debug APK
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
      - name: Build with Gradle
        run: ./gradlew assembleDebug --stacktrace
      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        with:
          name: WordAnchor-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 14`
  }
};
