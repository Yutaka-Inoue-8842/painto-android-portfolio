package com.yutakainoue.painto.domain.usecase

import android.graphics.*
import com.yutakainoue.painto.data.model.FilterType
import kotlin.math.cos
import kotlin.math.sin

/**
 * 画像編集機能を提供するユースケースクラス
 *
 * Clean Architectureのドメイン層に位置し、ビジネスロジックをカプセル化。
 * フィルター適用、色調補正、回転などの画像処理機能を提供。
 * AndroidのColorMatrixやCanvas APIを使用して高品質な画像処理を実現。
 * 各機能は元のBitmapを変更せず、新しいBitmapを返す。
 */
class ImageEditingUseCase {

    /**
     * 指定されたフィルターを画像に適用する
     *
     * フィルタータイプに応じて適切な画像処理を適用。
     * 各フィルターは異なる視覚効果を提供し、写真の雰囲気や印象を変える。
     * 元のBitmapは変更せず、新しいBitmapを作成して返す。
     *
     * @param bitmap フィルター適用対象のBitmap
     * @param filterType 適用するフィルターの種類
     * @return フィルター適用後の新しいBitmap
     */
    fun applyFilter(bitmap: Bitmap, filterType: FilterType): Bitmap {
        return when (filterType) {
            FilterType.NONE -> bitmap // フィルターなし（元の画像をそのまま返す）
            FilterType.VINTAGE -> applyVintageFilter(bitmap) // ビンテージ風効果
            FilterType.MONO -> applyMonochromeFilter(bitmap) // モノクロ効果
            FilterType.WARM -> applyWarmFilter(bitmap) // 暖色系効果
            FilterType.COOL -> applyCoolFilter(bitmap) // 寒色系効果
            FilterType.BRIGHT -> applyBrightnessFilter(bitmap, 0.3f) // 明るさ強化
            FilterType.DARK -> applyBrightnessFilter(bitmap, -0.3f) // 明るさ抑制
            FilterType.HIGH_CONTRAST -> applyContrastFilter(bitmap, 1.5f) // コントラスト強化
        }
    }

    /**
     * 画像の明度を調整する
     *
     * 指定された値で画像全体の明るさを調整。
     * 正の値で明るく、負の値で暗くなる。
     *
     * @param bitmap 調整対象のBitmap
     * @param value 明度調整値（-1.0～1.0の範囲）
     * @return 明度調整後の新しいBitmap
     */
    fun adjustBrightness(bitmap: Bitmap, value: Float): Bitmap {
        return applyBrightnessFilter(bitmap, value)
    }

    /**
     * 画像のコントラストを調整する
     *
     * 指定された値で画像の明暗の差を調整。
     * 正の値でメリハリを強く、負の値で弱くする。
     *
     * @param bitmap 調整対象のBitmap
     * @param value コントラスト調整値（-1.0～1.0の範囲）
     * @return コントラスト調整後の新しいBitmap
     */
    fun adjustContrast(bitmap: Bitmap, value: Float): Bitmap {
        return applyContrastFilter(bitmap, 1f + value)
    }

    /**
     * 画像の彩度を調整する
     *
     * 指定された値で色の鮮やかさを調整。
     * 正の値で鮮やかに、負の値でモノクロに近づく。
     *
     * @param bitmap 調整対象のBitmap
     * @param value 彩度調整値（-1.0～1.0の範囲）
     * @return 彩度調整後の新しいBitmap
     */
    fun adjustSaturation(bitmap: Bitmap, value: Float): Bitmap {
        return applySaturationFilter(bitmap, 1f + value)
    }

