package win.oscene.common;


/**
 *
 */
public class SnowFlake {
    /**
     * data id 与 work id 各占5个bit
     */
    private static final byte DATA_OR_WORK_BITS = 5;
    /**
     * 自增序列占12bit
     */
    private static final byte SEQUENCE_ID_BITS = 12;

    /**
     * data id 或 work id的最大值
     */
    private static final Short MAX_DATA_OR_WORK_ID = ~(-1 << DATA_OR_WORK_BITS);
    /**
     * 自增序列的最大值
     */
    private static final Short SEQUENCE_ID_MASK = ~(-1 << SEQUENCE_ID_BITS);
    /**
     * 起始时间
     */
    private static final long BASIC_TIME = 1218196800000L;

    /**
     * 位运算时间戳的偏移
     */
    private static final byte TIME_SHIFT = DATA_OR_WORK_BITS + DATA_OR_WORK_BITS + SEQUENCE_ID_BITS;
    /**
     * 数据中心位运算的偏移量
     */
    private static final byte DATA_ID_SHIFT = DATA_OR_WORK_BITS + SEQUENCE_ID_BITS;
    /**
     * 数据中心（机房码） 与 工作id（机器码）
     */
    private long dataId,workId;
    /**
     * 上一次执行获取id的时间
     */
    private long lastTime;
    /**
     * 自增id
     */
    private int sequenceId;




    public SnowFlake(long dataId, long workId) {
        if (dataId < 0 || dataId > MAX_DATA_OR_WORK_ID){
            throw new IllegalArgumentException("请设置正确的数据中心id");
        }
        if (workId < 0 || workId > MAX_DATA_OR_WORK_ID){
            throw new IllegalArgumentException("请设置正确的工作id");
        }
        this.dataId = dataId;
        this.workId = workId;
    }

    public SnowFlake() {
    }


    private long getCurrentTime(){
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间的下一个时间
     * @return long 时间戳
     */
    private long getNextTime(){
        long now = getCurrentTime();
        while (now == lastTime){
            now = getCurrentTime();
        }
        return now;
    }

    public synchronized long nextId(){
        long now = getCurrentTime();
        //当当前的时间与上一个时间在同一毫秒内
        if (now == lastTime){
            //同一毫秒内 自增id开始自增 达到最大值时，就需要等待时间流逝到下一个时间，因为已达到了一毫秒的最大值，理想情况下，qps = 1000 * 2^12
            sequenceId = (sequenceId + 1) & SEQUENCE_ID_MASK;
            if (sequenceId == 0){
                now = getNextTime();
            }
        }else {
            sequenceId = 0;
        }

        if (now < lastTime){
            //当系统时间出现波动时 抛出异常。这也是该算法的缺点 如何解决时间回流
            throw new IllegalStateException("The time is moving backwards. time is : " + now + "  last time is :" + lastTime);
        }

        lastTime = now;
        return ((now - BASIC_TIME) << TIME_SHIFT) |
                (dataId << DATA_ID_SHIFT) |
                (workId << SEQUENCE_ID_BITS) |
                sequenceId;
    }










}
