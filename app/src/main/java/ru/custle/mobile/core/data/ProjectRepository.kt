package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.custle.mobile.core.model.InviteObjectParticipantRequest
import ru.custle.mobile.core.model.ObjectDetailBundle
import ru.custle.mobile.core.model.ObjectNodeDto
import ru.custle.mobile.core.model.ParticipantDto
import ru.custle.mobile.core.model.UpdateWorkspaceMemberRoleRequest
import ru.custle.mobile.core.model.WidgetPlacement
import ru.custle.mobile.core.network.CustleApi

class ProjectRepository(
    private val api: CustleApi,
) {
    suspend fun tree(): List<ObjectNodeDto> = api.objectTree().data

    suspend fun detail(id: String): ObjectDetailBundle = coroutineScope {
        val detail = async { api.objectDetail(id).data }
        val ancestors = async { api.objectAncestors(id).data }
        val participants = async { api.objectParticipants(id).data }
        val plans = async { api.objectPlans(id).data }
        val dependencies = async { api.objectDependencies(id).data }

        val detailResult = detail.await()

        // Load widget layout for this object's type
        val layouts = async {
            runCatching {
                api.widgetLayouts(
                    pageType = "object-main",
                    objectId = detailResult.id,
                    typeId = detailResult.typeId,
                ).data
            }.getOrDefault(emptyList())
        }

        // Load requisite names for field_values display
        val requisites = async {
            runCatching { api.requisites().data }.getOrDefault(emptyList())
        }

        val placements = resolveWidgetPlacements(layouts.await())
        val reqNames = requisites.await().associate { it.id to it.name }

        ObjectDetailBundle(
            detail = detailResult,
            ancestors = ancestors.await(),
            participants = participants.await(),
            plans = plans.await(),
            dependencies = dependencies.await(),
            widgetPlacements = placements,
            requisiteNames = reqNames,
        )
    }

    suspend fun participants(id: String): List<ParticipantDto> = api.objectParticipants(id).data

    suspend fun addParticipant(objectId: String, userId: String, role: String) {
        api.addObjectParticipant(
            id = objectId,
            body = InviteObjectParticipantRequest(userId = userId, role = role),
        )
    }

    suspend fun updateParticipantRole(objectId: String, userId: String, role: String) {
        api.updateObjectParticipantRole(
            id = objectId,
            userId = userId,
            body = UpdateWorkspaceMemberRoleRequest(role = role),
        )
    }

    suspend fun removeParticipant(objectId: String, userId: String) {
        api.removeObjectParticipant(
            id = objectId,
            userId = userId,
        )
    }

    companion object {
        /** Default widget placements when no layout is configured */
        val DEFAULT_PLACEMENTS = listOf(
            WidgetPlacement(widgetId = "status-metrics", colSpan = 4, order = 0),
            WidgetPlacement(widgetId = "dates", colSpan = 8, order = 1),
            WidgetPlacement(widgetId = "requisites", colSpan = 6, order = 2),
            WidgetPlacement(widgetId = "description", colSpan = 6, order = 3),
            WidgetPlacement(widgetId = "hierarchy", colSpan = 12, order = 4),
        )

        /**
         * Resolve the best layout from the multi-tier system.
         * Priority: user-object > user-global > admin-object > admin-type > admin-all > builtin
         */
        private fun resolveWidgetPlacements(
            layouts: List<ru.custle.mobile.core.model.WidgetLayoutDto>,
        ): List<WidgetPlacement> {
            // Sort by priority: user scope first, then more specific
            val best = layouts
                .sortedWith(compareBy(
                    { if (it.scope == "user") 0 else 1 },
                    { if (it.objectId != null) 0 else if (it.typeId != null) 1 else 2 },
                ))
                .firstOrNull() ?: return DEFAULT_PLACEMENTS

            return parsePlacements(best.layout) ?: DEFAULT_PLACEMENTS
        }

        private fun parsePlacements(layout: kotlinx.serialization.json.JsonElement?): List<WidgetPlacement>? {
            if (layout == null) return null
            return try {
                val obj = layout.jsonObject
                val placementsArray = obj["placements"]?.jsonArray ?: return null
                placementsArray.mapNotNull { element ->
                    val p = element.jsonObject
                    val widgetId = (p["widgetId"] ?: p["widget_id"])?.jsonPrimitive?.content ?: return@mapNotNull null
                    val visible = p["visible"]?.jsonPrimitive?.booleanOrNull ?: true
                    if (!visible) return@mapNotNull null
                    WidgetPlacement(
                        widgetId = widgetId,
                        colSpan = p["colSpan"]?.jsonPrimitive?.intOrNull ?: p["col_span"]?.jsonPrimitive?.intOrNull ?: 12,
                        order = p["order"]?.jsonPrimitive?.intOrNull ?: 0,
                        visible = true,
                        title = p["title"]?.jsonPrimitive?.content,
                    )
                }.sortedBy { it.order }
            } catch (_: Exception) {
                null
            }
        }
    }
}
