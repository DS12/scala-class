
### (3m): Pit depth

A List of Integers passed to `def maxDepth(list: List[Int]): Int` describes a 2-dimensional terrain.  


```
scala> maxDepth(List(2,1,-2,-2,3,1,3,-4))
res19: Int = 4
```

The "drop-off" from 3 to -4 is not a pit, so does not count.

`List(0,1,2,3,0)` has no pit.  It is only a mound.  `maxDepth` returns a sentinel value, -1.
```
scala> maxDepth(List(0,1,2,3,0))
res24: Int = -1
```
