-- 播放日志按每次播放独立保存；学习记录页面只在查询时按课程取最近一条。
-- 如果曾应用过错误的用户+课程唯一索引，先安全移除。

SET @drop_play_unique_index = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE apx_course_play_log DROP INDEX uk_play_user_course',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'apx_course_play_log'
      AND index_name = 'uk_play_user_course'
);

PREPARE drop_play_unique_index_statement FROM @drop_play_unique_index;
EXECUTE drop_play_unique_index_statement;
DEALLOCATE PREPARE drop_play_unique_index_statement;

-- 只为仍为0的用户回填已有完整播放记录，不覆盖原先已经累计的分钟。
UPDATE obj_front_user user
JOIN (
    SELECT
        user_id,
        SUM(
            LEAST(
                720,
                GREATEST(
                    1,
                    CEIL(TIMESTAMPDIFF(SECOND, start_time, end_time) / 60)
                )
            )
        ) AS learned_minutes
    FROM apx_course_play_log
    WHERE end_time IS NOT NULL
      AND start_time IS NOT NULL
      AND end_time >= start_time
    GROUP BY user_id
) history ON history.user_id = user.id
SET user.study_duration = history.learned_minutes
WHERE COALESCE(user.study_duration, 0) = 0;
