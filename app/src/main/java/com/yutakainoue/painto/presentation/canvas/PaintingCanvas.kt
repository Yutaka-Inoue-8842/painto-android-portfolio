package com.yutakainoue.painto.presentation.canvas

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.yutakainoue.painto.data.model.BrushType
import com.yutakainoue.painto.data.model.PaintStroke
import com.yutakainoue.painto.data.model.PaintStyle

/**
 * ペイント描画用のCanvasコンポーネント
 *
 * Jetpack ComposeのCanvasをベースに、リアルタイム描画機能を提供。
 * 背景画像の上にユーザーが描画したストロークを重ねて表示。
 * タッチジェスチャーを検出して描画操作をハンドルし、リアルタイムフィードバックを提供。
 *
 * @param bitmap 背景として表示する元画像（nullの場合は背景なし）
 * @param strokes 描画済みのストローク一覧
 * @param currentBrushStyle 現在のブラシ設定（描画中のストロークに適用）
 * @param currentBrushSize 現在のブラシサイズ
 * @param currentPathPoints 現在描画中のパス座標リスト
 * @param onStartDrawing 描画開始時のコールバック（x, y座標）
 * @param onContinueDrawing 描画継続時のコールバック（x, y座標）
 * @param onEndDrawing 描画終了時のコールバック
 * @param modifier Compose Modifierでレイアウトやスタイルをカスタマイズ
 */
