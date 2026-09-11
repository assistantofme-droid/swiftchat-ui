# 📘 راهنمای جامع پیاده‌سازی بک‌اند — 7eve9chat

> این سند با هدف پیاده‌سازی کامل بک‌اند نوشته شده است. هر بخش شامل:
> - متد HTTP و مسیر دقیق
> - هدرهای مورد نیاز
> - ساختار درخواست (Body / Query / FormData)
> - ساختار پاسخ موفق
> - ساختار خطاهای احتمالی
> - فیلدهای MongoDB Schema
> - رویدادهای Socket.IO که باید emit شوند
>
> **تمام مسیرهای REST پیشوند `/api` دارند.**

---

## ⚙️ تنظیمات پایه

| مورد | مقدار |
|------|------|
| **BASE_URL** | `https://7eve9craft.ir` |
| **API_URL (REST)** | `https://7eve9craft.ir/api` |
| **SOCKET_URL** | `https://7eve9craft.ir` (همان Base URL) |
| **Auth روش** | `Bearer Token` در هدر `Authorization` |
| **Database** | MongoDB |
| **Storage** | محلی (Local) برای فایل‌ها در مسیر `/uploads/` |

### نحوه اتصال کلاینت (جهت مرجع)
```javascript
// axios instance
const instance = axios.create({
  baseURL: 'https://7eve9craft.ir/api',
  timeout: 30000,
});
// Auth interceptor
instance.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

### نحوه اتصال Socket.IO
```javascript
const socket = io('https://7eve9craft.ir', {
  query: { userId: authUser._id },
  transports: ['websocket'],
});
```

> 🔑 **نکته مهم:** توکن احراز هویت REST از طریق هدر `Authorization` ارسال می‌شود. اما در Socket.IO فقط `userId` در query ارسال می‌شود. بنابراین برای Socket باید از طریق `socket.handshake.query.userId` کاربر را شناسایی کنید.

---

## ۱) احراز هویت (Authentication)

### 1.1 ساختار Schema

#### User Schema (MongoDB)
```javascript
{
  _id: ObjectId,
  phone: String,              // شماره موبایل با کد کشور (مثلا 989121234567)
  name: String,
  username: { type: String, unique: true, sparse: true },
  bio: String,
  avatar: String,             // URL relative: /uploads/avatars/xxx.jpg
  avatars: [String],          // آرایه‌ای از آواتارها (می‌توان چند عکس داشت)
  isVerified: Boolean,
  isOwner: Boolean,
  isPhoneHidden: Boolean,
  birthday: Date,
  profileColor: String,
  profileFrame: ObjectId,     // ref: Frame
  profileBanner: {
    type: String,             // 'default' | 'image' | 'video'
    url: String,
    trimStart: Number,
    trimEnd: Number,
  },
  profileSongs: [{
    title: String,
    url: String,
    senderName: String,
    messageId: ObjectId,
    duration: Number,
    coverUrl: String,
  }],
  profileActiveSong: {
    title: String,
    url: String,
    // ... همان ساختار بالا
  },
  privacySettings: {
    lastSeen: String,         // 'Everyone' | 'Contacts' | 'Nobody'
    phoneNumber: String,
    forwardedMessages: String,
    groupsAndChannels: String,
  },
  settings: {
    language: String,         // 'fa' | 'en'
  },
  blockedUsers: [ObjectId],   // کاربران بلاک‌شده توسط این کاربر
  contacts: [ObjectId],       // ref: User
  createdAt: Date,
  updatedAt: Date,
}
```

#### Session Schema (برای مدیریت sessionهای فعال)
```javascript
{
  _id: ObjectId,
  userId: ObjectId,           // ref: User
  token: String,              // JWT hash
  deviceInfo: {
    platform: String,         // 'ios' | 'android' | 'web'
    deviceName: String,
    appVersion: String,
  },
  ipAddress: String,
  createdAt: Date,
  lastActiveAt: Date,
  expiresAt: Date,
}
```

### 1.1 ارسال کد تأیید (OTP)

**POST `/auth/send-otp`**

**Body:**
```json
{
  "phone": "989121234567",
  "forceSms": false
}
```

| فیلد | نوع | توضیح |
|------|-----|-------|
| `phone` | String | شماره موبایل با کد کشور (بدون + و 0 اول) |
| `forceSms` | Boolean | اگر `true` باشد، حتی اگر کد قبلی منقضی نشده، کد جدید پیامک می‌شود |

**Response 200:**
```json
{
  "message": "OTP sent successfully",
  "phone": "989121234567",
  "hash": "uuid-or-hash"  // اختیاری - برای محدودسازی rate-limit
}
```

**خطاها:**
- `400`: شماره نامعتبر
- `429`: درخواست زیاد (rate limit)

### 1.2 بررسی کد و ورود

**POST `/auth/verify-otp`**

**Body:**
```json
{
  "phone": "989121234567",
  "code": "1234"
}
```

**Response 200 (کاربر قدیمی):**
```json
{
  "token": "jwt-token-string",
  "user": {
    "_id": "60d5ec49c656d02958172070",
    "phone": "989121234567",
    "name": "John",
    "username": "john",
    "avatar": "/uploads/avatars/john.jpg",
    "isVerified": false,
    "isOwner": false
  }
}
```

**Response 200 (کاربر جدید):**
```json
{
  "token": "jwt-token-string",
  "user": {
    "_id": "60d5ec49c656d02958172070",
    "phone": "989121234567",
    "isNewUser": true
  }
}
```

> 💡 **رفتار مورد نیاز:** کلاینت بعد از دریافت توکن، اگر `user.name` یا `user.username` خالی باشد، به صفحه `SetupProfileScreen` می‌رود.

### 1.3 گرفتن پروفایل فعلی

**GET `/auth/me`**

**Headers:**
```
Authorization: Bearer <token>
```

**Response 200:** آبجکت کامل User (با تمام فیلدهای schema)

**خطاها:**
- `401`: توکن نامعتبر یا منقضی

### 1.4 بلاک / آنبلاک کاربر

**POST `/auth/block`**

**Body:**
```json
{ "userId": "60d5ec49c656d02958172055" }
```

**Response 200:**
```json
{ "message": "User blocked", "blockedUsers": ["..."] }
```

**POST `/auth/unblock`** — ساختار مشابه

### 1.5 گزارش کاربر

**POST `/auth/report`**

**Body:**
```json
{
  "reportedUserId": "60d5ec49c656d02958172055",
  "reason": "Spam or inappropriate content",
  "messageId": "60d5ec49c656d02958172100"  // اختیاری
}
```

**Response 200:**
```json
{ "message": "Report submitted" }
```

### 1.6 لیست کاربران بلاک‌شده

**GET `/auth/blocked/users`**

**Response 200:**
```json
[
  { "_id": "...", "name": "Alice", "username": "alice", "avatar": "..." },
  ...
]
```

### 1.7 لیست sessionهای فعال

**GET `/auth/sessions`**

**Response 200:**
```json
[
  {
    "_id": "session_id",
    "deviceInfo": { "platform": "ios", "deviceName": "iPhone 13", "appVersion": "1.0" },
    "ipAddress": "1.2.3.4",
    "createdAt": "2023-10-01T10:00:00.000Z",
    "lastActiveAt": "2023-10-05T12:00:00.000Z",
    "current": true   // نشون می‌دهد این session فعلی است
  }
]
```

### 1.8 مدیریت مخاطبین

#### 1.8.1 لیست مخاطبین
**GET `/auth/contacts`**

**Response 200:**
```json
[
  {
    "_id": "60d5ec49c656d02958172055",
    "name": "Alice",
    "username": "alice",
    "avatar": "/uploads/alice.jpg",
    "phone": "989121234567",
    "isVerified": false,
    "isPhoneHidden": false
  }
]
```

#### 1.8.2 افزودن مخاطب
**POST `/auth/contacts`**

**Body:**
```json
{ "identifier": "989121234567" }
// یا { "identifier": "alice_username" }
```

> کلاینت هم شماره و هم username را می‌پذیرد. سرور باید تشخیص دهد.

**Response 200:**
```json
{ "message": "Contact added", "contact": { "_id": "...", "name": "Alice" } }
```

#### 1.8.3 بررسی شماره‌های دستگاه
**POST `/auth/check-contacts`**

**Body:**
```json
{
  "phones": ["09121234567", "09127654321", ...]
}
```

**Response 200:**
```json
[
  {
    "_id": "60d5ec49c656d02958172055",
    "name": "Alice",
    "username": "alice",
    "avatar": "/uploads/alice.jpg",
    "phone": "09121234567",
    "isVerified": false
  }
]
```

> این API برای پیدا کردن کاربران ثبت‌نام‌شده در میان مخاطبین گوشی استفاده می‌شود.

### 1.9 بررسی موجود بودن username

**GET `/auth/getUserByUsername/:username`**

**Response 200 (یافت شد):**
```json
{
  "_id": "...",
  "username": "alice",
  "name": "Alice",
  "avatar": "..."
}
```

**Response 404 (موجود نیست):** کلاینت این را به‌عنوان "username قابل استفاده است" تفسیر می‌کند.

### 1.10 گرفتن پروفایل کاربر دیگر

**GET `/auth/users/:userId`**

**Response 200:** آبجکت User (با درنظرگرفتن privacySettings)

> اگر `privacySettings.phoneNumber === 'Nobody'` باشد، فیلد `phone` را برنگردان. اگر `'Contacts'` و کاربر در مخاطبین نباشد، فیلد `phone` را برنگردان.

---

## ۲) پروفایل (Profile)

### 2.1 ساختار کلی آپدیت پروفایل

> **مهم:** تمام آپدیت‌های پروفایل از طریق `PUT /auth/profile` انجام می‌شوند. این endpoint باید هر دو حالت `application/json` و `multipart/form-data` را پشتیبانی کند.

### 2.2 PUT `/auth/profile` — آپدیت فیلدهای متنی

**Content-Type: application/json**

**Body (تمام فیلدها اختیاری):**
```json
{
  "name": "John Doe",
  "username": "johndoe",
  "bio": "Hello world",
  "about": "Engineer",
  "birthday": "1990-01-01T00:00:00.000Z",   // یا null
  "avatar": "/uploads/avatars/xyz.jpg",     // انتخاب آواتار فعال از بین avatars
  "avatars": ["/uploads/avatars/a.jpg", "/uploads/avatars/b.jpg"],
  "theme": "dark",
  "profileActiveSong": null,                // یا { title, url, ... }
  "profileSongs": [{ "title": "Song", "url": "..." }],
  "profileColor": "#ff5722",
  "profileFrame": "60d5ec49c656d02958172090",  // یا null
  "profileBanner": {
    "type": "image",
    "url": "/uploads/banner.jpg",
    "trimStart": 0,
    "trimEnd": 10
  },
  "privacySettings": {
    "lastSeen": "Everyone",
    "phoneNumber": "Contacts",
    "forwardedMessages": "Everyone",
    "groupsAndChannels": "Everyone"
  },
  "isPhoneHidden": true,
  "settings": { "language": "fa" }
}
```

**Response 200:** آبجکت کامل User به‌روزرسانی‌شده

### 2.3 PUT `/auth/profile` — آپدیت با فایل (multipart)

**Content-Type: multipart/form-data**

**FormData fields:**
- `file`: فایل تصویری (پروفایل عکس یا بنر یا آواتار جدید)

> سرور فایل را در `/uploads/avatars/` ذخیره می‌کند، URL را به `avatars` اضافه و `avatar` را به این URL جدید تنظیم می‌کند. سپس آبجکت User برمی‌گردد.

### 2.4 GET `/users/frames`

> لیست قاب‌های موجود برای آواتار (فقط برای کاربران `isVerified` یا `isOwner`).

**Response 200:**
```json
[
  { "_id": "...", "name": "Gold Frame", "imageUrl": "/uploads/frames/gold.png" },
  { "_id": "...", "name": "Neon Frame", "imageUrl": "/uploads/frames/neon.png" }
]
```

### 2.5 سناریوهای کلیدی پروفایل (برای پیاده‌سازی صحیح)

#### سناریو 1: SetupProfile (اولین بار)
کلاینت متوالی:
1. `PUT /auth/profile` با `{ name, username }` (JSON)
2. اگر عکس دارد: `PUT /auth/profile` با FormData (`file`)

#### سناریو 2: آپدیت آواتار جدید
کلاینت: `PUT /auth/profile` با FormData (`file`)
- سرور باید عکس جدید را به `avatars` (در ابتدای آرایه) اضافه کند و `avatar` را به این URL جدید تنظیم کند.

#### سناریو 3: حذف آواتار
کلاینت: `PUT /auth/profile` با `{ avatars: [...], avatar: "..." }` (آرایه‌ی فیلترشده)

#### سناریو 4: تنظیم آهنگ فعال
- `PUT /auth/profile` با `{ profileActiveSong: { title, url, ... } }` برای فعال‌سازی
- `PUT /auth/profile` با `{ profileActiveSong: null }` برای غیرفعال‌سازی

#### سناریو 5: حریم خصوصی
کلاینت: `PUT /auth/profile` با:
```json
{
  "privacySettings": {
    "lastSeen": "Contacts",
    "phoneNumber": "Nobody",
    "forwardedMessages": "Everyone",
    "groupsAndChannels": "Everyone"
  },
  "isPhoneHidden": true
}
```

---

## ۳) گفتگوها و گروه‌ها و کانال‌ها (Conversations)

### 3.1 Schema

#### Conversation Schema
```javascript
{
  _id: ObjectId,
  type: String,               // 'private' | 'group' | 'channel'
  name: String,               // برای group/channel
  description: String,
  avatar: String,             // برای group/channel
  handle: String,             // publicId - برای گروه/کانال‌های عمومی
  isPrivateLink: Boolean,
  participants: [{
    user: ObjectId,            // ref: User
    role: String,              // 'owner' | 'admin' | 'member'
    isAdmin: Boolean,
    joinedAt: Date,
    customTitle: String,        // عنوان سفارشی عضو
    isAnonymous: Boolean,      // اگر در anonymousAdmins باشد
  }],
  owner: ObjectId,             // ref: User (برای group/channel)
  admins: [ObjectId],
  anonymousAdmins: [ObjectId],
  bannedUsers: [{
    user: ObjectId,
    bannedAt: Date,
    until: Date,               // null = permanent
    reason: String,
  }],
  restrictedUsers: [{
    user: ObjectId,
    restrictions: {
      canSendMessages: Boolean,
      canSendMedia: Boolean,
      canSendFiles: Boolean,
      canSendVoice: Boolean,
      canSendPolls: Boolean,
      canSendGifs: Boolean,
      canSendLinks: Boolean,
      canPinMessages: Boolean,
      canChangeGroupInfo: Boolean,
    },
    until: Date,
  }],
  defaultPermissions: {        // مشابه restrictions اما به‌عنوان پیش‌فرض
    canSendMessages: true,
    canSendMedia: true,
    // ...
    canAddMembers: true,
    canPinMessages: true,
    canChangeGroupInfo: true,
  },
  approveNewMembers: Boolean,
  slowModeInterval: Number,    // ثانیه - 0 یعنی غیرفعال
  protectedContent: Boolean,   // برای کانال - جلوگیری از فوروارد/ذخیره
  showSignatures: Boolean,     // برای کانال - نمایش نام نویسنده
  pinnedMessages: [ObjectId],  // ref: Message
  inviteCode: String,          // کد دعوت
  linkedDiscussionGroup: ObjectId,  // ref: Conversation - برای کانال
  isChannel: Boolean,          // alias for type === 'channel'
  lastMessage: ObjectId,       // ref: Message
  lastMessageAt: Date,
  createdAt: Date,
  updatedAt: Date,
}
```

#### JoinRequest Schema
```javascript
{
  _id: ObjectId,
  conversationId: ObjectId,
  userId: ObjectId,
  status: String,             // 'pending' | 'approved' | 'declined'
  createdAt: Date,
  resolvedAt: Date,
  resolvedBy: ObjectId,
}
```

#### AdminLog Schema
```javascript
{
  _id: ObjectId,
  conversationId: ObjectId,
  adminId: ObjectId,           // ممکن است null باشد اگر anonymousAdmins
  action: String,              // 'ban' | 'unban' | 'kick' | 'promote' | 'restrict' | ...
  targetUserId: ObjectId,
  details: Mixed,
  createdAt: Date,
}
```

### 3.2 ساخت چت خصوصی

> چت خصوصی به‌صورت خودکار هنگام ارسال اولین پیام ساخته می‌شود (در `/messages`).

### 3.3 ساخت گروه یا کانال

**POST `/conversations/group`**

**Content-Type:** `application/json` یا `multipart/form-data` (اگر avatar دارد)

**Body (JSON):**
```json
{
  "name": "My Group",
  "type": "group",             // یا "channel"
  "description": "Group description",
  "publicId": "mygroup",
  "isPrivateLink": false,
  "participants": ["userId1", "userId2", ...]
}
```

**Body (FormData):**
- `file`: عکس avatar
- `name`: نام گروه
- `type`: `group` یا `channel`
- `description`: توضیح
- `publicId`: آیدی عمومی
- `isPrivateLink`: `'true'` یا `'false'`
- `participants[]`: به‌صورت آرایه‌ی fields (تکراری)

**Response 200:**
```json
{
  "_id": "60d5ec49c656d02958172081",
  "name": "My Group",
  "type": "group",
  "description": "...",
  "avatar": "/uploads/groups/xyz.jpg",
  "participants": [{ "user": "...", "role": "owner" }],
  "owner": "60d5ec49c656d02958172070",
  "handle": "mygroup",
  "createdAt": "..."
}
```

> 💡 **نکته:** اگر `avatar` ارسال شود اما سرور نتواند پردازش کند، کلاینت retry می‌کند بدون avatar (فقط JSON). پس FormData باید با optional file کار کند.

### 3.4 گرفتن لیست گفتگوها

**GET `/conversations`**

**Response 200:**
```json
[
  {
    "_id": "...",
    "type": "private",
    "participants": [{ "_id": "...", "name": "Alice", "avatar": "..." }],
    "lastMessage": { "text": "...", "createdAt": "..." },
    "unreadCount": 5,
    "isMuted": false,
    "pinned": false,
    "lastMessageAt": "..."
  },
  {
    "_id": "...",
    "type": "group",
    "name": "My Group",
    "avatar": "...",
    "participants": [...],
    "lastMessage": {...},
    "unreadCount": 0
  }
]
```

### 3.5 گرفتن اطلاعات یک گفتگو

**GET `/conversations/:id`**

**Response 200:** آبجکت کامل Conversation شامل:
- participants با populate کاربران
- bannedUsers
- restrictedUsers
- defaultPermissions
- anonymousAdmins (در صورت نبودن کاربر فعلی در لیست)
- linkedDiscussionGroup (اگر کانال است)

### 3.6 گرفتن با handle

**GET `/conversations/handle/:handle`**

> برای زمانی که کاربر روی لینک `https://7eve9craft.ir/ch/handle` کلیک می‌کند.

