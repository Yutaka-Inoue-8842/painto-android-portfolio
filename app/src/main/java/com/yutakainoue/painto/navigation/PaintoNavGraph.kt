package com.yutakainoue.painto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yutakainoue.painto.presentation.editor.EditorScreen
import com.yutakainoue.painto.presentation.gallery.GalleryScreen
import com.yutakainoue.painto.presentation.home.HomeScreen

/**
 * Paintoアプリのナビゲーショングラフを定義するComposable関数
 *
 * Jetpack Navigation Composeを使用して、アプリ内の画面遷移とナビゲーション体系を管理。
 * 3つの主要画面（ホーム・エディター・ギャラリー）間の遷移ロジックと、
 * バックスタック管理を統合的に提供する。
 *
 * 画面遷移の構造:
 * - HOME ← → EDITOR: 新規作成・編集の双方向遷移
 * - HOME ← → GALLERY: ギャラリー閲覧・選択の双方向遷移
 * - GALLERY → EDITOR: 保存済みプロジェクトの編集遷移
 *
 * @param navController ナビゲーション制御を行うNavHostController
 * @param startDestination アプリ起動時の初期画面（デフォルト: ホーム画面）
 */
@Composable
fun PaintoNavGraph(
    navController: NavHostController,
    startDestination: String = PaintoDestination.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ホーム画面: アプリのエントリーポイント
        // 新規プロジェクト作成とギャラリー閲覧の選択画面
        composable(PaintoDestination.HOME) {
            HomeScreen(
                onNavigateToEditor = {
                    // エディター画面への遷移: 新規プロジェクト作成
                    // saveState = true: ホーム画面の状態を保持してスムーズな戻り遷移を実現
                    // launchSingleTop = true: 同一画面の重複起動を防止
                    navController.navigate(PaintoDestination.EDITOR) {
                        popUpTo(PaintoDestination.HOME) { saveState = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToGallery = {
                    // ギャラリー画面への遷移: 保存済みプロジェクト一覧
                    // バックスタック管理でホーム画面への適切な戻り機能を提供
                    navController.navigate(PaintoDestination.GALLERY) {
                        popUpTo(PaintoDestination.HOME) { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // エディター画面: 画像編集とペイント機能の中核画面
        // ホーム画面からの新規作成 or ギャラリーからの編集継続
        composable(PaintoDestination.EDITOR) {
            EditorScreen(
                onNavigateBack = {
                    // 戻るボタン: 前の画面（ホーム or ギャラリー）への遷移
                    // popBackStack()で自然なナビゲーション体験を提供
                    navController.popBackStack()
                }
            )
        }

        // ギャラリー画面: 保存済みプロジェクトの一覧表示・管理
        // プロジェクトの選択、削除、編集継続機能を提供
        composable(PaintoDestination.GALLERY) {
            GalleryScreen(
                onNavigateBack = {
                    // 戻るボタン: ホーム画面への遷移
                    // ギャラリーからホームへの直感的な戻り機能
                    navController.popBackStack()
                }
            )
        }
    }
}