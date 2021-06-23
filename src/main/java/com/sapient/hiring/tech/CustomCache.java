package com.sapient.hiring.tech;

import com.sapient.hiring.tech.common.*;
import java.text.MessageFormat;
import java.util.HashMap;

public class CustomCache<K, V> {
    private Entry<K, V>[] buckets;
    private int capacity=16;
    private HashMap<Class,ClassTypeCounter> keyTypeValueTypeMap;
    private int specialEntryCount=0;

    private int size = 0;

    private double lf = 0.75;

    public CustomCache(){
    	this.capacity = capacity;
        this.buckets = new Entry[this.capacity];
        keyTypeValueTypeMap = new HashMap<>();
    }

    public void put(K key, V value) throws CustomCacheInsertionException {
        if( keyTypeValueTypeMap.containsKey(key.getClass()) &&  (!value.getClass().isAssignableFrom(keyTypeValueTypeMap.get(key.getClass()).getClassName()) && !keyTypeValueTypeMap.get(key.getClass()).getClassName().isAssignableFrom(value.getClass())) ){
            throw new CustomCacheInsertionException(MessageFormat.format("Object of class [class {0}] not allowable for this Key Type [class {1}]. Allowed types are [class {2}] or it sub and super types",value.getClass().getName(),key.getClass().getName(), keyTypeValueTypeMap.get(key.getClass()).getClassName().getName()));
        }
    	if (size == lf * capacity) {
            // rehash
            Entry<K, V>[] old = buckets;

            capacity *= 2;
            size = 0;
            buckets = new Entry[capacity];

            for (Entry<K, V> e : old) {
                while (e != null) {
                    put(e.key, e.value);
                    e = e.next;
                }
            }
        }
        Entry<K, V> entry = new Entry<>(key, value, null);
        int bucket = getHash(key) % getBucketSize();

        Entry<K, V> existing = buckets[bucket];
        if (existing == null) {
            buckets[bucket] = entry;
            if(keyTypeValueTypeMap.containsKey(key.getClass())){
                keyTypeValueTypeMap.get(key.getClass()).setClassName(value.getClass());
                keyTypeValueTypeMap.get(key.getClass()).setCounter(keyTypeValueTypeMap.get(key.getClass()).getCounter()+1);
            } else {
                keyTypeValueTypeMap.put(key.getClass(), new ClassTypeCounter(value.getClass(),1));
            }

            size++;
        } else {
            // compare the keys see if key already exists
            while (existing.next != null) {
                if (existing.key.equals(key)) {
                    existing.value = value;
                    return;
                }
                existing = existing.next;
            }

            if (existing.key.equals(key)) {
                existing.value = value;
            } else {
                existing.next = entry;
                if(keyTypeValueTypeMap.containsKey(key.getClass())){
                    keyTypeValueTypeMap.get(key.getClass()).setCounter(keyTypeValueTypeMap.get(key.getClass()).getCounter()+1);
                } else {
                    keyTypeValueTypeMap.put(key.getClass(), new ClassTypeCounter(value.getClass(),1));
                }
                size++;
            }
        }
    }

    public boolean remove(K key){
    	int index = index(key);
        Entry previous = null;
        Entry entry = buckets[index];
        while (entry != null){
            if(entry.getKey().equals(key)){
                if(keyTypeValueTypeMap.containsKey(key.getClass())){
                    keyTypeValueTypeMap.get(key.getClass()).setCounter(keyTypeValueTypeMap.get(key.getClass()).getCounter()-1);
                }

                if(keyTypeValueTypeMap.get(key.getClass()).getCounter() == 0){
                    keyTypeValueTypeMap.remove(key.getClass());
                }
                if(previous == null){
                    entry = entry.getNext();
                    buckets[index] = entry;
                    return true;
                }else {
                    previous.next = entry.getNext();
                    return true;
                }
            }
            previous = entry;
            entry = entry.getNext();
        }
        return false;
    }

    public V get(K key){
        //TODO implement this method
    	Entry<K, V> bucket = buckets[getHash(key) % getBucketSize()];

        while (bucket != null) {
            if (key == bucket.key) {
                return bucket.value;
            }
            bucket = bucket.next;
        }
        return null;
    }
    
    private int index(K key){
        if(key == null){
            return 0;
        }
        return Math.abs(getHash(key) % capacity);
    }
    static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        public Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public Entry<K, V> getNext() {
            return next;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;

            if (obj instanceof Entry) {
                Entry entry = (Entry) obj;

                return key.equals(entry.getKey()) &&
                        value.equals(entry.getValue());
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 13;
            hash = 17 * hash + ((key == null) ? 0 : key.hashCode());
            hash = 17 * hash + ((value == null) ? 0 : value.hashCode());
            return hash;
        }

        @Override
        public String toString() {
            return "{" + key + ", " + value + "}";
        }
    }
    
    public int size() {
        return size;
    }

    private int getBucketSize() {
        return buckets.length;
    }

    private int getHash(K key) {
        return key == null ? 0 : Math.abs(key.hashCode());
    }

    private enum AllowedClasses{
        Rectangle,
        Square,
        Shape
    }

    class ClassTypeCounter{
        public ClassTypeCounter(Class className, int counter) {
            this.className = className;
            this.counter = counter;
        }

        public Class getClassName() {
            return className;
        }

        public void setClassName(Class className) {
            this.className = className;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        Class className;
        int counter;

    }

}
