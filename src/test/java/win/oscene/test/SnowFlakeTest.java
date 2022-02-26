package win.oscene.test;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import win.oscene.common.SnowFlake;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 1,time = 1)
@Measurement(iterations = 3,time = 3)
@Threads(5)
public class SnowFlakeTest {

    private static final CopyOnWriteArraySet<Long> set = new CopyOnWriteArraySet<>();

    private static final SnowFlake snowFlake = new SnowFlake(10,8);

    /**
     *
     *   4094805.979 ±(99.9%) 1970.559 ops/s [Average]
     *   (min, avg, max) = (4090894.649, 4094805.979, 4097363.658), stdev = 1843.262
     *   CI (99.9%): [4092835.420, 4096776.538] (assumes normal distribution)
     *
     *   SnowFlakeTest.test2  thrpt   15  4094805.979 ± 1970.559  ops/s
     *
     * @param args
     * @throws RunnerException
     */
    public static void main(String[] args) throws RunnerException {
//        new Runner(new OptionsBuilder().include(SnowFlakeTest.class.getName()).build())
//                .run();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            new Thread(()->{
                int ii = 0;
                while (ii < 10000){
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long l = snowFlake.nextId();
                    if (set.contains(l)){
                        throw new IllegalStateException("出现重复");
                    }else {
                        set.add(l);
                    }
                    ii++;
                }
                countDownLatch.countDown();
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        set.stream().limit(20).forEach(System.out::println);
        System.out.println(set.size());
        System.out.println(1000 * (Math.pow(2,12)));

    }

//    @Benchmark
//    public void test1(){
//        long l = snowFlake.nextId();
//        if (set.contains(l)){
//            System.out.println("SnowFlakeTest    出现了重复");
//            throw new IllegalStateException("SnowFlakeTest    出现了重复");
//        }else {
//            set.add(l);
//        }
//    }

    @Benchmark
    public void test2(){
        snowFlake.nextId();
    }



}
