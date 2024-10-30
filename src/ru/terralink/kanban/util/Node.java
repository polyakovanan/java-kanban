package ru.terralink.kanban.util;

import java.util.Objects;

public class Node<E> {
    private E item;
    private Node<E> next;
    private Node<E> prev;

    public Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }

    public E getItem() {
        return item;
    }

    public Node<E> getNext() {
        return next;
    }

    public Node<E> getPrev() {
        return prev;
    }

    public void setItem(E item) {
        this.item = item;
    }

    public void setNext(Node<E> next) {
        this.next = next;
    }

    public void setPrev(Node<E> prev) {
        this.prev = prev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(item, node.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
