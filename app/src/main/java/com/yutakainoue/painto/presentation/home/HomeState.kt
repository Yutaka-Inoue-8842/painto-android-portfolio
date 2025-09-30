package com.yutakainoue.painto.presentation.home

import com.yutakainoue.painto.core.mvi.UiState

/**
 * ホーム画面のUI状態を表すデータクラス
 *
 * MVIアーキテクチャにおけるModel部分として、
 * ホーム画面のUIに関する全ての状態情報を保持する。
 *
 * @param isLoading ローディング状態（true: 読み込み中、false: 通常状態）
 * @param errorMessage エラーメッセージ（null: エラーなし、String: エラー内容）
 * @param isPhotoPickerOpen フォトピッカーの開閉状態（true: 開いている、false: 閉じている）
 */
data class HomeState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPhotoPickerOpen: Boolean = false
) : UiState