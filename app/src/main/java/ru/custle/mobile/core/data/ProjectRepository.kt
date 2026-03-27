package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.custle.mobile.core.model.InviteObjectParticipantRequest
import ru.custle.mobile.core.model.ObjectDetailBundle
import ru.custle.mobile.core.model.ObjectNodeDto
import ru.custle.mobile.core.model.ParticipantDto
import ru.custle.mobile.core.model.UpdateWorkspaceMemberRoleRequest
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

        ObjectDetailBundle(
            detail = detail.await(),
            ancestors = ancestors.await(),
            participants = participants.await(),
            plans = plans.await(),
            dependencies = dependencies.await(),
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
}
