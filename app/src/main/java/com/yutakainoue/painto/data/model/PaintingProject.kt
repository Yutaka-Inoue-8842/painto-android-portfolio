package com.yutakainoue.painto.data.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * ペイントプロジェクトのデータクラス
 *
 * 一つの画像編集セッションに関する全ての情報を保持する。
 * 元画像、編集後画像、ペイントレイヤー、編集状態などを含む。
 *
 * @param id プロジェクトの一意識別子
 * @param originalImage 元の画像（未編集）
 * @param editedImage 編集後の画像
 * @param strokes ペイントストロークのリスト
 * @param editingState 画像編集の状態（明度、コントラストなど）
 * @param timestamp プロジェクト作成時刻
 */
data class PaintingProject(
    val id: String,
    val originalImage: Bitmap?,
    val editedImage: Bitmap?,
    val strokes: List<PaintStroke> = emptyList(),
    val editingState: EditingState = EditingState(),
    val timestamp: Long = System.currentTimeMillis()
)


/**
 * ペイントストロークのデータクラス
 *
 * ユーザーが描いた一筆の線を表現する。
 * ポイントリストと描画スタイルの情報を保持する。
 *
 * @param id ストロークの一意識別子
 * @param points 描画されたポイントリスト（線の軌跡）
 * @param paint 描画スタイル（色、太さなど）
 */
data class PaintStroke(
    val id: String,
    val points: List<Offset>,
    val paint: PaintStyle
)

/**
 * ペイントスタイルのデータクラス
 *
 * ブラシの描画設定を定義する。
 * 色、線の太さ、透明度、ブラシの種類を含む。
 *
 * @param color ブラシの色
 * @param strokeWidth 線の太さ（ピクセル）
 * @param alpha 透明度（0.0～1.0）
 * @param brushType ブラシの種類
 */
data class PaintStyle(
    val color: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val alpha: Float = 1f,
    val brushType: BrushType = BrushType.PEN
)

/**
 * ブラシの種類を定義する列挙型
 *
 * 各ブラシタイプは異なる描画特性を持つ。
 */
enum class BrushType {
    /** 通常のペン - シャープな線 */
    PEN,
    /** マーカー - 太めの線 */
    MARKER,
    /** ハイライター - 半透明の太い線 */
    HIGHLIGHTER,
    /** 消しゴム - 既存の線を消去 */
    ERASER
}

/**
 * 画像編集状態のデータクラス
 *
 * 画像に適用された編集パラメータを保持する。
 * 色調補正、クロップ、回転、フィルターなどの情報を含む。
 *
 * @param brightness 明度調整値（-1.0～1.0）
 * @param contrast コントラスト調整値（-1.0～1.0）
 * @param saturation 彩度調整値（-1.0～1.0）
 * @param temperature 色温度調整値（-1.0～1.0）
 * @param cropRect クロップ範囲（nullの場合はクロップなし）
 * @param rotation 回転角度（度数）
 * @param appliedFilters 適用されたフィルターのリスト
 */
data class EditingState(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,
    val cropRect: android.graphics.RectF? = null,
    val rotation: Float = 0f,
    val appliedFilters: List<FilterType> = emptyList()
)

/**
 * 画像フィルターの種類を定義する列挙型
 *
 * 画像に適用可能な様々なフィルター効果を列挙する。
 */
enum class FilterType {
    /** フィルターなし */
    NONE,
    /** ヴィンテージ風フィルター */
    VINTAGE,
    /** モノクロフィルター */
    MONO,
    /** 暖色系フィルター */
    WARM,
    /** 寒色系フィルター */
    COOL,
    /** 明るくするフィルター */
    BRIGHT,
    /** 暗くするフィルター */
    DARK,
    /** ハイコントラストフィルター */
    HIGH_CONTRAST
}