**Response 200:**
```json
{
  "_id": "...",
  "isUser": false,             // اگر true باشد یعنی handle مربوط به یک user است
  "name": "My Channel",
  "username": "...",           // اگر isUser=true
  "avatar": "...",
  "type": "channel",
  "isVerified": false
}
```

### 3.7 ویرایش تنظیمات اصلی گفتگو

**PUT `/conversations/:id`**

**Body (همه اختیاری):**
```json
{
  "name": "New Name",
  "description": "New description",
  "approveNewMembers": true,
  // فیلدهای دیگر schema...
}
```

### 3.8 ویرایش با FormData (آواتار/نام/توضیحات)

**PUT `/conversations/:id/edit`**

**Content-Type: multipart/form-data**

**FormData fields:**
- `file` (اختیاری): عکس avatar جدید
- `name`: نام جدید
- `description`: توضیح جدید

### 3.9 حذف / خروج از گفتگو

**DELETE `/conversations/:id`**

- برای private: چت برای هر دو کاربر حذف می‌شود (یا فقط برای کاربر درخواست‌کننده).
- برای group/channel: کاربر از participants حذف می‌شود.

### 3.10 Mute / Unmute

**PUT `/conversations/:id/mute`**

> No body. وضعیت mute toggle می‌شود.

**Response 200:**
```json
{ "message": "Mute toggled", "isMuted": true }
```

