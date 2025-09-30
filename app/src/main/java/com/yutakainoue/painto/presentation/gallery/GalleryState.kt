package com.yutakainoue.painto.presentation.gallery

import com.yutakainoue.painto.core.mvi.UiState
import com.yutakainoue.painto.data.model.PaintingProject

/**
 * ギャラリー画面のUI状態を表すデータクラス
 *
 * MVIアーキテクチャにおけるModel部分として、
 * ギャラリー画面のUIに関する全ての状態情報を保持する。
 * 保存されたプロジェクト一覧、ローディング状態、エラー状態などを管理。
 *
 * @param isLoading ローディング状態（true: 読み込み中、false: 通常状態）
 * @param errorMessage エラーメッセージ（null: エラーなし、String: エラー内容）
 * @param projects 保存されたプロジェクトの一覧（空リストの場合はプロジェクトなし）
 */
data class GalleryState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val projects: List<PaintingProject> = emptyList()
) : UiState