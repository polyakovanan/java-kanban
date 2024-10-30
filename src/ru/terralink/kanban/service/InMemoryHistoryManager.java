package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.util.Node;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final PatheticLinkedList<Task> taskHistory;
    private final Map<Integer,Node> taskHash;

    public InMemoryHistoryManager() {
        taskHistory = new PatheticLinkedList<>();
        taskHash = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        int taskId = task.getId();
        Node node = taskHash.get(taskId);
        if (node != null) {
            removeNode(node);
        }
        node = taskHistory.linkLast(task);
        taskHash.put(taskId, node);
    }

    @Override
    public void remove(int id) {
        Node node = taskHash.get(id);
        if (node != null) {
            removeNode(node);
            taskHash.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getItems();
    }

    private void removeNode(Node node) {
        Node nextNode = node.getNext();
        Node prevNode = node.getPrev();

        if (prevNode != null)  {
            prevNode.setNext(nextNode);
            node.setPrev(null);
        }

        if (nextNode != null) {
            nextNode.setPrev(prevNode);
            node.setNext(null);
        }

        if (node.equals(taskHistory.first)) {
            taskHistory.first = nextNode;
        }
        if (node.equals(taskHistory.last)) {
            taskHistory.last = prevNode;
        }

        node.setItem(null);
    }

    private static class PatheticLinkedList<E>{
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
            Node currentNode = first;
            while(currentNode != null) {
                items.add((E) currentNode.getItem());
                currentNode = currentNode.getNext();
            }
            return items;
        }
    }
}
