package map;

import java.util.*;

//HashMap using open addressing with quadratic probing
@SuppressWarnings("unchecked")
public class OpenAddressingHashMap<K, V> implements Map<K, V> {
    private static final int MAX_CAPACITY = 1 << 30;
    private static final int DEFAULT_CAPACITY = 1 << 4; // 16
    private static final double DEFAULT_LOADER_FACTOR = 0.75;
    private final double loaderFactor;
    private int mask, size, capacity, used, modCount;
    private Node<K, V>[] nodes;
    private Set<K> keySet;
    private Set<Map.Entry<K, V>> entrySet;
    private Collection<V> values;

    public OpenAddressingHashMap(int capacity, double loaderFactor) {
        if (capacity < 0) throw new IllegalArgumentException("Capacity must be positive");
        if (loaderFactor <= 0 || loaderFactor > 1) throw new IllegalArgumentException("Loader factor must be in (0,1]");
        if (capacity > MAX_CAPACITY) throw new IllegalArgumentException("Capacity must be less than " + MAX_CAPACITY);
        this.capacity = nextPow2(capacity);
        this.mask = this.capacity - 1;
        this.loaderFactor = loaderFactor;
        this.nodes = (Node<K, V>[]) new Node[this.capacity];
    }

    public OpenAddressingHashMap(int capacity) {
        this(capacity, DEFAULT_LOADER_FACTOR);
    }

    public OpenAddressingHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOADER_FACTOR);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) throw new NullPointerException();
        int hash = hash(key);
        int quadNum = 1;
        while (nodes[hash] != null && !nodes[hash].key.equals(key)) {
            hash = (hash + quadNum * quadNum) & mask;
            quadNum++;
        }
        return nodes[hash] != null && nodes[hash].value != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) throw new NullPointerException();
        for (Node<K, V> node : nodes) {
            if (node != null && node.value != null && node.value.equals(value)) return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (key == null) throw new NullPointerException();
        int hash = hash(key);
        int quadNum = 1;
        while (nodes[hash] != null && !nodes[hash].key.equals(key)) {
            hash = (hash + quadNum * quadNum) & mask;
            quadNum++;
        }
        return nodes[hash] != null && nodes[hash].value != null ? nodes[hash].value : null;
    }

    @Override
    public V put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException();
        int hash = hash(key);
        V oldValue = null;
        int quadNum = 1;
        while (nodes[hash] != null && !nodes[hash].key.equals(key)) {
            hash = (hash + quadNum * quadNum) & mask;
            quadNum++;
        }
        if (nodes[hash] == null) {
            nodes[hash] = new Node<>(key, value);
            used++;
            size++;
            modCount++;
        } else if (nodes[hash].value == null) {
            nodes[hash].value = value;
            size++;
            modCount++;
        } else {
            oldValue = nodes[hash].value;
            nodes[hash].value = value;
        }
        if (used > capacity * loaderFactor) {
            resize();
        }
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        if (key == null) return null;
        V oldValue = null;
        int hash = hash(key);
        int quadNum = 1;
        while (nodes[hash] != null && !nodes[hash].key.equals(key)) {
            hash = (hash + quadNum * quadNum) & mask;
            quadNum++;
        }
        if (nodes[hash] != null && nodes[hash].value != null) {
            oldValue = nodes[hash].value;
            nodes[hash] = null;
            modCount++;
            size--;
        }
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        if (used == 0) return;
        Arrays.fill(nodes, null);
        modCount++;
        used = 0;
        size = 0;
    }

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        if (es == null) {
            es = new EntrySet();
            entrySet = es;
        }
        return es;
    }

    private void resize() {
        if (capacity != MAX_CAPACITY) capacity = capacity << 1;
        Node<K, V>[] oldNodes = nodes;
        nodes = (Node<K, V>[]) new Node[capacity];
        mask = capacity - 1;
        used = 0;
        size = 0;
        for (Node<K, V> node : oldNodes) {
            if (node != null) put(node.key, node.value);
        }
    }

    private int hash(Object key) {
        return key.hashCode() & mask;
    }

    private static class Node<K, V> implements Map.Entry<K, V> {
        K key;
        V value;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            if (value == null) throw new NullPointerException();
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    final class KeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return OpenAddressingHashMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return OpenAddressingHashMap.this.remove(o) != null;
        }

        @Override
        public void clear() {
            OpenAddressingHashMap.this.clear();
        }
    }

    final class Values extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return OpenAddressingHashMap.this.containsValue(o);
        }

        @Override
        public void clear() {
            OpenAddressingHashMap.this.clear();
        }
    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        public int size() {
            return size;
        }

        public void clear() {
            OpenAddressingHashMap.this.clear();
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (o == null) throw new NullPointerException();
            if (!(o instanceof Entry<?, ?> e)) return false;
            Object key = e.getKey();
            Object value = e.getValue();
            if (key == null || value == null) throw new NullPointerException();
            V v = OpenAddressingHashMap.this.get(key);
            return v != null && v.equals(value);
        }

        public boolean remove(Object o) {
            if (o == null) throw new NullPointerException();
            if (!(o instanceof Entry<?, ?> e))
                return false;
            Object key = e.getKey();
            Object value = e.getValue();
            if (key == null || value == null) throw new NullPointerException();
            V v = OpenAddressingHashMap.this.get(key);
            if (v != null && v.equals(value)) {
                OpenAddressingHashMap.this.remove(key);
                return true;
            } else return false;
        }
    }

    abstract class HashIterator {
        int index;
        int expectedModCount;

        HashIterator() {
            expectedModCount = modCount;
            if (size != 0) {
                while (index < capacity && (nodes[index] == null || nodes[index].value == null)) index++;
            }
        }

        public boolean hasNext() {
            return size != 0 && index < capacity;
        }

        public Node<K, V> nextNode() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (!hasNext()) throw new NoSuchElementException();
            Node<K, V> node = nodes[index++];
            while (index < capacity && (nodes[index] == null || nodes[index].value == null)) index++;
            return node;
        }

        public void remove() {
            if (index == capacity) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            nodes[index++].value = null;
            size--;
            modCount++;
            expectedModCount++;
            while (index < capacity && (nodes[index] == null || nodes[index].value == null)) index++;
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<K> {
        @Override
        public K next() {
            return nextNode().key;
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V> {
        @Override
        public V next() {
            return nextNode().value;
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K, V>> {
        @Override
        public Map.Entry<K, V> next() {
            return nextNode();
        }
    }

    private static int nextPow2(int n) {
        if ((n & (n - 1)) == 0) return n;
        else return Integer.highestOneBit(n) << 1;
    }
}
