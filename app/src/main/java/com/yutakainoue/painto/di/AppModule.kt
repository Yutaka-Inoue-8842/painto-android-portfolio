package com.yutakainoue.painto.di

import com.yutakainoue.painto.data.repository.PhotoRepositoryImpl
import com.yutakainoue.painto.data.repository.ProjectRepositoryImpl
import com.yutakainoue.painto.domain.repository.PhotoRepository
import com.yutakainoue.painto.domain.repository.ProjectRepository
import com.yutakainoue.painto.domain.usecase.ImageEditingUseCase
import com.yutakainoue.painto.domain.usecase.UndoRedoUseCase
import com.yutakainoue.painto.presentation.editor.EditorViewModel
import com.yutakainoue.painto.presentation.gallery.GalleryViewModel
import com.yutakainoue.painto.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Paintoアプリの依存性注入モジュール定義
 *
 * Koin DIフレームワークを使用して、アプリケーション全体の依存関係を管理。
 * Clean Architectureの各層（Data、Domain、Presentation）のコンポーネントを
 * 適切なスコープとライフサイクルで定義し、自動的な依存性解決を提供する。
 *
 * 定義されるコンポーネント:
 * - Repositories: データアクセス層の実装（シングルトン）
 * - Use Cases: ビジネスロジック層（シングルトン）
 * - ViewModels: プレゼンテーション層のステート管理（ViewModel スコープ）
 *
 * スコープの説明:
 * - single: アプリケーション全体で単一インスタンス（シングルトン）
 * - viewModel: Android ViewModelのライフサイクルに連動（画面ごと）
 */
val appModule = module {

    // ==================== Repository層（データアクセス） ====================
    // Clean ArchitectureのData層として、外部データソースとのやり取りを担当

    /**
     * 写真関連の操作を提供するRepository
     *
     * MediaStore API、ImageDecoder、FileProviderを使用した
     * 写真の読み込み、保存、共有機能を提供。
     * get()でコンテキストを自動注入してリポジトリを初期化。
     */
    single<PhotoRepository> { PhotoRepositoryImpl(get()) }

    /**
     * プロジェクト状態管理Repository
     *
     * アプリケーション全体でのプロジェクト状態（現在編集中のプロジェクト）を
     * StateFlowを使用して管理。複数のViewModelが状態を共有するためシングルトン。
     */
    single<ProjectRepository> { ProjectRepositoryImpl() }

    // ==================== UseCase層（ビジネスロジック） ====================
    // Clean ArchitectureのDomain層として、ビジネスルールとロジックを担当

    /**
     * 画像編集機能のUseCase
     *
     * ColorMatrixを使用した画像フィルター処理（明度、コントラスト、彩度調整など）
     * とBitmapベースの画像変換処理を提供。複数画面で使用されるためシングルトン。
     */
    single { ImageEditingUseCase() }

    /**
     * Undo/Redo機能のUseCase
     *
     * ペイント操作の履歴管理と取り消し/やり直し機能を提供。
     * スタックベースの操作履歴管理でユーザーの編集フローを支援。
     */
    single { UndoRedoUseCase() }

    // ==================== ViewModel層（プレゼンテーション） ====================
    // MVIアーキテクチャのViewModelとして、UI状態とビジネスロジックを橋渡し

    /**
     * ホーム画面のViewModel
     *
     * 新規プロジェクト作成と保存済みプロジェクト選択の機能を提供。
     * PhotoRepository、ProjectRepository、ImageEditingUseCaseに依存し、
     * Koinが自動的に依存性を解決して注入。
     */
    viewModel { HomeViewModel(get(), get(), get()) }

    /**
     * エディター画面のViewModel
     *
     * ペイント機能、画像編集、Undo/Redo、保存処理の中核ビジネスロジックを担当。
     * 全てのUseCase及びRepositoryに依存し、最も複雑な依存関係を持つ。
     */
    viewModel { EditorViewModel(get(), get(), get(), get()) }

    /**
     * ギャラリー画面のViewModel
     *
     * 保存済みプロジェクトの一覧表示、選択、削除機能を提供。
     * ProjectRepositoryのみに依存するシンプルな構成。
     */
    viewModel { GalleryViewModel(get()) }
}