### 3.11 Slow Mode

**PUT `/conversations/:id/slow-mode`**

**Body:**
```json
{ "slowModeInterval": 30 }   // ثانیه - 0 یعنی غیرفعال
```

### 3.12 تنظیم permissions پیش‌فرض

**PUT `/conversations/:id/permissions`**

**Body:**
```json
{
  "defaultPermissions": {
    "canSendMessages": true,
    "canSendMedia": true,
    "canSendFiles": true,
    "canSendVoice": true,
    "canSendPolls": true,
    "canSendGifs": true,
    "canSendLinks": true,
    "canAddMembers": true,
    "canPinMessages": true,
    "canChangeGroupInfo": false
  }
}
```

### 3.13 Admin Anonymous

**PUT `/conversations/:id/admin/anonymous`**

**Body:**
```json
{ "isAnonymous": true }
```

> کاربر به/از `anonymousAdmins` اضافه/حذف می‌شود.

### 3.14 Custom Title

**PUT `/conversations/:id/custom-title`**

**Body:**
```json
{
  "userId": "60d5ec49c656d02958172055",
  "customTitle": "VIP Member"
}
```

### 3.15 انتقال مالکیت

**PUT `/conversations/:id/transfer-owner`**

**Body:**
```json
{ "newOwnerId": "60d5ec49c656d02958172055" }
```

> عملیات غیرقابل بازگشت است. مالک قبلی به admin تبدیل می‌شود.

### 3.16 تنظیمات اختصاصی کانال

#### Protected Content
**PUT `/conversations/:id/toggle-protected-content`**
```json
{ "protectedContent": true }
```

#### Signatures
**PUT `/conversations/:id/toggle-signatures`**
```json
{ "showSignatures": true }
```

### 3.17 Pin / Unpin Message

**PUT `/conversations/:id/pin`**

**Body:**
```json
{
  "messageId": "60d5ec49c656d02958172100",
  "action": "pin"            // یا "unpin" (اختیاری - اگر نبود، pin است)
}
```

> سرور باید Socket events `message_pinned` / `message_unpinned` / `pins_updated` را به اعضای گفتگو emit کند.

---

## ۴) جستجو و Invite (Discovery)

### 4.1 جستجوی گروه‌ها و کانال‌ها

**GET `/conversations/discover/search?query=<query>`**

**Response 200:**
```json
[
  {
    "_id": "...",
    "type": "channel",
    "name": "Tech News",
    "avatar": "...",
    "handle": "technews",
    "description": "...",
    "participantsCount": 1234,
    "isVerified": true
  }
]
```

### 4.2 گرفتن اطلاعات Invite Code

**GET `/conversations/invite/:code`**

**Response 200:**
```json
{
  "_id": "...",
  "name": "My Group",
  "type": "group",
  "avatar": "...",
  "participantsCount": 50,
  "code": "ABC123"
}
```

### 4.3 ساخت / گرفتن Invite برای گفتگو

**GET `/conversations/:id/invite`**

**Response 200:**
```json
{
  "code": "ABC123",
  "link": "https://7eve9craft.ir/invite/ABC123",
  "expiresAt": null  // null = never expires
}
```

### 4.4 عضویت با Invite Code

**POST `/conversations/invite/:code/join`**

**Response 200:** آبجکت Conversation کامل (شمارو هم اضافه کرده)

### 4.5 ابطال Invite

**POST `/conversations/:id/invite/revoke`**

**Response 200:**
```json
{ "code": "NEW_CODE", "link": "..." }
```

---

## ۵) عضویت و درخواست‌ها

### 5.1 عضویت مستقیم

**POST `/conversations/:id/join`**

> اگر `approveNewMembers === true` باشد، این endpoint نباید کاربر را اضافه کند — باید 400 برگرداند که بگوید "needs approval".

**Response 200:** Conversation با کاربر جدید
**Response 400:** `{ "message": "This group requires approval. Please send a join request." }`

### 5.2 ارسال درخواست عضویت

**POST `/conversations/:id/request-to-join`**

**Response 200:**
```json
{
  "_id": "request_id",
  "conversationId": "...",
  "userId": "...",
  "status": "pending",
  "createdAt": "..."
}
```

> 🔌 سرور باید socket event `join_request_created` را به owner/admins emit کند.

### 5.3 وضعیت درخواست من

**GET `/conversations/:id/my-request`**

**Response 200:**
```json
{
  "_id": "request_id",
  "status": "pending",   // یا 404 اگر درخواستی وجود ندارد
  "createdAt": "..."
}
```

### 5.4 تعداد درخواست‌های در انتظار

**GET `/conversations/:id/request-count`**

**Response 200:**
```json
{ "count": 5 }
```

### 5.5 لیست درخواست‌های در انتظار

**GET `/conversations/:id/pending-requests`**

**Response 200:**
```json
[
  {
    "_id": "request_id",
    "user": { "_id": "...", "name": "Alice", "avatar": "..." },
    "createdAt": "..."
  }
]
```

### 5.6 تاریخچه درخواست‌ها

**GET `/conversations/:id/request-history`**

**Response 200:**
```json
[
  {
    "_id": "...",
    "user": {...},
    "status": "approved",   // یا "declined"
    "createdAt": "...",
    "resolvedAt": "...",
    "resolvedBy": {...}
  }
]
```

### 5.7 تأیید درخواست (تک یا گروهی)

**POST `/conversations/:id/approve-request`**