    /**
     * 画像の色温度を調整する
     *
     * 指定された値で画像全体の色味を調整。
     * 正の値で暖色系（照明や太陽光のような暖かい色味）に、
     * 負の値で寒色系（蛍光灯や曇り空のような冷たい色味）になる。
     *
     * @param bitmap 調整対象のBitmap
     * @param value 色温度調整値（-1.0～1.0の範囲）
     * @return 色温度調整後の新しいBitmap
     */
    fun adjustTemperature(bitmap: Bitmap, value: Float): Bitmap {
        return if (value > 0) {
            // 正の値: 暖色系フィルターを適用
            applyWarmFilter(bitmap, value)
        } else {
            // 負の値: 寒色系フィルターを適用（絶対値で変換）
            applyCoolFilter(bitmap, -value)
        }
    }

    /**
     * 画像を指定された角度で回転させる
     *
     * Matrixを使用して数学的な回転変換を適用。
     * 一般的に90度単位で使用され、縦向き・横向きの切り替えに使用。
     * 元のBitmapのサイズを維持しつつ、指定された角度で回転した新しいBitmapを作成。
     *
     * @param bitmap 回転対象のBitmap
     * @param degrees 回転角度（正の値で時計回り、負の値で反時計回り）
     * @return 回転後の新しいBitmap
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * ビンテージ風フィルターを適用する私有メソッド
     *
     * セピア調の暖かい色味と軽微な彩度低下でクラシックな印象を演出。
     * ColorMatrixを使用して色相、彩度、明度を同時に調整。
     * 全体的に温かみのある色味と、レトロな雰囲気を作り出す。
     */
    private fun applyVintageFilter(bitmap: Bitmap): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            // 彩度を軽く落としてムードを出す
            setSaturation(0.8f)
            // セピア調の色味変換マトリックスを合成
            postConcat(ColorMatrix(floatArrayOf(
                1.2f, 0.2f, 0.1f, 0f, 10f,  // 赤色成分を強化
                0.1f, 1.1f, 0.1f, 0f, 5f,   // 緑色成分を微調整
                0.1f, 0.1f, 0.8f, 0f, -5f,  // 青色成分を抑制
                0f, 0f, 0f, 1f, 0f          // アルファ値は変更なし
            )))
        }
        return applyColorMatrix(bitmap, colorMatrix)
    }

    /**
     * モノクロフィルターを適用する私有メソッド
     *
     * 彩度を0に設定してグレースケールのモノクロ画像を作成。
     * 美術的な白黒写真の効果を演出し、被写体の形状やコントラストを強調。
     * 色情報を除去することで、組成や明暗に集中できる。
     */
    private fun applyMonochromeFilter(bitmap: Bitmap): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            // 彩度を0に設定して完全なグレースケールに変換
            setSaturation(0f)
        }
        return applyColorMatrix(bitmap, colorMatrix)
    }

    /**
     * 暖色系フィルターを適用する私有メソッド
     *
     * 赤・オレンジ系の色味を強化し、青系を抑制することで暖かい雰囲気を演出。
     * 太陽光、灯火、夕日などの暖かい光源をイメージした色調補正。
     * ポートレートや食物写真などで特に効果的。
     *
     * @param intensity 効果の強度（0.0～1.0、デフォルトは1.0）
     */
    private fun applyWarmFilter(bitmap: Bitmap, intensity: Float = 1f): Bitmap {
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f + intensity * 0.1f, 0f, 0f, 0f, intensity * 10f,  // 赤色を強化
            0f, 1f, 0f, 0f, intensity * 5f,                      // 緑色を微強化
            0f, 0f, 1f - intensity * 0.1f, 0f, -intensity * 5f,  // 青色を抑制
            0f, 0f, 0f, 1f, 0f                                   // アルファ値は変更なし
        ))
        return applyColorMatrix(bitmap, colorMatrix)
    }

    /**
     * 寒色系フィルターを適用する私有メソッド
     *
     * 青・シアン系の色味を強化し、赤系を抑制することで冷たい雰囲気を演出。
     * 蛍光灯、曇り空、冬の景色などの冷たい光源をイメージした色調補正。
     * クールで落ち着いた印象や、モダンでスタイリッシュな雰囲気を演出。
     *
     * @param intensity 効果の強度（0.0～1.0、デフォルトは1.0）
     */
    private fun applyCoolFilter(bitmap: Bitmap, intensity: Float = 1f): Bitmap {
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f - intensity * 0.1f, 0f, 0f, 0f, -intensity * 5f, // 赤色を抑制
            0f, 1f, 0f, 0f, 0f,                                  // 緑色はそのまま
            0f, 0f, 1f + intensity * 0.1f, 0f, intensity * 10f,  // 青色を強化
            0f, 0f, 0f, 1f, 0f                                   // アルファ値は変更なし
        ))
        return applyColorMatrix(bitmap, colorMatrix)
    }

    /**
     * 明度調整フィルターを適用する私有メソッド
     *
     * RGB各成分に一定値を加算して明度を調整。
     * 正の値で全体を明るく、負の値で全体を暗くする。
     * 線形変換なので、色のバランスは保たれる。
     *
     * @param brightness 明度調整値（-1.0～1.0の範囲、255倍してRGB値に加算）
     */
    private fun applyBrightnessFilter(bitmap: Bitmap, brightness: Float): Bitmap {
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightness * 255f, // 赤成分に明度調整値を加算
            0f, 1f, 0f, 0f, brightness * 255f, // 緑成分に明度調整値を加算
            0f, 0f, 1f, 0f, brightness * 255f, // 青成分に明度調整値を加算
            0f, 0f, 0f, 1f, 0f                 // アルファ値は変更なし
        ))
        return applyColorMatrix(bitmap, colorMatrix)
    }

    /**
     * コントラスト調整フィルターを適用する私有メソッド
     *
     * RGB各成分に乗算してコントラストを調整。
     * 1より大きい値でコントラストを強く、小さい値で弱くする。
     * 中間色（グレー）を基準にして、明暗の差を強調。
     *
     * @param contrast コントラスト値（1.0が標準、1.0より大で強いコントラスト）
     */
    private fun applyContrastFilter(bitmap: Bitmap, contrast: Float): Bitmap {
        // コントラスト調整時のオフセット値（中間色を基準に調整）
        val translate = (1f - contrast) * 0.5f * 255f
        val colorMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, translate, // 赤成分にコントラスト調整を適用
            0f, contrast, 0f, 0f, translate, // 緑成分にコントラスト調整を適用
            0f, 0f, contrast, 0f, translate, // 青成分にコントラスト調整を適用
            0f, 0f, 0f, 1f, 0f               // アルファ値は変更なし
        ))
        return applyColorMatrix(bitmap, colorMatrix)
    }

    /**
     * 彩度調整フィルターを適用する私有メソッド
     *
     * ColorMatrixの標準関数を使用して彩度を調整。
     * 1.0で標準、0.0でグレースケール、1.0より大で彩度強化。
     * 色相は保たれたまま、鮮やかさのみを変更。
     *
     * @param saturation 彩度値（1.0が標準、1.0より大で鮮やかに）
     */
    private fun applySaturationFilter(bitmap: Bitmap, saturation: Float): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            // ColorMatrixの標準関数で彩度を調整
            setSaturation(saturation)
        }
        return applyColorMatrix(bitmap, colorMatrix)
    }

    /**
     * ColorMatrixをBitmapに適用する共通処理メソッド
     *
     * 各フィルターで作成されたColorMatrixを実際の画像に適用。
     * CanvasとPaintを使用してGPUアクセラレーションを活用した高速処理。
     * 元のBitmapは変更せず、新しいBitmapを作成して返す。
     *
     * @param bitmap フィルター適用対象のBitmap
     * @param colorMatrix 適用する色変換マトリックス
     * @return フィルター適用後の新しいBitmap
     */
    private fun applyColorMatrix(bitmap: Bitmap, colorMatrix: ColorMatrix): Bitmap {
        // 元のBitmapと同じ設定で新しいBitmapを作成（編集可能）
        val result = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            // ColorMatrixをColorFilterとして設定
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        // 元のBitmapを新しいCanvasにフィルター付きで描画
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }
}