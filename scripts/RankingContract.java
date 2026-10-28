import java.lang.reflect.Method;
import java.time.LocalDateTime;
/** 通过最终可执行 JAR 验证时间衰减与行为权重，避免只验证未打包的源码。 */
public class RankingContract {
    public static void main(String[] args) throws Exception {
        Class<?> noteType=Class.forName("com.xiaoming.module.note.entity.Note");
        Class<?> ranking=Class.forName("com.xiaoming.module.note.service.HotRankingService");
        Method score=ranking.getMethod("score",noteType,LocalDateTime.class);
        Object note=noteType.getConstructor().newInstance();
        for(String field:new String[]{"ViewCount","LikeCount","CollectCount","CommentCount"})
            noteType.getMethod("set"+field,Integer.class).invoke(note,0);
        LocalDateTime now=LocalDateTime.of(2026,10,28,12,0);
        noteType.getMethod("setPublishTime",LocalDateTime.class).invoke(note,now);
        noteType.getMethod("setLikeCount",Integer.class).invoke(note,1);
        long like=((Number)score.invoke(null,note,now)).longValue();
        noteType.getMethod("setLikeCount",Integer.class).invoke(note,0);
        noteType.getMethod("setCollectCount",Integer.class).invoke(note,1);
        long collect=((Number)score.invoke(null,note,now)).longValue();
        if(collect<=like)throw new AssertionError("收藏权重应高于点赞");
        noteType.getMethod("setPublishTime",LocalDateTime.class).invoke(note,now.minusDays(7));
        long older=((Number)score.invoke(null,note,now)).longValue();
        if(older>=collect || older<0)throw new AssertionError("相同行为下旧内容应衰减且分数非负");
        noteType.getMethod("setPublishTime",LocalDateTime.class).invoke(note,now.plusDays(1));
        long future=((Number)score.invoke(null,note,now)).longValue();
        if(future!=collect)throw new AssertionError("未来时间不应额外放大热度");
        System.out.println("PASS 热度契约：收藏权重、时间衰减、未来时间边界");
    }
}
