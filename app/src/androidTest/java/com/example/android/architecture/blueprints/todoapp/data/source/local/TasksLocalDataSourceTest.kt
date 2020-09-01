package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.Result.Success
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class TasksLocalDataSourceTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var localDataSource: TasksLocalDataSource
    private lateinit var database: ToDoDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                ToDoDatabase::class.java
        ).allowMainThreadQueries().build()

        localDataSource = TasksLocalDataSource(database.taskDao(), Dispatchers.Main)
    }

    @After
    fun cleanup() = database.close()

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO: Replace with runBlockingTest once issue is resolved
    @Test
    fun saveTask_retrievesTask() = runBlocking {
        // GIVEN - A new task saved in the database.
        val task = Task("title", "description", false)
        localDataSource.saveTask(task)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getTask(task.id)

        // THEN - Same task is returned.
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`(task.title))
        assertThat(result.data.description, `is`(task.description))
        assertThat(result.data.isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun completeTask_retrievedTaskIsComplete() = runBlocking {
        // 1. Save a new active task in the local data source.
        val task = Task("title", "description")
        localDataSource.saveTask(task)

        // 2. Mark it as complete.
        localDataSource.completeTask(task.id)
        val result = localDataSource.getTask(task.id)


        // 3. Check that the task can be retrieved from the local data source and is complete.
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.isCompleted, `is`(true))
    }
}