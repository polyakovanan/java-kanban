package ru.terralink.kanban;

import ru.terralink.kanban.model.*;
import ru.terralink.kanban.service.Managers;
import ru.terralink.kanban.service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        //Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
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

        Subtask thirdSubtask = new Subtask("Пройти все WADники, которые получили Cacowards", "Это займет какое-то время...", firstEpic);
        taskManager.createTask(thirdSubtask);

        Epic secondEpic = new Epic("Встать на работу", "Это действительно эпик");
        taskManager.createTaskByType(secondEpic, TaskType.EPIC);

        //Запросите созданные задачи несколько раз в разном порядке.
        //После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
        taskManager.getTaskById(1);
        System.out.println(taskManager.getHistory()); // [1]
        taskManager.getTaskById(1);
        System.out.println(taskManager.getHistory()); // [1]
        taskManager.getTaskById(2);
        System.out.println(taskManager.getHistory()); // [1,2]
        taskManager.getTaskById(3);
        System.out.println(taskManager.getHistory()); // [1,2,3]
        taskManager.getTaskById(2);
        System.out.println(taskManager.getHistory()); // [1,3,2]
        taskManager.getTaskById(4);
        System.out.println(taskManager.getHistory()); // [1,3,2,4]
        taskManager.getTaskById(4);
        System.out.println(taskManager.getHistory()); // [1,3,2,4]
        taskManager.getTaskById(5);
        System.out.println(taskManager.getHistory()); // [1,3,2,4,5]
        taskManager.getTaskById(6);
        System.out.println(taskManager.getHistory()); // [1,3,2,4,5,6]

        //Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        taskManager.deleteTaskById(2);
        System.out.println(taskManager.getHistory()); // [1,3,4,5,6]

        //Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        taskManager.deleteTaskById(3);
        System.out.println(taskManager.getHistory()); // [1]

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
