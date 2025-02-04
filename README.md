# Javase: Firebase? More Like Java-Ease! 🚀  
![Maven Central](https://img.shields.io/maven-central/v/io.github.init-io/javase.svg?label=Maven%20Central)
[![GitHub Package](https://img.shields.io/github/v/package/init-io/DoEEP)](https://github.com/init-io/Javase/packages)
![GitHub Release](https://img.shields.io/github/v/release/init-io/Javase?label=release)
[![License](https://img.shields.io/github/license/init-io/Javase)](LICENSE)


**Version v1.2.3**  
*(Because good things come in threes)*  

---

### 📜 Introduction  
Welcome to **Javase** — a Java library so smooth, it’ll make working with Firebase feel like a walk in the park. Firebase can be a maze of APIs and JSON responses, but fear not! Javase is here to give you simple, streamlined, and asynchronous solutions for Firebase Database and Firebase Storage.

Think of it as your over-caffeinated, always-on-call Firebase assistant who actually *does what it’s told*. 🙌

---

### 🚀 Features  
#### Database Module:
- **CRUD without the Drama**: Perform `get`, `put`, `update`, and `remove` operations on Firebase Realtime Database. No sweat, no mess, just pure JSON magic.
- **Error Handling That Cares**: Get detailed error messages because "Unknown Error Occurred" isn't good enough.
- **Asynchronous Brilliance**: All tasks run on a separate thread, so your main thread can sip coffee in peace. ☕

#### Storage Module:
- **File Uploading (a.k.a. "Here, Take My Stuff!")**: Upload files to Firebase Storage with minimal effort.
- **Download URLs**: Retrieve public download URLs for your files faster than your WiFi can fail you.
- **Photo Listing**: Get a list of all your photos stored in Firebase. Finally, an easy way to inventory your cat pictures. 🐱
- **File Deletion**: Ruthlessly delete photos from Firebase Storage (because not every selfie is worth keeping).

---

### 🛠️ Installation  

#### Maven:
```xml
<dependency>
    <groupId>io.github.initio</groupId>
    <artifactId>javase</artifactId>
    <version>1.2.3</version>
</dependency>
```

#### Gradle:
```groovy
implementation 'io.github.init-io:javase:1.2.3'
```

---

### 📦 How to Use  
Here’s a quick peek at how ridiculously easy it is to make Firebase work for you with **Javase**.

#### 1. **Authenticaton Module**:
```java
Server server = new Server();
server.initialize(apiKey,authDomain,databaseUrl,storageBucket);
Authentication auth = new Authenticaton(server);

//SignUp
auth.signUp(email,password);

//SignIn
auth.signIn(email,password);

//Get idTokem
auth.getIdToken(); //expires in 1 hour

//Refresh toke
auth.refreshIdToken();

//Check if user is verified
auth.isVerified(auth.getIdToken());

//Verify user
auth.verifyUser(auth.getIdToken());

//Reset user password
auth.resetUser(email);

//Remove an user
auth.removeUser(auth.getIdToken());

```

#### 2. **Database Module**:  
```java
Server server = new Server();
server.initialize(apiKey,authDomain,databaseUrl,storageBucket);
Database database = new Database(server);

// Fetch data
String response = database.get("path/to/data", "id-token");
System.out.println("Database says: " + response);

// Add data
String putResponse = database.put("users/123", "name", "Java Enthusiast", "id-token");
System.out.println("Put Response: " + putResponse);

// Update data
String updateResponse = database.update("users/123", "status", "awesome", "id-token");
System.out.println("Update Response: " + updateResponse);

// Delete data
String deleteResponse = database.remove("users/123", "id-token");
System.out.println("Delete Response: " + deleteResponse);

```

#### 3. **Storage Module**:  
```java
Storage storage = new Storage(server, new Authenticate("id-token"));

// Upload a file
String uploadResponse = storage.uploadFile("/local/path/to/photo.jpg", "images/photo.jpg");
System.out.println("Upload Response: " + uploadResponse);

// Get a file URL
String fileUrl = storage.getFileUrl("images/photo.jpg");
System.out.println("Download URL: " + fileUrl);

// List all photos
String photos = storage.listPhotos("images");
System.out.println("Available Photos: " + photos);

// Delete a photo
String deletePhotoResponse = storage.deletePhoto("images/photo.jpg");
System.out.println("Delete Response: " + deletePhotoResponse);
```

---

### 🐛 Error Handling  
Whenever things go sideways (and let's face it, they will), **Javase** has your back with a `error()` method that provides detailed error messages.

Example:
```java
String result = database.get("non/existent/path", "id-token");
if (result == null) {
    System.out.println("Uh oh: " + database.error());
}
```

No more vague error messages like "Something went wrong." With Javase, you’ll get errors that actually tell you *what* went wrong. Revolutionary, isn’t it? 🚨

---

### 🤓 Why Choose Javase?  
1. **Asynchronous Operations**: Multithreading without the headache.
2. **Simple API**: Minimal boilerplate. Maximum productivity.
3. **Readable Errors**: You'll know exactly why Firebase isn't cooperating.
4. **Cool Factor**: Saying "I use Javase" sounds way better than "I wrestle with Firebase APIs."

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
