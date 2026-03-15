# Esprit-PIDEV-3A18-2026-Loopi

<div align="center">
  <img src="src/main/resources/images/logo/logo.png" alt="Loopi Logo" width="200"/>
  <h3>Plateforme d'Économie Circulaire & Solidarité</h3>
  <p>Développé à Esprit School of Engineering - Tunisie</p>
  
  [![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.java.net/)
  [![JavaFX](https://img.shields.io/badge/JavaFX-17-orange.svg)](https://openjfx.io/)
  [![MySQL](https://img.shields.io/badge/MySQL-5.6+-green.svg)](https://www.mysql.com/)
  [![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

---

## 📋 Table des matières

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Contributors](#contributors)
- [Academic Context](#academic-context)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage Guide](#usage-guide)
- [Acknowledgments](#acknowledgments)

---

# Overview

**Loopi** is a comprehensive JavaFX desktop application developed as part of the **PIDEV - 3rd Year Engineering Program** at **Esprit School of Engineering** (Academic Year 2025–2026).

This innovative platform promotes circular economy and environmental solidarity through **four integrated modules**:

| Module | Description |
|------|-------------|
| 📅 Event Management | Organization of ecological events with geolocation |
| 🖼️ Gallery Management | Exhibition of recycled art with review system |
| 👥 User Management | Multiple authentication methods and profile management |
| 📦 Collection Management | Recycling campaigns with gamification badges |

---

# Features

## 📅 Module 1: Event Management

| Feature | Description |
|------|-------------|
| Event Creation | Intuitive interface with 3-step validation (submission → admin → publication) |
| Automatic Geocoding | Address to GPS conversion using Nominatim (OpenStreetMap) |
| Interactive Map | Event visualization with Leaflet.js |
| Admin Validation | Approval workflow with comments (approved/rejected) |
| Participant Management | Registration tracking with status (registered/present/absent) |
| Notifications | Real-time alerts for all actions |
| AI Image Generation | Event image creation with Stability AI |
| QR Code Generation | QR codes for event sharing |

---

## 🖼️ Module 2: Gallery Management

| Feature | Description |
|------|-------------|
| Product Catalog | Complete CRUD for organizers with categories |
| Review System | 1-5 star ratings with sentiment analysis |
| Bad Words Detection | Automatic filtering of inappropriate comments |
| Favorites | Product saving for participants |
| AI Recommendations | Personalized suggestions based on history |
| Social Sharing | Facebook, Twitter, WhatsApp, LinkedIn integration |
| QR Codes | Generation and download for each product |
| PDF Export | Flagged review reports with iText 7 |

---

## 👥 Module 3: User Management

| Feature | Description |
|------|-------------|
| Multi-factor Authentication | Email/password, Google OAuth2, QR code, facial recognition |
| Role-based Access | Admin, Organizer, Participant with distinct permissions |
| User Profiles | Photo upload with automatic resizing |
| Real-time Validation | Password strength, email format, confirmation |
| Recycling Badges | First Timer, Plastic Pioneer, Paper Warrior, Glass Master, Metal Titan, Cardboard King |
| Progress Tree | Ecological impact visualization (grows with donations) |
| Embedded Web Server | QR code login via port 8081 |

---

## 📦 Module 4: Collection Management

| Feature | Description |
|------|-------------|
| Collection Campaigns | Creation with kg goals by material type |
| Donations | Intuitive contribution interface with slider |
| AI Impact Analysis | Environmental analysis with Groq (Llama 3.3) |
| Unlockable Badges | Automatically unlocked based on recycled quantities |
| Email Notifications | Alerts when goals are reached (SendGrid) |
| Statistics | Admin dashboards with charts |

---

# Tech Stack

## Frontend

| Technology | Version | Purpose |
|------|------|------|
| JavaFX | 17.0.6 | Desktop UI framework |
| CSS | - | Custom styling |
| FXML | - | Optional view definitions |
| WebView | - | Leaflet map integration |
| Leaflet.js | 1.9.4 | Interactive maps |

---

## Backend

| Technology | Version | Purpose |
|------|------|------|
| Java | 17 | Core programming language |
| MySQL | 5.6+ | Database management |
| JDBC | - | Database connectivity |
| Maven | 3.6+ | Build and dependency management |

---

## AI & Machine Learning

| Service | Purpose |
|------|------|
| Stability AI | Image generation |
| Groq AI | Environmental impact analysis |
| Google OAuth2 | Authentication |
| OpenCV | Facial recognition (simplified) |

---

## Libraries & Tools

| Library | Version | Purpose |
|------|------|------|
| MySQL Connector | 8.0.33 | Database driver |
| Google ZXing | 3.5.1 | QR code generation |
| Gson | 2.10.1 | JSON parsing |
| SendGrid | 4.10.1 | Email notifications |
| iText 7 | 7.2.5 | PDF report generation |
| Webcam Capture | 0.3.12 | Camera integration |
| JJWT | 0.11.5 | JWT tokens for QR login |
| Nominatim API | - | Geocoding services |
| JUnit | 5.10.1 | Unit testing |

---

# Architecture

## Project Structure

```
Esprit-PIDEV-3A18-2026-Loopi/
├── pom.xml
├── README.md
├── LICENSE
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── edu/
│   │   │       └── Loopi/
│   │   │           ├── config/
│   │   │           ├── entities/
│   │   │           ├── interfaces/
│   │   │           ├── services/
│   │   │           ├── tools/
│   │   │           ├── tests/
│   │   │           └── view/
│   │   └── resources/
│   │       ├── config/
│   │       ├── images/
│   │       ├── profiles/
│   │       ├── uploads/
│   │       └── style.css
│   └── test/
│       └── java/
└── .github/
```

---

# Contributors

| Name | Module | GitHub | Email |
|------|------|------|------|
| **Ouissale Hamhoum** | Module User + Module Event | https://github.com/OuissaleHamhoum | Ouissale.Hamhoum@esprit.tn |
| **Mehdi Werfelli** | Module Collection | https://github.com/werfellimahdi | mahdi.werfelli@esprit.tn |
| **Yassine Majdoub** | Module Gallery | https://github.com/yassine-majdoub | Yassine.Majdoub@esprit.tn |

---

# Academic Context

Developed at **Esprit School of Engineering - Tunisia**

**PIDEV - 3rd Year Engineering Program**  
Academic Year **2025–2026**

---

# Getting Started - Loopi Installation Guide

## 1. Prérequis

| Logiciel | Version |
|------|------|
| Java JDK | 21.0.10 |
| MySQL | 5.7+ (WampServer) |
| Maven | 3.9.12 |
| Git | 2.52.0 |

---

## 2. Cloner le Projet

Clone the repository from GitHub.

---

## 3. Configurer MySQL (WampServer)

## 4. Importer la Base de Données

1. Ouvrir `http://localhost/phpmyadmin`
2. Créer une base `loopi_db`
3. Importer `loopi_db-3 (1).sql`

---

## 5. Configurer la Connexion MySQL

Fichier: `src/main/java/edu/Loopi/tools/MyConnection.java`

```java
private static final String USERNAME = "root";
private static final String PASSWORD = ""; // Vide pour WampServer
```

---

## 6. Configurer les Clés API

Fichier: `src/main/resources/config/api.properties`

```properties
stability.api.key=VOTRE_CLE_ICI
stability.api.url=https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image
ai.image.default.width=1024
ai.image.default.height=1024
```

🔑 Obtenir une clé: https://platform.stability.ai/account/keys

---

## 7. Compiler le Projet

```powershell
mvn clean package
```

---

## 8. Lancer l'Application

```powershell
mvn javafx:run
```

---

## 9. Configurer le Pare-feu pour QR Login (⚠️ Important)

### Étapes pour Windows 11

1. `Win + R` → taper `wf.msc`
2. **Règles entrantes → Nouvelle règle**
3. **Port → TCP → Ports spécifiques: 8081**
4. **Autoriser la connexion**
5. Nom: `Loopi QR Login`

### Conditions requises

- PC et téléphone sur le **MÊME réseau WiFi**
- Port **8081** ouvert dans le pare-feu
- Utiliser l'IP affichée dans la console

### Tester le serveur QR

- `http://localhost:8081/test` - État du serveur
- `http://localhost:8081/ips` - IPs disponibles

---

## 10. Vérification Rapide

```powershell
java -version
mvn clean compile
mvn javafx:run
```

---

# Usage Guide

## For Administrators

1. Login with admin credentials
2. Dashboard view shows global statistics
3. User Management: Add/edit/delete users
4. Event Management: Validate pending events
5. Map View: See all events on interactive map
6. Gallery Management: Moderate products
7. Collection Dashboard: Monitor campaigns

---

## For Organizers

1. Create Events with automatic geocoding
2. Manage Products in gallery
3. Launch recycling campaigns
4. Track participants
5. Monitor donations
6. View notifications

---

## For Participants

1. Browse events on map
2. Register for events
3. Explore gallery
4. Favorite products
5. Make donations
6. Earn recycling badges
7. Track ecological impact

---

# Acknowledgments

| Organization | Contribution |
|------|------|
| Esprit School of Engineering | Academic framework |
| Stability AI | Image generation API |
| OpenStreetMap | Geocoding services |
| Groq | Environmental AI analysis |
| SendGrid | Email notifications |
| Google | OAuth2 authentication |

---

<div align="center">

Developed with ❤️ by **Esprit Engineering Students - Class of 2026**

© 2026 Esprit School of Engineering 

<img src="src/main/resources/images/logo/esprit.png" width="100"/>

</div>
