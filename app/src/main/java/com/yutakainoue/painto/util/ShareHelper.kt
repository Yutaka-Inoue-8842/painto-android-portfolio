package com.yutakainoue.painto.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat

/**
 * 画像共有機能を提供するユーティリティオブジェクト
 *
 * AndroidのSystem共有機能を使用して、編集後の画像を他のアプリやサービスで共有。
 * Intent.ACTION_SENDを使用して汎用的な共有インターフェースを提供し、
 * SNS、メール、クラウドストレージなどへの共有を可能にする。
 */
object ShareHelper {

    /**
     * 画像URIを他のアプリで共有する
     *
     * Androidの標準共有インテントを使用して、ユーザーが選択したアプリで画像を共有。
     * システムが対応する全てのアプリ（SNS、メール、クラウドストレージなど）が選択肢として表示される。
     * ファイルアクセス権限を適切に設定してセキュリティを確保。
     *
     * @param context Androidコンテキスト（インテント起動に必要）
     * @param imageUri 共有する画像のURI（MediaStoreやFileProvider経由で取得）
     * @param title 共有ダイアログのタイトル（デフォルト: "Share Image"）
     */
    fun shareImage(context: Context, imageUri: Uri, title: String = "Share Image") {
        // 共有用のインテントを作成
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND        // 単一ファイルの共有アクション
            type = "image/*"                  // MIMEタイプで画像ファイルであることを明示
            putExtra(Intent.EXTRA_STREAM, imageUri) // 共有するファイルのURIを追加
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 一時的な読み取り権限を付与
        }

        // システムのアプリ選択ダイアログを作成
        val chooser = Intent.createChooser(shareIntent, title)

        // 対応するアプリが存在するかチェックしてから起動
        if (chooser.resolveActivity(context.packageManager) != null) {
            ContextCompat.startActivity(context, chooser, null)
        }
    }
}