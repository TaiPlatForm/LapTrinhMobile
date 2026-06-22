package com.team.smartnutrition.meal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.smartnutrition.auth.data.UserRepository
import com.team.smartnutrition.meal.data.MealGeminiService
import com.team.smartnutrition.meal.data.MealRepository
import com.team.smartnutrition.meal.model.DayPlan
import com.team.smartnutrition.meal.model.Ingredient
import com.team.smartnutrition.meal.model.Meal
import com.team.smartnutrition.meal.model.MealPlan
import com.team.smartnutrition.meal.util.WeekUtils
import com.team.smartnutrition.pantry.data.PantryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════
 * MEAL PLAN UI STATE
 * ═══════════════════════════════════════════
 */
data class MealPlanUiState(
    val mealPlan: MealPlan? = null,          // Plan hiện tại (null = chưa generate)
    val selectedDayIndex: Int = 0,            // Tab đang chọn (0-6)
    val isLoading: Boolean = true,            // Loading ban đầu (đọc Firestore)
    val isGenerating: Boolean = false,        // Đang gọi Gemini AI
    val loadingMessage: String = "",          // Câu chữ vui cho loading dialog
    val errorMessage: String? = null,         // Lỗi hiển thị cho user
    val isGeneratingDetail: Boolean = false,  // Đang gọi AI tải chi tiết nguyên liệu + cách nấu cho 1 món
    val detailErrorMessage: String? = null    // Lỗi khi tải chi tiết
)

/**
 * ═══════════════════════════════════════════
 * MEAL PLAN VIEW MODEL
 * ═══════════════════════════════════════════
 *
 * Shared ViewModel cho MealPlanWeekScreen + MealDetailScreen.
 *
 * Actions:
 *   init → loadCurrentPlan()
 *   generateMealPlan() → Gemini + Firestore save → update UI
 *   selectDay(index) → update selectedDayIndex
 *   clearError() → clear errorMessage
 *
 * Cross-module reads:
 *   UserRepository (Module 1) → calorieTarget, proteinTarget, goal
 *   PantryRepository (Module 2) → available pantry items
 */
class MealPlanViewModel : ViewModel() {

    private val mealGeminiService = MealGeminiService()
    private val mealRepository = MealRepository()
    private val userRepository = UserRepository()
    private val pantryRepository = PantryRepository()

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    companion object {
        /** Danh sách câu loading vui vẻ, xoay vòng mỗi 2.5s */
        private val loadingMessages = listOf(
            "🤖 AI đang nghiên cứu dinh dưỡng cho bạn...",
            "🥗 Đang chọn nguyên liệu tươi nhất...",
            "👨‍🍳 Đang phối hợp thực đơn 7 ngày...",
            "📊 Đang cân đối calo và protein...",
            "🍲 Sắp xong rồi, chờ chút nhé..."
        )
    }

    init {
        loadCurrentPlan()
    }

    // ═══════════════════════════════════════════════════
    // PUBLIC ACTIONS
    // ═══════════════════════════════════════════════════

