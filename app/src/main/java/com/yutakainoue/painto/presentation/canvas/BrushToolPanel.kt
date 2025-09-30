package com.yutakainoue.painto.presentation.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.yutakainoue.painto.data.model.BrushType
import com.yutakainoue.painto.data.model.PaintStyle

/**
 * ブラシツール設定パネルコンポーネント
 *
 * ペイント機能のブラシ設定を行うUIコンポーネント。
 * ブラシタイプの選択、サイズ調整、色選択機能を統合的に提供。
 * Material Design 3のデザインシステムに準拠した直観的なUI。
 *
 * @param currentBrushStyle 現在のブラシ設定（選択状態表示用）
 * @param onBrushTypeChange ブラシタイプ変更時のコールバック
 * @param onBrushSizeChange ブラシサイズ変更時のコールバック
 * @param onColorChange 色変更時のコールバック
 * @param modifier Compose Modifierでレイアウトをカスタマイズ
 */
@Composable
fun BrushToolPanel(
    currentBrushStyle: PaintStyle,
    onBrushTypeChange: (BrushType) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // ブラシタイプ選択セクション
        Text(
            text = "ブラシ",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ブラシタイプボタンの横並びレイアウト
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // ペン: 最も一般的な描画ツール
            BrushTypeButton(
                brushType = BrushType.PEN,
                icon = Icons.Default.Create,
                label = "ペン",
                isSelected = currentBrushStyle.brushType == BrushType.PEN,
                onClick = { onBrushTypeChange(BrushType.PEN) }
            )
            // マーカー: 太めで半透明な描画
            BrushTypeButton(
                brushType = BrushType.MARKER,
                icon = Icons.Default.Brush,
                label = "マーカー",
                isSelected = currentBrushStyle.brushType == BrushType.MARKER,
                onClick = { onBrushTypeChange(BrushType.MARKER) }
            )
            // ハイライター: 幅広で透明なマーキング
            BrushTypeButton(
                brushType = BrushType.HIGHLIGHTER,
                icon = Icons.Default.FormatPaint,
                label = "蛍光ペン",
                isSelected = currentBrushStyle.brushType == BrushType.HIGHLIGHTER,
                onClick = { onBrushTypeChange(BrushType.HIGHLIGHTER) }
            )
        }

        // ブラシサイズ調整スライダーセクション
        Text(
            text = "太さ: ${currentBrushStyle.strokeWidth.toInt()}px",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1pxから50pxまでの範囲でサイズ調整可能
        Slider(
            value = currentBrushStyle.strokeWidth,
            onValueChange = onBrushSizeChange,
            valueRange = 1f..50f,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 色選択セクション
        Text(
            text = "色",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 事前定義された色の横スクロールリスト
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(predefinedColors) { color ->
                ColorSwatch(
                    color = color,
                    isSelected = currentBrushStyle.color == color,
                    onClick = { onColorChange(color) }
                )
            }
        }
    }
}

/**
 * ブラシタイプ選択ボタンコンポーネント
 *
 * 各ブラシタイプを表すアイコンとラベル付きのボタン。
 * 選択状態に応じて背景色とテキスト色が変化し、直観的なUIを提供。
 *
 * @param brushType ブラシタイプ（定義用、実際の表示には使用しない）
 * @param icon 表示するアイコン
 * @param label ボタンのラベルテキスト
 * @param isSelected 現在選択されているかどうか
 * @param onClick ボタンクリック時のコールバック
 * @param modifier Modifierでレイアウトをカスタマイズ
 */
@Composable
private fun BrushTypeButton(
    brushType: BrushType,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(
                // 選択状態に応じて背景色を変更
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(12.dp)
    ) {
        // ブラシタイプを表すアイコン
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        // ブラシタイプの名前ラベル
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 色選択用のカラースウォッチコンポーネント
 *
 * 円形の色見本で、クリックで色を選択できる。
 * 選択状態に応じてボーダーの太さと色が変化し、直観的な選択状態を表示。
 *
 * @param color 表示する色
 * @param isSelected 現在選択されているかどうか
 * @param onClick クリック時のコールバック
 * @param modifier Modifierでレイアウトをカスタマイズ
 */
@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)            // 固定サイズの円形
            .clip(CircleShape)       // 円形にクリップ
            .background(color)       // 指定された色で背景を塗りつぶし
            .border(
                // 選択状態に応じてボーダーの太さと色を変更
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable { onClick() } // タップで色選択
    )
}

/**
 * 事前定義されたカラーパレット
 *
 * 一般的な描画でよく使用される色を中心に選定。
 * 基本色（赤、緑、青、黄、白、黒）から、
 * 特殊色（茶色、オレンジ、紫、ピンク）まで幅幅いバリエーションを提供。
 */
private val predefinedColors = listOf(
    Color.Black,           // 黒: ラインアートや輪郭線用
    Color.White,           // 白: ハイライトや修正用
    Color.Red,             // 赤: 強調や注意喚起用
    Color.Green,           // 緑: 自然や植物の表現用
    Color.Blue,            // 青: 空や水の表現用
    Color.Yellow,          // 黄: 明るいアクセント用
    Color.Magenta,         // マゼンタ: 鮮やかなアクセント用
    Color.Cyan,            // シアン: 寒色系のアクセント用
    Color(0xFF8B4513),     // 茶色: 土や木の表現用
    Color(0xFFFFA500),     // オレンジ: 暖かいアクセント用
    Color(0xFF800080),     // 紫: 高貴さや神秘性の表現用
    Color(0xFFFF69B4),     // ホットピンク: かわいいアクセント用
)