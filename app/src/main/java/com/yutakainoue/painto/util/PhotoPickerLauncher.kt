package com.yutakainoue.painto.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 写真選択機能を提供するComposable関数
 *
 * AndroidのPhoto Picker APIを使用して、デバイス上の画像を選択するランチャーを作成。
 * Android 13以降の新しいPhoto Picker APIと、それ以前のバージョン向けのレガシーピッカーの
 * 両方に対応し、OSバージョンに応じて最適なUIを提供する。
 *
 * 主な機能:
 * - Android 13+ : プライバシー重視の新Photo Picker UI
 * - Android 12以下: 従来のファイル選択インターフェース
 * - 自動的なバージョン判定と適切なピッカーの選択
 * - ユーザーのキャンセル操作への対応
 *
 * @param onPhotoSelected 画像が選択された時のコールバック（選択されたURI）
 * @param onPickerClosed ユーザーがピッカーをキャンセルした時のコールバック（デフォルト: 何もしない）
 * @return PhotoPickerLancherインスタンス（launch()でピッカーを起動）
 */
@Composable
fun rememberPhotoPickerLauncher(
    onPhotoSelected: (Uri) -> Unit,
    onPickerClosed: () -> Unit = {}
): PhotoPickerLauncher {
    val context = LocalContext.current

    // Android 13以降のモダンなPhoto Pickerを使用、それ以前はレガシーピッカーを使用
    // 新しいPhoto Picker: プライバシー重視で、全画像へのアクセス権限が不要
    val modernPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // 画像が選択された場合: 選択されたURIをコールバックに渡す
            onPhotoSelected(uri)
        } else {
            // ユーザーがキャンセルまたはスワイプで閉じた場合
            onPickerClosed()
        }
    }

    // Android 12以下用のレガシーピッカー: GetContentインテントを使用
    // より広範囲のファイルアクセスが必要だが、既存デバイスとの互換性を保つ
    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // 画像が選択された場合: 選択されたURIをコールバックに渡す
            onPhotoSelected(uri)
        } else {
            // ユーザーがキャンセルまたはスワイプで閉じた場合
            onPickerClosed()
        }
    }

    // ランチャーのインスタンスをrememberで記憶し、再コンポジション時の無駄な再生成を防ぐ
    // キーとしてlauncher、context、callbackの変更を監視
    return remember(modernPickerLauncher, legacyPickerLauncher, context) {
        PhotoPickerLauncher(
            modernPickerLauncher = modernPickerLauncher,
            legacyPickerLauncher = legacyPickerLauncher,
            context = context
        )
    }
}

/**
 * 写真選択ランチャークラス
 *
 * OSバージョンに応じて適切な写真選択UIを起動するラッパークラス。
 * Android 13以降の新Photo Picker APIと、それ以前のレガシーAPI両方に対応し、
 * 実行時にデバイスの対応状況を判定して最適なピッカーを自動選択する。
 *
 * @param modernPickerLauncher Android 13+用の新Photo Pickerランチャー
 * @param legacyPickerLauncher Android 12以下用のレガシーピッカーランチャー
 * @param context 実行コンテキスト（Photo Picker可用性の判定に使用）
 */
class PhotoPickerLauncher(
    private val modernPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    private val legacyPickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    private val context: Context
) {
    /**
     * 写真選択ピッカーを起動
     *
     * デバイスのAndroidバージョンとPhoto Pickerの可用性を自動判定し、
     * 最適なピッカーUIを起動する。ユーザーは選択結果に応じて、
     * 初期化時に設定されたコールバック関数が呼び出される。
     *
     * 判定ロジック:
     * 1. Android 13 (API 33) 以降かチェック
     * 2. Photo Picker APIが利用可能かチェック
     * 3. 両方の条件を満たす場合: 新Photo Picker使用
     * 4. いずれかの条件を満たさない場合: レガシーピッカー使用
     */
    fun launch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
            // Android 13以降 && Photo Picker API利用可能: モダンなPhoto Pickerを使用
            // プライバシー重視のUIで、READ_EXTERNAL_STORAGE権限が不要
            modernPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            // Android 12以下 || Photo Picker API非対応: レガシーピッカーを使用
            // GetContentインテントによる従来のファイル選択UI
            legacyPickerLauncher.launch("image/*")
        }
    }
}