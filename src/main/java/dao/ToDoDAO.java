package dao;

import models.ToDo;

import java.util.List;
import java.util.UUID;

public interface ToDoDAO {
    void addToDo(ToDo toDo);
    List<ToDo> getAllToDos();
    void updateToDo(ToDo toDo);
    void deleteToDo(UUID id);
}