**Body:**
```json
// حالت تک:
{ "requestId": "60d5ec49c656d02958172100", "userId": "..." }

// حالت گروهی:
{ "requestIds": ["req1", "req2", ...] }
```

> 🔌 باید `join_request_approved` emit شود و `join_request_count_updated` با تعداد جدید.

### 5.8 رد درخواست

**POST `/conversations/:id/decline-request`**

ساختار مشابه 5.7.

> 🔌 باید `join_request_declined` emit شود.

### 5.9 لغو درخواست خود

**POST `/conversations/:id/decline-request`**

**Body:**
```json
{ "requestId": "...", "userId": "my_user_id" }
```

---

## ۶) مدیریت اعضا

### 6.1 حذف عضو (Kick)

**PUT `/conversations/:id/remove`**

**Body:**
```json
{ "userId": "60d5ec49c656d02958172055" }
```

> 🔌 باید `member_kicked` emit شود به کاربر حذف‌شده و به کانال.

### 6.2 بن کاربر

**POST `/conversations/:id/ban`**

**Body:**
```json
{
  "userId": "60d5ec49c656d02958172055",
  "customDate": null,           // ISO date برای ban موقت
  "durationType": "permanent"   // '1_hour' | '1_day' | '1_week' | '1_month' | 'permanent' | 'custom'
}
```

> 🔌 `member_banned` emit شود.

### 6.3 رفع بن

**POST `/conversations/:id/unban`**

**Body:**
```json
{ "userId": "60d5ec49c656d02958172055" }
```

### 6.4 محدود کردن کاربر

**POST `/conversations/:id/restrict`**

**Body:**
```json
{
  "userId": "60d5ec49c656d02958172055",
  "until": "2023-12-01T00:00:00.000Z",  // یا null
  "durationType": "1_week",
  "restrictions": {
    "canSendMessages": false,
    "canSendMedia": false,
    "canSendFiles": false,
    "canSendVoice": false,
    "canSendPolls": false,
    "canSendGifs": false,
    "canSendLinks": false,
    "canPinMessages": false,
    "canChangeGroupInfo": false
  }
}
```

### 6.5 رفع محدودیت

**POST `/conversations/:id/unrestrict`**

**Body:**
```json
{ "userId": "60d5ec49c656d02958172055" }
```

### 6.6 تنظیم ادمین

**PUT `/conversations/:id/admins`**

**Body:**
```json
{
  "userId": "60d5ec49c656d02958172055",
  "isAdmin": true
}
```

### 6.7 لاگ‌های ادمین

**GET `/conversations/:id/admin-logs`**

**Query params:**
- `page` (default 1)
- `limit` (default 50)
- `action` (optional filter)

**Response 200:**
```json
{
  "logs": [
    {
      "_id": "...",
      "admin": { "_id": "...", "name": "Alice" },  // یا null اگر anonymous
      "action": "ban",
      "targetUser": { "_id": "...", "name": "Bob" },
      "details": { ... },
      "createdAt": "..."
    }
  ],
  "totalPages": 3,
  "currentPage": 1
}
```

---

## ۷) گروه بحث کانال (Discussion Group)

### 7.1 اتصال به گروه موجود

**POST `/conversations/:id/link-discussion`**

**Body:**
```json
{ "discussionGroupId": "60d5ec49c656d02958172082" }
```

> 🔌 `discussion_group_linked` emit شود.

### 7.2 قطع اتصال

**POST `/conversations/:id/unlink-discussion`**

> 🔌 `discussion_group_unlinked` emit شود.

### 7.3 ساخت و اتصال

**POST `/conversations/:id/create-link-discussion`**

**Body:**
```json
{ "name": "Channel Discussion" }
```

**Response 200:**
```json
{
  "_id": "...",
  "linkedDiscussionGroup": "60d5ec49c656d02958172082"
}
```

---

## ۸) پیام‌ها (Messages)

### 8.1 Message Schema

```javascript
{
  _id: ObjectId,
  conversationId: ObjectId,    // ref: Conversation
  sender: ObjectId,            // ref: User (ممکن است null باشد برای anonymous)
  authorSignature: String,    // برای کانال‌هایی که showSignatures دارند
  text: String,
  type: String,                // 'text' | 'image' | 'video' | 'audio' | 'file' | 'location' | 'poll' | 'contact' | 'gif' | 'sticker' | 'album_group' | 'post_share' | 'callback_button'
  fileUrl: String,             // URL فایل برای مدیا
  videoThumbnailUrl: String,
  fileName: String,
  fileSize: Number,
  duration: Number,            // برای audio/video
  audioMetadata: {
    duration: Number,
    waveform: [Number],
  },
  width: Number,               // برای image/video
  height: Number,
  mimeType: String,
  location: {
    lat: Number,
    lng: Number,
    name: String,
  },
  replyTo: ObjectId,           // ref: Message
  forwardFrom: ObjectId,       // ref: Message (پیام اصلی)
  forwardedFromUser: ObjectId, // ref: User
  reactions: [{
    emoji: String,
    user: ObjectId,
  }],
  readBy: [ObjectId],          // کاربرانی که پیام را خوانده‌اند
  deliveredTo: [ObjectId],
  poll: {                       // اگر type === 'poll'
    question: String,
    options: [{ _id, text, voters: [ObjectId] }],
    anonymous: Boolean,
    multiSelect: Boolean,
    allowChangeVote: Boolean,
    closedAt: Date,
  },
  postShareId: ObjectId,        // برای اشتراک‌گذاری پست در چت
  postSharePreview: {
    mediaUrl: String,
    caption: String,
    type: String,
  },
  inlineKeyboard: [[{
    text: String,
    url: String,                // اختیاری
    callback_data: String,      // اختیاری
  }]],
  albumId: String,              // برای album_group
  albumOrder: Number,
  albumItems: [ObjectId],
  drawPaths: String,            // JSON.stringify از مسیرهای انگشت روی image
  textOverlays: String,         // JSON.stringify از متن‌های روی image
  rotation: Number,
  scale: Number,
  isSilent: Boolean,            // نوتیفیکیشن نده
  isEdited: Boolean,
  editedAt: Date,
  deletedAt: Date,              // soft delete
  createdAt: Date,
  updatedAt: Date,
}
```

### 8.2 گرفتن لیست پیام‌ها

**GET `/messages/:conversationId`**

**Query params:**
- `page` (default 1)
- `limit` (default 50)
- `before` (ISO date - for pagination)

**Response 200:**
```json
[
  {
    "_id": "...",
    "conversationId": "...",
    "sender": { "_id": "...", "name": "Alice", "avatar": "..." },
    "text": "Hello",
    "type": "text",
    "createdAt": "2023-10-01T10:00:00.000Z",
    "reactions": [{ "emoji": "👍", "user": "..." }],
    "readBy": [...],
    "replyTo": { "_id": "...", "text": "..." }   // populate شده
  }
]
```

### 8.3 گرفتن لیست conversationها

**GET `/messages/conversations`**

> برمی‌گرداند لیست conversationهایی که کاربر در آن‌ها عضو است با آخرین پیام.

**Response 200:**
```json
[
  {
    "_id": "...",
    "type": "private",
    "name": "Alice",                // برای private، نام طرف مقابل
    "avatar": "...",
    "participants": [...],
    "lastMessage": { "text": "Hi", "createdAt": "...", "sender": {...} },
    "unreadCount": 3,
    "isMuted": false,
    "pinned": false,
    "lastMessageAt": "..."
  }
]
```

### 8.4 گرفتن conversation با user id

**GET `/messages/conversations/:userId`**

> اگر conversation خصوصی بین این دو کاربر وجود داشته باشد، برمی‌گرداند. اگر نه، 404 یا می‌سازد.

**Response 200:**
```json
{
  "_id": "...",
  "type": "private",
  "participants": [{ "_id": "...", "name": "Alice" }, { "_id": "...", "name": "Me" }]
}
```

### 8.5 ارسال پیام متنی

**POST `/messages`**

**Content-Type: application/json**

**Body:**
```json
{
  "conversationId": "...",
  "receiverId": "...",             // برای ساخت چت خصوصی اگر conversationId نداریم
  "text": "Hello!",
  "type": "text",
  "replyTo": "60d5ec49c656d02958172100",   // اختیاری
  "isSilent": false
}
```

**Response 200:** آبجکت Message کامل (با sender populated)

> 🔌 **مهم:** سرور باید Socket event `new_message` را به همه‌ی اعضای conversation (به‌جز فرستنده) emit کند با آبجکت Message.

