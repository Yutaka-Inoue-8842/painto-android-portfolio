package com.yutakainoue.painto.navigation

/**
 * アプリ内のナビゲーション先を定義するオブジェクト
 *
 * Jetpack Navigation Composeで使用するルート文字列の定数を一元管理。
 * 文字列の定数化により、画面遷移時のタイポエラーを防止し、
 * コードの保守性とリファクタリング容易性を向上させる。
 *
 * 各画面の役割:
 * - HOME: アプリのエントリーポイント（新規作成・ギャラリー選択）
 * - EDITOR: 画像編集とペイント機能を提供
 * - GALLERY: 保存されたプロジェクトの一覧表示と管理
 */
object PaintoDestination {
    /** ホーム画面のルート文字列 */
    const val HOME = "home"

    /** エディター画面のルート文字列 */
    const val EDITOR = "editor"

    /** ギャラリー画面のルート文字列 */
    const val GALLERY = "gallery"
}

/**
 * タイプセーフなナビゲーションルートを提供するsealed class
 *
 * PaintoDestinationの文字列定数をラップし、型安全性を提供。
 * sealed classによりコンパイル時に全ての画面遷移パターンを検証可能。
 * Navigation Composeライブラリとの統合において、
 * ルート指定のミスを防ぎ、IDEの補完機能を活用できる。
 *
 * @param route ナビゲーションに使用するルート文字列
 */
sealed class PaintoRoute(val route: String) {
    /** ホーム画面への遷移ルート */
    data object Home : PaintoRoute(PaintoDestination.HOME)

    /** エディター画面への遷移ルート */
    data object Editor : PaintoRoute(PaintoDestination.EDITOR)

    /** ギャラリー画面への遷移ルート */
    data object Gallery : PaintoRoute(PaintoDestination.GALLERY)
}