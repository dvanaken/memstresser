package memstresser.util;

import java.util.Iterator;

public class CollectionUtil {
    
    /**
     * Return the last item in an array
     * @param <T>
     * @param items
     * @return
     */
    @SafeVarargs
    public static <T> T last(T...items) {
        if (items != null && items.length > 0) {
            return (items[items.length-1]);
        }
        return (null);
    }
    
    /**
     * Wrap an Iterable around an Iterator
     * @param <T>
     * @param it
     * @return
     */
    public static <T> Iterable<T> iterable(final Iterator<T> it) {
        return (new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return (it);
            }
        });
    }

}