#### سناریو: ساخت چت خصوصی
اگر `conversationId` ارسال نشود ولی `receiverId` باشد:
1. سرور چت خصوصی بین این دو کاربر را پیدا می‌کند یا می‌سازد.
2. پیام را در آن ذخیره می‌کند.
3. در response، فیلد `conversationId` را برمی‌گرداند تا کلاینت بداند.

### 8.6 ارسال پیام مدیا

**POST `/messages`**

**Content-Type: multipart/form-data**

**FormData fields:**
- `file`: فایل (image/video/audio/file)
- `conversationId`: (اختیاری)
- `receiverId`: (اختیاری)
- `type`: `image` | `video` | `audio` | `file`
- `replyTo`: (اختیاری)
- `text`: (اختیاری - caption)
- `existingFileUrl`: (اختیاری - اگر فایل قبلاً آپلود شده)
- `location`: JSON.stringify({lat, lng, name})
- `albumId`, `albumOrder`, `albumItems`: برای آلبوم
- `rotation`, `scale`: برای ویرایش image
- `drawPaths`, `textOverlays`: JSON.stringify از ویرایش‌ها
- `audioMetadata`: JSON.stringify({duration, waveform})
- `isSilent`: boolean

**Response 200:** Message object

### 8.7 ارسال نظرسنجی (Poll)

**POST `/messages`**

**Body:**
```json
{
  "conversationId": "...",
  "type": "poll",
  "poll": {
    "question": "What's your favorite color?",
    "options": [
      { "text": "Red" },
      { "text": "Blue" }
    ],
    "anonymous": true,
    "multiSelect": false,
    "allowChangeVote": true
  }
}
```

**Response 200:** Message با poll populated

### 8.8 اشتراک‌گذاری پست (Post Share)

**POST `/messages`** (یا از طریق Socket - بخش 13.1)

**Body:**
```json
{
  "conversationId": "...",
  "text": "Sent a post",
  "postShareId": "60d5ec49c656d02958172081",
  "postSharePreview": {
    "mediaUrl": "/uploads/post1.jpg",
    "caption": "My photo",
    "type": "image"
  }
}
```

### 8.9 ویرایش پیام

**PUT `/messages/:messageId`**

**Body:**
```json
{ "text": "Edited text" }
```

> 🔌 `message_updated` emit شود.

### 8.10 حذف پیام

**DELETE `/messages/:messageId`**

> 🔌 `message_deleted` با `{ messageId, conversationId }` emit شود.

### 8.11 واکنش به پیام

**POST `/messages/:messageId/react`**

**Body:**
```json
{ "emoji": "👍" }
```

> اگر کاربر قبلاً با این emoji واکنش داده، حذف می‌شود (toggle).

> 🔌 `message_reaction` emit شود با `{ messageId, reactions: [...] }`.

### 8.12 فوروارد پیام

**POST `/messages/forward`**

**Body:**
```json
{
  "conversationId": "target_conversation_id",
  "messageId": "original_message_id"
}
```

> سرور یک کپی از پیام می‌سازد با `forwardFrom` و `forwardedFromUser` تنظیم‌شده.

### 8.13 Callback Query (برای دکمه‌های اینلاین)

**POST `/messages/callback_query`**

**Body:**
```json
{
  "conversationId": "...",
  "messageId": "60d5ec49c656d02958172100",
  "callbackData": "vote_yes"
}
```

> این برای بات‌ها استفاده می‌شود. سرور باید callback را به بات مربوطه ارسال کند.

### 8.14 علامت‌گذاری خوانده‌شده

**POST `/messages/conversations/:conversationId/read`**

> تمام پیام‌های این conversation که کاربر نخوانده را به‌عنوان خوانده‌شده علامت می‌زند.

### 8.15 رأی در نظرسنجی

**POST `/messages/:messageId/poll/vote`**

**Body:**
```json
{ "optionId": "60d5ec49c656d02958172105" }
```

**Response 200:** آبجکت Message با poll به‌روزرسانی‌شده

### 8.16 بستن نظرسنجی

**POST `/messages/:messageId/poll/close`**

**Response 200:** Message با `poll.closedAt` تنظیم‌شده

---

## ۹) کامنت‌های کانال (Comments)

> این بخش برای کامنت‌هایی است که روی پست‌های کانال (نه پیام‌های معمولی) زده می‌شوند.

### 9.1 Schema

#### Comment Schema
```javascript
{
  _id: ObjectId,
  channelId: ObjectId,         // conversationId کانال
  messageId: ObjectId,         // پست کانال
  user: ObjectId,              // ref: User
  text: String,
  replyTo: ObjectId,           // ref: Comment
  reactions: [{ emoji, user }],
  isPinned: Boolean,
  createdAt: Date,
  updatedAt: Date,
}
```

### 9.2 گرفتن کامنت‌ها

**GET `/conversations/:channelId/messages/:postId/comments`**

**Response 200:**
```json
[
  {
    "_id": "...",
    "user": { "_id": "...", "username": "alice", "avatar": "..." },
    "text": "Great post!",
    "replyTo": { "_id": "...", "text": "..." },
    "createdAt": "...",
    "isPinned": false
  }
]
```

**Response 403 (اگر کاربر عضو discussion group نیست):**
```json
{
  "message": "MEMBERSHIP_REQUIRED",
  "discussionGroupId": "60d5ec49c656d02958172082"
}
```

> کلاینت با این خطا، کاربر را برای عضویت در discussion group دعوت می‌کند.

### 9.3 افزودن کامنت

**POST `/conversations/:channelId/messages/:postId/comments`**

**Body:**
```json
{
  "text": "Awesome!",
  "replyTo": "60d5ec49c656d02958172100"   // اختیاری
}
```

**Response 200:** Comment object

> 🔌 `comment_created` emit شود.

### 9.4 ویرایش کامنت

**PUT `/conversations/:channelId/comments/:commentId`**

**Body:**
```json
{ "text": "Edited comment" }
```

### 9.5 حذف کامنت

**DELETE `/conversations/:channelId/comments/:commentId`**

> 🔌 `comment_deleted` emit شود.

### 9.6 پین کردن کامنت

**POST `/conversations/:channelId/comments/:commentId/pin`**

### 9.7 واکنش به کامنت

**POST `/conversations/:channelId/comments/:commentId/react`**

**Body:**
```json
{ "emoji": "👍" }
```

---

## ۱۰) Stories (استوری)

### 10.1 Schema

#### Story Schema
```javascript
{
  _id: ObjectId,
  user: ObjectId,              // ref: User
  mediaUrl: String,
  mediaType: String,          // 'image' | 'video'
  caption: String,
  viewers: [{
    user: ObjectId,
    viewedAt: Date,
  }],
  expiresAt: Date,            // معمولا 24 ساعت بعد
  createdAt: Date,
}
```

### 10.2 گرفتن لیست استوری‌ها

**GET `/stories`**

> استوری‌های فعال کاربرانی که کاربر فعلی آن‌ها را دنبال می‌کند یا مخاطبینش هستند.

**Response 200:**
```json
[
  {
    "user": { "_id": "...", "name": "Alice", "avatar": "..." },
    "stories": [
      {
        "_id": "...",
        "mediaUrl": "/uploads/stories/xxx.jpg",
        "mediaType": "image",
        "caption": "...",
        "viewers": [...],
        "createdAt": "..."
      }
    ]
  }
]
```

### 10.3 استوری‌های خودم

**GET `/stories/me`**

### 10.4 ساخت استوری

**POST `/stories`**

**Content-Type: multipart/form-data**

**FormData:**
- `file`: فایل image یا video
- `caption`: (اختیاری) متن

> 🔌 `new_story` emit شود به فالوورها/مخاطبین.

### 10.5 ثبت بازدید

**POST `/stories/:id/view`**

> کاربر فعلی به `viewers` اضافه می‌شود.

### 10.6 حذف استوری

**DELETE `/stories/:id`**

---

## ۱۱) Gifts (کادوها)

### 11.1 Schema

#### Gift Schema
```javascript
{
  _id: ObjectId,
  name: String,
  description: String,
  imageUrl: String,
  animationUrl: String,
  displayValue: String,
  isTransferable: Boolean,
  // فیلدهای ادمین
  sender: ObjectId,            // اگر توسط ادمین داده شده
  senderName: String,
  receiver: ObjectId,         // ref: User
  isAnonymous: Boolean,
  caption: String,             // یادداشت فرستنده
  createdAt: Date,
}
```

### 11.2 گرفتن کادوهای کاربر

**GET `/gifts/user/:userId`**

**Response 200:**
```json
[
  {
    "_id": "...",
    "name": "Birthday Gift",
    "imageUrl": "...",
    "animationUrl": "...",
    "displayValue": "Premium",
    "isTransferable": true,
    "sender": { "_id": "...", "name": "Alice" },
    "senderName": "Alice",
    "isAnonymous": false,
    "caption": "Happy birthday!",
    "createdAt": "..."
  }
]
```

