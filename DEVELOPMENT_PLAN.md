# 📋 Domino Blockade Android — 開發計劃

## 🎯 專案目標

將 LifeSnap iOS 版的多米諾封鎖遊戲移植到 Android 平台，採用 Kotlin + Jetpack Compose 技術棧，打造高品質的多米諾骨牌對戰手遊。

---

## 🗓️ 開發階段

### Phase 1：專案基礎建設（Week 1-2）

**目標**：搭建 Android 專案骨架，完成 CI/CD 管線配置

- [ ] 初始化 Android 專案（Kotlin + Jetpack Compose）
- [ ] - [ ] 配置 Gradle 建置腳本（Version Catalog）
- [ ] - [ ] 設定 Hilt 依賴注入
- [ ] - [ ] 建立 Clean Architecture 目錄結構
- [ ] - [ ] 配置 GitHub Actions CI 管線
- [ ] - [ ] 設定 ktlint / detekt 程式碼風格檢查
- [ ] - [ ] 建立基本的 Navigation 路由框架

- [ ] **交付物**：可建置並通過 CI 的空白專案骨架

- [ ] ---

- [ ] ### Phase 2：核心領域模型（Week 3-4）

- [ ] **目標**：實作多米諾遊戲的核心邏輯，與 UI 無關

- [ ] - [ ] 定義 Domino（骨牌）資料模型
- [ ] - [ ] 定義 Player（玩家）資料模型
- [ ] - [ ] 定義 GameState（遊戲狀態）資料模型
- [ ] - [ ] 實作骨牌庫（DominoSet）— 生成完整 28 張骨牌
- [ ] - [ ] 實作發牌邏輯（DealUseCase）
- [ ] - [ ] 實作出牌驗證邏輯（ValidateMoveUseCase）
- [ ] - [ ] 實作抽牌邏輯（DrawUseCase）
- [ ] - [ ] 實作封鎖判定邏輯（BlockadeDetector）
- [ ] - [ ] 實作計分系統（ScoreCalculator）
- [ ] - [ ] 實作遊戲引擎（GameEngine）— 整合所有遊戲邏輯
- [ ] - [ ] 撰寫完整的單元測試（覆蓋率 > 90%）

- [ ] **交付物**：通過完整測試的遊戲核心邏輯模組

- [ ] ---

- [ ] ### Phase 3：遊戲 UI 實作（Week 5-7）

- [ ] **目標**：使用 Jetpack Compose 實作遊戲畫面

- [ ] - [ ] 設計並實作骨牌 Composable 元件（DominoTile）
- [ ] - [ ] 實作手牌區域（PlayerHand）
- [ ] - [ ] 實作遊戲桌面（GameBoard）— 顯示已出骨牌鏈
- [ ] - [ ] 實作牌堆區域（DrawPile）
- [ ] - [ ] 實作玩家資訊顯示（PlayerInfo）
- [ ] - [ ] 實作遊戲主畫面（GameScreen + GameViewModel）
- [ ] - [ ] 實作骨牌拖放或點選出牌互動
- [ ] - [ ] 實作主選單畫面（MenuScreen）
- [ ] - [ ] 實作遊戲結算畫面（ResultScreen）
- [ ] - [ ] 實作遊戲設定畫面（SettingsScreen）

- [ ] **交付物**：可操作的單人 vs AI 遊戲流程

- [ ] ---

- [ ] ### Phase 4：AI 對手系統（Week 8-9）

- [ ] **目標**：實作不同難度的 AI 玩家

- [ ] - [ ] 設計 AI 策略介面（AiStrategy）
- [ ] - [ ] 實作簡單 AI — 隨機出牌（EasyAi）
- [ ] - [ ] 實作中等 AI — 優先出大點數骨牌（MediumAi）
- [ ] - [ ] 實作困難 AI — 分析場上局勢、記牌（HardAi）
- [ ] - [ ] 實作 AI 思考延遲動畫效果
- [ ] - [ ] 撰寫 AI 策略的單元測試

- [ ] **交付物**：三種難度可切換的 AI 對手

- [ ] ---

- [ ] ### Phase 5：動畫與音效（Week 10-11）

