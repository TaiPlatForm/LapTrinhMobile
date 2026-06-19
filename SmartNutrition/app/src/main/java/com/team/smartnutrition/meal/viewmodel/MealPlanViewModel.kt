package com.team.smartnutrition.meal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.smartnutrition.auth.data.UserRepository
import com.team.smartnutrition.meal.data.MealGeminiService
import com.team.smartnutrition.meal.data.MealRepository
import com.team.smartnutrition.meal.model.DayPlan
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
    val errorMessage: String? = null          // Lỗi hiển thị cho user
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

                // 4. Xóa plan cũ tuần này (nếu có) → lưu plan mới (fire-and-forget)
                val weekId = WeekUtils.getCurrentWeekId()
                mealRepository.deleteMealPlan(uid, weekId)
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
