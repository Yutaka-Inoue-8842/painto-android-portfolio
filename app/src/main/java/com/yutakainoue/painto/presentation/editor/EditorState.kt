package com.yutakainoue.painto.presentation.editor

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.yutakainoue.painto.core.mvi.UiState
import com.yutakainoue.painto.data.model.BrushType
import com.yutakainoue.painto.data.model.PaintStroke
import com.yutakainoue.painto.data.model.PaintStyle
import com.yutakainoue.painto.data.model.PaintingProject

/**
 * エディター画面のUI状態を表すデータクラス
 *
 * MVIアーキテクチャにおけるModel部分として、
 * エディター画面のUIに関する全ての状態情報を保持する。
 * 画像編集に関する状態、ツール設定、Undo/Redo状態などを管理。
 *
 * @param currentProject 現在編集中のプロジェクト
 * @param isLoading ローディング状態（true: 処理中、false: 通常状態）
 * @param errorMessage エラーメッセージ（null: エラーなし、String: エラー内容）
 * @param currentTool 現在選択されている編集ツール
 * @param currentBrushStyle 現在のブラシスタイル設定
 * @param canUndo Undo操作が可能かどうか
 * @param canRedo Redo操作が可能かどうか
 * @param previewBitmap プレビュー用のビットマップ画像
 * @param currentStroke 現在描画中のストローク
 * @param currentPathPoints 現在描画中のパス座標リスト
 */
data class EditorState(
    val currentProject: PaintingProject? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentTool: Tool = Tool.Brush,
    val currentBrushStyle: PaintStyle = PaintStyle(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val previewBitmap: Bitmap? = null,
    val currentStroke: PaintStroke? = null,
    val currentPathPoints: List<Offset> = emptyList()
) : UiState

/**
 * 画像編集ツールの種類を定義する列挙型
 *
 * エディター画面で使用可能な編集ツールを定義。
 * 各ツールは異なる編集機能を提供する。
 */
enum class Tool {
    /** ブラシツール - 自由描画 */
    Brush,
    /** 消しゴムツール - 描画内容の消去 */
    Eraser,
    /** テキストツール - 文字入力 */
    Text,
    /** スタンプツール - 定型図形の配置 */
    Stamp,
    /** クロップツール - 画像の切り抜き */
    Crop,
    /** フィルターツール - 画像効果の適用 */
    Filter
}