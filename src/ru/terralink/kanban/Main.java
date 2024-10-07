package ru.terralink.kanban;

import ru.terralink.kanban.model.*;
import ru.terralink.kanban.service.Managers;
import ru.terralink.kanban.service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        //Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
        Task firstTask = new Task("Заехать в больницу", "Лучше ехать по Луначарского, там пробка меньше");
        taskManager.createTaskByType(firstTask, TaskType.TASK);

        Task secondTask = new Task("Купить корм кошке", "Не брать Пурину, она с него воняет");
        taskManager.createTask(secondTask);

        Epic firstEpic = new Epic("Получить достижения в Doom + Doom II", "А то выдали переиздание, теперь опять проходить");
        taskManager.createTask(firstEpic);

        Subtask firstSubtask = new Subtask("Пройти оригинальные маппаки", "Тут можно по-быстрому через idclevXX", firstEpic);
        taskManager.createTaskByType(firstSubtask, TaskType.SUBTASK);

        Subtask secondSubtask = new Subtask("Пройти Sigil и Legacy of Rust", "А тут можно и нормально поиграть", firstEpic);
        taskManager.createTask(secondSubtask);

        Epic secondEpic = new Epic("Встать на работу", "Это действительно эпик");
        taskManager.createTaskByType(secondEpic, TaskType.EPIC);

        Subtask thirdSubtask = new Subtask("Высунуть ногу из-под одеяла", "Че-то холодно", secondEpic);
        taskManager.createTask(thirdSubtask);

        //Распечатайте списки эпиков, задач и подзадач через System.out.println(..).
        printAll(taskManager);

        //Измените статусы созданных объектов, распечатайте их. Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        firstTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTaskById(firstTask, firstTask.getId());

        secondTask.setStatus(TaskStatus.DONE);
        taskManager.updateTaskByIdAndType(secondTask, secondTask.getId(), TaskType.TASK);

        firstSubtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTaskById(firstSubtask, firstSubtask.getId());

        thirdSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateTaskById(thirdSubtask, thirdSubtask.getId());
        printAll(taskManager);

        taskManager.getTaskById(4);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(4);
        printAll(taskManager);

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        taskManager.deleteTaskById(secondTask.getId());
        taskManager.deleteTaskByIdAndType(secondEpic.getId(), TaskType.EPIC);
        printAll(taskManager);

        System.out.println("Если ты это читаешь, значит оно не трейсит, ура");
    }

    private static void printAll(TaskManager taskManager){
        System.out.println("Эпики");
        System.out.println(taskManager.getTasksByType(TaskType.EPIC));
        System.out.println();
        System.out.println("Таски");
        System.out.println(taskManager.getTasksByType(TaskType.TASK));
        System.out.println();
        System.out.println("Сабтаски");
        System.out.println(taskManager.getTasksByType(TaskType.SUBTASK));
        System.out.println();
        System.out.println("История");
        System.out.println(taskManager.getHistory());
        System.out.println();
    }
}
