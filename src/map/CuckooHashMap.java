package map;

import hash.MurmurHash3;

import java.util.*;

//HashMap using open addressing with linear probing
@SuppressWarnings("unchecked")
public class CuckooHashMap<K, V> implements Map<K, V> {
    private static final int MAX_CAPACITY = 1 << 30;
    private static final int DEFAULT_CAPACITY = 1 << 4; // 16
    private static final int MAX_LOOP = 10;
    private int mask, size, capacity, modCount, seed1, seed2;
    private Node<K, V>[] table1, table2;
    private Set<K> keySet;
    private Set<Entry<K, V>> entrySet;
    private Collection<V> values;

    public CuckooHashMap(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Capacity must be positive");
        if (capacity > MAX_CAPACITY) throw new IllegalArgumentException("Capacity must be less than " + MAX_CAPACITY);
        this.capacity = nextPow2(capacity);
        this.mask = this.capacity - 1;
        this.table1 = (Node<K, V>[]) new Node[this.capacity];
        this.table2 = (Node<K, V>[]) new Node[this.capacity];
        this.seed1 = new Random().nextInt();
        this.seed2 = seed1 + 1;
    }

    public CuckooHashMap() {
        this(DEFAULT_CAPACITY);
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
        int hash1 = hash1(key);
        int hash2 = hash2(key);
        return table1[hash1] != null && table1[hash1].key.equals(key)
                || table2[hash2] != null && table2[hash2].key.equals(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) throw new NullPointerException();
        for (Node<K, V> node : table1) {
            if (node != null && node.value != null && node.value.equals(value)) return true;
        }
        for (Node<K, V> node : table2) {
            if (node != null && node.value != null && node.value.equals(value)) return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (key == null) throw new NullPointerException();
        int hash1 = hash1(key);
        int hash2 = hash2(key);
        if (table1[hash1] != null && table1[hash1].key.equals(key)) return table1[hash1].value;
        if (table2[hash2] != null && table2[hash2].key.equals(key)) return table2[hash2].value;
        return null;
    }

    @Override
    public V put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException();
        int hash1 = hash1(key);
        int hash2 = hash2(key);
        if (table1[hash1] != null && table1[hash1].key.equals(key)) {
            V oldValue = table1[hash1].value;
            table1[hash1].value = value;
            return oldValue;
        }
        if (table2[hash2] != null && table2[hash2].key.equals(key)) {
            V oldValue = table2[hash2].value;
            table2[hash2].value = value;
            return oldValue;
        }
        modCount++;
        size++;
        Node<K, V> newNode = new Node<>(key, value);
        if (table1[hash1] == null) {
            table1[hash1] = newNode;
            return null;
        }
        if (table2[hash2] == null) {
            table2[hash2] = newNode;
            return null;
        }

        int loop = 0;
        while (loop < MAX_LOOP) {
            //try to find an empty slot in table1
            if (table1[hash1] == null) {
                table1[hash1] = newNode;
                return null;
            }

            // x <-> T1[h1(x))]
            K tmpKey = table1[hash1].key;
            V tmpValue = table1[hash1].value;
            table1[hash1].key = key;
            table1[hash1].value = value;
            key = tmpKey;
            value = tmpValue;
            hash2 = hash2(key);

            //try to find an empty slot in table2
            if (table2[hash2] == null) {
                table2[hash2] = new Node<>(key, value);
                return null;
            }

            // x <-> T2[h2(x))]
            tmpKey = table2[hash2].key;
            tmpValue = table2[hash2].value;
            table2[hash2].key = key;
            table2[hash2].value = value;
            key = tmpKey;
            value = tmpValue;
            hash1 = hash1(key);
            loop++;
        }
        rehash();
        put(key, value);
        return null;
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new NullPointerException();
        int hash1 = hash1(key);
        int hash2 = hash2(key);
        V oldValue = null;
        if (table1[hash1] != null && table1[hash1].key.equals(key)) {
            oldValue = table1[hash1].value;
            table1[hash1] = null;
        }
        if (table2[hash2] != null && table2[hash2].key.equals(key)) {
            oldValue = table2[hash2].value;
            table2[hash2] = null;
        }
        if (oldValue != null) {
            size--;
            modCount++;
        }
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m == null) throw new NullPointerException();
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        Arrays.fill(table1, null);
        Arrays.fill(table2, null);
        modCount++;
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
        Set<Entry<K, V>> es = entrySet;
        if (es == null) {
            es = new EntrySet();
            entrySet = es;
        }
        return es;
    }

    private void rehash() {
        Node<K, V>[] tmp1 = table1;
        Node<K, V>[] tmp2 = table2;
        if (capacity != MAX_CAPACITY) {
            capacity = capacity << 1;
            mask = capacity - 1;
        }
        table1 = (Node<K, V>[]) new Node[capacity];
        table2 = (Node<K, V>[]) new Node[capacity];
        seed1 = seed2 + 1;
        seed2 = seed1 + 1;

        this.size = 0;
        for (Node<K, V> node : tmp1) if (node != null) put(node.key, node.value);
        for (Node<K, V> node : tmp2) if (node != null) put(node.key, node.value);
    }

    private int hash1(Object key) {
        return MurmurHash3.hash32x64(key.hashCode(), seed1) & mask;
    }

    private int hash2(Object key) {
        return MurmurHash3.hash32x64(key.hashCode(), seed2) & mask;
    }

    private static class Node<K, V> implements Entry<K, V> {
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
            return CuckooHashMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return CuckooHashMap.this.remove(o) != null;
        }

        @Override
        public void clear() {
            CuckooHashMap.this.clear();
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
            return CuckooHashMap.this.containsValue(o);
        }

        @Override
        public void clear() {
            CuckooHashMap.this.clear();
        }
    }

    final class EntrySet extends AbstractSet<Entry<K, V>> {
        public int size() {
            return size;
        }

        public void clear() {
            CuckooHashMap.this.clear();
        }

        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (o == null) throw new NullPointerException();
            if (!(o instanceof Entry<?, ?> e)) return false;
            Object key = e.getKey();
            Object value = e.getValue();
            if (key == null || value == null) throw new NullPointerException();
            V v = CuckooHashMap.this.get(key);
            return v != null && v.equals(value);
        }

        public boolean remove(Object o) {
            if (o == null) throw new NullPointerException();
            if (!(o instanceof Entry<?, ?> e))
                return false;
            Object key = e.getKey();
            Object value = e.getValue();
            if (key == null || value == null) throw new NullPointerException();
            V v = CuckooHashMap.this.get(key);
            if (v != null && v.equals(value)) {
                CuckooHashMap.this.remove(key);
                return true;
            } else return false;
        }
    }

    abstract class HashIterator {
        int index1, index2;
        int expectedModCount;

        HashIterator() {
            expectedModCount = modCount;
            if (size != 0) {
                while (index1 < capacity && table1[index1] == null) index1++;
                if (index1 == capacity) {
                    while (index2 < capacity && table2[index2] == null) index2++;
                }
            }
        }

        public boolean hasNext() {
            return size != 0 && (index1 < capacity || index2 < capacity);
        }

        public Node<K, V> nextNode() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (!hasNext()) throw new NoSuchElementException();
            Node<K, V> node;
            if (index1 < capacity) node = table1[index1++];
            else node = table2[index2++];

            while (index1 < capacity && table1[index1] == null) index1++;
            if (index1 == capacity) {
                while (index2 < capacity && table2[index2] == null) index2++;
            }
            return node;
        }

        public void remove() {
            if (index1 == capacity && index2 == capacity) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (index1 != capacity) table1[index1++] = null;
            else table2[index2++] = null;
            size--;
            modCount++;
            expectedModCount++;
            while (index1 < capacity && table1[index1] == null) index1++;
            if (index1 == capacity) {
                while (index2 < capacity && table2[index2] == null) index2++;
            }
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

    final class EntryIterator extends HashIterator implements Iterator<Entry<K, V>> {
        @Override
        public Entry<K, V> next() {
            return nextNode();
        }
    }

    private static int nextPow2(int n) {
        if ((n & (n - 1)) == 0) return n;
        else return Integer.highestOneBit(n) << 1;
    }
}
