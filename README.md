<p align="center">
  <img src="assets/banner.png" alt="Smart E-Voting System Banner" width="100%">
</p>

<h1 align="center">🗳️ Smart E-Voting System</h1>

<p align="center">
Secure Android Voting Application using <b>NFC Verification</b> and <b>Face Authentication</b>
</p>

<p align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge\&logo=openjdk)
![Android](https://img.shields.io/badge/Android-Studio-green?style=for-the-badge\&logo=android)
![Firebase](https://img.shields.io/badge/Firebase-Backend-yellow?style=for-the-badge\&logo=firebase)
![Google ML Kit](https://img.shields.io/badge/Google-ML%20Kit-blue?style=for-the-badge\&logo=google)
![GitHub stars](https://img.shields.io/github/stars/MithranIlayaraja/E-Voting-System?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/MithranIlayaraja/E-Voting-System?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-success?style=for-the-badge)

</p>

---

# 📖 About

The **Smart E-Voting System** is an Android application designed to provide a secure, reliable, and user-friendly electronic voting experience.

The application combines **Near Field Communication (NFC)** technology with **AI-powered facial authentication** to verify voter identity before allowing a vote to be cast.

Each voter is verified through an NFC card and facial recognition. After successful verification, the vote is securely recorded and the voter is marked as having voted, preventing duplicate voting.

This project demonstrates the integration of Android development, Firebase cloud services, NFC communication, and biometric authentication into a real-world software solution.

---

# ✨ Features

* 🔐 Secure Booth Administrator Login
* 📱 NFC Card-Based Voter Identification
* 👤 Face Authentication
* ☁️ Firebase Realtime Database
* 🗳️ Digital Vote Casting
* 🚫 Duplicate Vote Prevention
* ⚡ Real-Time Cloud Synchronization
* 📊 Candidate Management
* 📂 Secure Voter Database
* 🎯 Clean Material Design UI

---

# 🏗️ System Architecture

```text
                 ┌─────────────────┐
                 │ Booth Manager   │
                 └────────┬────────┘
                          │
                    Login Authentication
                          │
                          ▼
                ┌─────────────────────┐
                │ NFC Card Scanning   │
                └────────┬────────────┘
                         │
                         ▼
              Retrieve Voter Information
                         │
                         ▼
              Firebase Realtime Database
                         │
                         ▼
              Face Authentication (ML Kit)
                         │
              ┌──────────┴──────────┐
              │                     │
          Verified             Verification Failed
              │                     │
              ▼                     ▼
        Voting Screen          Access Denied
              │
              ▼
         Store Vote Securely
              │
              ▼
       Update Candidate Count
              │
              ▼
      Mark Voter as Voted
```

---

# 🛠️ Technology Stack

| Category             | Technology                 |
| -------------------- | -------------------------- |
| Programming Language | Java                       |
| IDE                  | Android Studio             |
| Database             | Firebase Realtime Database |
| Storage              | Firebase Storage           |
| Authentication       | Google ML Kit              |
| Communication        | Android NFC API            |
| UI                   | XML                        |
| Version Control      | Git & GitHub               |

---

# 📂 Project Structure

```text
E-Voting-System
│
├── app
│   ├── java
│   │   ├── activities
│   │   ├── adapters
│   │   ├── authentication
│   │   ├── firebase
│   │   ├── models
│   │   ├── nfc
│   │   └── utils
│   │
│   ├── res
│   │   ├── layout
│   │   ├── drawable
│   │   ├── values
│   │   └── mipmap
│   │
│   └── AndroidManifest.xml
│
├── assets
├── README.md
└── LICENSE
```

---

# 🚀 Installation

```bash
git clone https://github.com/MithranIlayaraja/E-Voting-System.git
```

Open the project in Android Studio.

Connect your Firebase project.

Add your `google-services.json` file.

Enable NFC on a physical Android device.

Build and run the application.

---



# 🔒 Security Features

* NFC-based identity verification
* AI-assisted facial authentication
* One Person – One Vote enforcement
* Secure cloud database
* Duplicate vote prevention
* Authorized booth-only operation

---

# 📈 Future Enhancements

* Blockchain-backed vote storage
* Fingerprint authentication
* Aadhaar eKYC integration (where legally appropriate)
* Election analytics dashboard
* Multi-language support
* End-to-end encrypted communication
* Offline voting synchronization

---

# 🎓 Learning Outcomes

This project strengthened my practical knowledge in:

* Android Application Development
* Java Programming
* Firebase Integration
* NFC Communication
* Mobile Authentication
* Cloud Database Design
* Mobile Security
* Software Engineering Practices

---

# 👨‍💻 Developer

**Mithran Ilayaraja**

Computer Science Student

GitHub: https://github.com/MithranIlayaraja



---

# ⭐ Show Your Support

If you like this project,

⭐ Star the repository

🍴 Fork the repository

💬 Share your feedback

🤝 Contribute to future improvements

---

<p align="center">

Made with ❤️ using Java, Android Studio, Firebase & Google ML Kit

</p>