- [ ] **目標**：提升遊戲體驗，加入動畫和音效

- [ ] - [ ] 骨牌放置動畫（Compose Animation）
- [ ] - [ ] 骨牌翻轉動畫
- [ ] - [ ] 抽牌動畫
- [ ] - [ ] 封鎖發生時的視覺特效
- [ ] - [ ] 勝利/失敗結算動畫
- [ ] - [ ] 背景音樂整合
- [ ] - [ ] 音效整合（出牌、抽牌、封鎖）
- [ ] - [ ] 震動回饋（Haptic Feedback）

- [ ] **交付物**：具備完整動畫和音效的遊戲體驗

- [ ] ---

- [ ] ### Phase 6：資料持久化與統計（Week 12）

- [ ] **目標**：儲存遊戲紀錄和玩家統計

- [ ] - [ ] 設計 Room 資料庫 Schema（GameRecord, PlayerStats）
- [ ] - [ ] 實作 DAO 與 Repository
- [ ] - [ ] 實作歷史紀錄頁面
- [ ] - [ ] 實作玩家統計頁面（勝率、最高分等）
- [ ] - [ ] 實作 DataStore 設定儲存

- [ ] **交付物**：遊戲紀錄持久化功能

- [ ] ---

- [ ] ### Phase 7：多人對戰模式（Week 13-14）

- [ ] **目標**：支援本地多人對戰

- [ ] - [ ] 實作 2 人對戰模式
- [ ] - [ ] 實作 3 人對戰模式
- [ ] - [ ] 實作 4 人對戰模式
- [ ] - [ ] 實作傳遞裝置提示畫面（隱藏手牌）
- [ ] - [ ] 調整 UI 適配不同玩家數量

- [ ] **交付物**：支援 2-4 人本地對戰的完整遊戲

- [ ] ---

- [ ] ### Phase 8：測試、優化與發布準備（Week 15-16）

- [ ] **目標**：全面測試、效能優化、發布準備

- [ ] - [ ] 全面 UI 測試（Compose Testing）
- [ ] - [ ] 效能優化（減少重組、記憶體優化）
- [ ] - [ ] 不同螢幕尺寸適配（手機、平板）
- [ ] - [ ] 深色模式支援
- [ ] - [ ] 多語系支援（繁中、簡中、英文）
- [ ] - [ ] 應用圖示與啟動畫面設計
- [ ] - [ ] ProGuard / R8 設定
- [ ] - [ ] 準備 Google Play 上架資料

- [ ] **交付物**：可發布的 Production Release

- [ ] ---

- [ ] ## 📊 里程碑總覽

- [ ] | 里程碑 | 預計完成 | 關鍵交付物 |
- [ ] |--------|----------|------------|
- [ ] | M1 - 專案骨架 | Week 2 | CI 通過的空白專案 |
- [ ] | M2 - 核心邏輯 | Week 4 | 遊戲引擎 + 單元測試 |
- [ ] | M3 - 可玩版本 | Week 7 | 單人 vs AI 遊戲流程 |
- [ ] | M4 - AI 系統 | Week 9 | 三種難度 AI |
- [ ] | M5 - 體驗優化 | Week 11 | 動畫 + 音效 |
- [ ] | M6 - 資料持久化 | Week 12 | 歷史紀錄與統計 |
- [ ] | M7 - 多人模式 | Week 14 | 本地多人對戰 |
- [ ] | M8 - 發布 | Week 16 | Production Release |

- [ ] ---

- [ ] ## 🔧 技術決策紀錄

- [ ] | 決策 | 選擇 | 原因 |
- [ ] |------|------|------|
- [ ] | UI 框架 | Jetpack Compose | 現代宣告式 UI，適合遊戲介面 |
- [ ] | 架構 | MVVM + Clean Architecture | 關注點分離，易於測試 |
- [ ] | DI | Hilt | Android 官方推薦，整合良好 |
- [ ] | 非同步 | Coroutines + Flow | Kotlin 原生支援，與 Compose 整合佳 |
- [ ] | 資料庫 | Room | Android 官方 ORM，型別安全 |
- [ ] | 測試 | JUnit 5 + Turbine | 完整的單元測試與 Flow 測試支援 |
