package com.yutakainoue.painto.domain.repository

import android.graphics.Bitmap
import android.net.Uri

/**
 * 写真関連の操作を定義するリポジトリインターフェース
 *
 * Clean Architectureのドメイン層で定義され、データ層で実装される。
 * 写真の読み込み、保存、共有に関する抽象化された操作を提供。
 * プラットフォーム固有の実装詳細を隐蔽し、テスタブルで保守可能なコードを実現。
 */
interface PhotoRepository {

    /**
     * 指定されたURIから画像を読み込む
     *
     * フォトピッカーやカメラから取得したURIを使用して、
     * Bitmap形式で画像を読み込み。EXIF情報に基づいた回転補正も適用。
     *
     * @param uri 読み込み対象の画像URI
     * @return 読み込み成功時はBitmap、失敗時はnull
     */
    suspend fun loadImageFromUri(uri: Uri): Bitmap?

    /**
     * Bitmapをデバイスのフォトギャラリーに保存
     *
     * MediaStore APIを使用して、他のアプリからもアクセス可能な形で画像を保存。
     * スコープドストレージに対応し、適切なフォルダ構造で保存。
     *
     * @param bitmap 保存対象のBitmap
     * @param filename 保存するファイル名（拡張子含む）
     * @return 保存成功時は保存先URI、失敗時はnull
     */
    suspend fun saveImageToGallery(bitmap: Bitmap, filename: String): Uri?

    /**
     * Bitmapを共有用に一時保存
     *
     * 他のアプリでの共有に使用するための一時ファイルを作成。
     * 共有インテントで使用できるURIを返す。
     *
     * @param bitmap 共有対象のBitmap
     * @return 共有用ファイルのURI、失敗時はnull
     */
    suspend fun shareImage(bitmap: Bitmap): Uri?
}