    /**
     * Gọi Gemini generate thực đơn tuần mới.
     * Flow: đọc user profile + pantry → build prompt → gọi AI → save Firestore → update UI
     */
    fun generateMealPlan() {
        val uid = mealRepository.currentUid ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    loadingMessage = loadingMessages[0],
                    errorMessage = null
                )
            }

            // Xoay vòng loading messages mỗi 2.5s để UX vui vẻ
            val messageJob = launch {
                var index = 1
                while (true) {
                    delay(2500)
                    _uiState.update {
                        it.copy(loadingMessage = loadingMessages[index % loadingMessages.size])
                    }
                    index++
                }
            }

            try {
                // 1. Đọc user profile
                val user = userRepository.getUser(uid)
                    ?: throw Exception("Chưa có thông tin cá nhân. Vui lòng cập nhật profile trước.")

                // Fallback nếu chưa setup profile đầy đủ
                val safeUser = if (user.calorieTarget == 0) {
                    user.copy(
                        calorieTarget = 2000,
                        proteinTarget = 75,
                        carbTarget = 250,
                        fatTarget = 65
                    )
                } else user

                // 2. Đọc pantry items (có thể trống — không crash)
                val pantryItems = try {
                    pantryRepository.getAvailableItems(uid)
                } catch (e: CancellationException) {
                    throw e // phải re-throw để coroutine cancel đúng
                } catch (e: Exception) {
                    emptyList() // Pantry lỗi → treat as trống, AI vẫn generate
                }

                // 3. Gọi Gemini AI
                val mealPlan = mealGeminiService.generateMealPlan(safeUser, pantryItems)

                // 4. Lưu plan mới (fire-and-forget, set() sẽ tự động ghi đè bản ghi cũ nếu trùng ID)
                mealRepository.saveMealPlan(uid, mealPlan)

                _uiState.update {
                    it.copy(
                        mealPlan = mealPlan,
                        selectedDayIndex = 0,
                        isGenerating = false,
                        errorMessage = null
                    )
                }
            } catch (e: CancellationException) {
                // Re-throw CancellationException để coroutine framework xử lý đúng
                // (TimeoutCancellationException cũng là CancellationException)
                // isGenerating sẽ được reset trong finally
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Lỗi không xác định"
                    )
                }
            } finally {
                // ✅ LUÔN reset isGenerating dù thành công, lỗi, hay bị timeout/cancel
                messageJob.cancel()
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    /**
     * Chọn ngày trong TabRow (index 0-6).
     */
    fun selectDay(index: Int) {
        _uiState.update { it.copy(selectedDayIndex = index) }
    }

    /**
     * Xóa error message sau khi user đã đọc.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Tải chi tiết nguyên liệu + cách nấu cho một bữa ăn cụ thể của một ngày.
     * Nếu món ăn đã có nguyên liệu -> bỏ qua (đã được lưu cache).
     * Nếu chưa có -> gọi Gemini, sau đó cập nhật đối tượng MealPlan và lưu đè lên Firestore.
     */
    fun loadMealDetail(dayIndex: Int, mealType: String) {
        val uid = mealRepository.currentUid ?: return
        val currentPlan = _uiState.value.mealPlan ?: return
        val day = currentPlan.days.getOrNull(dayIndex) ?: return
        val meal = day.meals[mealType] ?: return

        // Đã có chi tiết -> không gọi AI nữa
        if (meal.ingredients.isNotEmpty() && meal.recipe.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingDetail = true,
                    detailErrorMessage = null
                )
            }

            try {
                // 1. Đọc user profile để lấy mục tiêu sức khỏe
                val user = userRepository.getUser(uid)
                val userGoal = user?.goal ?: "maintain"
                val goalVi = when (userGoal) {
                    "lose_weight" -> "Giảm cân"
                    "gain_muscle" -> "Tăng cơ"
                    else -> "Duy trì cân nặng"
                }

                // 2. Gọi Gemini để lấy công thức chi tiết
                val detailResult = mealGeminiService.generateMealDetail(
                    mealName = meal.name,
                    userGoal = goalVi,
                    calorieTarget = meal.totalCalories
                )

                // 3. Cập nhật đối tượng MealPlan mới
                val updatedIngredients = detailResult.ingredients.map {
                    Ingredient(name = it.name, amount = it.amount)
                }

                val updatedMeal = meal.copy(
                    ingredients = updatedIngredients,
                    recipe = detailResult.recipe
                )

                val updatedMeals = day.meals.toMutableMap().apply {
                    put(mealType, updatedMeal)
                }

                val updatedDay = day.copy(meals = updatedMeals)

                val updatedDays = currentPlan.days.toMutableList().apply {
                    set(dayIndex, updatedDay)
                }

                val updatedPlan = currentPlan.copy(days = updatedDays)

                // 4. Lưu đè lên local cache & Firestore (offline-first)
                mealRepository.saveMealPlan(uid, updatedPlan)

                // 5. Cập nhật UI State
                _uiState.update {
                    it.copy(
                        mealPlan = updatedPlan,
                        isGeneratingDetail = false,
                        detailErrorMessage = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingDetail = false,
                        detailErrorMessage = "Không thể tải công thức: ${e.message ?: "Lỗi kết nối"}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // HELPER PROPERTIES
    // ═══════════════════════════════════════════════════

    /** DayPlan đang được chọn (cho MealPlanWeekScreen) */
    val selectedDay: DayPlan?
        get() {
            val state = _uiState.value
            return state.mealPlan?.days?.getOrNull(state.selectedDayIndex)
        }

    /**
     * Lấy Meal cụ thể theo dayIndex + mealType.
     * Dùng cho MealDetailScreen.
     */
    fun getMeal(dayIndex: Int, mealType: String): Meal? {
        return _uiState.value.mealPlan?.days?.getOrNull(dayIndex)?.meals?.get(mealType)
    }

    // ═══════════════════════════════════════════════════
    // PRIVATE
    // ═══════════════════════════════════════════════════

    /**
     * Load meal plan tuần hiện tại từ Firestore.
     * Nếu không có plan tuần này → thử load plan gần nhất.
     * Nếu không có plan nào → hiện empty state.
     */
    private fun loadCurrentPlan() {
        val uid = mealRepository.currentUid ?: run {
            // User chưa login → bỏ loading, hiện empty state
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val weekId = WeekUtils.getCurrentWeekId()

                // Thử load plan tuần hiện tại
                var plan = mealRepository.getMealPlan(uid, weekId)

                // Fallback: load plan gần nhất nếu tuần này chưa có
                if (plan == null) {
                    plan = mealRepository.getLatestMealPlan(uid)
                }

                _uiState.update {
                    it.copy(
                        mealPlan = plan,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải thực đơn: ${e.message}"
                    )
                }
            }
        }
    }
}
