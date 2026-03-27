package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.CreateTodoRequest
import ru.custle.mobile.core.model.TodoDto
import ru.custle.mobile.core.model.UpdateTodoRequest
import ru.custle.mobile.core.network.CustleApi

class TodoRepository(
    private val api: CustleApi,
) {
    suspend fun list(): List<TodoDto> = api.todos().data

    suspend fun create(
        title: String,
        dueDate: String? = null,
        objectId: String? = null,
    ): TodoDto = api.createTodo(
        CreateTodoRequest(
            title = title,
            dueDate = dueDate,
            objectId = objectId,
        ),
    ).data

    suspend fun update(
        id: String,
        title: String,
        dueDate: String? = null,
        objectId: String? = null,
        isDone: Boolean? = null,
    ): TodoDto = api.updateTodo(
        id = id,
        body = UpdateTodoRequest(
            title = title,
            dueDate = dueDate,
            objectId = objectId,
            isDone = isDone,
        ),
    ).data

    suspend fun toggle(id: String): TodoDto = api.toggleTodo(id).data

    suspend fun delete(id: String) {
        api.deleteTodo(id)
    }
}
