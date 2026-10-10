# ChatDrs (تطبيق مراسلة عربي عصري ومشفّر)

تطبيق مراسلة عربي مبني باستخدام **Kotlin** و **Jetpack Compose (Material 3)** مع دعم كامل للاتجاه من اليمين إلى اليسار (**RTL**) وخط **Cairo**، ومرتبط سحابياً بخدمات **Firebase Authentication (Google Sign-In via Credential Manager)** و **Cloud Firestore**.

---

## المزايا الرئيسية

- **تسجيل الدخول عبر Google (Credential Manager API):**
  - مصادقة آمنة وسريعة عبر `GetSignInWithGoogleOption` و `GetGoogleIdOption`.
  - مزامنة تلقائية لملف المستخدم الشخصي في مجموعة `/users/{userId}` على Firestore.
- **إدارة الحالة والتخزين السحابي (`ChatViewModel` & `MessageRepository`):**
  - تدفق بيانات لحظي (`StateFlow`) لقائمة المحادثات والرسائل عبر مستمعات Firestore (`addSnapshotListener`).
  - إرسال واستقبال الرسائل النصية والملاحظات الصوتية وحفظها بشكل دائم في `/chats/{chatId}/messages`.
  - دعم التفاعلات بالرموز التعبيرية (Emoji Reactions)، تثبيت المحادثات، كتم الإشعارات، والبحث الفوري.
- **قواعد أمان محصنة (`firestore.rules`):**
  - عزل بيانات المحادثات بحيث لا يُسمح بالقراءة أو الكتابة إلا للمشاركين المصادق عليهم (`participantIds`).

---

## إعدادات Firebase (`google-services.json`)

تم تضمين ملف الإعدادات المُهيأ للمشروع في المسار:
- `app/google-services.json` (يحتوي على معرّف الحزمة `com.drs.chatdrs` ومعرّف عميل OAuth 2.0 `default_web_client_id`).
- `app/src/main/res/values/firebase_applet_config.xml` (يحتوي على معرّف قاعدة البيانات `firestore_database_id`).

> **ملاحظة عند التصدير أو التشغيل على بيئة خارجية (Android Studio محلي أو مستودع GitHub خاص بك):**
> إذا قمت ببناء التطبيق باستخدام مفتاح توقيع (`debug.keystore` أو `release.keystore`) مختلف عن بيئة التشغيل الحالية، أو إذا رغبت في ربطه بمشروع Firebase خاص بك:
> 1. افتح [Firebase Console](https://console.firebase.google.com/).
> 2. أضف بصمة شهادة التوقيع (`SHA-1` و `SHA-256`) الخاصة بجهازك لتطبيق الأندرويد `com.drs.chatdrs`.
> 3. قم بتحميل ملف `google-services.json` المحدث وضعه في المسار `app/google-services.json`.
