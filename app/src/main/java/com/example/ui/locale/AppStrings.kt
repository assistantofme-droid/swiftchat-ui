package com.example.ui.locale

import androidx.compose.runtime.compositionLocalOf

data class AppStrings(
    val isPersian: Boolean = false,

    // Bottom Navigation
    val navChats: String = if (isPersian) "پیام‌ها" else "Chats",
    val navContacts: String = if (isPersian) "مخاطبین" else "Contacts",
    val navSettings: String = if (isPersian) "تنظیمات" else "Settings",
    val navProfile: String = if (isPersian) "پروفایل" else "Profile",

    // Chat List
    val allChats: String = if (isPersian) "همه" else "All Chats",
    val privateChats: String = if (isPersian) "شخصی" else "Private",
    val groups: String = if (isPersian) "گروه‌ها" else "Groups",
    val channels: String = if (isPersian) "کانال‌ها" else "Channels",
    val searchPlaceholder: String = if (isPersian) "جستجو..." else "Search",
    val noChatsYet: String = if (isPersian) "هنوز پیامی وجود ندارد" else "No chats yet",
    val startMessaging: String = if (isPersian) "شروع پیام جدید" else "Start Messaging",
    val savedMessages: String = if (isPersian) "پیام‌های ذخیره شده" else "Saved Messages",
    val newGroup: String = if (isPersian) "گروه جدید" else "New Group",
    val newChannel: String = if (isPersian) "کانال جدید" else "New Channel",
    val connecting: String = if (isPersian) "در حال اتصال..." else "Connecting...",

    // Contacts
    val contactsTitle: String = if (isPersian) "مخاطبین" else "Contacts",
    val searchContacts: String = if (isPersian) "جستجوی مخاطبین..." else "Search contacts...",
    val addContact: String = if (isPersian) "افزودن مخاطب" else "Add Contact",
    val allowContacts: String = if (isPersian) "اجازه دسترسی به مخاطبین" else "Allow Contacts",
    val noContactsFound: String = if (isPersian) "مخاطبی یافت نشد" else "No contacts found",

    // Settings
    val settingsTitle: String = if (isPersian) "تنظیمات" else "Settings",
    val account: String = if (isPersian) "حساب کاربری" else "Account",
    val chatSettings: String = if (isPersian) "تنظیمات گفتگو" else "Chat Settings",
    val privacySecurity: String = if (isPersian) "حریم خصوصی و امنیت" else "Privacy & Security",
    val dataStorage: String = if (isPersian) "داده‌ها و حافظه" else "Data and Storage",
    val chatFolders: String = if (isPersian) "پوشه‌های گفتگو" else "Chat Folders",
    val devices: String = if (isPersian) "دستگاه‌ها" else "Devices",
    val powerSaving: String = if (isPersian) "صرفه‌جویی باتری" else "Power Saving",
    val language: String = if (isPersian) "زبان" else "Language",
    val askQuestion: String = if (isPersian) "پشتیبانی و سوالات" else "Ask a Question",
    val logOut: String = if (isPersian) "خروج از حساب" else "Log Out",
    val helpSection: String = if (isPersian) "راهنما و پشتیبانی" else "Help",

    // Chat Details
    val messageHint: String = if (isPersian) "پیام..." else "Message...",
    val online: String = if (isPersian) "آنلاین" else "online",
    val offline: String = if (isPersian) "آخرین بازدید به تازگی" else "last seen recently",
    val typing: String = if (isPersian) "در حال نوشتن..." else "typing..."
)

val LocalAppStrings = compositionLocalOf { AppStrings() }
