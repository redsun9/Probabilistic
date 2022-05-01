package map;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAddressingHashMapTest {

    @Test
    void size() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
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
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertTrue(map.isEmpty());
        map.put("a", 1);
        assertFalse(map.isEmpty());
        map.remove("a");
        assertTrue(map.isEmpty());
    }

    @Test
    void containsKey() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertFalse(map.containsKey("a"));
        map.put("a", 1);
        assertTrue(map.containsKey("a"));
        map.remove("a");
        assertFalse(map.containsKey("a"));
    }

    @Test
    void get() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertNull(map.get("a"));
        map.put("a", 1);
        assertEquals(1, map.get("a"));
        map.remove("a");
        assertNull(map.get("a"));
    }

    @Test
    void put() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertNull(map.put("a", 1));
        assertEquals(1, map.put("a", 2));
        assertEquals(2, map.get("a"));
    }

    @Test
    void remove() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertNull(map.remove("a"));
        map.put("a", 1);
        assertEquals(1, map.remove("a"));
        assertNull(map.remove("a"));
    }

    @Test
    void putAll() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.putAll(Map.of("a", 1, "b", 2, "c", 3));
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void clear() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
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
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
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
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
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
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
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
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertThrows(NullPointerException.class,()->map.putAll(null));
    }

    @Test
    void putAllWithEmptyMap() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.putAll(new HashMap<>());
        assertEquals(0, map.size());
    }

    @Test
    void putAllWithNonEmptyMap() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.putAll(Map.of("a", 1, "b", 2, "c", 3));
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelf() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.putAll(map);
        assertEquals(0, map.size());
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelfWithEmptyMap() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.putAll(map);
        map.putAll(new HashMap<>());
        assertEquals(0, map.size());
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelfWithNonEmptyMap() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
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
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.putAll(map);
        map.putAll(map);
        assertEquals(0, map.size());
    }

    @SuppressWarnings("CollectionAddedToSelf")
    @Test
    void putAllWithSelfWithSelfWithEmptyMap() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.putAll(map);
        map.putAll(map);
        map.putAll(new HashMap<>());
        assertEquals(0, map.size());
    }

    @Test
    void checkResize(){
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        for (int i = 0; i < 1000; i++) map.put("a" + i, i);
        assertEquals(1000, map.size());
        for (int i = 0; i < 1000; i++) assertEquals(i, map.get("a" + i));
    }

    @Test
    void mergeTwoBigMaps() {
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        OpenAddressingHashMap<String, Integer> map2 = new OpenAddressingHashMap<>();
        for (int i = 0; i < 1000; i++) map.put("a" + i, i);
        for (int i = 0; i < 1000; i++) map2.put("b" + i, i);
        map.putAll(map2);
        assertEquals(2000, map.size());
        for (int i = 0; i < 1000; i++) assertEquals(i, map.get("a" + i));
        for (int i = 0; i < 1000; i++) assertEquals(i, map.get("b" + i));
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    void checkNonExistingKey(){
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        assertNull(map.get("a"));
    }

    @Test
    void checkExistingKey(){
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.put("a", 1);
        assertEquals(1, map.get("a"));
    }

    @Test
    void checkRemove(){
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.put("a", 1);
        assertEquals(1, map.get("a"));
        map.remove("a");
        assertNull(map.get("a"));
    }

    @Test
    void checkRemoveNonExistingKey(){
        OpenAddressingHashMap<String, Integer> map = new OpenAddressingHashMap<>();
        map.remove("a");
        assertNull(map.get("a"));
    }
}