package com.lemonjun.mysql.orm.sharding;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * 短URL的分表策略分表字段
 *
 * @author WangYazhou
 * @date  2015年11月24日 下午4:50:02
 * @see
 */
public class ShardTableName {

    private static final int TABLE_SIZE = 4;

    private static final String table_name = "t_jobcv_relation_";

    private static final String db_name = "ddwww_chr_jobcv_";

    private static final HashFunction murmur_32 = Hashing.murmur3_32();

    public static String getShardingTableName(long hashKey) {
        HashCode code = murmur_32.hashLong(hashKey);
        int index = (code.asInt() & 0x7FFFFFFF) % TABLE_SIZE;
        return String.format("%s_%d", table_name, index);
    }

    public static String getShardingDBName(long hashKey) {
        HashCode code = murmur_32.hashLong(hashKey);
        int index = (code.asInt() & 0x7FFFFFFF) % TABLE_SIZE;
        return String.format("%s_%d.%s", db_name, index, "t_jobcv_relation");
    }

}
