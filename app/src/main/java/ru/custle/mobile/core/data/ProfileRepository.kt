package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.TelegramLinkCodeDto
import ru.custle.mobile.core.model.TelegramStatusDto
import ru.custle.mobile.core.model.UpdateProfileRequest
import ru.custle.mobile.core.model.UpdateTelegramSettingsRequest
import ru.custle.mobile.core.model.UpdateWorkspaceMemberRoleRequest
import ru.custle.mobile.core.model.UserDto
import ru.custle.mobile.core.model.WorkspaceMemberDto
import ru.custle.mobile.core.model.InviteWorkspaceMemberRequest
import ru.custle.mobile.core.network.CustleApi

class ProfileRepository(
    private val api: CustleApi,
) {
    suspend fun updateProfile(firstName: String, lastName: String, email: String): UserDto =
        api.updateProfile(
            UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
            )
        ).data

    suspend fun telegramStatus(): TelegramStatusDto = api.telegramStatus().data

    suspend fun workspaceMembers(): List<WorkspaceMemberDto> = api.workspaceMembers().data

    suspend fun inviteMember(email: String, role: String): String =
        api.inviteWorkspaceMember(InviteWorkspaceMemberRequest(email = email, role = role)).data.token

    suspend fun updateWorkspaceMemberRole(userId: String, role: String) {
        api.updateWorkspaceMemberRole(userId, UpdateWorkspaceMemberRoleRequest(role))
    }

    suspend fun removeWorkspaceMember(userId: String) {
        api.removeWorkspaceMember(userId)
    }

    suspend fun generateTelegramCode(): TelegramLinkCodeDto = api.telegramLinkCode().data

    suspend fun updateTelegramAutoDelete(minutes: Int) {
        api.updateTelegramSettings(UpdateTelegramSettingsRequest(minutes))
    }
}
