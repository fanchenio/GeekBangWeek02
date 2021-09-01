# 作业（四）


#### 测试命令

```
java -Xmx1024m -Xms1024m -XX:+UseSerialGC GCLogAnalysis
java -Xmx1024m -Xms1024m -XX:+UseParNewGC GCLogAnalysis
java -Xmx1024m -Xms1024m -XX:+UseParallelGC GCLogAnalysis
java -Xmx1024m -Xms1024m -XX:+UseParallelOldGC GCLogAnalysis
java -Xmx1024m -Xms1024m -XX:+UseConcMarkSweepGC GCLogAnalysis
java -Xmx1024m -Xms1024m -XX:+UseG1GC GCLogAnalysis
```

#### 测试结果

| 垃圾回收器      | 第1次 | 第2次 | 第3次 | 第4次 | 第5次 | 第6次 | 第7次 | 第8次 | 第9次 | 第10次 | 平均    |
| --------------- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ------ | ------- |
| SerialGC        | 13798 | 14854 | 14619 | 14859 | 14534 | 14803 | 14277 | 14735 | 14875 | 14693  | 14604.7 |
| ParNewGC        | 14813 | 14696 | 14858 | 14833 | 14971 | 15213 | 15000 | 13806 | 15622 | 15697  | 14950.9 |
| ParallelGC      | 13217 | 13494 | 13153 | 12967 | 12933 | 13485 | 13411 | 12919 | 12977 | 13165  | 13172.1 |
| ParallelOldGC   | 13342 | 13209 | 13545 | 13098 | 13049 | 13030 | 13050 | 13904 | 13459 | 13562  | 13324.8 |
| ConcMarkSweepGC | 14985 | 14943 | 14908 | 14825 | 14949 | 15562 | 15167 | 15757 | 14626 | 14803  | 15052.5 |
| G1GC            | 16223 | 16254 | 15686 | 16010 | 16446 | 15903 | 15956 | 15470 | 15288 | 15756  | 15899.2 |

测试后发现了一个奇怪的现象就是ParallelGC速度竟然是最慢的，比SerialGC都慢，连ParNewGC都超过了SerialGC，但是ParallelGC却没有，ParallelGC和ParNew可都是并行GC，为什么ParallelGC会这么慢，随后我观察了一下ParallelGC的日志。

![image-20210831172221769](https://github.com/fanchenio/GeekBangWeek02/blob/master/images/image-20210831172221769.png?raw=true)

我发现在ParallelGC中，年轻代的容量是随时变化的，结合官方文档：https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/parallel.html，了解到Parallel Scavenge会在每次GC结束之后判断这次垃圾回收有没有达到我们设定最大回收时间（MaxGCPauseMillis）或吞吐量（GCTimeRatio）的值，来动态调整年轻代的大小。

![image-20210831174157297](https://github.com/fanchenio/GeekBangWeek02/blob/master/images/image-20210831174157297.png?raw=true)

然而正是因为这样频繁的动态调整年轻代区域的大小而导致了多次GC，下面我贴出来ParNewGC的GC次数。

![image-20210831172457229](https://github.com/fanchenio/GeekBangWeek02/blob/master/images/image-20210831172457229.png?raw=true)

很明显ParallelGC和ParNewGC相比，ParallelGC的次数比较多，而且ParNewGC的年轻代是从始至终都是固定大小的，没有被动态改变，所以ParallelGC生成类的数量会比ParNewGC少，甚至比SerialGC都少。

到这里我卡住了很久，我想解决一下，或者是说调整一下，让ParallelGC运行GCLogAnalysis.java生成类的数量比SerialGC多，并且和ParNewGC持平。

后面我就想到了，手动设定年轻代比例。

#### 测试命令
```
java -Xmx1024m -Xms1024m -XX:+UseParallelGC -XX:SurvivorRatio=8 GCLogAnalysis
```

#### 测试结果

| 垃圾回收器 | 第1次 | 第2次 | 第3次 | 第4次 | 第5次 | 第6次 | 第7次 | 第8次 | 第9次 | 第10次 | 平均  |
| ---------- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ------ | ----- |
| ParallelGC | 15772 | 14733 | 14739 | 14809 | 15418 | 14614 | 14775 | 15092 | 15571 | 15837  | 15136 |

果然，手动设定年轻代比例后，Parallel GC就不会自动调整年轻代的大小了，GC的次数也就变少了，生成的类数量也比SerialGC多，并且和ParNew持平，如图。


![image-20210831174855033](https://github.com/fanchenio/GeekBangWeek02/blob/master/images/image-20210831174855033.png?raw=trueg)

下面使用2G的堆容量运行一下GCLogAnalysis.java。

#### 测试命令

```
java -Xmx2048m -Xms2048m -XX:+UseSerialGC GCLogAnalysis
java -Xmx2048m -Xms2048m -XX:+UseParNewGC GCLogAnalysis
java -Xmx2048m -Xms2048m -XX:+UseParallelGC GCLogAnalysis
java -Xmx2048m -Xms2048m -XX:+UseParallelOldGC GCLogAnalysis
java -Xmx2048m -Xms2048m -XX:+UseConcMarkSweepGC GCLogAnalysis
java -Xmx2048m -Xms2048m -XX:+UseG1GC GCLogAnalysis
```

#### 运行结果

| 垃圾回收器      | 第1次 | 第2次 | 第3次 | 第4次 | 第5次 | 第6次 | 第7次 | 第8次 | 第9次 | 第10次 | 平均    |
| --------------- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ------ | ------- |
| SerialGC        | 14830 | 14956 | 14864 | 14937 | 15077 | 14922 | 15070 | 15232 | 14623 | 14806  | 14931.7 |
| ParNewGC        | 16931 | 16802 | 14857 | 15083 | 16891 | 14879 | 14995 | 15414 | 14909 | 14898  | 15565.9 |
| ParallelGC      | 16593 | 16382 | 14606 | 15451 | 15274 | 15380 | 16809 | 15167 | 16692 | 15415  | 15776.9 |
| ParallelOldGC   | 16864 | 16462 | 14558 | 15652 | 16078 | 14740 | 15586 | 15307 | 15593 | 16168  | 15700.8 |
| ConcMarkSweepGC | 16444 | 14666 | 15764 | 16291 | 16436 | 15291 | 15635 | 14764 | 14354 | 14771  | 15441.6 |
| G1GC            | 15733 | 17770 | 15743 | 17058 | 15486 | 16229 | 15909 | 16487 | 15887 | 15070  | 16137.2 |

我们可以看到，在堆空间是2G的情况下ParallelGC才和其他垃圾回收器持平，在这种情况下得出一个结论就是在内存小于2G的时候最好用ParNewGC或者是SerialGC，内存大于2G的情况下使用ParallelGC。



# 作业（六）

NIO目录。

