# 🚌 ESI-RUN: Transport Management System

**ESI-RUN** is a comprehensive **JavaFX application** for managing public transportation systems. It provides a full suite of features including user and employee management, transport pass issuance, complaint resolution, and vehicle/station tracking.

---

## ✨ Features

- **User Management:** Register and manage regular users and employees
- **Pass Management:** Issue & validate:
    - Personal cards (Regular, Student, Senior, Employee)
    - Single-use tickets
- **Complaint System:** Submit and resolve complaints against stations/vehicles
- **Validation System:** Check pass validity with expiration checks
- **Persistence:** All data stored in CSV files

---

## 💻 System Requirements

- Java Development Kit (JDK) **17 or higher**
- JavaFX SDK (download from [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/))
- IntelliJ IDEA (recommended)
- Maven (comes bundled with IntelliJ)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/esi-run.git
cd esi-run
```

---

### 2. JavaFX Setup in IntelliJ IDEA

#### 📂 Step A: Add JavaFX SDK to Project Structure

1. Download JavaFX SDK (e.g., `javafx-sdk-17.0.15`)
2. In IntelliJ:
    - Go to **File > Project Structure (Ctrl+Alt+Shift+S)**
    - Navigate to **Libraries > + > Java**
    - Select the `lib` folder inside your JavaFX SDK
    - Click OK

#### ⚙️ Step B: Add VM Options to Run Configuration

Go to **Run > Edit Configurations** and add the following to **VM Options**:

```
--module-path "C:\path\to\javafx-sdk-17.0.15\lib" --add-modules javafx.controls,javafx.fxml
```

> 🔁 Replace `C:\path\to\...` with your actual SDK path.

##### ✅ Example (Windows):

```
--module-path "C:\javafx-sdk-17.0.15\lib" --add-modules javafx.controls,javafx.fxml
```

##### ✅ Example (macOS/Linux):

```
--module-path "/Users/yourname/Downloads/javafx-sdk-17.0.15/lib" --add-modules javafx.controls,javafx.fxml
```

---

## 🧪 Running the App

You can run the app via:

### ✅ IntelliJ (Recommended)
Use the `App.java` class as the entry point in the `./src/transport/ui/App.java` Make sure VM options are set (see above).

---

## 🗂️ Project Structure

```
esi-run/
├── transport.core/       # Domain models & business logic
├── transport.control/    # JavaFX Controllers
├── transport.ui/         # FXML Views & Styling
├── data/                 # CSV Data Storage
│   ├── users.csv
│   ├── passes.csv
│   ├── stations.csv
│   ├── vehicles.csv
│   └── complaints.csv
└── README.md
```

---

## 🔎 Usage Overview

### 🧍‍♂️ User Management
- Add/view users & employees
- Track user details (e.g., handicap status)

### 🎫 Pass Management
- Issue cards by type (Regular, Student, etc.)
- Create single-use tickets
- Validate passes with expiry logic

### 📣 Complaint System
- Submit & track complaints
- Automatic suspension for recurring offenders

### 📊 Dashboard
- See live stats: Users, Passes, Vehicles, Complaints

---

## 🧪 Testing

Run unit & integration tests via:`./src/test/`


Includes:
- Unit tests for core components
- Integration tests for workflows

---

## 📦 Tech Stack

- **JavaFX** for UI
- **MVC architecture**
- **CSV** for simple persistent storage

---

## ❓ Need Help?

If you run into any issues setting up JavaFX or launching the app, please open an issue or reach out directly.

---