@Composable
fun PaintingCanvas(
    bitmap: Bitmap?,
    strokes: List<PaintStroke>,
    currentBrushStyle: PaintStyle,
    currentBrushSize: Float,
    currentPathPoints: List<Offset>,
    onStartDrawing: (Float, Float) -> Unit,
    onContinueDrawing: (Float, Float) -> Unit,
    onEndDrawing: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 現在タッチしている座標
    var currentTouchPosition by remember { mutableStateOf(Offset.Zero) }
    // タッチ中かどうかの状態
    var isTouching by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            // タッチジェスチャー検出で描画操作をハンドル
            // currentBrushStyleが変更されたらジェスチャー検出を再初期化
            .pointerInput(currentBrushStyle) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // 描画開始: 開始地点を設定
                        currentTouchPosition = offset
                        onStartDrawing(offset.x, offset.y)
                        isTouching = true
                    },
                    onDrag = { _, dragAmount ->
                        // 描画継続: 差分を現在位置に加算して絶対座標を計算
                            currentTouchPosition += dragAmount
                            onContinueDrawing(currentTouchPosition.x, currentTouchPosition.y)
                    },
                    onDragEnd = {
                        // 描画終了: 状態をリセットし、ストロークを確定
                        isTouching = false
                        onEndDrawing()
                    }
                )
            }
    ) {
        // 背景画像の描画（ある場合）
        bitmap?.let { bmp ->
            val imageBitmap = bmp.asImageBitmap()
            // Canvasのサイズに合わせて画像をスケール（アスペクト比保持）
            val canvasSize = size.width.coerceAtMost(size.height)
            val scale = canvasSize / bmp.width.coerceAtLeast(bmp.height)

            // 中央揃えで画像を描画
            drawImage(
                image = imageBitmap,
                topLeft = Offset(
                    x = (size.width - bmp.width * scale) / 2,
                    y = (size.height - bmp.height * scale) / 2
                ),
                colorFilter = null,
                alpha = 1f
            )
        }

        // 完成した全てのストロークを描画
        strokes.forEach { stroke ->
            drawStroke(stroke)
        }

        // 描画中の現在のストロークをリアルタイム描画
        if (currentPathPoints.isNotEmpty()) {
            drawCurrentStroke(currentPathPoints, currentBrushStyle)
        }

        // タッチ中にブラシサイズのサークルを描画
        if (isTouching) {
            // メインのサークルを描画
            drawCircle(
                color = Color.White,
                radius = currentBrushSize / 2f,
                center = currentTouchPosition
            )
            // 黒枠を描画
            drawCircle(
                color = Color.Black,
                radius = currentBrushSize / 2f,
                center = currentTouchPosition,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

/**
 * 完成したストロークを描画する私有関数
 *
 * ブラシタイプに応じて異なる描画スタイルを適用。
 * ペン、マーカー、ハイライター、消しゴムのそれぞれに合った視覚効果を提供。
 *
 * @param stroke 描画するストロークオブジェクト（パスとスタイル情報を含む）
 */
private fun DrawScope.drawStroke(stroke: PaintStroke) {
    if (stroke.points.size < 2) return // 2点未満では線を描画できない

    when (stroke.paint.brushType) {
        BrushType.PEN -> {
            // ペン: シャープで精密な線、通常の描画用
            drawPointsAsPath(
                points = stroke.points,
                color = stroke.paint.color,
                strokeWidth = stroke.paint.strokeWidth,
                alpha = stroke.paint.alpha,
                cap = StrokeCap.Round
            )
        }
        BrushType.MARKER -> {
            // マーカー: 太めで半透明な線、強調用
            drawPointsAsPath(
                points = stroke.points,
                color = stroke.paint.color,
                strokeWidth = stroke.paint.strokeWidth * 1.5f, // ベースサイズの1.5倍
                alpha = 0.7f, // 半透明で重ねた時の効果を演出
                cap = StrokeCap.Round
            )
        }
        BrushType.HIGHLIGHTER -> {
            // ハイライター: 幅広で透明な線、マーキング用
            drawPointsAsPath(
                points = stroke.points,
                color = stroke.paint.color,
                strokeWidth = stroke.paint.strokeWidth * 2f, // ベースサイズの2倍
                alpha = 0.3f, // 高透明で下の文字が透けて見える
                cap = StrokeCap.Square // 四角い線端（ハイライターらしい形）
            )
        }
        BrushType.ERASER -> {
            // 消しゴム: 現在は白色描画で代用（将来的にブレンドモードで実装予定）
            // TODO: ブレンドモードを使用した適切な消しゴム実装
            drawPointsAsPath(
                points = stroke.points,
                color = Color.White, // 仮の消しゴムは白色で上書き
                strokeWidth = stroke.paint.strokeWidth,
                alpha = 1f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 描画中の現在のストロークをリアルタイム描画する私有関数
 *
 * ユーザーがタッチして描画中のストロークをリアルタイムで表示。
 * drawStroke関数と同様のロジックで、ブラシタイプごとに異なる描画スタイルを適用。
 * ユーザーが描画結果を即座に確認できるためのUX向上。
 *
 * @param points 描画中のポイントリスト
 * @param paintStyle 適用するペイントスタイル
 */
private fun DrawScope.drawCurrentStroke(points: List<Offset>, paintStyle: PaintStyle) {
    if (points.size < 2) return // 2点未満では線を描画できない

    when (paintStyle.brushType) {
        BrushType.PEN -> {
            // ペン: シャープで精密な線
            drawPointsAsPath(
                points = points,
                color = paintStyle.color,
                strokeWidth = paintStyle.strokeWidth,
                alpha = paintStyle.alpha,
                cap = StrokeCap.Round
            )
        }
        BrushType.MARKER -> {
            // マーカー: 太めで半透明な線
            drawPointsAsPath(
                points = points,
                color = paintStyle.color,
                strokeWidth = paintStyle.strokeWidth * 1.5f,
                alpha = 0.7f,
                cap = StrokeCap.Round
            )
        }
        BrushType.HIGHLIGHTER -> {
            // ハイライター: 幅広で透明な線
            drawPointsAsPath(
                points = points,
                color = paintStyle.color,
                strokeWidth = paintStyle.strokeWidth * 2f,
                alpha = 0.3f,
                cap = StrokeCap.Square
            )
        }
        BrushType.ERASER -> {
            // 消しゴム: 白色で上書き
            drawPointsAsPath(
                points = points,
                color = Color.White,
                strokeWidth = paintStyle.strokeWidth,
                alpha = 1f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * ポイントリストからパスを作成して描画するヘルパー関数
 *
 * Offsetのリストを受け取り、Pathに変換して線として描画する。
 * 各ポイント間をスムーズに繋げる処理を行う。
 *
 * @param points 描画するポイントリスト
 * @param color 描画色
 * @param strokeWidth 線の太さ
 * @param alpha 透明度
 * @param cap 線端のスタイル
 * @param join 線の接続スタイル
 */
private fun DrawScope.drawPointsAsPath(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
    alpha: Float,
    cap: StrokeCap,
) {
    if (points.isEmpty()) return

    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = cap,
        ),
        alpha = alpha
    )
}