---
# Javase: Firebase? More Like Java-Ease! 🚀

![Maven Central](https://img.shields.io/maven-central/v/io.github.init-io/javase.svg?label=Maven%20Central)
![GitHub Release](https://img.shields.io/github/v/release/init-io/Javase?label=release)
[![License](https://img.shields.io/github/license/init-io/Javase)](LICENSE)
![downloads](https://img.shields.io/badge/downloads-1k%2Fmonth-brightgreen)

**Version v1.3.5**
*Now with `appendJson()` and `pushJson()` to make Firebase less annoying and more delightful.*

---

### 📜 Introduction

Welcome to **Javase** — a Java library so smooth, it’ll make working with Firebase feel like a walk in the park. Firebase can be a maze of APIs and JSON responses, but fear not! Javase is here to give you simple, streamlined, and asynchronous solutions for Firebase Database and Firebase Storage.

Think of it as your over-caffeinated, always-on-call Firebase assistant who actually *does what it’s told*. 🙌

---

### 🚀 Features

#### Database Module:

* **CRUD without the Drama**: Perform `get`, `put`, `update`, and `remove` operations on Firebase Realtime Database. No sweat, no mess, just pure JSON magic.
* **JSON Appending**: Use `appendJson()` for auto-indexing like `0`, `1`, `2`... or `pushJson()` to use Firebase's built-in unique IDs.
* **Error Handling That Cares**: Get detailed error messages because "Unknown Error Occurred" isn't good enough.
* **Asynchronous Brilliance**: All tasks run on a separate thread, so your main thread can sip coffee in peace. ☕

#### Storage Module:

* **File Uploading (a.k.a. "Here, Take My Stuff!")**: Upload files to Firebase Storage with minimal effort.
* **Download URLs**: Retrieve public download URLs for your files faster than your WiFi can fail you.
* **Photo Listing**: Get a list of all your photos stored in Firebase. Finally, an easy way to inventory your cat pictures. 🐱
* **File Deletion**: Ruthlessly delete photos from Firebase Storage (because not every selfie is worth keeping).

---

### 🛠️ Installation

#### Maven:

```xml
<dependency>
    <groupId>io.github.initio</groupId>
    <artifactId>javase</artifactId>
    <version>1.3.5</version>
</dependency>
```

#### Gradle:

```groovy
implementation 'io.github.init-io:javase:1.3.5'
```

---

### 📦 How to Use

Here’s a quick peek at how ridiculously easy it is to make Firebase work for you with **Javase**.

#### 1. **Authentication Module**:

```java
Server server = new Server();
server.initialize(apiKey, authDomain, databaseUrl, storageBucket);
Authentication auth = new Authenticaton(server);

// SignUp
auth.signUp(email, password);

// SignIn
auth.signIn(email, password);

// Get idToken
auth.getIdToken(); // expires in 1 hour

// Refresh token
auth.refreshIdToken();

// Check if user is verified
auth.isVerified(auth.getIdToken());

// Verify user
auth.verifyUser(auth.getIdToken());

// Reset user password
auth.resetUser(email);

// Remove a user
auth.removeUser(auth.getIdToken());
```

#### 2. **Database Module**:

```java
Database database = new Database(server);

// Fetch data
String response = database.get("path/to/data", "id-token");

// Add single key-value pair
database.put("users/123", "name", "Java Enthusiast", "id-token");

// Update a value
database.update("users/123", "status", "awesome", "id-token");

// Delete a node
database.remove("users/123", "id-token");

// Put entire JSONObject
JSONObject json = new JSONObject();
json.put("title", "New Notification");
json.put("message", "You've got mail!");
database.putJson("notifications/123", json, "id-token");

// Append JSONObject at next index (0,1,2,...)
database.appendJson("notifications", json, "id-token");

// Push JSONObject with Firebase-style random key
database.pushJson("notifications", json, "id-token");
```

##### Reading pushed notifications:

```java
List<JSONObject> notifications = database.getJsonList("notifications", "id-token");
for (JSONObject notif : notifications) {
    System.out.println("Title: " + notif.optString("title"));
}
```

#### 3. **Storage Module**:

```java
Storage storage = new Storage(server, new Authenticate("id-token"));

// Upload a file
String uploadResponse = storage.uploadFile("/local/path/to/photo.jpg", "images/photo.jpg");

// Get a file URL
String fileUrl = storage.getFileUrl("images/photo.jpg");

// List all photos
String photos = storage.listPhotos("images");

// Delete a photo
String deletePhotoResponse = storage.deletePhoto("images/photo.jpg");
```

---

### 🐛 Error Handling

Whenever things go sideways (and let's face it, they will), **Javase** has your back with a `error()` method that provides detailed error messages.

```java
String result = database.get("non/existent/path", "id-token");
if (result == null) {
    System.out.println("Uh oh: " + database.error());
}
```

---

### 🤓 Why Choose Javase?

1. **Asynchronous Operations**: Multithreading without the headache.
2. **Simple API**: Minimal boilerplate. Maximum productivity.
3. **Readable Errors**: You'll know exactly why Firebase isn't cooperating.
4. **New JSON Helpers**: `appendJson()` and `pushJson()` for easy batch inserts.
5. **Cool Factor**: Saying "I use Javase" sounds way better than "I wrestle with Firebase APIs."

---

### 🤝 Contributing

Want to make Javase even better? Fork it, fix it, or file an issue. Just make sure your code is cleaner than your desk. 🙃

---

### 📜 License

This project is licensed under the [MIT License](LICENSE). In short: use it, modify it, but don’t blame us if you break it.

---

### 💬 Closing Words

Firebase is great, but let’s be honest — their APIs can be... *quirky*. Javase turns those quirks into an elegant experience. So, go ahead: download, experiment, and enjoy Firebase like never before.

Remember, **life’s too short for bad libraries.** 🧑‍💻

---
