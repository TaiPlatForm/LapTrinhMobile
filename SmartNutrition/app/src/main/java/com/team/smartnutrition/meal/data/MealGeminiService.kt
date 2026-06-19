package com.team.smartnutrition.meal.data

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.team.smartnutrition.BuildConfig
import com.team.smartnutrition.auth.model.User
import com.team.smartnutrition.meal.model.DayPlan
import com.team.smartnutrition.meal.model.Ingredient
import com.team.smartnutrition.meal.model.Meal
import com.team.smartnutrition.meal.model.MealPlan
import com.team.smartnutrition.meal.util.WeekUtils
import com.team.smartnutrition.pantry.model.PantryItem
import kotlinx.coroutines.withTimeoutOrNull

/**
 * ═══════════════════════════════════════════
 * MEAL GEMINI SERVICE - Gọi Gemini text API để lên thực đơn
 * ═══════════════════════════════════════════
 *
 * Pipeline:
 *   User profile + Pantry items → build prompt → Gemini API → JSON parse → MealPlan
 *
 * 4 tầng phòng thủ chống JSON sai:
 *   1. Prompt ép trả JSON thuần
 *   2. Strip markdown wrapper (```json...```)
 *   3. Gson parse với try-catch
 *   4. Validate structure: 7 ngày, 3 bữa, tên không blank
 */
class MealGeminiService {

    companion object {
        private const val TAG = "MealGeminiService"
        private const val MODEL_NAME = "gemini-2.5-flash"
        private const val TIMEOUT_MS = 60_000L  // 60s cho response lớn

        /** System prompt tiếng Việt - ép AI trả JSON chuẩn */
        private const val SYSTEM_PROMPT = """
Bạn là chuyên gia dinh dưỡng Việt Nam. Lên thực đơn 7 ngày (Thứ 2 → Chủ Nhật), mỗi ngày 3 bữa (breakfast, lunch, dinner).

PHONG CÁCH: Món ăn gia đình Việt Nam, dễ nấu, lành mạnh, đa dạng nguyên liệu giữa các ngày.

QUY TẮC:
- Ưu tiên sử dụng nguyên liệu từ danh sách "Kho thực phẩm" bên dưới (nếu có)
- Calo mỗi ngày bám sát mục tiêu được cung cấp (±10%)
- KHÔNG lặp lại cùng 1 món trong tuần
- Mỗi bữa phải có đủ: tên món, calo, protein, nguyên liệu kèm định lượng, công thức nấu

TRẢ VỀ ĐÚNG JSON theo format sau, KHÔNG giải thích thêm, KHÔNG wrap trong markdown:
{
  "days": [
    {
      "dayLabel": "Thứ 2",
      "meals": {
        "breakfast": {
          "name": "tên món bằng tiếng Việt",
          "totalCalories": 450,
          "totalProtein": 25,
          "ingredients": [{"name": "nguyên liệu", "amount": "100g"}],
          "recipe": "1. Bước 1\n2. Bước 2\n3. Bước 3"
        },
        "lunch": {},
        "dinner": {}
      }
    }
  ]
}

QUAN TRỌNG: Chỉ trả JSON thuần, KHÔNG wrap trong ```json, KHÔNG giải thích."""
    }

