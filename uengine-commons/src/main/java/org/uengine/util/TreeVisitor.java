package org.uengine.util;

import java.util.HashSet;
import java.util.List;

/**
 * Created by jjy on 2016. 10. 11..
 */
public abstract class TreeVisitor<T> {
    private HashSet<T> visited = new HashSet<>(); // 방문한 노드를 추적하는 HashSet

    public void run(T escTree) {

         if (!visited.contains(escTree)) { // 해당 노드를 방문하지 않았다면
            visited.add(escTree); // 방문한 노드에 추가

            for(T child : getChild(escTree)){
                logic(child);

                run(child);
            }
        }

    }

    public abstract List<T> getChild(T parent);

    public abstract void logic(T elem);
}
