package sensorserver.struct;

import java.util.HashMap;
import java.util.Map;

/**
 * Bi-directional map with KeyValue and ValueKey lookup. Pretty much just bundles together 2 hashmaps.
 */
public class BiMap<K, V> {

	private final Map<K, V> keyValue;
	private final Map<V, K> valueKey;
	
	public BiMap()
	{
		keyValue = new HashMap<K, V>();
		valueKey = new HashMap<V, K>();
	}
	
	/**
	 * Put the key value pair into the bimap. Duplicate values are not allowed.
	 */
	public void put(final K key, final V value)
    {
        if(keyValue.containsValue(value))
        {
            keyValue.remove(valueKey.get(value));
        }

        keyValue.put(key, value);
        valueKey.put(value, key);
    }

    public V getValueForKey(final K key)
    {
        return (keyValue.get(key));
    }

    public K getKeyForValue(final V value)
    {
        return (valueKey.get(value));
    }

}
