package th.ab.demo.todolist.integrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import th.ab.demo.todolist.Todo;
import th.ab.demo.todolist.TodoController;
import th.ab.demo.todolist.TodoRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TodolistIntegrationTests {

	@Autowired
	TodoController todoController;

	@Autowired
	TodoRepository todoRepository;


	@Test
	void deleteTodo() {
		Todo todo = todoRepository.save(new Todo(1L, "TestDelete"));

		todoController.delete(todo.getId());

		assertThat(todoRepository.findById(todo.getId())).isEmpty();
	}

	@Test
	void addTodo() {
		Todo todo = new Todo(5L, "TestAdd");

		todoController.add(todo);

		assertThat(todoRepository.findAll().stream().map(Todo::getText)).contains("TestAdd");
	}


}
