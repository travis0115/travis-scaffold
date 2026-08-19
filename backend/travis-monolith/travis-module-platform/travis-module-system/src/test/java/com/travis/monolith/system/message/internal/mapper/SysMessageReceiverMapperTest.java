package com.travis.monolith.system.message.internal.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class SysMessageReceiverMapperTest {

    @Test
    void shouldMatchStringReceiverIdsSerializedByJackson() throws Exception {
        var configuration = new Configuration();
        try (var input = Resources.getResourceAsStream("mapper/SysMessageReceiverMapper.xml")) {
            new XMLMapperBuilder(
                            input,
                            configuration,
                            "mapper/SysMessageReceiverMapper.xml",
                            configuration.getSqlFragments())
                    .parse();
        }

        var parameters =
                Map.<String, Object>of(
                        "userId",
                        1L,
                        "receiverType",
                        "admin",
                        "roleIds",
                        List.of(2L),
                        "deptId",
                        3L);
        String sql =
                configuration
                        .getMappedStatement(
                                SysMessageReceiverMapper.class.getName() + ".countUnreadInbox")
                        .getBoundSql(parameters)
                        .getSql()
                        .replaceAll("\\s+", " ")
                        .trim();

        assertThat(sql)
                .contains("JSON_QUOTE(CAST(? AS CHAR))")
                .contains("JSON_ARRAY( CAST(? AS CHAR) )");
    }
}