### 11.3 جزئیات یک کادو

**GET `/gifts/:giftId`**

### 11.4 انتقال کادو

**POST `/gifts/transfer`**

**Body:**
```json
{
  "giftId": "60d5ec49c656d02958172090",
  "targetUserQuery": "60d5ec49c656d02958172055",   // userId یا username یا phone
  "isAnonymous": false,
  "caption": "For you!"
}
```

**Response 200:**
```json
{ "message": "Gift transferred successfully" }
```

> 🔌 `gift_received` به کاربر گیرنده emit شود.

---

## ۱۲) GIFs & Stickers

### 12.1 GIF Schema
```javascript
{
  _id: ObjectId,
  user: ObjectId,              // owner (برای GIFهای شخصی)
  url: String,
  sourceUrl: String,
  width: Number,
  height: Number,
  isGlobal: Boolean,           // برای GIFهای trending
  createdAt: Date,
}
```

### 12.2 Sticker Schema
```javascript
{
  _id: ObjectId,
  name: String,
  url: String,
  pack: String,                // نام پک استیکر
  isGlobal: Boolean,
}
```

### 12.3 گرفتن GIFهای کاربر

**GET `/gifs`**

### 12.4 گرفتن GIFهای trending

**GET `/gifs/global`**

### 12.5 ذخیره GIF

**POST `/gifs/save`**

**Body:**
```json
{ "messageId": "60d5ec49c656d02958172100" }
```

> از روی یک پیام، GIF را extract و در کتابخانه‌ی کاربر ذخیره می‌کند.

### 12.6 حذف GIF

**DELETE `/gifs/:gifId`**

### 12.7 گرفتن استیکرها

**GET `/stickers`**

---

## ۱۳) تماس‌های صوتی و تصویری

> ⚠️ این بخش حیاتی است و پیاده‌سازی صحیح آن نیاز به دقت دارد. دو نوع تماس داریم:
> 1. **تماس P2P (خصوصی):** بین دو کاربر با استفاده از WebRTC signaling از طریق Socket.IO
> 2. **تماس گروهی صوتی:** مدیریت‌شده توسط REST + Socket.IO

### 13.1 تماس P2P (Video/Voice Call بین دو نفر)

#### 13.1.1 جریان کامل تماس

```
A (Caller)                      Server (Socket.IO)                  B (Callee)
   |                                   |                               |
   |--- callUser { userToCall: B, signalData: offer, from: A, name, isVideo } -->|
   |                                   |--- callUser { from: A, signal: offer, name, isVideo } -->|
   |                                   |                               |
   |                                   |<-- answerCall { to: A, signal: answer } ---|
   |<-- callAccepted { signal: answer } ---|                               |
   |                                   |                               |
   |--- endCall { to: B } ----------->|--- callEnded { } ------------>| (call terminated)
```

#### 13.1.2 Socket Events برای تماس P2P

| Event | Direction | Payload | توضیح |
|-------|-----------|---------|-------|
| `callUser` | Client → Server | `{ userToCall, signalData, from, name, isVideo }` | شروع تماس - سرور باید به `userToCall` منتقل کند |
| `callUser` | Server → Client | `{ from, signal, name, isVideo }` | دریافت تماس ورودی |
| `answerCall` | Client → Server | `{ to, signal }` | پاسخ به تماس |
| `callAccepted` | Server → Client | `{ signal }` | تماس پذیرفته شد |
| `endCall` | Client → Server | `{ to }` | پایان تماس |
| `callEnded` | Server → Client | `{ from }` | تماس پایان یافت |

#### 13.1.3 پیاده‌سازی سرور Socket.IO برای P2P

```javascript
// in socket.io connection handler
socket.on('callUser', ({ userToCall, signalData, from, name, isVideo }) => {
  // find target socket by userId
  const targetSocket = getUserSocket(userToCall);
  if (targetSocket) {
    io.to(targetSocket.id).emit('callUser', {
      from,
      signal: signalData,
      name,
      isVideo,
    });
  }
});

socket.on('answerCall', ({ to, signal }) => {
  const targetSocket = getUserSocket(to);
  if (targetSocket) {
    io.to(targetSocket.id).emit('callAccepted', { signal });
  }
});

socket.on('endCall', ({ to }) => {
  const targetSocket = getUserSocket(to);
  if (targetSocket) {
    io.to(targetSocket.id).emit('callEnded', { from: socket.userId });
  }
});
```

> 💡 **نکته مهم:** در کد فعلی کلاینت، `signalData` به‌صورت `{ type: 'dummy_webrtc_offer' }` است. این یعنی **WebRTC واقعی پیاده‌سازی نشده**. برای production باید کلاینت هم آپدیت شود تا SDP و ICE واقعی بفرستد.

### 13.2 Voice Call گروهی (Group Voice Call)

#### 13.2.1 REST Endpoints

| متد | مسیر | کاربرد |
|-----|------|--------|
| `POST` | `/conversations/:id/voice-call/start` | شروع تماس گروهی (فقط ادمین/owner) |
| `POST` | `/conversations/:id/voice-call/stop` | توقف تماس گروهی (فقط ادمین/owner) |
| `POST` | `/conversations/:id/voice-call/join` | پیوستن به تماس فعال |
| `POST` | `/conversations/:id/voice-call/leave` | ترک تماس |
| `POST` | `/conversations/:id/voice-call/toggle-mute` | تغییر وضعیت میکروفون |
| `POST` | `/conversations/:id/voice-call/admin-control` | کنترل ادمین روی کاربر دیگر |

**Response همگی:**
```json
// آبجکت Conversation با وضعیت تماس:
{
  "_id": "...",
  "voiceCall": {
    "active": true,
    "startedBy": "userId",
    "startedAt": "2023-...",
    "participants": [
      { "userId": "...", "socketId": "...", "isMuted": false, "name": "Alice" }
    ]
  }
}
```

#### 13.2.2 Socket Events برای Voice Call گروهی

| Event | Direction | Payload | توضیح |
|-------|-----------|---------|-------|
| `join_group_call` | C→S | `{ conversationId, userId, name, profileColor }` | عضویت در تماس گروهی |
| `existing_group_call_users` | S→C | `[{ socketId, userId, name, profileColor }]` | لیست کاربران موجود (به‌محض join) |
| `group_call_user_joined` | S→C | `{ socketId, userId, name, profileColor }` | کاربر جدید پیوست |
| `leave_group_call` | C→S | — | خروج از تماس |
| `group_call_user_left` | S→C | `{ socketId, userId }` | کاربر ترک کرد |
| `group_call_signal` | C→S | `{ targetSocketId, data: { sdp? or candidate? } }` | ارسال WebRTC signal به peer |
| `group_call_signal` | S→C | `{ fromSocketId, data: { sdp? or candidate? } }` | دریافت signal |
| `group_call_toggle_mute` | C→S | `{ conversationId, isMuted }` | تغییر میوت |
| `group_call_user_mute_changed` | S→C | `{ socketId, isMuted }` | تغییر میوت دیگران |
| `voice_call_state_changed` | S→C | `{ conversationId, active, startedBy, ... }` | وضعیت کلی تماس تغییر کرد |
| `voice_call_admin_handled` | S→C | `{ conversationId, targetUserId, action }` | ادمین کاری روی کاربر انجام داد |

#### 13.2.3 جریان کامل Voice Call گروهی

```
A (Admin)            Server              B (Member)           C (Member)
   |                   |                     |                    |
   |-- REST: POST /voice-call/start -->|    |                    |
   |<-- { voiceCall: { active: true } }    |                    |
   |                   |--- voice_call_state_changed { active: true } --> B, C
   |                   |                     |                    |
   |-- socket: join_group_call { conversationId, userId: A } -->|  |
   |<-- existing_group_call_users [] (empty) --|                  |  |
   |                   |                     |                    |
   |                   |<-- join_group_call { conversationId, userId: B } ---|
   |<-- group_call_user_joined { socketId: B } ---|                  |  |
   |-- group_call_signal { targetSocketId: B, data: { sdp: offer } } -->|
   |                   |--- group_call_signal { fromSocketId: A, data: { sdp: offer } } -->|
   |                   |<-- group_call_signal { targetSocketId: A, data: { sdp: answer } } ---|
   |<-- group_call_signal { fromSocketId: B, data: { sdp: answer } } ---|
   |                   |                     |                    |
   |--- group_call_signal { targetSocketId: B, data: { candidate } } -->|  (ICE candidates exchange)
   |<-- group_call_signal { fromSocketId: B, data: { candidate } } ---|
   |                   |                     |                    |
   |--- group_call_toggle_mute { isMuted: true } -->|              |  |
   |<-- group_call_user_mute_changed { socketId: A, isMuted: true } --|  |
   |                   |--- group_call_user_mute_changed --> B, C   |  |
```

