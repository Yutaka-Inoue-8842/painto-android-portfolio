package com.yutakainoue.painto.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.yutakainoue.painto.domain.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 写真関連の操作を実装するリポジトリクラス
 *
 * AndroidのMediaStore APIやImageDecoderを使用して、
 * 写真の読み込み、保存、共有機能を提供する。
 * EXIF情報に基づいた回転補正やバージョン互換性も考慮。
 *
 * @param context Androidコンテキスト（ContentResolverやファイルアクセスに必要）
 */
class PhotoRepositoryImpl(
    private val context: Context
) : PhotoRepository {

    /**
     * URIから画像を読み込む処理
     *
     * Androidのバージョンに応じて適切なAPIを使用し、
     * 画像をBitmap形式で読み込み。EXIF情報に基づいた回転補正も適用。
     * メインスレッドをブロックしないようIOスレッドで実行。
     *
     * @param uri 読み込み対象の画像URI（フォトピッカーから取得）
     * @return 読み込み成功時は補正されたBitmap、失敗時はnull
     */
    override suspend fun loadImageFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Android P以降ではImageDecoder、それ以前は非EXIF用のgetBitmapを使用
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    // 編集可能なBitmapとして読み込み
                    decoder.isMutableRequired = true
                }
            } else {
                // 旧バージョン対応用の非EXIFメソッド
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            // EXIF情報に基づいた回転補正を適用
            correctOrientation(bitmap, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * EXIF情報に基づいた画像の回転補正処理
     *
     * カメラで撮影された写真はEXIF情報に回転情報が含まれているため、
     * その情報を読み取って適切な回転を適用し、正しい向きで表示されるようにする。
     * エラーが発生した場合は元のBitmapをそのまま返す。
     *
     * @param bitmap 補正対象の元のBitmap
     * @param uri EXIF情報を読み取るための画像URI
     * @return 回転補正されたBitmap（補正不要の場合は元のBitmap）
     */
    private fun correctOrientation(bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exifInterface = ExifInterface(inputStream)
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                // EXIFの回転情報に基づいて適切な角度で回転
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap // 回転不要または標準向き
                }
            } ?: bitmap
        } catch (e: IOException) {
            // EXIF読み取り失敗時は元のBitmapをそのまま返す
            bitmap
        }
    }

    /**
     * 指定された角度でBitmapを回転させる処理
     *
     * Matrixを使用して数学的な回転変換を適用。
     * 元のBitmapのサイズを維持しつつ、指定された角度で回転した新しいBitmapを作成。
     *
     * @param bitmap 回転対象のBitmap
     * @param degrees 回転角度（正の値で時計回り）
     * @return 回転された新しいBitmap
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * 画像をデバイスのフォトギャラリーに保存する処理
     *
     * MediaStore APIを使用して、他のアプリからもアクセス可能な形で画像を保存。
     * Android Q以降ではScoped Storageに対応し、Pictures/Paintoフォルダに保存。
     * JPEG形式で品質90%で圧縮してファイルサイズを最適化。
     *
     * @param bitmap 保存対象のBitmap
     * @param filename 保存するファイル名（拡張子含む）
     * @return 保存成功時は保存先のURI、失敗時はnull
     */
    override suspend fun saveImageToGallery(bitmap: Bitmap, filename: String): Uri? = withContext(Dispatchers.IO) {
        try {
            // MediaStoreに保存するためのメタデータを設定
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Scoped Storage対応: アプリ専用フォルダに保存
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Painto")
                    put(MediaStore.Images.Media.IS_PENDING, 1) // 書き込み中フラグ
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                // 実際の画像データを書き込み
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                // Android Q以降では書き込み完了を通知
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            }

            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 画像を共有用に一時保存する処理
     *
     * 共有インテントで使用するための一時ファイルを作成。
     * ファイル名にタイムスタンプを含めて重複を防止。
     * 内部的にはsaveImageToGalleryを使用してギャラリーに一時保存。
     *
     * @param bitmap 共有対象のBitmap
     * @return 保存成功時は一時ファイルのURI、失敗時はnull
     */
    override suspend fun shareImage(bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        try {
            // 共有用のユニークなファイル名を生成
            val filename = "painto_share_${System.currentTimeMillis()}.jpg"
            // ギャラリーに一時保存してURIを取得
            saveImageToGallery(bitmap, filename)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}