package map;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CuckooHashMapTest {

    @Test
    void size() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertEquals(0, map.size());
        map.put("a", 1);
        assertEquals(1, map.size());
        map.put("b", 2);
        assertEquals(2, map.size());
        map.put("c", 3);
        assertEquals(3, map.size());

        map.remove("a");
        assertEquals(2, map.size());
        map.remove("b");
        assertEquals(1, map.size());
        map.remove("c");
        assertEquals(0, map.size());

        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertEquals(3, map.size());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void isEmpty() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertTrue(map.isEmpty());
        map.put("a", 1);
        assertFalse(map.isEmpty());
        map.remove("a");
        assertTrue(map.isEmpty());
    }

    @Test
    void containsKey() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertFalse(map.containsKey("a"));
        map.put("a", 1);
        assertTrue(map.containsKey("a"));
        map.remove("a");
        assertFalse(map.containsKey("a"));
    }

    @Test
    void get() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertNull(map.get("a"));
        map.put("a", 1);
        assertEquals(1, map.get("a"));
        map.remove("a");
        assertNull(map.get("a"));
    }

    @Test
    void put() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertNull(map.put("a", 1));
        assertEquals(1, map.put("a", 2));
        assertEquals(2, map.get("a"));
    }

    @Test
    void remove() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertNull(map.remove("a"));
        map.put("a", 1);
        assertEquals(1, map.remove("a"));
        assertNull(map.remove("a"));
    }

    @Test
    void putAll() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(Map.of("a", 1, "b", 2, "c", 3));
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void clear() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.clear();
        assertEquals(0, map.size());
        assertNull(map.get("a"));
        assertNull(map.get("b"));
        assertNull(map.get("c"));
    }

    @SuppressWarnings("RedundantCollectionOperation")
    @Test
    void keySet() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertEquals(3, map.size());
        assertTrue(map.keySet().contains("a"));
        assertTrue(map.keySet().contains("b"));
        assertTrue(map.keySet().contains("c"));
    }

    @SuppressWarnings("RedundantCollectionOperation")
    @Test
    void values() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertEquals(3, map.size());
        assertTrue(map.values().contains(1));
        assertTrue(map.values().contains(2));
        assertTrue(map.values().contains(3));
    }

    @Test
    void entrySet() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertEquals(3, map.size());
        assertTrue(map.entrySet().contains(Map.entry("a", 1)));
        assertTrue(map.entrySet().contains(Map.entry("b", 2)));
        assertTrue(map.entrySet().contains(Map.entry("c", 3)));
    }

    @SuppressWarnings({"ConstantConditions", "MismatchedQueryAndUpdateOfCollection"})
    @Test
    void putAllWithNull() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertThrows(NullPointerException.class, () -> map.putAll(null));
    }

    @Test
    void putAllWithEmptyMap() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(new HashMap<>());
        assertEquals(0, map.size());
    }

    @Test
    void putAllWithNonEmptyMap() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(Map.of("a", 1, "b", 2, "c", 3));
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelf() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(map);
        assertEquals(0, map.size());
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelfWithEmptyMap() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(map);
        map.putAll(new HashMap<>());
        assertEquals(0, map.size());
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelfWithNonEmptyMap() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(map);
        map.putAll(Map.of("a", 1, "b", 2, "c", 3));
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelfWithSelf() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(map);
        map.putAll(map);
        assertEquals(0, map.size());
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelfWithSelfWithEmptyMap() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.putAll(map);
        map.putAll(map);
        map.putAll(new HashMap<>());
        assertEquals(0, map.size());
    }

    @Test
    void checkResize() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        for (int i = 0; i < 1000; i++) map.put("a" + i, i);
        assertEquals(1000, map.size());
        for (int i = 0; i < 1000; i++) assertEquals(i, map.get("a" + i));
    }

    @Test
    void mergeTwoBigMaps() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        CuckooHashMap<String, Integer> map2 = new CuckooHashMap<>();
        for (int i = 0; i < 1000; i++) map.put("a" + i, i);
        for (int i = 0; i < 1000; i++) map2.put("b" + i, i);
        map.putAll(map2);
        assertEquals(2000, map.size());
        for (int i = 0; i < 1000; i++) assertEquals(i, map.get("a" + i));
        for (int i = 0; i < 1000; i++) assertEquals(i, map.get("b" + i));
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void checkNonExistingKey() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        assertNull(map.get("a"));
    }

    @Test
    void checkExistingKey() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.put("a", 1);
        assertEquals(1, map.get("a"));
    }

    @Test
    void checkRemove() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.put("a", 1);
        assertEquals(1, map.get("a"));
        map.remove("a");
        assertNull(map.get("a"));
    }

    @Test
    void checkRemoveNonExistingKey() {
        CuckooHashMap<String, Integer> map = new CuckooHashMap<>();
        map.remove("a");
        assertNull(map.get("a"));
    }
}