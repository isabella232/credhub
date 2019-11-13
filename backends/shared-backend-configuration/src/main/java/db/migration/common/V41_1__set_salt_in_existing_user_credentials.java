package db.migration.common;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.cloudfoundry.credhub.CryptSaltFactory;
import org.cloudfoundry.credhub.utils.UuidUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unused")
public class V41_1__set_salt_in_existing_user_credentials extends BaseJavaMigration {

  @SuppressFBWarnings(
    value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
    justification = "The database will definitely exist"
  )
  @Override
  public void migrate(Context context) throws Exception {
    JdbcTemplate jdbcTemplate =
      new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));

    final String databaseName = jdbcTemplate
      .getDataSource()
      .getConnection()
      .getMetaData()
      .getDatabaseProductName()
      .toLowerCase();

    final CryptSaltFactory saltFactory = new CryptSaltFactory();

    final List<UUID> uuids = jdbcTemplate.query("select uuid from user_credential", (rowSet, rowNum) -> {
      final byte[] uuidBytes = rowSet.getBytes("uuid");
      if ("postgresql".equals(databaseName)) {
        return UUID.fromString(new String(uuidBytes, UTF_8));
      } else {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(uuidBytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
      }
    });

    for (final UUID uuid: uuids) {
      final String salt = saltFactory.generateSalt();

      jdbcTemplate.update(
          "update user_credential set salt = ? where uuid = ?",
          new Object[]{salt, getUuidParam(databaseName, uuid)}
      );
    }
  }

  private Object getUuidParam(final String databaseName, final UUID uuid) {
    if ("postgresql".equals(databaseName)) {
      return uuid;
    } else {
      return UuidUtil.uuidToByteArray(uuid);
    }
  }
}
