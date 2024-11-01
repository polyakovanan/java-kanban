package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.util.Node;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final PatheticLinkedList<Task> taskHistory;
    private final Map<Integer,Node<Task>> taskHash;

    public InMemoryHistoryManager() {
        taskHistory = new PatheticLinkedList<>();
        taskHash = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        int taskId = task.getId();
        removeNode(taskHash.get(taskId));
        taskHash.put(taskId, taskHistory.linkLast(task));
    }

    @Override
    public void remove(int id) {
        removeNode(taskHash.remove(id));
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getItems();
    }

    private void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }

        Node<Task> nextNode = node.getNext();
        Node<Task> prevNode = node.getPrev();

        if (prevNode != null)  {
            prevNode.setNext(nextNode);
            node.setPrev(null);
        } else {
            taskHistory.first = nextNode;
        }

        if (nextNode != null) {
            nextNode.setPrev(prevNode);
            node.setNext(null);
        } else {
            taskHistory.last = prevNode;
        }

        node.setItem(null);
    }

    private static class PatheticLinkedList<E> {
        private Node<E> first;
        private Node<E> last;

        public Node<E> linkLast(E element) {
            Node<E> newNode = new Node<>(last, element, null);
            if (last == null) {
                first = newNode;
            } else {
                last.setNext(newNode);
            }
            last = newNode;
            return newNode;
        }

        public List<E> getItems() {
            List<E> items = new ArrayList<>();
            Node<E> currentNode = first;
            while (currentNode != null) {
                items.add(currentNode.getItem());
                currentNode = currentNode.getNext();
            }
            return items;
        }
    }
}