    // Lazy init để throw rõ ràng nếu API key trống
    private val model: GenerativeModel by lazy {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            throw IllegalStateException(
                "GEMINI_API_KEY chưa được cấu hình. " +
                "Thêm dòng GEMINI_API_KEY=your_key vào file local.properties"
            )
        }
        GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey)
    }

    // ═══════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════

    /**
     * Gọi Gemini để generate thực đơn cả tuần.
     * @throws Exception nếu timeout, parse fail, hoặc validation fail
     */
    suspend fun generateMealPlan(
        user: User,
        pantryItems: List<PantryItem>
    ): MealPlan {
        val userPrompt = buildUserPrompt(user, pantryItems)

        // Gọi Gemini với timeout 60s
        val response = withTimeoutOrNull(TIMEOUT_MS) {
            model.generateContent(
                content {
                    text(SYSTEM_PROMPT)
                    text(userPrompt)
                }
            )
        } ?: throw Exception("Timeout khi gọi AI lên thực đơn (quá 60 giây)")

        val responseText = response.text
            ?: throw Exception("AI không trả về kết quả")

        Log.d(TAG, "Gemini response length: ${responseText.length}")
        Log.d(TAG, "Gemini response preview: ${responseText.take(300)}")

        // Parse + validate
        return parseAndValidate(responseText, user.calorieTarget)
    }

    // ═══════════════════════════════════════════════════
    // PRIVATE: BUILD PROMPT
    // ═══════════════════════════════════════════════════

    /**
     * Tạo user-specific prompt từ profile + pantry items.
     */
    private fun buildUserPrompt(
        user: User,
        pantryItems: List<PantryItem>
    ): String {
        val goalVi = when (user.goal) {
            "lose_weight" -> "Giảm cân"
            "gain_muscle" -> "Tăng cơ"
            else -> "Duy trì cân nặng"
        }

        val pantrySection = if (pantryItems.isEmpty()) {
            "KHO THỰC PHẨM: Đang trống — hãy gợi ý thực đơn tự do bám sát chỉ số calo mục tiêu."
        } else {
            val itemsList = pantryItems.joinToString("\n") { item ->
                "- ${item.name}: ${item.caloriesPer100g} kcal/100g, " +
                "${item.proteinPer100g}g protein/100g, " +
                "còn ${item.quantityGrams}${item.unit}, " +
                "trạng thái: ${item.status}"
            }
            "KHO THỰC PHẨM HIỆN CÓ (ưu tiên sử dụng, đặc biệt items sắp hết hạn):\n$itemsList"
        }

        return """
THÔNG TIN NGƯỜI DÙNG:
- Mục tiêu: $goalVi
- Calo mục tiêu: ${user.calorieTarget} kcal/ngày
- Protein mục tiêu: ${user.proteinTarget}g/ngày
- Carb mục tiêu: ${user.carbTarget}g/ngày
- Fat mục tiêu: ${user.fatTarget}g/ngày

$pantrySection

Hãy lên thực đơn 7 ngày theo đúng format JSON đã hướng dẫn."""
    }

    // ═══════════════════════════════════════════════════
    // PRIVATE: JSON PARSE (4 tầng phòng thủ)
    // ═══════════════════════════════════════════════════

    // Tầng 1: Intermediate classes cho Gson parse
    private data class GeminiMealResponse(
        val days: List<GeminiDayResponse> = emptyList()
    )
    private data class GeminiDayResponse(
        val dayLabel: String = "",
        val meals: Map<String, GeminiMealItem> = emptyMap()
    )
    private data class GeminiMealItem(
        val name: String = "",
        val totalCalories: Int = 0,
        val totalProtein: Int = 0,
        val ingredients: List<GeminiIngredient> = emptyList(),
        val recipe: String = ""
    )
    private data class GeminiIngredient(
        val name: String = "",
        val amount: String = ""
    )

    /**
     * Parse + validate Gemini response → MealPlan.
     * Ném Exception rõ ràng nếu structure sai.
     */
    private fun parseAndValidate(responseText: String, calorieTarget: Int): MealPlan {
        // Tầng 2: Strip markdown wrapper
        val jsonString = stripMarkdownWrapper(responseText)

        // Tầng 3: Gson parse
        val geminiResponse = try {
            Gson().fromJson(jsonString, GeminiMealResponse::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON parse fail: ${jsonString.take(300)}", e)
            throw Exception("AI trả kết quả không đúng format. Vui lòng thử lại.")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected parse error", e)
            throw Exception("Lỗi xử lý kết quả từ AI. Vui lòng thử lại.")
        }

        // Tầng 4: Validate structure
        if (geminiResponse.days.isEmpty()) {
            throw Exception("AI không trả về thực đơn. Vui lòng thử lại.")
        }
        if (geminiResponse.days.size != 7) {
            Log.w(TAG, "AI returned ${geminiResponse.days.size} days instead of 7")
            throw Exception("AI chỉ trả về ${geminiResponse.days.size} ngày thay vì 7. Thử lại.")
        }
        geminiResponse.days.forEach { day ->
            if (day.meals.size < 3) {
                throw Exception("Ngày '${day.dayLabel}' thiếu bữa ăn. Thử lại.")
            }
            day.meals.values.forEach { meal ->
                if (meal.name.isBlank()) {
                    throw Exception("Có bữa ăn thiếu tên món. Thử lại.")
                }
            }
        }

        // Convert → domain model
        return toMealPlan(geminiResponse, calorieTarget)
    }

    /**
     * Convert Gemini intermediate model → domain MealPlan.
     */
    private fun toMealPlan(response: GeminiMealResponse, calorieTarget: Int): MealPlan {
        val days = response.days.map { dayResp ->
            val meals = dayResp.meals.mapValues { (_, mealResp) ->
                Meal(
                    name = mealResp.name,
                    totalCalories = mealResp.totalCalories,
                    totalProtein = mealResp.totalProtein,
                    ingredients = mealResp.ingredients.map {
                        Ingredient(name = it.name, amount = it.amount)
                    },
                    recipe = mealResp.recipe
                )
            }
            val dayCalories = meals.values.sumOf { it.totalCalories }
            val dayProtein = meals.values.sumOf { it.totalProtein }
            DayPlan(
                dayLabel = dayResp.dayLabel,
                meals = meals,
                totalCalories = dayCalories,
                totalProtein = dayProtein
            )
        }

        return MealPlan(
            weekId = WeekUtils.getCurrentWeekId(),
            days = days,
            totalCalories = days.sumOf { it.totalCalories },
            calorieTarget = calorieTarget,
            createdAt = Timestamp.now()
        )
    }

    /**
     * Strip markdown code block wrapper.
     * Gemini đôi khi trả: ```json\n{...}\n``` thay vì pure JSON.
     */
    private fun stripMarkdownWrapper(text: String): String {
        var result = text.trim()
        if (result.startsWith("```")) {
            result = result.removePrefix("```json")
                .removePrefix("```JSON")
                .removePrefix("```")
            if (result.endsWith("```")) {
                result = result.removeSuffix("```")
            }
        }
        return result.trim()
    }
}