#### 13.2.4 پیاده‌سازی سرور (نمونه)

```javascript
// Track active group calls per conversation
const groupCalls = new Map(); // conversationId -> Set of socket ids

socket.on('join_group_call', ({ conversationId, userId, name, profileColor }) => {
  socket.join(`call_${conversationId}`);
  
  if (!groupCalls.has(conversationId)) {
    groupCalls.set(conversationId, new Map());
  }
  
  const participants = groupCalls.get(conversationId);
  const existingUsers = Array.from(participants.values());
  
  // Add this user
  participants.set(socket.id, { socketId: socket.id, userId, name, profileColor });
  
  // Notify existing users that a new user joined
  socket.to(`call_${conversationId}`).emit('group_call_user_joined', {
    socketId: socket.id,
    userId,
    name,
    profileColor,
  });
  
  // Send list of existing users to the new joiner
  socket.emit('existing_group_call_users', existingUsers);
});

socket.on('group_call_signal', ({ targetSocketId, data }) => {
  io.to(targetSocketId).emit('group_call_signal', {
    fromSocketId: socket.id,
    data,
  });
});

socket.on('group_call_toggle_mute', ({ conversationId, isMuted }) => {
  socket.to(`call_${conversationId}`).emit('group_call_user_mute_changed', {
    socketId: socket.id,
    isMuted,
  });
});

socket.on('leave_group_call', () => {
  for (const [convId, participants] of groupCalls.entries()) {
    if (participants.has(socket.id)) {
      participants.delete(socket.id);
      socket.to(`call_${convId}`).emit('group_call_user_left', {
        socketId: socket.id,
        userId: socket.userId,
      });
      if (participants.size === 0) {
        groupCalls.delete(convId);
      }
      break;
    }
  }
});

// Handle disconnect
socket.on('disconnect', () => {
  // Same as leave_group_call
});
```

#### 13.2.5 REST Endpoint: Admin Control

**POST `/conversations/:id/voice-call/admin-control`**

**Body:**
```json
{
  "targetUserId": "60d5ec49c656d02958172055",
  "action": "mute"   // 'mute' | 'kick' | 'unmute'
}
```

> سرور باید این کاربر را mute کند (با REST) و سپس `voice_call_admin_handled` به socket او emit کند تا کلاینت محلی خودش را هم mute کند.

### 13.3 گرفتن ICE Servers

**GET `/webrtc/ice-servers`**

**Response 200:**
```json
[
  { "urls": "stun:stun.l.google.com:19302" },
  {
    "urls": "turn:your.turn.server:3478",
    "username": "user",
    "credential": "pass"
  }
]
```

> 💡 **توصیه:** از یک TURN server استفاده کنید (مثل coturn) تا کاربرانی که پشت NAT هستند هم بتوانند تماس برقرار کنند.

---

## ۱۴) Socket.IO — رویدادهای کلاینت ← سرور (Emit)

| Event | پارامتر | توضیح |
|-------|---------|-------|
| `join_conversation` | `conversationId` | ورود به روم گفتگو (برای دریافت پیام‌ها) |
| `leave_conversation` | `conversationId` | خروج از روم |
| `send_message` | `{ conversationId, senderId?, text, postShareId?, postSharePreview? }` | ارسال پیام از طریق socket (برای share post) |
| `callUser` | `{ userToCall, signalData, from, name, isVideo }` | شروع تماس P2P |
| `answerCall` | `{ to, signal }` | پاسخ به تماس |
| `endCall` | `{ to }` | پایان تماس |
| `join_group_call` | `{ conversationId, userId, name, profileColor }` | عضویت در تماس گروهی |
| `leave_group_call` | — | خروج از تماس گروهی |
| `group_call_signal` | `{ targetSocketId, data }` | سیگنال WebRTC برای تماس گروهی |
| `group_call_toggle_mute` | `{ conversationId, isMuted }` | تغییر وضعیت mute |
| `invite_game_direct` | `{ gameType, opponentId }` | دعوت به بازی (در scope نیست) |
| `join_game` | `{ gameId }` | (در scope نیست) |
| `send_game_move` | `{ gameId, boardState }` | (در scope نیست) |
| `abandon_game` | `{ gameId }` | (در scope نیست) |
| `join_matchmaking` | `{ gameType }` | (در scope نیست) |
| `leave_matchmaking` | `{ gameType }` | (در scope نیست) |

---

## ۱۵) Socket.IO — رویدادهای سرور ← کلاینت (On)

### 15.1 رویدادهای چت و پیام
| Event | Payload | توضیح |
|-------|---------|-------|
| `new_message` | `MessageObject` | پیام جدید دریافت شد |
| `message_deleted` | `{ messageId, conversationId }` | پیام حذف شد |
| `message_updated` | `MessageObject` | پیام ویرایش شد |
| `message_reaction` | `{ messageId, reactions }` | واکنش به پیام |
| `message_pinned` | `{ conversationId, messageId }` | پیام پین شد |
| `message_unpinned` | `{ conversationId, messageId }` | آنپین |
| `pins_updated` | `{ conversationId, pinnedMessages }` | آپدیت کامل پین‌ها |

### 15.2 رویدادهای Conversation
| Event | Payload |
|-------|---------|
| `conversation_updated` | `ConversationObject` |
| `conversation_deleted` | `{ conversationId }` |
| `member_kicked` | `{ conversationId, userId }` |
| `member_banned` | `{ conversationId, userId }` |

### 15.3 رویدادهای Presence
| Event | Payload |
|-------|---------|
| `user_status_change` | `{ userId, status: 'online' \| 'offline', lastSeen }` |

### 15.4 رویدادهای تماس P2P
| Event | Payload |
|-------|---------|
| `callUser` | `{ from, signal, name, isVideo }` |
| `callAccepted` | `{ signal }` |
| `callEnded` | `{ from }` |

### 15.5 رویدادهای Voice Call گروهی
| Event | Payload |
|-------|---------|
| `voice_call_state_changed` | `{ conversationId, active, startedBy }` |
| `voice_call_admin_handled` | `{ conversationId, targetUserId, action }` |
| `existing_group_call_users` | `[{ socketId, userId, name, profileColor }]` |
| `group_call_user_joined` | `{ socketId, userId, name, profileColor }` |
| `group_call_user_left` | `{ socketId, userId }` |
| `group_call_user_mute_changed` | `{ socketId, isMuted }` |
| `group_call_signal` | `{ fromSocketId, data }` |

### 15.6 رویدادهای Gifts / Stories / Comments
| Event | Payload |
|-------|---------|
| `gift_received` | `GiftObject` |
| `new_story` | `StoryObject` |
| `comment_created` | `CommentObject` |
| `comment_deleted` | `{ commentId, messageId }` |
| `comment_updated` | `CommentObject` |

### 15.7 رویدادهای Discussion Group / Join Requests
| Event | Payload |
|-------|---------|
| `discussion_group_linked` | `{ channelId, discussionGroupId }` |
| `discussion_group_unlinked` | `{ channelId }` |
| `join_request_created` | `{ conversationId, request }` |
| `join_request_approved` | `{ conversationId, userId }` |
| `join_request_declined` | `{ conversationId, userId }` |
| `join_request_count_updated` | `{ conversationId, count }` |

### 15.8 رویدادهای Channel Settings
| Event | Payload |
|-------|---------|
| `protected_content_updated` | `{ conversationId, protectedContent }` |
| `channel_signature_updated` | `{ conversationId, showSignatures }` |

---

## ۱۶) تبلیغات و متفرقه

### 16.1 GET `/ads/public`

> تبلیغات عمومی برای نمایش بنر در چت‌لیست.

**Response 200:**
```json
[
  {
    "_id": "...",
    "title": "...",
    "imageUrl": "/uploads/ads/banner.jpg",
    "targetUrl": "https://...",
    "impressions": 1234,
    "clicks": 56
  }
]
```

---

## ۱۷) ساختار پروژه Express پیشنهادی

