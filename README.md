# 🀄 Domino Blockade Android（多米諾封鎖）

> 多米諾封鎖 Android 版 — 基於 LifeSnap iOS 版移植的多米諾骨牌對戰手遊
>
> [![CI Build](https://github.com/cancleeric/domino-blockade-android/actions/workflows/ci.yml/badge.svg)](https://github.com/cancleeric/domino-blockade-android/actions/workflows/ci.yml)
> [![Release](https://github.com/cancleeric/domino-blockade-android/actions/workflows/release.yml/badge.svg)](https://github.com/cancleeric/domino-blockade-android/actions/workflows/release.yml)
> [![Platform](https://img.shields.io/badge/platform-Android-green.svg)]()
> [![Language](https://img.shields.io/badge/language-Kotlin-purple.svg)]()
> [![Min SDK](https://img.shields.io/badge/minSdk-26-blue.svg)]()
>
> ## 📖 專案簡介
>
> **Domino Blockade** 是一款多米諾骨牌對戰手遊，玩家透過策略性地放置骨牌來封鎖對手，率先出完手中骨牌者獲勝。本專案為 Android 版本，從 LifeSnap iOS 版移植而來，使用現代 Android 開發技術棧重新打造。
>
> ### 🎮 遊戲特色
>
> - **經典多米諾規則**：遵循標準多米諾骨牌配對規則
> - - **封鎖機制**：當無法出牌時觸發封鎖，策略性更強
>   - - **多人對戰**：支援 2-4 人本地對戰模式
>     - - **AI 對手**：內建 AI 玩家，支援不同難度等級
>       - - **計分系統**：自動計算剩餘骨牌點數，累積分數
>         - - **動畫效果**：流暢的骨牌放置與翻轉動畫
>          
>           - ## 🏗️ 技術架構
>          
>           - ### 技術棧
>          
>           - | 技術 | 說明 |
> |------|------|
> | **語言** | Kotlin |
> | **UI 框架** | Jetpack Compose |
> | **架構模式** | MVVM + Clean Architecture |
> | **依賴注入** | Hilt |
> | **非同步處理** | Kotlin Coroutines + Flow |
> | **資料庫** | Room |
> | **導航** | Navigation Compose |
> | **測試** | JUnit 5 + Espresso + Compose Testing |
> | **CI/CD** | GitHub Actions |
>
> ### 專案結構
>
> ```
> app/
> ├── src/
> │   ├── main/
> │   │   ├── java/com/cancleeric/dominoblockade/
> │   │   │   ├── data/           # 資料層
> │   │   │   │   ├── local/      # Room 資料庫
> │   │   │   │   ├── model/      # 資料模型
> │   │   │   │   └── repository/ # Repository 實作
> │   │   │   ├── domain/         # 領域層
> │   │   │   │   ├── model/      # 領域模型（Domino, Player, Game）
> │   │   │   │   ├── usecase/    # 用例
> │   │   │   │   └── repository/ # Repository 介面
> │   │   │   ├── presentation/   # 表現層
> │   │   │   │   ├── game/       # 遊戲畫面
> │   │   │   │   ├── menu/       # 主選單
> │   │   │   │   ├── result/     # 結算畫面
> │   │   │   │   └── components/ # 共用 UI 元件
> │   │   │   ├── ai/             # AI 對手邏輯
> │   │   │   └── di/             # Hilt 依賴注入模組
> │   │   └── res/                # 資源檔案
> │   ├── test/                   # 單元測試
> │   └── androidTest/            # UI 測試
> ├── build.gradle.kts
> └── proguard-rules.pro
> ```
>
> ## 🚀 快速開始
>
> ### 環境需求
>
> - Android Studio Hedgehog (2023.1.1) 或更新版本
> - - JDK 17+
>   - - Android SDK 34
>     - - Kotlin 1.9+
>      
>       - ### 建置與執行
>      
>       - ```bash
>         # 1. Clone 專案
>         git clone https://github.com/cancleeric/domino-blockade-android.git
>
>         # 2. 開啟 Android Studio 匯入專案
>
>         # 3. 同步 Gradle
>         ./gradlew build
>
>         # 4. 執行 App
>         ./gradlew installDebug
>
>         # 5. 執行測試
>         ./gradlew test
>         ./gradlew connectedAndroidTest
>         ```
>
> ## 🎯 遊戲規則
>
> 1. **發牌**：每位玩家分配 7 張骨牌（2 人局）或 5 張（3-4 人局）
> 2. 2. **出牌**：玩家輪流放置骨牌，骨牌兩端數字必須與場上端點數字相符
>    3. 3. **抽牌**：無法出牌時，從牌堆中抽取骨牌直到可出牌或牌堆耗盡
>       4. 4. **封鎖**：當牌堆耗盡且無人可出牌時，遊戲進入封鎖狀態
>          5. 5. **勝利**：率先出完所有骨牌的玩家獲勝；封鎖時手中骨牌點數最少者獲勝
>            
>             6. ## 📋 開發計劃
>            
>             7. 詳見 [DEVELOPMENT_PLAN.md](./DEVELOPMENT_PLAN.md)
>            
>             8. ## 🤝 貢獻指南
>
> 1. Fork 此專案
> 2. 2. 建立功能分支 (`git checkout -b feature/amazing-feature`)
>    3. 3. 提交變更 (`git commit -m 'feat: add amazing feature'`)
>       4. 4. 推送至分支 (`git push origin feature/amazing-feature`)
>          5. 5. 建立 Pull Request
>            
>             6. ### Commit 規範
>            
>             7. 使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：
>            
>             8. - `feat:` 新功能
> - `fix:` 修復 Bug
> - - `docs:` 文件更新
>   - - `style:` 程式碼格式調整
>     - - `refactor:` 重構
>       - - `test:` 測試相關
>         - - `chore:` 建置/工具相關
>          
>           - ## 📄 授權
>          
>           - 此專案為私有專案，版權所有 © 2026 cancleeric (Eric Wang)。

## 🚀 CI/CD Pipeline

### 概覽

| Workflow | 觸發條件 | 說明 |
|----------|----------|------|
| **CI Build** | push / PR to `main`, `develop` | 建置 Debug APK、執行單元測試 |
| **Release** | push tag `v*.*.*` | 建置 Signed Release APK、建立 GitHub Release、上傳 Firebase / Google Play |

---

### 版號管理（自動）

| 欄位 | 規則 |
|------|------|
| `versionCode` | `git rev-list --count HEAD`（每次 commit 自動遞增） |
| `versionName` | 從 git tag 讀取（`v1.2.3` → `1.2.3`）；非 tag commit 加 `-SNAPSHOT` 後綴 |

---

### 發布 Release APK

只需推送符合 `v*.*.*` 格式的 tag：

```bash
git tag v1.0.0
git push origin v1.0.0
```

Release workflow 將自動：
1. 建置 Signed Release APK
2. 從 commit messages 產生 Release Notes（支援 Conventional Commits 分類）
3. 在 GitHub Releases 上傳 APK
4. 上傳至 Firebase App Distribution（beta 測試者）
5. 上傳至 Google Play Internal Testing（正式版 tag）

---

### 必要的 GitHub Secrets

前往 `Settings → Secrets and variables → Actions` 設定以下 secrets：

#### 🔑 APK 簽署（必要）

| Secret | 說明 |
|--------|------|
| `KEYSTORE_BASE64` | Base64 編碼的 `.jks` keystore 檔案 |
| `STORE_PASSWORD` | Keystore 密碼 |
| `KEY_ALIAS` | Key alias 名稱 |
| `KEY_PASSWORD` | Key 密碼 |

產生 Base64 keystore：
```bash
base64 -i release.jks | pbcopy   # macOS
base64 release.jks | xclip       # Linux
```

#### 🔥 Firebase App Distribution（選用）

| Secret | 說明 |
|--------|------|
| `FIREBASE_APP_ID` | Firebase 控制台中的 App ID（如 `1:123456789:android:abcdef`） |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase Service Account JSON 金鑰內容 |

#### 🛒 Google Play（選用）

| Secret | 說明 |
|--------|------|
| `PLAY_STORE_JSON_KEY` | Google Play API Service Account JSON 金鑰內容 |

---

### 本地簽署設定

```bash
# 1. 複製範本
cp keystore.properties.template keystore.properties

# 2. 編輯 keystore.properties，填入你的簽署資訊
# keystore.properties 已列於 .gitignore，不會被 commit

# 3. 建置 Release APK
./gradlew assembleRelease
```

### fastlane 軌道管理

```bash
# 上傳至 Internal Testing
bundle exec fastlane android internal apk:app/build/outputs/apk/release/app-release.apk

# 升級至 Alpha
bundle exec fastlane android promote_to_alpha

# 升級至 Beta（公開測試）
bundle exec fastlane android promote_to_beta

# 升級至 Production（分階段 10% 發布）
bundle exec fastlane android promote_to_production rollout:0.1
```
