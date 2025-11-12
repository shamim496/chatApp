# 💬 Java Chat Application

একটি simple কিন্তু সুন্দর real-time messaging application যা Java Swing এবং Socket Programming ব্যবহার করে তৈরি।

## 🎯 Features

- ✅ **Real-time messaging** - Socket programming দিয়ে instant message delivery
- 👥 **Multiple users** - একসাথে অনেক user chat করতে পারবে
- 🎨 **Beautiful UI** - Modern Swing interface with custom styling
- ⏰ **Timestamp** - প্রতিটি message এ সময় দেখাবে
- 🔔 **Join/Leave notifications** - User join/leave করলে notification আসবে
- 💻 **Server-Client Architecture** - Centralized message relay system

## 📁 Project Structure

```
chatApp/
├── Server.java    # Server application (message relay)
├── Client.java    # Client application (GUI chat client)
└── README.md      # Documentation
```

## 🚀 কীভাবে Run করবেন

### Prerequisites
- Java Development Kit (JDK) 8 বা তার উপরে installed থাকতে হবে
- Command prompt/Terminal access

### Step 1: Files Compile করা

প্রথমে chat application folder এ যান:
```bash
cd "c:\Users\Shamim Hasan\Downloads\chatApp"
```

Server এবং Client উভয় file compile করুন:
```bash
javac Server.java
javac Client.java
```

### Step 2: Server চালু করা

প্রথমে server start করতে হবে। একটি নতুন terminal/command prompt খুলুন এবং run করুন:

```bash
java Server
```

আপনি এই message দেখতে পাবেন:
```
🚀 Chat Server শুরু হচ্ছে...
📡 Port: 5000
⏳ Client দের জন্য অপেক্ষা করছি...
```

✅ Server এখন running এবং client connection এর জন্য অপেক্ষা করছে!

### Step 3: Client চালু করা (Multiple Users)

এখন আলাদা আলাদা terminal/command prompt এ multiple client চালান:

**প্রথম User এর জন্য:**
```bash
java Client
```

**দ্বিতীয় User এর জন্য:**
নতুন terminal খুলে:
```bash
java Client
```

**তৃতীয় User এর জন্য:**
আরেকটি নতুন terminal খুলে:
```bash
java Client
```

প্রতিটি client window এ আপনার নাম দিয়ে join করুন!

## 🎮 কীভাবে ব্যবহার করবেন

1. **Login**: Client open হলে একটি dialog box আসবে যেখানে আপনার নাম লিখবেন
2. **Message পাঠানো**: নিচের text field এ message লিখে "Send" button এ click করুন অথবা Enter press করুন
3. **Message দেখা**: চ্যাট area তে সব user এর message timestamp সহ দেখা যাবে
4. **Exit**: Window close করলে automatically server থেকে disconnect হয়ে যাবে

## 💡 Code Structure বুঝা

### Server.java
- **Main Server**: Port 5000 তে listen করে এবং client connection accept করে
- **ClientHandler**: প্রতিটি client এর জন্য আলাদা thread যা message receive এবং broadcast করে
- **Broadcast Method**: সব connected client দের কাছে message relay করে

### Client.java
- **GUI Setup**: Swing components দিয়ে beautiful chat interface তৈরি
- **Network Connection**: Server এর সাথে socket connection establish করে
- **Message Sender**: User input নিয়ে server এ পাঠায়
- **Message Receiver**: আলাদা thread এ server থেকে message receive করে এবং UI তে দেখায়

## 🎨 UI Features

- **Modern Design**: Steel Blue color scheme with white background
- **Smooth Scrolling**: Automatic scroll to latest message
- **Hover Effects**: Button hover করলে color change হয়
- **Responsive Layout**: BorderLayout ব্যবহার করে flexible UI
- **Custom Fonts**: Segoe UI font family ব্যবহার করা হয়েছে
- **Emojis**: Message এ emoji support আছে

## 🔧 Technical Details

- **Language**: Java
- **GUI Framework**: Swing (javax.swing)
- **Networking**: Java Socket Programming (java.net)
- **Threading**: Multi-threaded architecture
- **Port**: 5000 (customizable)
- **Protocol**: TCP/IP

## 📝 Code Comments

সব code এ বাংলা comment দেওয়া আছে যাতে সহজে বুঝতে পারেন:
- প্রতিটি method এর কাজ
- Network connection কীভাবে কাজ করে
- UI components কীভাবে setup করা হয়েছে
- Thread management

## 🐛 Troubleshooting

### "Server এর সাথে সংযোগ করতে সমস্যা হয়েছে"
- নিশ্চিত করুন Server চালু আছে
- Check করুন port 5000 ব্যবহার করছে কিনা অন্য কোন application

### Multiple clients connect হচ্ছে না
- Server properly চালু আছে কিনা check করুন
- Firewall বা antivirus block করছে কিনা দেখুন

### Messages দেখা যাচ্ছে না
- Server terminal এ error message আছে কিনা দেখুন
- Client properly connect হয়েছে কিনা verify করুন

## 🎯 Future Enhancements

কিছু idea যা add করা যেতে পারে:
- Private messaging (specific user কে message)
- File sharing capability
- Message history save করা
- User list দেখানো
- Emoji picker
- Dark mode
- Audio notification

## 👨‍💻 Developer Notes

এই application educational purpose এর জন্য তৈরি করা হয়েছে Java networking এবং GUI development শেখার জন্য।

---

**Enjoy chatting! 💬✨**
