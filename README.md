# painto-android

画像ペイント・編集Androidアプリの開発プロジェクト

## 🚀 現在の実装状況

### 実装済み機能

#### アーキテクチャ
- ✅ **MVI アーキテクチャ**
- ✅ **Jetpack Compose** - 完全なComposeベースUI
- ✅ **依存性注入** - Koin使用

#### 描画システム
- ✅ **PaintingCanvas** - リアルタイム描画キャンバス
- ✅ **ブラシシステム** - PEN/MARKER/HIGHLIGHTER/ERASER
- ✅ **リアルタイム描画** - 描画中のストロークプレビュー
- ✅ **ブラシサイズ表示** - タッチ時のブラシサイズサークル
- ✅ **ポイントベース描画** - Offset リストによる効率的な描画システム
- ✅ **Undo/Redo** - 描画操作の取り消し・やり直し

#### UI コンポーネント
- ✅ **EditorScreen** - メイン編集画面
- ✅ **BrushToolPanel** - ブラシツール設定パネル
- ✅ **TopAppBar** - ナビゲーション・操作ボタン

#### データモデル
- ✅ **PaintingProject** - プロジェクト管理
- ✅ **PaintStroke** - ストローク情報（Offsetリスト）
- ✅ **PaintStyle** - ブラシスタイル設定
- ✅ **EditingState** - 画像編集状態

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
