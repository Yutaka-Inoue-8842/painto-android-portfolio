# painto-android

## 📖 このアプリについて

> 📌 **このリポジトリは技術スキルを示すためのポートフォリオプロジェクトです**

**painto-android**は、写真に手軽にペイント・装飾ができる軽量Androidアプリです。SNS投稿前の写真加工を想定し、「読み込み → 編集 → 共有」を3タップ以内で完了できるシンプルな体験を目指しています。

### 主な機能
- 🎨 **ペイント機能** - ペン、マーカー、蛍光ペン、消しゴムなど多彩なブラシツール
- 📸 **写真編集** - フィルタ適用、明るさ・コントラスト調整、トリミング、回転
- ✨ **装飾機能** - テキスト追加、スタンプ配置
- 💾 **ローカル処理** - すべての処理を端末内で完結、外部サーバーへのアップロード不要
- 🔄 **Undo/Redo** - 操作の取り消し・やり直しに対応

### ターゲットユーザー
写真をSNSに投稿する前に、ちょっとした加工や装飾をしたいライトユーザー層を想定しています。

### 技術スタック

#### フレームワーク・ライブラリ
- **Kotlin** 2.0.21
- **Jetpack Compose BOM** 2024.09.00
- **Material3** Design System
- **Koin** 依存性注入
- **Compose Navigation** 画面遷移
- **ViewModel & StateFlow** 状態管理

#### Android設定
- **Target SDK** 36 (Android 15)
- **Minimum SDK** 28 (Android 9)
- **Gradle** 8.13.0 with Version Catalog

## 📁 プロジェクト構造

```
app/src/main/java/com/yutakainoue/painto/
├── presentation/
│   ├── canvas/
│   │   ├── PaintingCanvas.kt      # メイン描画キャンバス
│   │   └── BrushToolPanel.kt      # ブラシツール設定UI
│   └── editor/
│       ├── EditorScreen.kt        # エディター画面
│       ├── EditorViewModel.kt     # 状態管理・ビジネスロジック
│       ├── EditorState.kt         # UI状態定義
│       └── EditorIntent.kt        # ユーザーアクション定義
├── data/
│   └── model/
│       └── PaintingProject.kt     # データモデル定義
├── core/
│   └── mvi/                       # MVIベースクラス
├── di/                            # 依存性注入設定
├── navigation/                    # ナビゲーション定義
└── ui/theme/                      # テーマ設定
```

## 🛠 開発環境

### 必要な環境
- Android Studio Koala (2024.1.1) 以上
- JDK 17 以上
- Android SDK 34 以上

## 🔄 現在の動作フロー

1. **アプリ起動** → MainActivity → Compose Navigation
2. **エディター画面** → 画像読み込み・編集開始
3. **描画操作** → リアルタイムストローク描画
4. **ツール変更** → ブラシタイプ・サイズ・色変更
5. **保存・共有** → ギャラリー保存・外部アプリ共有
---

## 📄 関連ドキュメント
- **REQUIREMENTS.md** - 要件定義・機能仕様書
