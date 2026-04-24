# 📱 Expense Tracker — Admin App

## 🎯 Overview

This is the **Admin application** of my Expense Tracker system, developed using Kotlin and Jetpack Compose.

The purpose of this app is to allow administrators to fully manage project data, including creating projects, tracking expenses, and synchronizing data with the cloud.

In this project, I focused not only on implementing features, but also on designing a **scalable and maintainable architecture** using modern Android development practices.

---

## 🚀 Features

### 📂 Project Management
- Create new projects
- View all projects
- Edit project details
- Delete projects
- Search and filter projects by name and status

### 💸 Expense Management
- View all expenses within a project
- Add new expenses
- Edit existing expenses
- Delete expenses
- Filter expenses by type and status

### 📊 Budget Tracking
- Automatically calculates total expenses per project
- Compares expenses with project budget
- Displays financial status (e.g. on track, over budget)

### ☁️ Data Synchronization
- Save data locally using Room Database
- Sync data with Firebase Firestore
- Manual sync and restore options
- Handles offline scenarios

### ⚙️ Settings
- Light mode / Dark mode toggle
- Manual data synchronization controls

---

## 💡 Design Decisions

### 1. MVVM Architecture
I used the MVVM pattern to separate concerns:
- UI handles rendering
- ViewModel manages state and logic
- Repository handles data operations

This makes the app easier to maintain and scale.

---

### 2. Offline-First Approach
I designed the app to work without internet.

All data is:
- Saved locally first (Room Database)
- Then synchronized with Firebase

This ensures the app is still usable even when offline.

---

### 3. Repository Pattern
I introduced a repository layer to act as a single source of truth.

This allows:
- Clean separation between local database and cloud
- Easier data management
- Better scalability

---

### 4. Reactive UI with StateFlow
I used StateFlow to update the UI automatically when data changes.

This removes the need for manual UI updates and ensures consistency.

---

### 5. Reusable Forms (Create & Edit)
I reused the same form for both creating and editing data using an `isEditMode` flag.

This reduces duplication and improves development efficiency.

---

## 🔄 Data Flow

### Add Expense Flow

1. User submits expense form
2. ViewModel validates input
3. Repository saves data to Room database
4. Repository syncs data to Firebase
5. StateFlow updates UI automatically

Flow summary:

UI → ViewModel → Repository → Room DB → Firebase → UI

---

## 📂 Project Structure

- `ui/` → Screens and UI components (Jetpack Compose)
- `viewmodel/` → Handles state and business logic
- `repository/` → Data management (local + cloud)
- `data/local/` → Room database (DAO, entities)
- `data/remote/` → Firebase Firestore logic
- `utils/` → Helper functions

---

## ⚙️ Technologies Used

- Kotlin
- Jetpack Compose
- MVVM Architecture
- Room Database
- StateFlow / Coroutines
- Firebase Firestore

---

## 📌 Limitations

- No authentication system implemented
- Conflict resolution during sync is basic
- Manual sync is required in some cases

---

## 🔮 Future Improvements

- Add authentication and user roles
- Improve conflict resolution strategy
- Implement real-time sync
- Enhance UI/UX design

---

## 👨‍💻 Author

Le Minh Thanh