```
backend/
├── src/
│   ├── config/
│   │   ├── database.js          # MongoDB connection
│   │   ├── socket.js            # Socket.IO setup
│   │   └── multer.js           # File upload config
│   ├── models/
│   │   ├── User.js
│   │   ├── Session.js
│   │   ├── Conversation.js
│   │   ├── Message.js
│   │   ├── Story.js
│   │   ├── Gift.js
│   │   ├── Gif.js
│   │   ├── Sticker.js
│   │   ├── Comment.js
│   │   ├── JoinRequest.js
│   │   ├── AdminLog.js
│   │   └── Report.js
│   ├── middleware/
│   │   ├── auth.js              # JWT verification
│   │   ├── upload.js           # Multer middleware
│   │   ├── rateLimit.js
│   │   └── errorHandler.js
│   ├── routes/
│   │   ├── auth.js
│   │   ├── profile.js
│   │   ├── conversations.js
│   │   ├── messages.js
│   │   ├── stories.js
│   │   ├── gifts.js
│   │   ├── gifs.js
│   │   ├── stickers.js
│   │   ├── webrtc.js
│   │   └── ads.js
│   ├── controllers/
│   │   ├── authController.js
│   │   ├── profileController.js
│   │   ├── conversationController.js
│   │   ├── messageController.js
│   │   ├── storyController.js
│   │   ├── giftController.js
│   │   ├── voiceCallController.js
│   │   └── ...
│   ├── services/
│   │   ├── otpService.js       # SMS provider (Kavenegar, etc.)
│   │   ├── pushService.js      # Push notifications
│   │   └── turnService.js      # TURN server management
│   ├── socket/
│   │   ├── index.js            # Socket.IO main
│   │   ├── chatHandler.js      # new_message, message_deleted, ...
│   │   ├── callHandler.js      # callUser, answerCall, endCall
│   │   ├── groupCallHandler.js # join_group_call, signals
│   │   └── presenceHandler.js  # user_status_change
│   ├── utils/
│   │   ├── jwt.js
│   │   ├── upload.js
│   │   └── validators.js
│   └── app.js                  # Express app entry
├── package.json
└── .env
```

---

## ۱۸) نکات کلیدی پیاده‌سازی

### 18.1 احراز هویت و امنیت
- **JWT Token:** با `jsonwebtoken` بسازید. مدت اعتبار طولانی (مثلاً 30 روز) چون کلاینت آن را در AsyncStorage ذخیره می‌کند.
- **Rate Limiting:** روی `/auth/send-otp` حتماً rate limit بگذارید (مثلاً 1 درخواست در 60 ثانیه).
- **OTP Storage:** کد OTP را در Redis یا MongoDB با TTL ذخیره کنید.

### 18.2 آپلود فایل
- **مسیر ذخیره:** `/uploads/avatars/`, `/uploads/groups/`, `/uploads/messages/`, `/uploads/stories/`, `/uploads/posts/`
- **URL ساختار:** همیشه relative path برگردانید (مثل `/uploads/avatars/xyz.jpg`) تا کلاینت با `BASE_URL + path` استفاده کند.
- **Multer Config:** 
  ```javascript
  const storage = multer.diskStorage({
    destination: (req, file, cb) => {
      const dir = `uploads/${req.uploadType || 'misc'}/`;
      cb(null, dir);
    },
    filename: (req, file, cb) => {
      cb(null, `${Date.now()}-${file.originalname}`);
    }
  });
  ```

### 18.3 Socket.IO
- **Room Join:** هنگام `join_conversation`، socket را به room `conversation_<id>` اضافه کنید.
- **Broadcasting:** برای emit به همه‌ی اعضای conversation: `io.to('conversation_<id>').emit(...)` به‌جز فرستنده: `socket.to('conversation_<id>').emit(...)`
- **User Status:** نگه‌دارید Map از userId → Set of socketIds. وقتی خالی شد، `user_status_change` با `offline` emit کنید.
- **Reconnection:** در صورت قطع، Socket.IO خودش reconnect می‌کند. ولی کلاینت باید دوباره `join_conversation` را emit کند.

### 18.4 WebRTC و TURN
- **STUN gratuito:** `stun:stun.l.google.com:19302`
- **TURN server:** پیشنهاد `coturn` روی VPS خودتان.
- **Coturn config نمونه:**
  ```
  lt-cred-mech
  user=myuser:mypassword
  realm=mydomain.com
  listening-ip=MY_IP
  external-ip=MY_PUBLIC_IP
  listening-port=3478
  relay-ip=MY_IP
  min-port=49152
  max-port=65535
  ```

### 18.5 نکات MongoDB
- **Indexes:** 
  - `User.phone` (unique)
  - `User.username` (unique, sparse)
  - `Message.conversationId` + `Message.createdAt`
  - `Conversation.participants.user`
  - `Story.user` + `Story.expiresAt` (TTL index)
- **TTL Index برای Story:** 
  ```javascript
  storySchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 });
  ```

### 18.6 نکات Push Notification
- **Expo Push Tokens:** در User schema ذخیره کنید: `pushTokens: [{ platform, token }]`
- **NotificationPayload:**
  ```javascript
  {
    title: message.sender?.name || 'New Message',
    body: message.type === 'text' ? message.text : `Sent a ${message.type}`,
    data: { conversationId },
  }
  ```
- **isSilent:** اگر `message.isSilent === true`، نوتیفیکیشن نفرستید.

### 18.7 رویدادهای بحرانی برای emit
این رویدادها باید حتماً emit شوند وگرنه کلاینت درست کار نمی‌کند:

| رویداد | موقع emit |
|--------|----------|
| `new_message` | بعد از ذخیره پیام (REST یا Socket) |
| `message_deleted` | بعد از حذف |
| `message_updated` | بعد از ویرایش |
| `conversation_updated` | بعد از هر تغییری در conversation (name, avatar, participants, ...) |
| `conversation_deleted` | بعد از حذف |
| `join_request_count_updated` | بعد از ایجاد/تأیید/رد هر درخواست |
| `voice_call_state_changed` | بعد از start/stop تماس گروهی |

---

## ۱۹) میان‌برهای سند

| # | بخش | مسیرها |
|---|------|--------|
| 1 | احراز هویت | `/auth/*` |
| 2 | پروفایل | `PUT /auth/profile`, `GET /users/frames` |
| 3 | گفتگوها | `/conversations/*` |
| 4 | جستجو و Invite | `/conversations/discover/*`, `/conversations/invite/*` |
| 5 | عضویت و درخواست‌ها | `/conversations/:id/join`, `/conversations/:id/request-*`, ... |
| 6 | مدیریت اعضا | `/conversations/:id/ban`, `/unban`, `/restrict`, ... |
| 7 | گروه بحث | `/conversations/:id/link-discussion`, ... |
| 8 | پیام‌ها | `/messages/*` |
| 9 | کامنت‌های کانال | `/conversations/:channelId/messages/:postId/comments` |
| 10 | Stories | `/stories/*` |
| 11 | Gifts | `/gifts/*` |
| 12 | GIFs & Stickers | `/gifs/*`, `/stickers` |
| 13 | تماس‌ها | `/conversations/:id/voice-call/*`, `/webrtc/ice-servers` |
| 14 | Socket Emit | 13 رویداد |
| 15 | Socket On | 6 گروه رویداد |
| 16 | تبلیغات | `/ads/public` |

---

## ۲۰) گام‌های بعدی پیاده‌سازی

### مرحله 1: Setup پایه
- [ ] Express + MongoDB + Socket.IO setup
- [ ] JWT auth middleware
- [ ] Multer upload middleware

### مرحله 2: Auth
- [ ] User و Session schema
- [ ] send-otp + verify-otp (با Kavenegar یا Twilio)
- [ ] /auth/me + /auth/profile

### مرحله 3: Conversations
- [ ] Conversation schema
- [ ] ساخت private/group/channel
- [ ] لیست گفتگوها
- [ ] Socket.IO room management

### مرحله 4: Messages
- [ ] Message schema
- [ ] ارسال متن + مدیا
- [ ] ویرایش/حذف/react/forward
- [ ] Socket event `new_message`

### مرحله 5: Voice Calls
- [ ] WebRTC signaling برای P2P
- [ ] Group voice call با mesh topology (یا SFU برای scale)
- [ ] TURN server setup

### مرحله 6: Features تکمیلی
- [ ] Stories
- [ ] Gifts
- [ ] GIFs & Stickers
- [ ] Comments (کانال)
- [ ] Admin logs

### مرحله 7: Production
- [ ] Push notifications (Expo)
- [ ] Rate limiting
- [ ] Logging (Winston)
- [ ] Monitoring (PM2 / Docker)
- [ ] SSL/HTTPS
- [ ] MongoDB backup

---

> این سند بر اساس تحلیل کامل کد فرانت‌اند نوشته شده است. هر endpoint که کلاینت استفاده می‌کند در اینجا مستند شده و باید در بک‌اند پیاده‌سازی شود. در صورت بروز مشکل در پیاده‌سازی، می‌توانید به این سند مراجعه کنید.
