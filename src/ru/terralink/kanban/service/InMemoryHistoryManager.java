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
        removeNode(taskHash.remove(taskId));
        taskHash.put(taskId, taskHistory.linkLast(task));
    }

    @Override
    public void remove(int id) {
        removeNode(taskHash.remove(id));
        taskHash.remove(id);
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
        } else if (node.equals(taskHistory.first)) {
            taskHistory.first = nextNode;
        }

        if (nextNode != null) {
            nextNode.setPrev(prevNode);
            node.setNext(null);
        } else if (node.equals(taskHistory.last)) {
            taskHistory.last = prevNode;
        }

        node.setItem(null);
    }

    private static class PatheticLinkedList<E> {
        private Node<E> first;
        private Node<E> last;

        public Node<E> linkLast(E element) {
            Node<E> lastNode = this.last;
            Node<E> newNode = new Node<>(lastNode, element, null);
            last = newNode;
            if (lastNode == null) {
                first = newNode;
            } else {
                lastNode.setNext(newNode);
            }
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
